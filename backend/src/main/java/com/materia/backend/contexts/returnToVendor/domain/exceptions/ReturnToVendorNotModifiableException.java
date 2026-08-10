package com.materia.backend.contexts.returnToVendor.domain.exceptions;

public class ReturnToVendorNotModifiableException extends ReturnToVendorBusinessException {

    public ReturnToVendorNotModifiableException(String message) {
        super(message, "RETURN_TO_VENDOR_NOT_MODIFIABLE");
    }
}
