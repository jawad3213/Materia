package com.materia.backend.contexts.payement.application.services;

import com.materia.backend.common.application.AbstractCrudApplicationService;
import com.materia.backend.contexts.payement.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payement.application.dtos.PaymentOutput;
import com.materia.backend.contexts.payement.application.dtos.UpdatePaymentInput;
import com.materia.backend.contexts.payement.application.mappers.PaymentMapper;
import com.materia.backend.contexts.payement.domain.entities.Payment;
import com.materia.backend.contexts.payement.domain.entities.PaymentLine;
import com.materia.backend.contexts.payement.domain.enums.PaymentStatus;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentAmountMismatchException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentRuleViolationException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentSupplierMismatchException;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotFoundException;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceUseCase;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentNotFoundException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentNotModifiableException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentValidationException;
import com.materia.backend.contexts.payement.domain.ports.in.PaymentUseCase;
import com.materia.backend.contexts.payement.domain.ports.out.PaymentPort;
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

@Service
public class PaymentService extends AbstractCrudApplicationService<
        Payment, CreatePaymentInput, UpdatePaymentInput, PaymentOutput> implements PaymentUseCase {

    private final PaymentPort paymentPort;
    private final PaymentMapper paymentMapper;
    private final PaymentCodeGeneratorService codeGeneratorService;
    private final InvoiceUseCase invoiceUseCase;

    public PaymentService(PaymentPort paymentPort, PaymentMapper paymentMapper, PaymentCodeGeneratorService codeGeneratorService,
                          InvoiceUseCase invoiceUseCase) {
        super(paymentPort, paymentMapper);
        this.paymentPort = paymentPort;
        this.paymentMapper = paymentMapper;
        this.codeGeneratorService = codeGeneratorService;
        this.invoiceUseCase = invoiceUseCase;
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
            invoiceUseCase.pay(UUID.fromString(line.getInvoiceId()), userId, line.getAmount().getAmount().doubleValue());
        }
        return toResponse(saveEntity(payment));
    }

    @Override
    @Transactional
    public PaymentOutput cancel(UUID id, String userId, String reason) {
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
            InvoiceOutput invoice = loadInvoice(line.getInvoiceId());
            if (invoice.getStatus() != InvoiceStatus.VERIFIED) {
                throw new PaymentRuleViolationException(
                        "La facture " + invoice.getInvoiceCode() + " n'est pas vérifiée (statut " + invoice.getStatus() + ")");
            }
            if (invoice.getInvoiceType() == InvoiceType.CREDIT_NOTE) {
                throw new PaymentRuleViolationException("L'avoir " + invoice.getInvoiceCode() + " ne se paie pas");
            }
            if (!invoice.getSupplierId().equals(payment.getSupplierId())) {
                throw new PaymentSupplierMismatchException(
                        "La facture " + invoice.getInvoiceCode() + " appartient à un autre fournisseur");
            }
            if (!invoice.getCurrencyCode().equalsIgnoreCase(payment.getCurrencyCode())) {
                throw new PaymentRuleViolationException("La facture " + invoice.getInvoiceCode()
                        + " est en " + invoice.getCurrencyCode() + ", le paiement en " + payment.getCurrencyCode());
            }
            BigDecimal total = invoice.getTotalAmountWithTax() != null ? invoice.getTotalAmountWithTax().getAmount() : BigDecimal.ZERO;
            BigDecimal paid = invoice.getPaidAmount() != null ? invoice.getPaidAmount().getAmount() : BigDecimal.ZERO;
            BigDecimal available = total.subtract(paid).subtract(reserved.getOrDefault(line.getInvoiceId(), BigDecimal.ZERO));
            if (line.getAmount().getAmount().compareTo(available) > 0) {
                throw new PaymentAmountMismatchException("Le montant pour la facture " + invoice.getInvoiceCode() + " ("
                        + line.getAmount().getAmount().stripTrailingZeros().toPlainString() + ") dépasse ce qui reste à payer ("
                        + available.max(BigDecimal.ZERO).stripTrailingZeros().toPlainString() + ")");
            }
            line.setInvoiceCode(invoice.getInvoiceCode());
            line.setSupplierId(invoice.getSupplierId());
            line.setSupplierName(invoice.getSupplierName());
            if (supplierName == null) supplierName = invoice.getSupplierName();
        }
        if (supplierName != null) payment.setSupplierName(supplierName);
    }

    private InvoiceOutput loadInvoice(String invoiceId) {
        try {
            return invoiceUseCase.getById(UUID.fromString(invoiceId));
        } catch (IllegalArgumentException | InvoiceNotFoundException e) {
            throw new PaymentValidationException("Facture introuvable : " + invoiceId);
        }
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
