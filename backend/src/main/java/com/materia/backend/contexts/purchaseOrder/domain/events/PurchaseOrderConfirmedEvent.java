package com.materia.backend.contexts.purchaseOrder.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a purchase order is confirmed.
 */
public class PurchaseOrderConfirmedEvent extends DomainEvent {

    private final String orderCode;
    private final UUID supplierId;
    private final String supplierName;
    private final String confirmedBy;

    public PurchaseOrderConfirmedEvent(UUID purchaseOrderId,
                                       String orderCode,
                                       UUID supplierId,
                                       String supplierName,
                                       String confirmedBy) {
        super(purchaseOrderId);
        this.orderCode = orderCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.confirmedBy = confirmedBy;
    }

    public PurchaseOrderConfirmedEvent(UUID eventId,
                                       LocalDateTime occurredOn,
                                       UUID purchaseOrderId,
                                       String orderCode,
                                       UUID supplierId,
                                       String supplierName,
                                       String confirmedBy) {
        super(eventId, occurredOn, purchaseOrderId);
        this.orderCode = orderCode;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.confirmedBy = confirmedBy;
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

    public String getConfirmedBy() {
        return confirmedBy;
    }
}
