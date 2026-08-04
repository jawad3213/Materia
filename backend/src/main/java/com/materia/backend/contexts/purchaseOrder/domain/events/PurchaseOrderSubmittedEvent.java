package com.materia.backend.contexts.purchaseOrder.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a purchase order is submitted.
 */
public class PurchaseOrderSubmittedEvent extends DomainEvent {

    private final String orderCode;
    private final UUID supplierId;
    private final String supplierName;
    private final String submittedBy;

    public PurchaseOrderSubmittedEvent(UUID purchaseOrderId,
                                       String orderCode,
                                       UUID supplierId,
                                       String supplierName,
                                       String submittedBy) {
        super(purchaseOrderId);
        this.orderCode = orderCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.submittedBy = submittedBy;
    }

    public PurchaseOrderSubmittedEvent(UUID eventId,
                                       LocalDateTime occurredOn,
                                       UUID purchaseOrderId,
                                       String orderCode,
                                       UUID supplierId,
                                       String supplierName,
                                       String submittedBy) {
        super(eventId, occurredOn, purchaseOrderId);
        this.orderCode = orderCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.submittedBy = submittedBy;
    }

    public UUID getPurchaseOrderId() {
        return (UUID) getAggregateId();
    }

    public String getOrderCode() {
        return orderCode;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }
}
