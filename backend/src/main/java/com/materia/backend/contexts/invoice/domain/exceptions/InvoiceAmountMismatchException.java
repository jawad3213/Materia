package com.materia.backend.contexts.invoice.domain.exceptions;

import com.materia.backend.contexts.masterData.domain.valueObjects.Money;

/**
 * Exception levée lorsque le montant total de la facture est incohérent
 */
public class InvoiceAmountMismatchException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceAmountMismatchException(String invoiceId, Money calculated, Money provided) {
        super("Le montant total de la facture " + invoiceId + " est incohérent. " +
              "Calculé: " + calculated + ", Fourni: " + provided);
    }
}
