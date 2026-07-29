package com.materia.backend.contexts.masterdata.domain.exceptions;


/**
 * Exception levÃ©e lorsqu'un code matÃ©riau existe dÃ©jÃ 
 */
public class DuplicateMaterialCodeException extends RuntimeException {

    private final String code;

    public DuplicateMaterialCodeException(String code) {
        super("Le code matÃ©riau existe dÃ©jÃ  : " + code);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

