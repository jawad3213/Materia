package com.materia.backend.contexts.purchaseRequisition.domain.exceptions;

public class RequisitionNotDeletableException extends RequisitionBusinessException {

    public RequisitionNotDeletableException() {
        super("Only draft requisitions can be deleted", "REQUISITION_NOT_DELETABLE");
    }
}
