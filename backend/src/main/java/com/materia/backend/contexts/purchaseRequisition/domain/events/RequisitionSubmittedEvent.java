package com.materia.backend.contexts.purchaseRequisition.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a requisition is submitted.
 */
public class RequisitionSubmittedEvent extends DomainEvent {

    private final String requisitionCode;
    private final String requesterId;
    private final String requesterName;
    private final String currencyCode;

    public RequisitionSubmittedEvent(UUID requisitionId,
                                     String requisitionCode,
                                     String requesterId,
                                     String requesterName,
                                     String currencyCode) {
        super(requisitionId);
        this.requisitionCode = requisitionCode;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.currencyCode = currencyCode;
    }

    public RequisitionSubmittedEvent(UUID eventId,
                                     LocalDateTime occurredOn,
                                     UUID requisitionId,
                                     String requisitionCode,
                                     String requesterId,
                                     String requesterName,
                                     String currencyCode) {
        super(eventId, occurredOn, requisitionId);
        this.requisitionCode = requisitionCode;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.currencyCode = currencyCode;
    }

    public UUID getRequisitionId() {
        return (UUID) getAggregateId();
    }

    public String getRequisitionCode() {
        return requisitionCode;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
