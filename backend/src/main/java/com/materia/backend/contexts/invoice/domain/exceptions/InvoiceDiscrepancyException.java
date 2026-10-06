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
        super("Discrepancy detected for field '" + field + "': attendu " + expected + ", received " + actual);
        this.field = field;
        this.expected = expected;
        this.actual = actual;
    }
    
    public String getField() { return field; }
    public Object getExpected() { return expected; }
    public Object getActual() { return actual; }
}
