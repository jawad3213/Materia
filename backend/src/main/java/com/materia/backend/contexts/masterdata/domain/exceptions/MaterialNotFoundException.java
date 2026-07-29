package com.materia.backend.contexts.masterdata.domain.exceptions;

/**
 * Exception thrown when a material is not found
 */
public class MaterialNotFoundException extends RuntimeException {

    private final String identifier;

    public MaterialNotFoundException(String identifier) {
        super("Material not found: " + identifier);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
