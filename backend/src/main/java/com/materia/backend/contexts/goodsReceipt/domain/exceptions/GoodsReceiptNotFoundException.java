package com.materia.backend.contexts.goodsReceipt.domain.exceptions;

import com.materia.backend.common.application.exceptions.NotFoundException;

public class GoodsReceiptNotFoundException extends NotFoundException {

    public GoodsReceiptNotFoundException(String message) {
        super(message);
    }
}
