package com.materia.backend.contexts.returnToVendor.domain.exceptions;

import com.materia.backend.common.application.exceptions.BusinessException;

public class ReturnToVendorBusinessException extends BusinessException {

    public ReturnToVendorBusinessException(String message, String errorCode) {
        super(message, errorCode);
    }
}
