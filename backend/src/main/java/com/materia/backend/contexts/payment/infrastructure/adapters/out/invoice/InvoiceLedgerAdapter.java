package com.materia.backend.contexts.payment.infrastructure.adapters.out.invoice;

import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotFoundException;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceUseCase;
import com.materia.backend.contexts.payment.domain.ports.out.PayableInvoices;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/** Reads and pays invoices through the public use case of the invoice context. */
@Component
public class InvoiceLedgerAdapter implements PayableInvoices {

    private final InvoiceUseCase invoices;

    public InvoiceLedgerAdapter(InvoiceUseCase invoices) {
        this.invoices = invoices;
    }

    @Override
    public Optional<PayableInvoice> find(String invoiceId) {
        try {
            return Optional.of(toPayable(invoices.getById(UUID.fromString(invoiceId))));
        } catch (IllegalArgumentException | InvoiceNotFoundException notFound) {
            return Optional.empty();
        }
    }

    @Override
    public void recordPayment(String invoiceId, String userId, BigDecimal amount) {
        invoices.pay(UUID.fromString(invoiceId), userId, amount.doubleValue());
    }

    private static PayableInvoice toPayable(InvoiceOutput invoice) {
        return new PayableInvoice(
                invoice.getId() != null ? invoice.getId().toString() : null,
                invoice.getInvoiceCode(),
                String.valueOf(invoice.getStatus()),
                invoice.getStatus() == InvoiceStatus.VERIFIED,
                invoice.getInvoiceType() == InvoiceType.CREDIT_NOTE,
                invoice.getSupplierId(),
                invoice.getSupplierName(),
                invoice.getCurrencyCode(),
                invoice.getTotalAmountWithTax() != null ? invoice.getTotalAmountWithTax().getAmount() : BigDecimal.ZERO,
                invoice.getPaidAmount() != null ? invoice.getPaidAmount().getAmount() : BigDecimal.ZERO);
    }
}
