package com.materia.backend.contexts.masterdata.domain.exceptions;


/**
 * Exception levÃ©e lorsque le stock est insuffisant pour une opÃ©ration
 */
public class InsufficientStockException extends RuntimeException {

    private final String materialCode;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(String materialCode, int requestedQuantity, int availableQuantity) {
        super("Stock insuffisant pour le matÃ©riau " + materialCode +
                ". DemandÃ©: " + requestedQuantity +
                ", Disponible: " + availableQuantity);
        this.materialCode = materialCode;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}

