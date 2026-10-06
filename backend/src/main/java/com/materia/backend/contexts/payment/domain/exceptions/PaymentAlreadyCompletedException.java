package com.materia.backend.contexts.payment.domain.exceptions;

/**
 * Exception levée lorsqu'un paiement est déjà effectué
 */
public class PaymentAlreadyCompletedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentAlreadyCompletedException(String paymentId) {
        super("Payment with ID " + paymentId + " is already completed");
    }
}
