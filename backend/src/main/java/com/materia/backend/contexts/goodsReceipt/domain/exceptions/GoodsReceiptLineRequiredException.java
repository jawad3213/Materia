package com.materia.backend.contexts.goodsReceipt.domain.exceptions;

public class GoodsReceiptLineRequiredException extends GoodsReceiptBusinessException {

    public GoodsReceiptLineRequiredException(String message) {
        super(message, "GR_LINE_REQUIRED");
    }
}
