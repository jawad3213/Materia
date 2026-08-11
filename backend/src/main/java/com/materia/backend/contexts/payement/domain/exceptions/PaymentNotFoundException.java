package com.materia.backend.contexts.payement.domain.exceptions;

/**
 * Exception levée lorsqu'un paiement n'est pas trouvé
 */
public class PaymentNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentNotFoundException(String paymentId) {
        super("Paiement non trouvé avec l'ID: " + paymentId);
    }
    
    public PaymentNotFoundException(String field, String value) {
        super("Paiement non trouvé avec " + field + ": " + value);
    }
}
