package com.materia.backend.contexts.payment.domain.ports.out;

import java.math.BigDecimal;
import java.util.Optional;

/** The invoices a payment settles, as the payment context needs to see them. */
public interface PayableInvoices {

    /** The invoice with this id; empty when the id is not a UUID or no invoice has it. */
    Optional<PayableInvoice> find(String invoiceId);

    /** Records a payment on the invoice; the invoice applies its own rules and refuses what breaks them. */
    void recordPayment(String invoiceId, String userId, BigDecimal amount);

    /**
     * @param status      the invoice status code, e.g. VERIFIED
     * @param total       the amount due including tax; zero when unknown
     * @param paid        the amount already paid; zero when nothing was paid
     */
    record PayableInvoice(String id, String code, String status, boolean verified, boolean creditNote,
                          String supplierId, String supplierName, String currencyCode,
                          BigDecimal total, BigDecimal paid) {
    }
}
