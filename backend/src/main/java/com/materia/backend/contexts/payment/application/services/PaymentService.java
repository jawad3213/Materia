package com.materia.backend.contexts.payment.application.services;

import com.materia.backend.common.audit.application.AuditContext;
import com.materia.backend.common.application.AbstractCrudApplicationService;
import com.materia.backend.contexts.payment.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payment.application.dtos.PaymentOutput;
import com.materia.backend.contexts.payment.application.dtos.UpdatePaymentInput;
import com.materia.backend.contexts.payment.application.mappers.PaymentMapper;
import com.materia.backend.contexts.payment.domain.entities.Payment;
import com.materia.backend.contexts.payment.domain.entities.PaymentLine;
import com.materia.backend.contexts.payment.domain.enums.PaymentStatus;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentAmountMismatchException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentRuleViolationException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentSupplierMismatchException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNotFoundException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNotModifiableException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentValidationException;
import com.materia.backend.contexts.payment.domain.ports.in.PaymentUseCase;
import com.materia.backend.contexts.payment.domain.ports.out.PayableInvoices;
import com.materia.backend.contexts.payment.domain.ports.out.PayableInvoices.PayableInvoice;
import com.materia.backend.contexts.payment.domain.ports.out.PaymentPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Reads run in a read-only transaction: payment lines are lazily loaded and open-in-view is off, so mapping a
 * payment outside a transaction fails (as in GoodsReceiptService). Writes declare their own.
 */
