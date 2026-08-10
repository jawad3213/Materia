package com.materia.backend.contexts.returnToVendor.domain.exceptions;

public class ReturnToVendorInvalidStatusTransitionException extends ReturnToVendorBusinessException {

    public ReturnToVendorInvalidStatusTransitionException(String message) {
        super(message, "RETURN_TO_VENDOR_INVALID_STATUS_TRANSITION");
    }
}
