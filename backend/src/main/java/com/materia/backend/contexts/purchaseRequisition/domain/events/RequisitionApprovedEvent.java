package com.materia.backend.contexts.purchaseRequisition.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a requisition is approved.
 */
public class RequisitionApprovedEvent extends DomainEvent {

    private final String requisitionCode;
    private final String approverId;
    private final String approverName;
    private final String approvalNotes;

    public RequisitionApprovedEvent(UUID requisitionId,
                                    String requisitionCode,
                                    String approverId,
                                    String approverName,
                                    String approvalNotes) {
        super(requisitionId);
        this.requisitionCode = requisitionCode;
        this.approverId = approverId;
        this.approverName = approverName;
        this.approvalNotes = approvalNotes;
    }

    public RequisitionApprovedEvent(UUID eventId,
                                    LocalDateTime occurredOn,
                                    UUID requisitionId,
                                    String requisitionCode,
                                    String approverId,
                                    String approverName,
                                    String approvalNotes) {
        super(eventId, occurredOn, requisitionId);
        this.requisitionCode = requisitionCode;
        this.approverId = approverId;
        this.approverName = approverName;
        this.approvalNotes = approvalNotes;
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

    public String getApprovalNotes() {
        return approvalNotes;
    }
}
