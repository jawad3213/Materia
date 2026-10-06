package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsqu'une facture n'est pas trouvée
 */
public class InvoiceNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceNotFoundException(String invoiceId) {
        super("Invoice not found with ID: " + invoiceId);
    }
    
    public InvoiceNotFoundException(String field, String value) {
        super("Invoice not found with " + field + ": " + value);
    }
}
