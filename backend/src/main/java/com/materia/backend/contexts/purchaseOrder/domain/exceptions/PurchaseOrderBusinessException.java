package com.materia.backend.contexts.purchaseOrder.domain.exceptions;

import com.materia.backend.common.application.exceptions.BusinessException;

public class PurchaseOrderBusinessException extends BusinessException {

    public PurchaseOrderBusinessException(String message, String errorCode) {
        super(message, errorCode);
    }
}
