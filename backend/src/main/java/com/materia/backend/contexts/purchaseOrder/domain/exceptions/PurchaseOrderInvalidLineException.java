package com.materia.backend.contexts.purchaseOrder.domain.exceptions;

public class PurchaseOrderInvalidLineException extends PurchaseOrderBusinessException {

    public PurchaseOrderInvalidLineException(String message) {
        super(message, "PURCHASE_ORDER_INVALID_LINE");
    }
}
