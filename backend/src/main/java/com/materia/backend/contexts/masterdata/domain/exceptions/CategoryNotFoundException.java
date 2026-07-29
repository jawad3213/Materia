package com.materia.backend.contexts.masterdata.domain.exceptions;

/**
 * Exception thrown when a category is not found
 */
public class CategoryNotFoundException extends RuntimeException {

    private final String identifier;

    public CategoryNotFoundException(String identifier) {
        super("Category not found: " + identifier);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
