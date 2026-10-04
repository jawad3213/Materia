package com.materia.backend.contexts.payement;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.invoice.application.dtos.CreateInvoiceInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceLineInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceUseCase;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.payement.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payement.application.dtos.CreatePaymentLineInput;
import com.materia.backend.contexts.payement.application.dtos.PaymentOutput;
import com.materia.backend.contexts.payement.domain.enums.PaymentStatus;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentAmountMismatchException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentRuleViolationException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentSupplierMismatchException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentValidationException;
import com.materia.backend.contexts.payement.domain.ports.in.PaymentUseCase;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.UserFixtures.aPurchaser;
import static com.materia.backend.support.fixtures.UserFixtures.aReceiver;
import static org.junit.jupiter.api.Assertions.*;

/** Paying invoices against the real database: a payment records its amounts on each invoice, all or nothing. */
class PaymentFlowIT extends AbstractIntegrationTest {

    @Autowired private PurchaseOrderUseCase orders;
    @Autowired private GoodsReceiptUseCase receipts;
    @Autowired private InvoiceUseCase invoices;
    @Autowired private PaymentUseCase payments;
    @Autowired private MaterialRepository materials;
    @Autowired private UserRepository users;
    @PersistenceContext private EntityManager em;

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    /** A verified invoice of 32.00 MAD (6 × 5.00 + 2.00 tax) for an order of the given supplier. */
    private InvoiceOutput verifiedInvoice(UUID supplierId) {
        Material material = materials.save(aMaterial().stock(0).build());
        User receiver = users.save(aReceiver().build());
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setMaterialId(material.getId());
        line.setMaterialCode(material.getCode().getValue());
        line.setMaterialName(material.getName());
        line.setQuantity(10);
        line.setUnitPrice(Money.of("5.00", CurrencyCode.MAD));
        CreatePurchaseOrderInput order = new CreatePurchaseOrderInput();
        order.setSupplierId(supplierId);
        order.setSupplierName("Acme Supplies");
        order.setOrderedBy("buyer-1");
        order.setCurrencyCode("MAD");
        order.setLines(new ArrayList<>(List.of(line)));
        order.setUserId("buyer-1");
        UUID orderId = orders.create(order).getId();
        orders.submit(orderId, "buyer-1");
        orders.confirm(orderId, "admin-1");
        PurchaseOrderOutput ready = orders.assignReceiver(orderId, "buyer-1", "Bob", receiver.getId().toString(), null);

        GoodsReceiptLineInput receiptLine = new GoodsReceiptLineInput();
        receiptLine.setPurchaseOrderLineId(ready.getLines().get(0).getId().toString());
        receiptLine.setMaterialCode(ready.getLines().get(0).getMaterialCode());
        receiptLine.setQuantityReceived(6);
        receiptLine.setQuantityRejected(0);
        receiptLine.setQualityStatus("ACCEPTED");
        CreateGoodsReceiptInput receipt = new CreateGoodsReceiptInput();
        receipt.setPurchaseOrderId(orderId.toString());
        receipt.setReceivedBy(receiver.getId().toString());
        receipt.setReceivedByName("Rita");
        receipt.setUserId(receiver.getId().toString());
        receipt.setLines(new ArrayList<>(List.of(receiptLine)));
        receipts.complete(receipts.create(receipt).getId(), receiver.getId().toString());
        flushAndClear();

        InvoiceLineInput invoiceLine = new InvoiceLineInput();
        invoiceLine.setPurchaseOrderLineId(ready.getLines().get(0).getId().toString());
        invoiceLine.setMaterialCode(ready.getLines().get(0).getMaterialCode());
        invoiceLine.setMaterialName("Bolts");
        invoiceLine.setQuantityInvoiced(6);
        invoiceLine.setUnitPrice(Money.of("5.00", CurrencyCode.MAD));
        invoiceLine.setTaxAmount(Money.of("2.00", CurrencyCode.MAD));
        CreateInvoiceInput invoice = new CreateInvoiceInput();
        invoice.setPurchaseOrderId(orderId.toString());
        invoice.setSupplierId(supplierId.toString());
        invoice.setSupplierName("Acme Supplies");
        invoice.setInvoiceType(InvoiceType.STANDARD);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setCurrencyCode("MAD");
        invoice.setLines(new ArrayList<>(List.of(invoiceLine)));
        invoice.setUserId("buyer-1");
        UUID invoiceId = invoices.create(invoice).getId();
        flushAndClear();
        invoices.submit(invoiceId, "buyer-1");
        flushAndClear();
        invoices.verify(invoiceId, "admin-1");
        flushAndClear();
        return invoices.getById(invoiceId);
    }

