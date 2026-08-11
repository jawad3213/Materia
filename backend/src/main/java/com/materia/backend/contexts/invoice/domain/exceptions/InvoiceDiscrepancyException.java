package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsqu'un écart est détecté dans une facture
 */
public class InvoiceDiscrepancyException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String field;
    private final Object expected;
    private final Object actual;
    
    public InvoiceDiscrepancyException(String field, Object expected, Object actual) {
        super("Écart détecté pour le champ '" + field + "': attendu " + expected + ", reçu " + actual);
        this.field = field;
        this.expected = expected;
        this.actual = actual;
    }
    
    public String getField() { return field; }
    public Object getExpected() { return expected; }
    public Object getActual() { return actual; }
}
