package com.materia.backend.contexts.payment.domain.exceptions;

/**
 * Exception levée lorsque le montant total est incohérent
 */
public class PaymentAmountMismatchException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentAmountMismatchException(String message) {
        super(message);
    }
}
