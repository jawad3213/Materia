package com.materia.backend.contexts.masterdata.domain.exceptions;


/**
 * Exception levÃ©e lorsqu'un fournisseur n'est pas trouvÃ©
 */
public class SupplierNotFoundException extends RuntimeException {

    private final String identifier;

    public SupplierNotFoundException(String identifier) {
        super("Fournisseur non trouvÃ© : " + identifier);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}

