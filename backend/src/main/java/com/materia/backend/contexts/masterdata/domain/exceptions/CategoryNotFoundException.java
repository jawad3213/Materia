package com.materia.backend.contexts.masterdata.domain.exceptions;


/**
 * Exception levÃ©e lorsqu'une catÃ©gorie n'est pas trouvÃ©e
 */
public class CategoryNotFoundException extends RuntimeException {

    private final String identifier;

    public CategoryNotFoundException(String identifier) {
        super("CatÃ©gorie non trouvÃ©e : " + identifier);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}

