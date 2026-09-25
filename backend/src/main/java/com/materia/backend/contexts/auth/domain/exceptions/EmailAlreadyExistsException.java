package com.materia.backend.contexts.auth.domain.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("L'email existe déjà: " + email);
    }
}
