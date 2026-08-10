package com.materia.backend.contexts.returnToVendor.domain.exceptions;

public class ReturnToVendorInvalidQuantityException extends ReturnToVendorBusinessException {

    public ReturnToVendorInvalidQuantityException(String message) {
        super(message, "RETURN_TO_VENDOR_INVALID_QUANTITY");
    }
}
