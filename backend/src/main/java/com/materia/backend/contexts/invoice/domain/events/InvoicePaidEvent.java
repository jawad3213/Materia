package com.materia.backend.contexts.invoice.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when an invoice is paid.
 */
public class InvoicePaidEvent extends DomainEvent {

    private final String invoiceCode;
    private final String supplierId;
    private final String supplierName;
    private final BigDecimal paidAmount;
    private final String paidBy;
    private final String paidByName;

    public InvoicePaidEvent(UUID invoiceId,
                            String invoiceCode,
                            String supplierId,
                            String supplierName,
                            BigDecimal paidAmount,
                            String paidBy,
                            String paidByName) {
        super(invoiceId);
        this.invoiceCode = invoiceCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.paidAmount = paidAmount;
        this.paidBy = paidBy;
        this.paidByName = paidByName;
    }

    public InvoicePaidEvent(UUID eventId,
                            LocalDateTime occurredOn,
                            UUID invoiceId,
                            String invoiceCode,
                            String supplierId,
                            String supplierName,
                            BigDecimal paidAmount,
                            String paidBy,
                            String paidByName) {
        super(eventId, occurredOn, invoiceId);
        this.invoiceCode = invoiceCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.paidAmount = paidAmount;
        this.paidBy = paidBy;
        this.paidByName = paidByName;
    }

    public UUID getInvoiceId() { return (UUID) getAggregateId(); }
    public String getInvoiceCode() { return invoiceCode; }
    public String getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public String getPaidBy() { return paidBy; }
    public String getPaidByName() { return paidByName; }
}
