package com.materia.backend.contexts.auth.domain.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Incorrect username or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
