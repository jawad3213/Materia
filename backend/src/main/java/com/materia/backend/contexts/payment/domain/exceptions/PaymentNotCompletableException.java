package com.materia.backend.contexts.payment.domain.exceptions;

/**
 * Exception levée lorsque le paiement ne peut pas être marqué comme payé
 */
public class PaymentNotCompletableException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentNotCompletableException(String paymentId, String reason) {
        super("Payment with ID " + paymentId + " cannot be marked as paid: " + reason);
    }
}
