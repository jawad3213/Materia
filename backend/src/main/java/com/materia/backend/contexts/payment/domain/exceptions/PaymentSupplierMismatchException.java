package com.materia.backend.contexts.payment.domain.exceptions;

/**
 * Exception levée lorsque le fournisseur est incohérent
 */
public class PaymentSupplierMismatchException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentSupplierMismatchException(String message) {
        super(message);
    }
}
