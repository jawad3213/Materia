package com.materia.backend.common.application.exceptions;

public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final String formattedMessage;

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.formattedMessage = message;
    }

    public BusinessException(String message, String errorCode, String formattedMessage) {
        super(message);
        this.errorCode = errorCode;
        this.formattedMessage = formattedMessage;
    }

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.formattedMessage = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getFormattedMessage() {
        return formattedMessage;
    }
}
