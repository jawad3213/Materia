package com.materia.backend.contexts.goodsReceipt.domain.exceptions;

public class GoodsReceiptInvalidLineException extends GoodsReceiptBusinessException {

    public GoodsReceiptInvalidLineException(String message) {
        super(message, "GR_INVALID_LINE");
    }
}
