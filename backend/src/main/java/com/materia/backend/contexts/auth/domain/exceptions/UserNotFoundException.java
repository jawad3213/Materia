package com.materia.backend.contexts.auth.domain.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String id) {
        super("Utilisateur non trouvé: " + id);
    }
}
