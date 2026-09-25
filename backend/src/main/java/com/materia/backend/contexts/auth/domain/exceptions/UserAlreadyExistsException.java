package com.materia.backend.contexts.auth.domain.exceptions;

import com.materia.backend.common.application.exceptions.BusinessException;

/**
 * 🔹 USER ALREADY EXISTS EXCEPTION
 * 
 * Thrown when trying to register a user with an existing email.
 */
public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super(message, "AUTH_USER_ALREADY_EXISTS");
    }

    public UserAlreadyExistsException(String message, String errorCode) {
        super(message, errorCode);
    }
}
