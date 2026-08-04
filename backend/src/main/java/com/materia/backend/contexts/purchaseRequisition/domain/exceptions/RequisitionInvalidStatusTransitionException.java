package com.materia.backend.contexts.purchaseRequisition.domain.exceptions;

public class RequisitionInvalidStatusTransitionException extends RequisitionBusinessException {

    public RequisitionInvalidStatusTransitionException(String message) {
        super(message, "REQUISITION_INVALID_STATUS_TRANSITION");
    }
}
