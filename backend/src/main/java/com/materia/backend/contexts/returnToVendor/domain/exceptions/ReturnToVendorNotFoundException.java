package com.materia.backend.contexts.returnToVendor.domain.exceptions;

import com.materia.backend.common.application.exceptions.NotFoundException;

import java.util.UUID;

public class ReturnToVendorNotFoundException extends NotFoundException {

    public ReturnToVendorNotFoundException(UUID id) {
        super("Return to vendor not found: " + id, "RETURN_TO_VENDOR_NOT_FOUND");
    }

    public ReturnToVendorNotFoundException(String code) {
        super("Return to vendor not found: " + code, "RETURN_TO_VENDOR_NOT_FOUND");
    }
}
