package com.materia.backend.contexts.invoice.application.services;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.invoice.application.dtos.CreateInvoiceInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceLineInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.application.dtos.UpdateInvoiceInput;
import com.materia.backend.contexts.invoice.application.mappers.InvoiceMapper;
import com.materia.backend.contexts.invoice.domain.entities.Invoice;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotModifiableException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceRuleViolationException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceValidationException;
import com.materia.backend.contexts.invoice.domain.ports.out.InvoiceRepository;
import com.materia.backend.contexts.invoice.domain.ports.out.UserDirectory;
import com.materia.backend.contexts.invoice.domain.valueObjects.InvoiceCode;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Invoice service: code generation and the three-way match against the order, receipts and other invoices. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InvoiceServiceMatchTest {

    @Mock private InvoiceRepository invoices;
    @Mock private InvoiceCodeGeneratorService codeGenerator;
    @Mock private PurchaseOrderRepository orders;
    @Mock private GoodsReceiptRepository receipts;
    @Mock private UserDirectory users;

    private InvoiceService service;
    private PurchaseOrder order;

    @BeforeEach
    void setUp() {
        service = new InvoiceService(invoices, new InvoiceMapper(), codeGenerator, orders, receipts, users);
        when(users.displayName("admin-1")).thenReturn("Ada Admin");
        when(invoices.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(codeGenerator.generateCode()).thenReturn(InvoiceCode.of("INV-2026-0042"));
        order = anOrder().withLine(10, "5.00").inStatus(OrderStatus.PARTIALLY_RECEIVED).build();
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));
        GoodsReceipt validated = aReceipt(order).receiving(0, 6, 0, null).inStatus(ReceiptStatus.PARTIAL).build();
        GoodsReceipt draft = aReceipt(order).receiving(0, 4, 0, null).build();
        when(receipts.findByPurchaseOrderId(order.getId().toString())).thenReturn(List.of(validated, draft));
        when(invoices.findByPurchaseOrderId(order.getId().toString())).thenReturn(List.of());
    }

    private CreateInvoiceInput input(InvoiceType type, int quantity) {
        InvoiceLineInput line = new InvoiceLineInput();
        line.setPurchaseOrderLineId(order.getLines().get(0).getId().toString());
        line.setMaterialCode("CLIENT");
        line.setMaterialName("Client name");
        line.setQuantityInvoiced(quantity);
        line.setUnitPrice(Money.of("5.00", CurrencyCode.MAD));
        CreateInvoiceInput in = new CreateInvoiceInput();
        in.setPurchaseOrderId(order.getId().toString());
        in.setSupplierId(order.getSupplierId().toString());
        in.setSupplierName("Client supplier name");
        in.setInvoiceType(type);
        in.setInvoiceDate(LocalDate.now());
        in.setCurrencyCode("MAD");
        in.setLines(new ArrayList<>(List.of(line)));
        in.setUserId("buyer-1");
        return in;
    }

    private Invoice existing(InvoiceType type, int quantity) {
        Invoice inv = new InvoiceMapper().toEntity(input(type, quantity));
        inv.getLines().forEach(l -> l.setPurchaseOrderLineId(order.getLines().get(0).getId().toString()));
        return inv;
    }

    @Test
    @DisplayName("create: the code is generated; material and supplier come from the order; only validated receipts count")
    void create_generatesCodeAndUsesOrderData() {
        InvoiceOutput out = service.create(input(InvoiceType.STANDARD, 6));

        assertEquals("INV-2026-0042", out.getInvoiceCode());
        assertEquals(order.getSupplierName(), out.getSupplierName());
        assertEquals(order.getOrderCode().getValue(), out.getPurchaseOrderCode());
        assertEquals(order.getLines().get(0).getMaterialCode(), out.getLines().get(0).getMaterialCode());
        assertEquals(6, out.getLines().get(0).getQuantityReceived(), "the draft receipt of 4 is ignored");
        assertFalse(out.isHasDiscrepancy());
    }

    @Test
    @DisplayName("match: earlier invoices reduce what is billable, credit notes add it back, cancelled ones are ignored")
    void otherInvoices_affectBillable() {
        Invoice billed = existing(InvoiceType.STANDARD, 5);
        Invoice credit = existing(InvoiceType.CREDIT_NOTE, 2);
        Invoice cancelled = existing(InvoiceType.STANDARD, 6);
        cancelled.cancel("buyer-1", "Wrong");
        when(invoices.findByPurchaseOrderId(order.getId().toString())).thenReturn(List.of(billed, credit, cancelled));

        InvoiceOutput out = service.create(input(InvoiceType.STANDARD, 4));

        // 6 received − (5 − 2) billed = 3 billable; 4 billed → 1 too many.
        assertTrue(out.isHasDiscrepancy());
        assertEquals(1, out.getLines().get(0).getQuantityDiscrepancy());
    }

    @Test
    @DisplayName("match: a credit note may credit up to what was billed, not what was received")
    void creditNote_billableIsWhatWasInvoiced() {
        when(invoices.findByPurchaseOrderId(order.getId().toString())).thenReturn(List.of(existing(InvoiceType.STANDARD, 2)));

        InvoiceOutput out = service.create(input(InvoiceType.CREDIT_NOTE, 3));

        assertEquals(1, out.getLines().get(0).getQuantityDiscrepancy());
    }

    @Test
    @DisplayName("rules: an order with nothing received, another currency or a foreign line is refused; nothing is saved")
    void orderRules_refuse() {
        PurchaseOrder confirmed = anOrder().withLine(10, "5.00").inStatus(OrderStatus.CONFIRMED).build();
        when(orders.findById(confirmed.getId())).thenReturn(Optional.of(confirmed));
        CreateInvoiceInput notReceived = input(InvoiceType.STANDARD, 1);
        notReceived.setPurchaseOrderId(confirmed.getId().toString());
        notReceived.setSupplierId(confirmed.getSupplierId().toString());
        assertThrows(InvoiceRuleViolationException.class, () -> service.create(notReceived));

        CreateInvoiceInput euro = input(InvoiceType.STANDARD, 1);
        euro.setCurrencyCode("EUR");
        euro.getLines().get(0).setUnitPrice(Money.of("5.00", CurrencyCode.EUR));
        assertThrows(InvoiceRuleViolationException.class, () -> service.create(euro));

        CreateInvoiceInput foreign = input(InvoiceType.STANDARD, 1);
        foreign.getLines().get(0).setPurchaseOrderLineId(UUID.randomUUID().toString());
        assertThrows(InvoiceRuleViolationException.class, () -> service.create(foreign));

        CreateInvoiceInput badId = input(InvoiceType.STANDARD, 1);
        badId.setPurchaseOrderId("not-a-uuid");
        assertThrows(InvoiceValidationException.class, () -> service.create(badId));

        CreateInvoiceInput unknown = input(InvoiceType.STANDARD, 1);
        unknown.setPurchaseOrderId(UUID.randomUUID().toString());
        assertThrows(InvoiceValidationException.class, () -> service.create(unknown));

        verify(invoices, never()).save(any());
    }

    @Test
    @DisplayName("update: the purchase order cannot be changed; a non-modifiable invoice cannot be edited")
    void update_rules() {
        Invoice draft = existing(InvoiceType.STANDARD, 2);
        when(invoices.findById(draft.getId())).thenReturn(Optional.of(draft));
        UpdateInvoiceInput moveOrder = new UpdateInvoiceInput();
        moveOrder.setPurchaseOrderId(UUID.randomUUID().toString());
        assertThrows(InvoiceRuleViolationException.class, () -> service.update(draft.getId(), moveOrder));

        Invoice paid = existing(InvoiceType.STANDARD, 2);
        paid.submit("u");
        paid.verify("a", "A");
        paid.pay("a", "A", Money.of("10.00", CurrencyCode.MAD));
        when(invoices.findById(paid.getId())).thenReturn(Optional.of(paid));
        assertThrows(InvoiceNotModifiableException.class, () -> service.update(paid.getId(), new UpdateInvoiceInput()));
    }

    @Test
    @DisplayName("update: changing a line's quantity re-runs the match and the totals")
    void update_rematches() {
        Invoice draft = existing(InvoiceType.STANDARD, 2);
        when(invoices.findById(draft.getId())).thenReturn(Optional.of(draft));
        UpdateInvoiceInput edit = new UpdateInvoiceInput();
        InvoiceLineInput line = input(InvoiceType.STANDARD, 8).getLines().get(0);
        edit.setLines(new ArrayList<>(List.of(line)));
        edit.setUserId("buyer-1");

        InvoiceOutput out = service.update(draft.getId(), edit);

        assertEquals(2, out.getLines().get(0).getQuantityDiscrepancy());
        assertEquals(0, new java.math.BigDecimal("40.00").compareTo(out.getTotalAmount().getAmount()));
    }

    @Test
    @DisplayName("verify: the match is refreshed with receipts validated since the invoice was saved")
    void verify_rematches() {
        Invoice submitted = existing(InvoiceType.STANDARD, 8);
        submitted.submit("buyer-1");
        when(invoices.findById(submitted.getId())).thenReturn(Optional.of(submitted));
        GoodsReceipt more = aReceipt(order).alreadyReceived(0, 6).receiving(0, 4, 0, null).inStatus(ReceiptStatus.COMPLETED).build();
        when(receipts.findByPurchaseOrderId(order.getId().toString()))
                .thenReturn(List.of(aReceipt(order).receiving(0, 6, 0, null).inStatus(ReceiptStatus.PARTIAL).build(), more));

        InvoiceOutput out = service.verify(submitted.getId(), "admin-1");

        assertFalse(out.isHasDiscrepancy(), "10 now received, 8 billed");
        assertEquals("Ada Admin", out.getVerifiedByName(), "named from the verifier's account");
        assertEquals(10, out.getLines().get(0).getQuantityReceived());
    }

    @Test
    @DisplayName("pay: the payer is named from their account; partial payments keep the invoice open until fully paid")
    void pay_namesPayerAndAccumulates() {
        Invoice verified = existing(InvoiceType.STANDARD, 2);
        verified.submit("buyer-1");
        verified.verify("admin-1", "Ada Admin");
        when(invoices.findById(verified.getId())).thenReturn(Optional.of(verified));

        InvoiceOutput partly = service.pay(verified.getId(), "admin-1", 4.0);
        assertEquals(com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus.VERIFIED, partly.getStatus());
        assertEquals("Ada Admin", partly.getPaidByName());

        InvoiceOutput settled = service.pay(verified.getId(), "admin-1", 6.0);
        assertEquals(com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus.PAID, settled.getStatus());
        assertEquals(0, new java.math.BigDecimal("10").compareTo(settled.getPaidAmount().getAmount()));
    }

    @Test
    @DisplayName("delete: only a draft is deleted; a submitted invoice is refused")
    void delete_onlyDrafts() {
        Invoice draft = existing(InvoiceType.STANDARD, 1);
        Invoice submitted = existing(InvoiceType.STANDARD, 1);
        submitted.submit("u");
        when(invoices.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(invoices.findById(submitted.getId())).thenReturn(Optional.of(submitted));

        service.delete(draft.getId());
        assertThrows(InvoiceNotModifiableException.class, () -> service.delete(submitted.getId()));

        ArgumentCaptor<UUID> deleted = ArgumentCaptor.forClass(UUID.class);
        verify(invoices, times(1)).deleteById(deleted.capture());
        assertEquals(draft.getId(), deleted.getValue());
    }
}
