package com.materia.backend.contexts.purchaseRequisition.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a requisition is converted to a purchase order.
 */
public class RequisitionConvertedEvent extends DomainEvent {

    private final String requisitionCode;
    private final String purchaseOrderId;
    private final String purchaseOrderCode;
    private final String convertedBy;

    public RequisitionConvertedEvent(UUID requisitionId,
                                     String requisitionCode,
                                     String purchaseOrderId,
                                     String purchaseOrderCode,
                                     String convertedBy) {
        super(requisitionId);
        this.requisitionCode = requisitionCode;
        this.purchaseOrderId = purchaseOrderId;
        this.purchaseOrderCode = purchaseOrderCode;
        this.convertedBy = convertedBy;
    }

    public RequisitionConvertedEvent(UUID eventId,
                                     LocalDateTime occurredOn,
                                     UUID requisitionId,
                                     String requisitionCode,
                                     String purchaseOrderId,
                                     String purchaseOrderCode,
                                     String convertedBy) {
        super(eventId, occurredOn, requisitionId);
        this.requisitionCode = requisitionCode;
        this.purchaseOrderId = purchaseOrderId;
        this.purchaseOrderCode = purchaseOrderCode;
        this.convertedBy = convertedBy;
    }

    public UUID getRequisitionId() {
        return (UUID) getAggregateId();
    }

    public String getRequisitionCode() {
        return requisitionCode;
    }

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public String getPurchaseOrderCode() {
        return purchaseOrderCode;
    }

    public String getConvertedBy() {
        return convertedBy;
    }
}
