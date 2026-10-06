package com.materia.backend.contexts.payment.application.services;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceValidationException;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceUseCase;
import com.materia.backend.contexts.payment.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payment.application.dtos.CreatePaymentLineInput;
import com.materia.backend.contexts.payment.application.dtos.PaymentOutput;
import com.materia.backend.contexts.payment.application.mappers.PaymentMapper;
import com.materia.backend.contexts.payment.domain.entities.Payment;
import com.materia.backend.contexts.payment.domain.enums.PaymentStatus;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNotCancellableException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNotCompletableException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNotPreparableException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentRuleViolationException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentValidationException;
import com.materia.backend.contexts.payment.domain.ports.out.PaymentPort;
import com.materia.backend.contexts.payment.domain.valueObjects.PaymentCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Payment rules and orchestration: totals, invoice checks, all-or-nothing completion, and the lifecycle. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    private static final String SUPPLIER = UUID.randomUUID().toString();

    @Mock private PaymentPort payments;
    @Mock private PaymentCodeGeneratorService codes;
    @Mock private InvoiceUseCase invoices;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(payments, new PaymentMapper(), codes, invoices);
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(payments.findBySupplierId(anyString())).thenReturn(List.of());
        when(codes.generateCode()).thenReturn(PaymentCode.of("PAY-2026-0007"));
    }

    private InvoiceOutput invoice(String total, String paid) {
        InvoiceOutput inv = new InvoiceOutput();
        inv.setId(UUID.randomUUID());
        inv.setInvoiceCode("INV-2026-" + inv.getId().toString().substring(0, 4));
        inv.setStatus(InvoiceStatus.VERIFIED);
        inv.setInvoiceType(InvoiceType.STANDARD);
        inv.setSupplierId(SUPPLIER);
        inv.setSupplierName("Acme Supplies");
        inv.setCurrencyCode("MAD");
        inv.setTotalAmountWithTax(Money.of(total, CurrencyCode.MAD));
        inv.setPaidAmount(paid != null ? Money.of(paid, CurrencyCode.MAD) : null);
        when(invoices.getById(inv.getId())).thenReturn(inv);
        return inv;
    }

    private static CreatePaymentInput input(InvoiceOutput... lines) {
        CreatePaymentInput in = new CreatePaymentInput();
        in.setSupplierId(SUPPLIER);
        in.setCurrencyCode("MAD");
        in.setTotalAmount(new BigDecimal("1"));
        List<CreatePaymentLineInput> l = new ArrayList<>();
        for (InvoiceOutput inv : lines) {
            CreatePaymentLineInput line = new CreatePaymentLineInput();
            line.setInvoiceId(inv.getId().toString());
            line.setAmount(new BigDecimal("10.00"));
            l.add(line);
        }
        in.setLines(l);
        in.setUserId("admin-1");
        return in;
    }

    private Payment stored(CreatePaymentInput in) {
        Payment payment = new PaymentMapper().toEntity(in);
        when(payments.findById(payment.getId())).thenReturn(Optional.of(payment));
        return payment;
    }

    @Test
    @DisplayName("create: the total is the sum of the lines, whatever the client sent; code and invoice details come from the server")
    void create_derivesTotalAndDetails() {
        InvoiceOutput a = invoice("32.00", null);
        InvoiceOutput b = invoice("20.00", "5.00");

        PaymentOutput out = service.create(input(a, b));

        assertEquals(0, new BigDecimal("20.00").compareTo(out.getTotalAmount()));
        assertEquals("PAY-2026-0007", out.getPaymentCode());
        assertEquals("Acme Supplies", out.getSupplierName());
        assertEquals(a.getInvoiceCode(), out.getLines().get(0).getInvoiceCode());
        assertEquals(PaymentStatus.DRAFT, out.getStatus());
    }

    @Test
    @DisplayName("create: a credit note, another currency, or an amount above the balance is refused; nothing is saved")
    void create_refusals() {
        InvoiceOutput credit = invoice("32.00", null);
        credit.setInvoiceType(InvoiceType.CREDIT_NOTE);
        assertThrows(PaymentRuleViolationException.class, () -> service.create(input(credit)));

        InvoiceOutput euro = invoice("32.00", null);
        euro.setCurrencyCode("EUR");
        assertThrows(PaymentRuleViolationException.class, () -> service.create(input(euro)));

        InvoiceOutput almostPaid = invoice("32.00", "25.00");
        assertThrows(RuntimeException.class, () -> service.create(input(almostPaid)), "7.00 left, 10.00 asked");

        CreatePaymentInput badId = input(invoice("32.00", null));
        badId.getLines().get(0).setInvoiceId("not-a-uuid");
        assertThrows(PaymentValidationException.class, () -> service.create(badId));

        verify(payments, never()).save(any());
    }

    @Test
    @DisplayName("complete: each invoice records its amount; if one invoice refuses, the payment is not saved")
    void complete_paysEachInvoice_allOrNothing() {
        InvoiceOutput a = invoice("32.00", null);
        InvoiceOutput b = invoice("32.00", null);
        Payment ok = stored(input(a, b));

        service.complete(ok.getId(), "admin-1", "VIR-1", null, "BANK_TRANSFER");

        verify(invoices).pay(a.getId(), "admin-1", 10.0);
        verify(invoices).pay(b.getId(), "admin-1", 10.0);
        assertEquals(PaymentStatus.COMPLETED, ok.getStatus());

        Payment refused = stored(input(a, b));
        when(invoices.pay(eq(b.getId()), anyString(), anyDouble()))
                .thenThrow(new InvoiceValidationException("amount", "dépasse le reste à payer"));
        clearInvocations(payments);

        assertThrows(InvoiceValidationException.class,
                () -> service.complete(refused.getId(), "admin-1", null, null, "CASH"));
        verify(payments, never()).save(any());
    }

    @Test
    @DisplayName("lifecycle: prepare only a draft, complete only an open payment, cancel neither a completed nor a cancelled one")
    void lifecycle() {
        InvoiceOutput a = invoice("32.00", null);
        Payment payment = stored(input(a));

        service.prepare(payment.getId(), "admin-1");
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertThrows(PaymentNotPreparableException.class, () -> service.prepare(payment.getId(), "admin-1"));

        service.complete(payment.getId(), "admin-1", null, null, "card");
        assertEquals("CARD", payment.getPaymentMethod());
        assertTrue(payment.getLines().get(0).isPaid());
        assertThrows(PaymentNotCompletableException.class,
                () -> service.complete(payment.getId(), "admin-1", null, null, "CASH"));
        assertThrows(PaymentNotCancellableException.class, () -> service.cancel(payment.getId(), "admin-1", "x"));

        Payment other = stored(input(invoice("32.00", null)));
        assertThrows(PaymentValidationException.class, () -> service.cancel(other.getId(), "admin-1", " "));
        service.cancel(other.getId(), "admin-1", "Duplicate");
        assertThrows(PaymentNotCancellableException.class, () -> service.cancel(other.getId(), "admin-1", "again"));
        assertTrue(other.getNotes().endsWith("Cancelled: Duplicate"));
    }

    @Test
    @DisplayName("complete: transfers and cheques need a bank reference; unknown methods are refused")
    void complete_methodRules() {
        Payment payment = stored(input(invoice("32.00", null)));

        assertThrows(PaymentValidationException.class, () -> service.complete(payment.getId(), "a", null, null, "CHECK"));
        assertThrows(PaymentValidationException.class, () -> service.complete(payment.getId(), "a", "x", null, "BARTER"));
        assertThrows(PaymentValidationException.class, () -> service.complete(payment.getId(), "a", "x", null, null));
        assertEquals(PaymentStatus.DRAFT, payment.getStatus());
        verify(invoices, never()).pay(any(), any(), any());
    }
}
