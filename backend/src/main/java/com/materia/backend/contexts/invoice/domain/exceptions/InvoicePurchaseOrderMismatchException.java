package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsque la commande ne correspond pas
 */
public class InvoicePurchaseOrderMismatchException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoicePurchaseOrderMismatchException(String invoicePO, String expectedPO) {
        super("The invoice purchase order (" + invoicePO + 
              ") does not match the expected purchase order (" + expectedPO + ")");
    }
}
