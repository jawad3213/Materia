package com.materia.backend.contexts.purchaseOrder.domain.events;

import com.materia.backend.common.domain.DomainEvent;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a purchase order is delivered.
 */
public class PurchaseOrderDeliveredEvent extends DomainEvent {

    private final String orderCode;
    private final DeliveryStatus deliveryStatus;
    private final String updatedBy;

    public PurchaseOrderDeliveredEvent(UUID purchaseOrderId,
                                       String orderCode,
                                       DeliveryStatus deliveryStatus,
                                       String updatedBy) {
        super(purchaseOrderId);
        this.orderCode = orderCode;
        this.deliveryStatus = deliveryStatus;
        this.updatedBy = updatedBy;
    }

    public PurchaseOrderDeliveredEvent(UUID eventId,
                                       LocalDateTime occurredOn,
                                       UUID purchaseOrderId,
                                       String orderCode,
                                       DeliveryStatus deliveryStatus,
                                       String updatedBy) {
        super(eventId, occurredOn, purchaseOrderId);
        this.orderCode = orderCode;
        this.deliveryStatus = deliveryStatus;
        this.updatedBy = updatedBy;
    }

    public UUID getPurchaseOrderId() {
        return (UUID) getAggregateId();
    }

    public String getOrderCode() {
        return orderCode;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