@Service
@Transactional(readOnly = true)
public class PaymentService extends AbstractCrudApplicationService<
        Payment, CreatePaymentInput, UpdatePaymentInput, PaymentOutput> implements PaymentUseCase {

    private final PaymentPort paymentPort;
    private final PaymentMapper paymentMapper;
    private final PaymentCodeGeneratorService codeGeneratorService;
    private final PayableInvoices payableInvoices;

    private com.materia.backend.common.domain.fx.ReportingFx reportingFx;

    /** Values committed documents in the reporting currency; absent in unit tests that do not need it. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setReportingFx(com.materia.backend.common.domain.fx.ReportingFx reportingFx) {
        this.reportingFx = reportingFx;
    }

    private void valueInReportingCurrency(UUID id) {
        if (reportingFx != null && id != null) {
            reportingFx.valueDocument(com.materia.backend.common.domain.fx.FxDocument.PAYMENT, id);
        }
    }

    public PaymentService(PaymentPort paymentPort, PaymentMapper paymentMapper, PaymentCodeGeneratorService codeGeneratorService,
                          PayableInvoices payableInvoices) {
        super(paymentPort, paymentMapper);
        this.paymentPort = paymentPort;
        this.paymentMapper = paymentMapper;
        this.codeGeneratorService = codeGeneratorService;
        this.payableInvoices = payableInvoices;
    }

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public PaymentOutput create(CreatePaymentInput request) {
        Payment payment = paymentMapper.toEntity(request);
        payment.setPaymentCode(codeGeneratorService.generateCode());
        normalizeLines(payment.getLines(), payment.getCurrencyCode());
        matchInvoices(payment);
        payment.recalculateTotal();
        return toResponse(saveEntity(payment));
    }

    @Override
    @Transactional
    public PaymentOutput update(UUID id, CreatePaymentInput request) {
        UpdatePaymentInput updateRequest = new UpdatePaymentInput();
        updateRequest.setNotes(request.getNotes());
        updateRequest.setInternalNotes(request.getInternalNotes());
        updateRequest.setUserId(request.getUserId());
        return update(id, updateRequest);
    }

    @Override
    @Transactional
    public PaymentOutput update(UUID id, UpdatePaymentInput request) {
        Payment payment = getPaymentById(id);
        if (!payment.isModifiable()) {
            throw new PaymentNotModifiableException(payment.getId().toString(), payment.getStatus().name());
        }

        paymentMapper.updateEntity(payment, request);
        payment.setUpdatedAt(LocalDateTime.now());
        if (request instanceof com.materia.backend.common.application.BaseInput) {
            payment.setUpdatedBy(request.getUserId());
        }
        return toResponse(saveEntity(payment));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Payment payment = getPaymentById(id);
        // A prepared payment is cancelled (leaving a trail), not deleted.
        if (payment.getStatus() != PaymentStatus.DRAFT) {
            throw new PaymentNotModifiableException(payment.getId().toString(), payment.getStatus().name());
        }
        paymentPort.deleteById(id);
    }

    @Override
    public PaymentOutput getById(UUID id) {
        return toResponse(getPaymentById(id));
    }

    @Override
    public PaymentOutput getByCode(String code) {
        return toResponse(
                paymentPort.findByCode(code)
                        .orElseThrow(() -> new PaymentNotFoundException("Code", code))
        );
    }

    @Override
    public List<PaymentOutput> getAll() {
        return getAllResponses();
    }

    @Override
    public List<PaymentOutput> getByStatus(String status) {
        PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        return toResponseList(paymentPort.findByStatus(paymentStatus));
    }

    @Override
    public List<PaymentOutput> getBySupplierId(String supplierId) {
        return toResponseList(paymentPort.findBySupplierId(supplierId));
    }

    @Override
    public List<PaymentOutput> searchByKeyword(String keyword) {
        return toResponseList(paymentPort.searchByKeyword(keyword));
    }

    @Override
    @Transactional
    public PaymentOutput prepare(UUID id, String userId) {
        Payment payment = getPaymentById(id);
        matchInvoices(payment);
        payment.prepare(userId);
        return toResponse(saveEntity(payment));
    }

    @Override
    @Transactional
    public PaymentOutput complete(UUID id, String userId, String bankReference, String transactionId, String paymentMethod) {
        Payment payment = getPaymentById(id);
        // Invoices may have changed since the payment was prepared: check them again first.
        matchInvoices(payment);
        payment.markAsCompleted(userId, bankReference, transactionId, paymentMethod);
        // Each invoice records its share through its own rules (verified, within the balance, same currency).
        // Everything runs in this transaction, so one refused invoice leaves every invoice and the payment unchanged.
        for (PaymentLine line : payment.getLines()) {
            payableInvoices.recordPayment(line.getInvoiceId(), userId, line.getAmount().getAmount());
        }
        Payment saved = saveEntity(payment);
        // Executed: the payment is now valued at the rate of its execution date.
        valueInReportingCurrency(saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentOutput cancel(UUID id, String userId, String reason) {
        AuditContext.because(reason);
        Payment payment = getPaymentById(id);
        payment.cancel(userId, reason);
        return toResponse(saveEntity(payment));
    }

    /**
     * Checks every line against its invoice: verified, a standard invoice of the payment's supplier and
     * currency, listed once, and paying no more than its outstanding balance less what other open
     * (draft or prepared) payments already set aside for it. Invoice code and supplier come from the invoice.
     */
    private void matchInvoices(Payment payment) {
        Map<String, BigDecimal> reserved = reservedByOtherOpenPayments(payment);
        Set<String> seen = new HashSet<>();
        String supplierName = null;
        for (PaymentLine line : payment.getLines()) {
            if (!seen.add(line.getInvoiceId())) {
                throw new PaymentValidationException("La facture " + line.getInvoiceId() + " figure deux fois dans le paiement");
            }
            PayableInvoice invoice = loadInvoice(line.getInvoiceId());
            if (!invoice.verified()) {
                throw new PaymentRuleViolationException(
                        "La facture " + invoice.code() + " n'est pas vérifiée (statut " + invoice.status() + ")");
            }
            if (invoice.creditNote()) {
                throw new PaymentRuleViolationException("L'avoir " + invoice.code() + " ne se paie pas");
            }
            if (!invoice.supplierId().equals(payment.getSupplierId())) {
                throw new PaymentSupplierMismatchException(
                        "La facture " + invoice.code() + " appartient à un autre fournisseur");
            }
            if (!invoice.currencyCode().equalsIgnoreCase(payment.getCurrencyCode())) {
                throw new PaymentRuleViolationException("La facture " + invoice.code()
                        + " est en " + invoice.currencyCode() + ", le paiement en " + payment.getCurrencyCode());
            }
            BigDecimal available = invoice.total().subtract(invoice.paid()).subtract(reserved.getOrDefault(line.getInvoiceId(), BigDecimal.ZERO));
            if (line.getAmount().getAmount().compareTo(available) > 0) {
                throw new PaymentAmountMismatchException("Le montant pour la facture " + invoice.code() + " ("
                        + line.getAmount().getAmount().stripTrailingZeros().toPlainString() + ") dépasse ce qui reste à payer ("
                        + available.max(BigDecimal.ZERO).stripTrailingZeros().toPlainString() + ")");
            }
            line.setInvoiceCode(invoice.code());
            line.setSupplierId(invoice.supplierId());
            line.setSupplierName(invoice.supplierName());
            if (supplierName == null) supplierName = invoice.supplierName();
        }
        if (supplierName != null) payment.setSupplierName(supplierName);
    }

    private PayableInvoice loadInvoice(String invoiceId) {
        return payableInvoices.find(invoiceId)
                .orElseThrow(() -> new PaymentValidationException("Facture introuvable : " + invoiceId));
    }

    /** Amount per invoice already set aside by the supplier's other draft or prepared payments. */
    private Map<String, BigDecimal> reservedByOtherOpenPayments(Payment payment) {
        Map<String, BigDecimal> reserved = new HashMap<>();
        for (Payment other : paymentPort.findBySupplierId(payment.getSupplierId())) {
            if (other.getId().equals(payment.getId())
                    || (other.getStatus() != PaymentStatus.DRAFT && other.getStatus() != PaymentStatus.PENDING)) {
                continue;
            }
            for (PaymentLine line : other.getLines()) {
                if (line.getAmount() != null) {
                    reserved.merge(line.getInvoiceId(), line.getAmount().getAmount(), BigDecimal::add);
                }
            }
        }
        return reserved;
    }

    private Payment getPaymentById(UUID id) {
        return getEntityByIdOrThrow(id, () -> new PaymentNotFoundException(id.toString()));
    }

    private void normalizeLines(List<PaymentLine> lines, String currencyCode) {
        if (lines == null || lines.isEmpty()) {
            throw new PaymentValidationException("Un paiement doit contenir au moins une ligne (facture)");
        }

        for (int i = 0; i < lines.size(); i++) {
            PaymentLine line = lines.get(i);
            if (line == null) {
                throw new PaymentValidationException("La ligne de paiement " + (i + 1) + " ne peut pas être nulle");
            }

            if (line.getId() == null) {
                line.setId(UUID.randomUUID());
            }

            line.setLineNumber(i + 1);
            if (line.getCurrencyCode() == null) {
                line.setCurrencyCode(currencyCode);
            }
        }
    }
}
