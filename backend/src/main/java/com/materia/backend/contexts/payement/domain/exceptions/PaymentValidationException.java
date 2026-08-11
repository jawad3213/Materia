package com.materia.backend.contexts.payement.domain.exceptions;

/**
 * Exception levée lorsque les données de paiement sont invalides
 */
public class PaymentValidationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentValidationException(String message) {
        super(message);
    }
}
