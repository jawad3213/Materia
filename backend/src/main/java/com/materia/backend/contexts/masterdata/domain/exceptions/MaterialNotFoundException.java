package com.materia.backend.contexts.masterdata.domain.exceptions;

import com.materia.backend.common.application.exceptions.NotFoundException;

/**
 * Exception thrown when a material is not found
 */
public class MaterialNotFoundException extends NotFoundException {

    private final String identifier;

    public MaterialNotFoundException(String identifier) {
        super("Material not found: " + identifier, "MATERIAL_NOT_FOUND");
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
