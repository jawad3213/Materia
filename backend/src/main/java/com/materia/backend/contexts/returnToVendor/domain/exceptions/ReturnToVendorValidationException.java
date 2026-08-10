package com.materia.backend.contexts.returnToVendor.domain.exceptions;

import com.materia.backend.common.application.exceptions.ValidationException;

public class ReturnToVendorValidationException extends ValidationException {

    public ReturnToVendorValidationException(String message) {
        super(message);
    }
}
