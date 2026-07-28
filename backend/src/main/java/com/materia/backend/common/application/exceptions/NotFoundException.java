package com.materia.backend.common.application.exceptions;

public class NotFoundException extends BusinessException {
    public NotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public NotFoundException(String message) {
        super(message, "NOT_FOUND");
    }
}