    private static CreatePaymentInput paymentFor(UUID supplierId, Object... invoiceAndAmount) {
        List<CreatePaymentLineInput> lines = new ArrayList<>();
        for (int i = 0; i < invoiceAndAmount.length; i += 2) {
            CreatePaymentLineInput line = new CreatePaymentLineInput();
            line.setInvoiceId(((InvoiceOutput) invoiceAndAmount[i]).getId().toString());
            line.setAmount(new BigDecimal((String) invoiceAndAmount[i + 1]));
            lines.add(line);
        }
        CreatePaymentInput input = new CreatePaymentInput();
        input.setSupplierId(supplierId.toString());
        input.setSupplierName("Whatever the client sent");
        input.setCurrencyCode("MAD");
        input.setTotalAmount(new BigDecimal("999999"));
        input.setLines(lines);
        input.setUserId("admin-1");
        return input;
    }

    @Test
    @DisplayName("flow: one payment settles two invoices of a supplier; codes, totals and invoice details come from the server")
    void paymentSettlesInvoices() {
        UUID supplier = UUID.randomUUID();
        InvoiceOutput first = verifiedInvoice(supplier);
        InvoiceOutput second = verifiedInvoice(supplier);
        String payer = users.save(aPurchaser().firstName("Paula").lastName("Payer").build()).getId().toString();

        PaymentOutput created = payments.create(paymentFor(supplier, first, "32.00", second, "32.00"));
        flushAndClear();

        assertEquals(PaymentStatus.DRAFT, created.getStatus());
        assertTrue(created.getPaymentCode().matches("PAY-\\d{4}-\\d+"), created.getPaymentCode());
        assertEquals(0, new BigDecimal("64.00").compareTo(created.getTotalAmount()), "total derived from the lines");
        assertEquals("Acme Supplies", created.getSupplierName());
        assertEquals(first.getInvoiceCode(), created.getLines().get(0).getInvoiceCode());

        payments.prepare(created.getId(), payer);
        flushAndClear();
        PaymentOutput completed = payments.complete(created.getId(), payer, "VIR-2026-001", null, "bank_transfer");
        flushAndClear();

        assertEquals(PaymentStatus.COMPLETED, completed.getStatus());
        assertEquals("BANK_TRANSFER", completed.getPaymentMethod());
        assertTrue(completed.getLines().stream().allMatch(l -> l.isPaid()));
        for (InvoiceOutput invoice : List.of(first, second)) {
            InvoiceOutput paid = invoices.getById(invoice.getId());
            assertEquals(InvoiceStatus.PAID, paid.getStatus());
            assertEquals("Paula Payer", paid.getPaidByName(), "the invoice names the payer from their account");
        }
    }

    @Test
    @DisplayName("partial: a partial payment leaves the invoice verified with the balance due; a second payment settles it")
    void partialPayments() {
        UUID supplier = UUID.randomUUID();
        InvoiceOutput invoice = verifiedInvoice(supplier);

        PaymentOutput part = payments.create(paymentFor(supplier, invoice, "20.00"));
        flushAndClear();
        payments.complete(part.getId(), "admin-1", null, null, "CASH");
        flushAndClear();
        InvoiceOutput afterFirst = invoices.getById(invoice.getId());
        assertEquals(InvoiceStatus.VERIFIED, afterFirst.getStatus());
        assertEquals(0, new BigDecimal("20.00").compareTo(afterFirst.getPaidAmount().getAmount()));

        PaymentOutput rest = payments.create(paymentFor(supplier, invoice, "12.00"));
        flushAndClear();
        payments.complete(rest.getId(), "admin-1", "CHQ-77", null, "CHECK");
        flushAndClear();

        assertEquals(InvoiceStatus.PAID, invoices.getById(invoice.getId()).getStatus());
    }

