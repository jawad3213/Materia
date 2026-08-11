package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsque la commande ne correspond pas
 */
public class InvoicePurchaseOrderMismatchException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoicePurchaseOrderMismatchException(String invoicePO, String expectedPO) {
        super("La commande de la facture (" + invoicePO + 
              ") ne correspond pas à la commande attendue (" + expectedPO + ")");
    }
}
