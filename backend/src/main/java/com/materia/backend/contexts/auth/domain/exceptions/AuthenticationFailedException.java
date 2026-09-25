package com.materia.backend.contexts.auth.domain.exceptions;

import com.materia.backend.common.application.exceptions.BusinessException;

/**
 * 🔹 AUTHENTICATION FAILED EXCEPTION
 * 
 * Thrown when credentials validation fails or an account is disabled/unauthorized.
 */
public class AuthenticationFailedException extends BusinessException {

    public AuthenticationFailedException(String message) {
        super(message, "AUTH_AUTHENTICATION_FAILED");
    }

    public AuthenticationFailedException(String message, String errorCode) {
        super(message, errorCode);
    }
}
