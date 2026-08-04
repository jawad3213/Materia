package com.materia.backend.contexts.goodsReceipt.domain.exceptions;

public class GoodsReceiptNotModifiableException extends GoodsReceiptBusinessException {

    public GoodsReceiptNotModifiableException(String message) {
        super(message, "GR_NOT_MODIFIABLE");
    }
}
