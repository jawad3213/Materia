package com.materia.backend.contexts.invoice.application.services;

import com.materia.backend.common.application.AbstractCrudApplicationService;
import com.materia.backend.contexts.invoice.application.dtos.CreateInvoiceInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.application.dtos.UpdateInvoiceInput;
import com.materia.backend.contexts.invoice.application.mappers.InvoiceMapper;
import com.materia.backend.contexts.invoice.domain.entities.Invoice;
import com.materia.backend.contexts.invoice.domain.entities.InvoiceLine;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceLineValidationException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotFoundException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotModifiableException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceValidationException;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceUseCase;
import com.materia.backend.contexts.invoice.domain.ports.out.InvoiceRepository;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService extends AbstractCrudApplicationService<
        Invoice, CreateInvoiceInput, UpdateInvoiceInput, InvoiceOutput> implements InvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceMapper invoiceMapper) {
        super(invoiceRepository, invoiceMapper);
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
    }

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public InvoiceOutput create(CreateInvoiceInput request) {
        Invoice invoice = invoiceMapper.toEntity(request);
        normalizeLines(invoice.getLines(), invoice.getCurrencyCode());
        return toResponse(saveEntity(invoice));
    }

    @Override
    public InvoiceOutput update(UUID id, CreateInvoiceInput request) {
        UpdateInvoiceInput updateRequest = new UpdateInvoiceInput();
        updateRequest.setPurchaseOrderId(request.getPurchaseOrderId());
        updateRequest.setPurchaseOrderCode(request.getPurchaseOrderCode());
        updateRequest.setGoodsReceiptId(request.getGoodsReceiptId());
        updateRequest.setGoodsReceiptCode(request.getGoodsReceiptCode());
        updateRequest.setSupplierId(request.getSupplierId());
        updateRequest.setSupplierName(request.getSupplierName());
        updateRequest.setSupplierCode(request.getSupplierCode());
        updateRequest.setInvoiceType(request.getInvoiceType());
        updateRequest.setExternalReference(request.getExternalReference());
        updateRequest.setInvoiceDate(request.getInvoiceDate());
        updateRequest.setDueDate(request.getDueDate());
        updateRequest.setCurrencyCode(request.getCurrencyCode());
        updateRequest.setNotes(request.getNotes());
        updateRequest.setInternalNotes(request.getInternalNotes());
        updateRequest.setLines(request.getLines());
        updateRequest.setUserId(request.getUserId());
        return update(id, updateRequest);
    }

    @Override
    @Transactional
    public InvoiceOutput update(UUID id, UpdateInvoiceInput request) {
        Invoice invoice = getInvoiceById(id);
        if (!invoice.isModifiable()) {
            throw new InvoiceNotModifiableException(invoice.getId().toString(), invoice.getStatus().name());
        }

        invoiceMapper.updateEntity(invoice, request);
        normalizeLines(invoice.getLines(), invoice.getCurrencyCode());
        invoice.setUpdatedAt(LocalDateTime.now());
        invoice.setUpdatedBy(request.getUserId());
        return toResponse(saveEntity(invoice));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Invoice invoice = getInvoiceById(id);
        if (!invoice.isModifiable()) {
            throw new InvoiceNotModifiableException(invoice.getId().toString(), invoice.getStatus().name());
        }
        invoiceRepository.deleteById(id);
    }

    @Override
    public InvoiceOutput getById(UUID id) {
        return toResponse(getInvoiceById(id));
    }

    @Override
    public InvoiceOutput getByCode(String code) {
        return toResponse(
                invoiceRepository.findByInvoiceCode(code)
                        .orElseThrow(() -> new InvoiceNotFoundException("Code", code))
        );
    }

    @Override
    public List<InvoiceOutput> getAll() {
        return getAllResponses();
    }

    @Override
    public List<InvoiceOutput> getByStatus(String status) {
        InvoiceStatus invoiceStatus = InvoiceStatus.valueOf(status.toUpperCase());
        return toResponseList(invoiceRepository.findByStatus(invoiceStatus));
    }

    @Override
    public List<InvoiceOutput> getBySupplierId(String supplierId) {
        return toResponseList(invoiceRepository.findBySupplierId(supplierId));
    }

    @Override
    public List<InvoiceOutput> getByPurchaseOrderId(String purchaseOrderId) {
        return toResponseList(invoiceRepository.findByPurchaseOrderId(purchaseOrderId));
    }

    @Override
    public List<InvoiceOutput> searchByKeyword(String keyword) {
        return toResponseList(invoiceRepository.search(keyword));
    }

    @Override
    @Transactional
    public InvoiceOutput submit(UUID id, String userId) {
        Invoice invoice = getInvoiceById(id);
        invoice.submit(userId);
        return toResponse(saveEntity(invoice));
    }

    @Override
    @Transactional
    public InvoiceOutput verify(UUID id, String userId, String userName) {
        Invoice invoice = getInvoiceById(id);
        invoice.verify(userId, userName);
        return toResponse(saveEntity(invoice));
    }

    @Override
    @Transactional
    public InvoiceOutput pay(UUID id, String userId, String userName, Double amount) {
        Invoice invoice = getInvoiceById(id);
        CurrencyCode currency = CurrencyCode.valueOf(invoice.getCurrencyCode() != null ? invoice.getCurrencyCode() : "MAD");
        invoice.pay(userId, userName, Money.of(amount, currency));
        return toResponse(saveEntity(invoice));
    }

    @Override
    @Transactional
    public InvoiceOutput cancel(UUID id, String userId, String reason) {
        Invoice invoice = getInvoiceById(id);
        invoice.cancel(userId, reason);
        return toResponse(saveEntity(invoice));
    }

    private Invoice getInvoiceById(UUID id) {
        return getEntityByIdOrThrow(id, () -> new InvoiceNotFoundException(id.toString()));
    }

    private void normalizeLines(List<InvoiceLine> lines, String orderCurrencyCode) {
        if (lines == null || lines.isEmpty()) {
            throw new InvoiceValidationException("Lines", "Invoice line required");
        }

        for (int i = 0; i < lines.size(); i++) {
            InvoiceLine line = lines.get(i);
            if (line == null) {
                throw new InvoiceLineValidationException(i, "Invoice line cannot be null");
            }

            if (line.getId() == null) {
                line.setId(UUID.randomUUID());
            }

            line.setLineNumber(i + 1);
            if (line.getCurrencyCode() == null) {
                line.setCurrencyCode(orderCurrencyCode);
            }
        }
    }
}
