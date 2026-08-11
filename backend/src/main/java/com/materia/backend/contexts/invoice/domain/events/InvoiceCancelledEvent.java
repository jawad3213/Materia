package com.materia.backend.contexts.invoice.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when an invoice is cancelled.
 */
public class InvoiceCancelledEvent extends DomainEvent {

    private final String invoiceCode;
    private final String supplierId;
    private final String supplierName;
    private final String cancelledBy;
    private final String reason;

    public InvoiceCancelledEvent(UUID invoiceId,
                                 String invoiceCode,
                                 String supplierId,
                                 String supplierName,
                                 String cancelledBy,
                                 String reason) {
        super(invoiceId);
        this.invoiceCode = invoiceCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.cancelledBy = cancelledBy;
        this.reason = reason;
    }

    public InvoiceCancelledEvent(UUID eventId,
                                 LocalDateTime occurredOn,
                                 UUID invoiceId,
                                 String invoiceCode,
                                 String supplierId,
                                 String supplierName,
                                 String cancelledBy,
                                 String reason) {
        super(eventId, occurredOn, invoiceId);
        this.invoiceCode = invoiceCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.cancelledBy = cancelledBy;
        this.reason = reason;
    }

    public UUID getInvoiceId() { return (UUID) getAggregateId(); }
    public String getInvoiceCode() { return invoiceCode; }
    public String getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public String getCancelledBy() { return cancelledBy; }
    public String getReason() { return reason; }
}
