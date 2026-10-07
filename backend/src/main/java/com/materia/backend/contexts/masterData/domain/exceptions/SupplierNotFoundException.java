package com.materia.backend.contexts.masterData.domain.exceptions;

import com.materia.backend.common.application.exceptions.NotFoundException;

/**
 * Exception thrown when a supplier is not found
 */
public class SupplierNotFoundException extends NotFoundException {

    private final String identifier;

    public SupplierNotFoundException(String identifier) {
        super("Supplier not found: " + identifier, "SUPPLIER_NOT_FOUND");
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
