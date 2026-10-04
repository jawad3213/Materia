package com.materia.backend.contexts.invoice.domain.entities;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceCancellationException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotPayableException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotVerifiableException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceRuleViolationException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Invoice totals, three-way match recording and workflow rules. */
class InvoiceRulesTest {

    private static InvoiceLine line(int quantity, String price, String tax, CurrencyCode currency) {
        return InvoiceLine.builder()
                .materialCode("MAT-1")
                .materialName("Bolts")
                .quantityInvoiced(quantity)
                .unitPrice(Money.of(price, currency))
                .taxAmount(tax != null ? Money.of(tax, currency) : null)
                .build();
    }

    private static Invoice invoice(String currency, InvoiceLine... lines) {
        return Invoice.builder()
                .purchaseOrderId("po-1")
                .supplierId("sup-1")
                .supplierName("Acme")
                .invoiceType(InvoiceType.STANDARD)
                .currencyCode(currency)
                .lines(List.of(lines))
                .build();
    }

    private static Invoice inStatus(InvoiceStatus status) {
        Invoice invoice = invoice("MAD", line(2, "5.00", "1.00", CurrencyCode.MAD));
        if (status == InvoiceStatus.DRAFT) return invoice;
        invoice.submit("buyer-1");
        if (status == InvoiceStatus.SUBMITTED) return invoice;
        if (status == InvoiceStatus.CANCELLED) {
            invoice.cancel("buyer-1", "Duplicate");
            return invoice;
        }
        invoice.verify("admin-1", "Ada");
        if (status == InvoiceStatus.VERIFIED) return invoice;
        invoice.pay("admin-1", "Ada", Money.of("11.00", CurrencyCode.MAD));
        return invoice;
    }

    // ---- Totals ----

    @Test
    @DisplayName("totals: an invoice is built with totals computed from its lines, in its own currency")
    void build_computesTotalsInInvoiceCurrency() {
        Invoice eur = invoice("EUR", line(2, "5.00", "1.00", CurrencyCode.EUR), line(1, "3.00", null, CurrencyCode.EUR));

        assertEquals(CurrencyCode.EUR, eur.getTotalAmount().getCurrency());
        assertEquals(0, new BigDecimal("13.00").compareTo(eur.getTotalAmount().getAmount()));
        assertEquals(0, new BigDecimal("1.00").compareTo(eur.getTotalTaxAmount().getAmount()));
        assertEquals(0, new BigDecimal("14.00").compareTo(eur.getTotalAmountWithTax().getAmount()));
    }

    @Test
    @DisplayName("totals: recalculating after a line changes updates line and invoice totals")
    void recalculate_afterLineChange() {
        Invoice invoice = invoice("MAD", line(2, "5.00", "1.00", CurrencyCode.MAD));
        invoice.getLines().get(0).setQuantityInvoiced(4);

        invoice.recalculateTotals();

        assertEquals(0, new BigDecimal("20.00").compareTo(invoice.getLines().get(0).getLineTotal().getAmount()));
        assertEquals(0, new BigDecimal("21.00").compareTo(invoice.getTotalAmountWithTax().getAmount()));
    }

    // ---- Three-way match ----

    @Test
    @DisplayName("match: billing within what is billable at the order price records quantities and no discrepancy")
    void recordMatch_withinBillable() {
        InvoiceLine l = line(4, "5.00", null, CurrencyCode.MAD);

        l.recordMatch(10, 6, 6, Money.of("5.00", CurrencyCode.MAD));

        assertEquals(10, l.getQuantityOrdered());
        assertEquals(6, l.getQuantityReceived());
        assertFalse(l.isHasQuantityDiscrepancy());
        assertEquals(0, l.getQuantityDiscrepancy());
        assertNull(l.getDiscrepancyNotes());
    }

    @Test
    @DisplayName("match: billing beyond what is billable, or at another price, is recorded with reasons")
    void recordMatch_overBilledAndRepriced() {
        InvoiceLine l = line(5, "5.50", null, CurrencyCode.MAD);

        l.recordMatch(10, 6, 2, Money.of("5.00", CurrencyCode.MAD));

        assertTrue(l.isHasQuantityDiscrepancy());
        assertEquals(3, l.getQuantityDiscrepancy());
        assertTrue(l.getDiscrepancyNotes().contains("(2)"));
        assertTrue(l.getDiscrepancyNotes().contains("Prix unitaire (5.5)"));
    }

    @Test
    @DisplayName("match: a negative billable quantity counts as zero")
    void recordMatch_negativeBillable() {
        InvoiceLine l = line(1, "5.00", null, CurrencyCode.MAD);

        l.recordMatch(10, 2, -3, null);

        assertEquals(1, l.getQuantityDiscrepancy());
    }

    @Test
    @DisplayName("match: the invoice has a discrepancy when any line does, with a summary naming the lines")
    void refreshDiscrepancies_rollsUp() {
        InvoiceLine ok = line(1, "5.00", null, CurrencyCode.MAD);
        ok.setLineNumber(1);
        ok.recordMatch(10, 5, 5, Money.of("5.00", CurrencyCode.MAD));
        InvoiceLine over = line(4, "5.00", null, CurrencyCode.MAD);
        over.setLineNumber(2);
        over.recordMatch(10, 5, 1, Money.of("5.00", CurrencyCode.MAD));
        Invoice invoice = invoice("MAD", ok, over);

        invoice.refreshDiscrepancies();
        assertTrue(invoice.hasDiscrepancy());
        assertTrue(invoice.getDiscrepancySummary().startsWith("Ligne 2 :"));

        over.recordMatch(10, 5, 5, Money.of("5.00", CurrencyCode.MAD));
        invoice.refreshDiscrepancies();
        assertFalse(invoice.hasDiscrepancy());
        assertNull(invoice.getDiscrepancySummary());
    }

