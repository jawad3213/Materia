package com.materia.backend.contexts.purchaseOrder.domain.exceptions;

public class PurchaseOrderInvalidStatusTransitionException extends PurchaseOrderBusinessException {

    public PurchaseOrderInvalidStatusTransitionException(String message) {
        super(message, "PURCHASE_ORDER_INVALID_STATUS_TRANSITION");
    }
}
