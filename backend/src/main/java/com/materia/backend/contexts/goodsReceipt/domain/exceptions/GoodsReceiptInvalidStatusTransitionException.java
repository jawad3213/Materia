package com.materia.backend.contexts.goodsReceipt.domain.exceptions;

public class GoodsReceiptInvalidStatusTransitionException extends GoodsReceiptBusinessException {

    public GoodsReceiptInvalidStatusTransitionException(String message) {
        super(message, "GR_INVALID_STATUS_TRANSITION");
    }
}
