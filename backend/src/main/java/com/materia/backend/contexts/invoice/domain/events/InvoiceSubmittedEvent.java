package com.materia.backend.contexts.invoice.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when an invoice is submitted for review.
 */
public class InvoiceSubmittedEvent extends DomainEvent {

    private final String invoiceCode;
    private final String supplierId;
    private final String supplierName;
    private final String submittedBy;

    public InvoiceSubmittedEvent(UUID invoiceId,
                                 String invoiceCode,
                                 String supplierId,
                                 String supplierName,
                                 String submittedBy) {
        super(invoiceId);
        this.invoiceCode = invoiceCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.submittedBy = submittedBy;
    }

    public InvoiceSubmittedEvent(UUID eventId,
                                 LocalDateTime occurredOn,
                                 UUID invoiceId,
                                 String invoiceCode,
                                 String supplierId,
                                 String supplierName,
                                 String submittedBy) {
        super(eventId, occurredOn, invoiceId);
        this.invoiceCode = invoiceCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.submittedBy = submittedBy;
    }

    public UUID getInvoiceId() { return (UUID) getAggregateId(); }
    public String getInvoiceCode() { return invoiceCode; }
    public String getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public String getSubmittedBy() { return submittedBy; }
}
