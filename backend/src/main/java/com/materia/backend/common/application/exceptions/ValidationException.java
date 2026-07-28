package com.materia.backend.common.application.exceptions;

import java.util.Map;
import java.util.HashMap;

public class ValidationException extends BusinessException {
    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.errors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message, "VALIDATION_ERROR");
        this.errors = errors != null ? errors : new HashMap<>();
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
