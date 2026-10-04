package com.materia.backend.contexts.purchaseOrder.domain.exceptions;

/** An order created from a requisition does not match it (lines, materials, quantities, supplier or currency). */
public class PurchaseOrderRequisitionMismatchException extends PurchaseOrderBusinessException {

    public PurchaseOrderRequisitionMismatchException(String message) {
        super(message, "PURCHASE_ORDER_REQUISITION_MISMATCH");
    }
}
