package com.materia.backend.contexts.purchaseRequisition.domain.exceptions;

public class RequisitionNotModifiableException extends RequisitionBusinessException {

    public RequisitionNotModifiableException() {
        super("Only draft requisitions can be updated", "REQUISITION_NOT_MODIFIABLE");
    }
}
