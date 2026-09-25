package com.materia.backend.contexts.auth.domain.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Nom d'utilisateur ou mot de passe incorrect");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
