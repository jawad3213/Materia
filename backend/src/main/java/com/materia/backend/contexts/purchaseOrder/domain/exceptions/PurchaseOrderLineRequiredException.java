package com.materia.backend.contexts.purchaseOrder.domain.exceptions;

public class PurchaseOrderLineRequiredException extends PurchaseOrderBusinessException {

    public PurchaseOrderLineRequiredException() {
        super("At least one purchase order line is required", "PURCHASE_ORDER_LINE_REQUIRED");
    }
}
