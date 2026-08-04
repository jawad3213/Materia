package com.materia.backend.contexts.purchaseRequisition.domain.exceptions;

public class RequisitionCurrencyMismatchException extends RequisitionBusinessException {

    public RequisitionCurrencyMismatchException() {
        super("All requisition lines must use the same currency", "REQUISITION_CURRENCY_MISMATCH");
    }
}
