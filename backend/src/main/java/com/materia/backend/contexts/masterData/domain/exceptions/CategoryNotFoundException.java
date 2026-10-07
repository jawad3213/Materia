package com.materia.backend.contexts.masterData.domain.exceptions;

import com.materia.backend.common.application.exceptions.NotFoundException;

/**
 * Exception thrown when a category is not found
 */
public class CategoryNotFoundException extends NotFoundException {

    private final String identifier;

    public CategoryNotFoundException(String identifier) {
        super("Category not found: " + identifier, "CATEGORY_NOT_FOUND");
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
