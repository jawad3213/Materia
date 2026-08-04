package com.materia.backend.contexts.masterData.domain.exceptions;

/**
 * Exception thrown when a material code already exists
 */
public class DuplicateMaterialCodeException extends RuntimeException {

    private final String code;

    public DuplicateMaterialCodeException(String code) {
        super("Material code already exists: " + code);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
