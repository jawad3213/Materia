package com.materia.backend.contexts.purchaseRequisition.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a requisition is rejected.
 */
public class RequisitionRejectedEvent extends DomainEvent {

    private final String requisitionCode;
    private final String approverId;
    private final String approverName;
    private final String rejectionReason;

    public RequisitionRejectedEvent(UUID requisitionId,
                                    String requisitionCode,
                                    String approverId,
                                    String approverName,
                                    String rejectionReason) {
        super(requisitionId);
        this.requisitionCode = requisitionCode;
        this.approverId = approverId;
        this.approverName = approverName;
        this.rejectionReason = rejectionReason;
    }

    public RequisitionRejectedEvent(UUID eventId,
                                    LocalDateTime occurredOn,
                                    UUID requisitionId,
                                    String requisitionCode,
                                    String approverId,
                                    String approverName,
                                    String rejectionReason) {
        super(eventId, occurredOn, requisitionId);
        this.requisitionCode = requisitionCode;
        this.approverId = approverId;
        this.approverName = approverName;
        this.rejectionReason = rejectionReason;
    }

    public UUID getRequisitionId() {
        return (UUID) getAggregateId();
    }

    public String getRequisitionCode() {
        return requisitionCode;
    }

    public String getApproverId() {
        return approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
