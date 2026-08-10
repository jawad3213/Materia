package com.materia.backend.contexts.returnToVendor.domain.exceptions;

public class ReturnToVendorInvalidLineException extends ReturnToVendorBusinessException {

    public ReturnToVendorInvalidLineException(String message) {
        super(message, "RETURN_TO_VENDOR_INVALID_LINE");
    }
}
