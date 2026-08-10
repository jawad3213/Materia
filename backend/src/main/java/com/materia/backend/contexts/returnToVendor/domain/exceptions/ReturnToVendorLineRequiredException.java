package com.materia.backend.contexts.returnToVendor.domain.exceptions;

public class ReturnToVendorLineRequiredException extends ReturnToVendorBusinessException {

    public ReturnToVendorLineRequiredException(String message) {
        super(message, "RETURN_TO_VENDOR_LINE_REQUIRED");
    }
}