    // ---- Workflow ----

    @Test
    @DisplayName("workflow: draft → submitted → verified → paid records the verifier and the payment")
    void happyPath() {
        Invoice paid = inStatus(InvoiceStatus.PAID);

        assertEquals(InvoiceStatus.PAID, paid.getStatus());
        assertTrue(paid.isVerified());
        assertEquals("Ada", paid.getVerifiedByName());
        assertEquals(0, new BigDecimal("11.00").compareTo(paid.getPaidAmount().getAmount()));
        assertNotNull(paid.getPaymentDate());
    }

    @Test
    @DisplayName("workflow: each step is refused from the wrong status")
    void outOfOrder_isRefused() {
        assertThrows(InvoiceRuleViolationException.class, () -> inStatus(InvoiceStatus.SUBMITTED).submit("u"));
        assertThrows(InvoiceNotVerifiableException.class, () -> inStatus(InvoiceStatus.DRAFT).verify("u", "U"));
        assertThrows(InvoiceNotPayableException.class,
                () -> inStatus(InvoiceStatus.SUBMITTED).pay("u", "U", Money.of("1.00", CurrencyCode.MAD)));
    }

    @Test
    @DisplayName("pay: the amount must be positive and in the invoice currency")
    void pay_amountRules() {
        assertThrows(InvoiceValidationException.class,
                () -> inStatus(InvoiceStatus.VERIFIED).pay("u", "U", Money.of("0.00", CurrencyCode.MAD)));
        assertThrows(InvoiceValidationException.class,
                () -> inStatus(InvoiceStatus.VERIFIED).pay("u", "U", Money.of("11.00", CurrencyCode.EUR)));
        assertThrows(InvoiceValidationException.class, () -> inStatus(InvoiceStatus.VERIFIED).pay("u", "U", null));
    }

    @Test
    @DisplayName("pay: partial payments accumulate; the invoice is paid when the total with tax is reached")
    void pay_partialPayments() {
        Invoice invoice = inStatus(InvoiceStatus.VERIFIED);

        invoice.pay("admin-1", "Ada", Money.of("4.00", CurrencyCode.MAD));
        assertEquals(InvoiceStatus.VERIFIED, invoice.getStatus());
        assertTrue(invoice.isPartiallyPaid());
        assertEquals(0, new BigDecimal("7.00").compareTo(invoice.getOutstandingAmount().getAmount()));
        assertNull(invoice.getPaymentDate(), "the payment date is set once fully paid");

        invoice.pay("admin-2", "Bea", Money.of("7.00", CurrencyCode.MAD));
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertFalse(invoice.isPartiallyPaid());
        assertEquals(0, new BigDecimal("11.00").compareTo(invoice.getPaidAmount().getAmount()));
        assertEquals(0, invoice.getOutstandingAmount().getAmount().signum());
        assertEquals("Bea", invoice.getPaidByName(), "the last payer is recorded");
        assertNotNull(invoice.getPaymentDate());
    }

    @Test
    @DisplayName("pay: a payment above the outstanding balance is refused and changes nothing")
    void pay_overpayment_isRefused() {
        Invoice invoice = inStatus(InvoiceStatus.VERIFIED);
        invoice.pay("admin-1", "Ada", Money.of("10.00", CurrencyCode.MAD));

        assertThrows(InvoiceValidationException.class, () -> invoice.pay("admin-1", "Ada", Money.of("1.01", CurrencyCode.MAD)));
        assertEquals(0, new BigDecimal("10.00").compareTo(invoice.getPaidAmount().getAmount()));
        assertEquals(InvoiceStatus.VERIFIED, invoice.getStatus());
    }

    @Test
    @DisplayName("cancel: a partly paid invoice cannot be cancelled")
    void cancel_partlyPaid_isRefused() {
        Invoice invoice = inStatus(InvoiceStatus.VERIFIED);
        invoice.pay("admin-1", "Ada", Money.of("1.00", CurrencyCode.MAD));

        assertThrows(InvoiceCancellationException.class, () -> invoice.cancel("admin-1", "x"));
        assertEquals(InvoiceStatus.VERIFIED, invoice.getStatus());
    }

    @Test
    @DisplayName("cancel: paid or already cancelled invoices cannot be cancelled; a reason is required and must fit the notes")
    void cancel_rules() {
        assertThrows(InvoiceCancellationException.class, () -> inStatus(InvoiceStatus.PAID).cancel("u", "x"));
        assertThrows(InvoiceCancellationException.class, () -> inStatus(InvoiceStatus.CANCELLED).cancel("u", "x"));
        assertThrows(InvoiceValidationException.class, () -> inStatus(InvoiceStatus.DRAFT).cancel("u", " "));

        Invoice withNotes = inStatus(InvoiceStatus.SUBMITTED);
        withNotes.setNotes("n".repeat(990));
        assertThrows(InvoiceValidationException.class, () -> withNotes.cancel("u", "a reason that is too long"));
        assertEquals(InvoiceStatus.SUBMITTED, withNotes.getStatus());

        Invoice verified = inStatus(InvoiceStatus.VERIFIED);
        verified.cancel("admin-1", "Supplier withdrew it");
        assertEquals(InvoiceStatus.CANCELLED, verified.getStatus());
        assertTrue(verified.getNotes().endsWith("Annulée: Supplier withdrew it"));
    }
}
