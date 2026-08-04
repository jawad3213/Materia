package com.materia.backend.contexts.purchaseOrder.domain.exceptions;

public class PurchaseOrderNotModifiableException extends PurchaseOrderBusinessException {

    public PurchaseOrderNotModifiableException() {
        super("Purchase order is not modifiable in its current status", "PURCHASE_ORDER_NOT_MODIFIABLE");
    }
}
