package com.materia.backend.contexts.masterdata.domain.exceptions;

/**
 * Exception thrown when a supplier is not found
 */
public class SupplierNotFoundException extends RuntimeException {

    private final String identifier;

    public SupplierNotFoundException(String identifier) {
        super("Supplier not found: " + identifier);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
