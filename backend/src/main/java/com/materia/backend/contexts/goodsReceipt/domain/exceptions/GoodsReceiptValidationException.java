package com.materia.backend.contexts.goodsReceipt.domain.exceptions;

import com.materia.backend.common.application.exceptions.ValidationException;

public class GoodsReceiptValidationException extends ValidationException {

    public GoodsReceiptValidationException(String message) {
        super(message);
    }
}
