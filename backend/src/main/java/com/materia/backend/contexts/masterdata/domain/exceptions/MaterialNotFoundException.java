package com.materia.backend.contexts.masterdata.domain.exceptions;


/**
 * Exception levÃ©e lorsqu'un matÃ©riau n'est pas trouvÃ©
 */
public class MaterialNotFoundException extends RuntimeException {

    private final String identifier;

    public MaterialNotFoundException(String identifier) {
        super("MatÃ©riau non trouvÃ© : " + identifier);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}

