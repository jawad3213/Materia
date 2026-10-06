package com.materia.backend.contexts.payment.domain.exceptions;

/**
 * Exception levée lorsque le paiement ne peut pas être annulé
 */
public class PaymentNotCancellableException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentNotCancellableException(String paymentId, String reason) {
        super("Payment with ID " + paymentId + " cannot be cancelled: " + reason);
    }
}
