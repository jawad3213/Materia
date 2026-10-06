package com.materia.backend.contexts.payment.domain.exceptions;

/**
 * Exception levée lorsqu'un paiement existe déjà
 */
public class PaymentAlreadyExistsException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentAlreadyExistsException(String paymentCode) {
        super("Payment with code " + paymentCode + " already exists");
    }
}