    @Test
    @DisplayName("reservations: an open payment sets its amount aside, so a second payment cannot pay the same balance again")
    void openPayments_reserveTheBalance() {
        UUID supplier = UUID.randomUUID();
        InvoiceOutput invoice = verifiedInvoice(supplier);
        PaymentOutput pending = payments.create(paymentFor(supplier, invoice, "30.00"));
        flushAndClear();

        assertThrows(PaymentAmountMismatchException.class, () -> payments.create(paymentFor(supplier, invoice, "2.01")));
        assertDoesNotThrow(() -> payments.create(paymentFor(supplier, invoice, "2.00")));
        flushAndClear();

        payments.cancel(pending.getId(), "admin-1", "Wrong amount");
        flushAndClear();
        assertDoesNotThrow(() -> payments.create(paymentFor(supplier, invoice, "30.00")), "a cancelled payment releases its amount");
    }

    @Test
    @DisplayName("rules: another supplier's invoice, an unverified invoice, a duplicate line or an unknown invoice is refused")
    void invoiceRules() {
        UUID supplier = UUID.randomUUID();
        InvoiceOutput invoice = verifiedInvoice(supplier);

        assertThrows(PaymentSupplierMismatchException.class, () -> payments.create(paymentFor(UUID.randomUUID(), invoice, "1.00")));
        assertThrows(PaymentValidationException.class, () -> payments.create(paymentFor(supplier, invoice, "1.00", invoice, "1.00")));

        CreatePaymentInput unknown = paymentFor(supplier, invoice, "1.00");
        unknown.getLines().get(0).setInvoiceId(UUID.randomUUID().toString());
        assertThrows(PaymentValidationException.class, () -> payments.create(unknown));

        PaymentOutput payment = payments.create(paymentFor(supplier, invoice, "32.00"));
        flushAndClear();
        payments.complete(payment.getId(), "admin-1", null, null, "CARD");
        flushAndClear();
        assertThrows(PaymentRuleViolationException.class, () -> payments.create(paymentFor(supplier, invoice, "1.00")),
                "a paid invoice is no longer verified");
    }

    @Test
    @DisplayName("lifecycle: a transfer needs a bank reference; completed payments cannot be cancelled; only drafts are deleted")
    void lifecycleRules() {
        UUID supplier = UUID.randomUUID();
        InvoiceOutput invoice = verifiedInvoice(supplier);
        PaymentOutput payment = payments.create(paymentFor(supplier, invoice, "10.00"));
        flushAndClear();

        assertThrows(PaymentValidationException.class, () -> payments.complete(payment.getId(), "admin-1", " ", null, "BANK_TRANSFER"));
        assertThrows(PaymentValidationException.class, () -> payments.complete(payment.getId(), "admin-1", null, null, "BARTER"));
        flushAndClear();
        assertEquals(0, invoices.getById(invoice.getId()).getPaidAmount() == null ? 0 : 1, "nothing was paid by refused attempts");

        payments.prepare(payment.getId(), "admin-1");
        flushAndClear();
        assertThrows(RuntimeException.class, () -> payments.delete(payment.getId()), "a prepared payment is cancelled, not deleted");
        flushAndClear();
        payments.complete(payment.getId(), "admin-1", null, null, "CASH");
        flushAndClear();
        assertThrows(RuntimeException.class, () -> payments.cancel(payment.getId(), "admin-1", "Too late"));

        PaymentOutput draft = payments.create(paymentFor(supplier, invoice, "1.00"));
        flushAndClear();
        payments.delete(draft.getId());
        flushAndClear();
        assertEquals(1, payments.getBySupplierId(supplier.toString()).size());
    }
}
