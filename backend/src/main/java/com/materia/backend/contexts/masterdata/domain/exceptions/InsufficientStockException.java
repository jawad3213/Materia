package com.materia.backend.contexts.masterdata.domain.exceptions;

/**
 * Exception thrown when stock is insufficient for an operation
 */
public class InsufficientStockException extends RuntimeException {

    private final String materialCode;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(String materialCode, int requestedQuantity, int availableQuantity) {
        super("Insufficient stock for material " + materialCode +
                ". Requested: " + requestedQuantity +
                ", Available: " + availableQuantity);
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
