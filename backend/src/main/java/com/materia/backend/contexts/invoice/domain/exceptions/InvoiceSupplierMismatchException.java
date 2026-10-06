package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsque le fournisseur ne correspond pas
 */
public class InvoiceSupplierMismatchException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceSupplierMismatchException(String invoiceSupplier, String orderSupplier) {
        super("The invoice supplier (" + invoiceSupplier + 
              ") does not match the purchase order supplier (" + orderSupplier + ")");
    }
}
