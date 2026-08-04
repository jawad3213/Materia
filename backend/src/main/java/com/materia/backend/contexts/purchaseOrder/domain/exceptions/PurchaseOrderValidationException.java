package com.materia.backend.contexts.purchaseOrder.domain.exceptions;

import com.materia.backend.common.application.exceptions.ValidationException;

public class PurchaseOrderValidationException extends ValidationException {

    public PurchaseOrderValidationException(String message) {
        super(message);
    }
}
