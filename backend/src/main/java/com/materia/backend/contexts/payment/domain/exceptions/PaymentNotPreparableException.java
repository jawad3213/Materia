package com.materia.backend.contexts.payment.domain.exceptions;

/**
 * Exception levée lorsque le paiement ne peut pas être préparé
 */
public class PaymentNotPreparableException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentNotPreparableException(String paymentId, String reason) {
        super("Payment with ID " + paymentId + " cannot be prepared: " + reason);
    }
}
