package com.materia.backend.contexts.invoice.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when an invoice is verified.
 */
public class InvoiceVerifiedEvent extends DomainEvent {

    private final String invoiceCode;
    private final String supplierId;
    private final String supplierName;
    private final String verifiedBy;
    private final String verifiedByName;
    private final boolean hasDiscrepancy;

    public InvoiceVerifiedEvent(UUID invoiceId,
                                String invoiceCode,
                                String supplierId,
                                String supplierName,
                                String verifiedBy,
                                String verifiedByName,
                                boolean hasDiscrepancy) {
        super(invoiceId);
        this.invoiceCode = invoiceCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.verifiedBy = verifiedBy;
        this.verifiedByName = verifiedByName;
        this.hasDiscrepancy = hasDiscrepancy;
    }

    public InvoiceVerifiedEvent(UUID eventId,
                                LocalDateTime occurredOn,
                                UUID invoiceId,
                                String invoiceCode,
                                String supplierId,
                                String supplierName,
                                String verifiedBy,
                                String verifiedByName,
                                boolean hasDiscrepancy) {
        super(eventId, occurredOn, invoiceId);
        this.invoiceCode = invoiceCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.verifiedBy = verifiedBy;
        this.verifiedByName = verifiedByName;
        this.hasDiscrepancy = hasDiscrepancy;
    }

    public UUID getInvoiceId() { return (UUID) getAggregateId(); }
    public String getInvoiceCode() { return invoiceCode; }
    public String getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public String getVerifiedBy() { return verifiedBy; }
    public String getVerifiedByName() { return verifiedByName; }
    public boolean isHasDiscrepancy() { return hasDiscrepancy; }
}
