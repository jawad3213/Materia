package com.materia.backend.contexts.payment.domain.exceptions;

/**
 * Exception levée lorsqu'un paiement n'est pas trouvé
 */
public class PaymentNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentNotFoundException(String paymentId) {
        super("Payment not found with ID: " + paymentId);
    }
    
    public PaymentNotFoundException(String field, String value) {
        super("Payment not found with " + field + ": " + value);
    }
}
