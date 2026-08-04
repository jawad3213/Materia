package com.materia.backend.contexts.goodsReceipt.domain.exceptions;

import com.materia.backend.common.application.exceptions.BusinessException;

public class GoodsReceiptBusinessException extends BusinessException {

    public GoodsReceiptBusinessException(String message, String errorCode) {
        super(message, errorCode);
    }
}
