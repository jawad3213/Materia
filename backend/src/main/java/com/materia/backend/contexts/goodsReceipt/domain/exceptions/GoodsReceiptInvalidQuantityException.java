package com.materia.backend.contexts.goodsReceipt.domain.exceptions;

/** Raised when receipt quantities do not respect the purchase order rules. */
public class GoodsReceiptInvalidQuantityException extends GoodsReceiptBusinessException {

    public GoodsReceiptInvalidQuantityException(String message) {
        super(message, "GR_INVALID_QUANTITY");
    }
}
