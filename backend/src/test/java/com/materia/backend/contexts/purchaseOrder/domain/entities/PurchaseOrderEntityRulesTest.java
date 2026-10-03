package com.materia.backend.contexts.purchaseOrder.domain.entities;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidLineException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderLineRequiredException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.valueObjects.OrderCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.aLine;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;

/** Builder defaults, required fields, currency resolution and identity of purchase orders and their lines (US1, edge cases). */
class PurchaseOrderEntityRulesTest {

    private static PurchaseOrder.Builder minimal() {
        return PurchaseOrder.builder().supplierId(UUID.randomUUID()).supplierName("Acme").orderedBy("buyer-1")
                .addLine(aLine(2, "5.00", CurrencyCode.MAD));
    }

    @Test
    @DisplayName("builder: a minimal order defaults to draft, not shipped, EXW, today, with a generated id and code")
    void builder_defaults() {
        PurchaseOrder order = minimal().build();

        assertEquals(OrderStatus.DRAFT, order.getStatus());
        assertEquals(DeliveryStatus.NOT_SHIPPED, order.getDeliveryStatus());
        assertEquals("EXW", order.getIncoterm());
        assertEquals("MAD", order.getCurrencyCode());
        assertEquals(LocalDate.now(), order.getOrderDate());
        assertNotNull(order.getId());
        assertNotNull(order.getOrderCode());
        assertNotNull(order.getCreatedAt());
    }

    @Test
    @DisplayName("builder: explicit values are kept, including a null order date meaning today")
    void builder_keepsExplicitValues() {
        LocalDateTime created = LocalDateTime.now().minusDays(2);
        PurchaseOrder order = minimal().orderCode("PO-2026-0042").status(OrderStatus.SUBMITTED)
                .deliveryStatus(DeliveryStatus.SHIPPED).incoterm("DAP").orderDate(null)
                .createdAt(created).updatedAt(created).createdBy("buyer-9").build();

        assertEquals(OrderCode.of("PO-2026-0042"), order.getOrderCode());
        assertEquals(OrderStatus.SUBMITTED, order.getStatus());
        assertEquals(DeliveryStatus.SHIPPED, order.getDeliveryStatus());
        assertEquals("DAP", order.getIncoterm());
        assertEquals(LocalDate.now(), order.getOrderDate());
        assertEquals(created, order.getCreatedAt());
        assertEquals("buyer-9", order.getCreatedBy());
    }

    @Test
    @DisplayName("builder: supplier, supplier name and orderer are required")
    void builder_requiredFields() {
        assertThrows(PurchaseOrderValidationException.class, () -> PurchaseOrder.builder().supplierName("A").orderedBy("b")
                .addLine(aLine(1, "1.00", CurrencyCode.MAD)).build());
        assertThrows(PurchaseOrderValidationException.class, () -> PurchaseOrder.builder().supplierId(UUID.randomUUID())
                .supplierName(" ").orderedBy("b").addLine(aLine(1, "1.00", CurrencyCode.MAD)).build());
        assertThrows(PurchaseOrderValidationException.class, () -> PurchaseOrder.builder().supplierId(UUID.randomUUID())
                .supplierName("A").orderedBy(" ").addLine(aLine(1, "1.00", CurrencyCode.MAD)).build());
    }

    @Test
    @DisplayName("builder: a null line or line list is refused; a line without material or quantity is refused")
    void builder_lineValidation() {
        assertThrows(PurchaseOrderInvalidLineException.class, () -> PurchaseOrder.builder().addLine(null));
        assertThrows(PurchaseOrderInvalidLineException.class, () -> PurchaseOrder.builder().lines(null));
        assertThrows(PurchaseOrderLineRequiredException.class, () -> PurchaseOrder.builder().supplierId(UUID.randomUUID())
                .supplierName("A").orderedBy("b").build());

        PurchaseOrderLine noMaterial = aLine(1, "1.00", CurrencyCode.MAD);
        noMaterial.setMaterialCode(" ");
        assertThrows(PurchaseOrderInvalidLineException.class, () -> PurchaseOrder.builder().supplierId(UUID.randomUUID())
                .supplierName("A").orderedBy("b").lines(List.of(noMaterial)).build());

        PurchaseOrderLine noQuantity = aLine(1, "1.00", CurrencyCode.MAD);
        noQuantity.setQuantity(null);
        assertThrows(PurchaseOrderInvalidLineException.class, () -> PurchaseOrder.builder().supplierId(UUID.randomUUID())
                .supplierName("A").orderedBy("b").lines(List.of(noQuantity)).build());
    }

    @Test
    @DisplayName("builder: without an order currency, the first priced line decides it; mixed currencies are refused")
    void builder_currencyFromLines() {
        PurchaseOrder eur = PurchaseOrder.builder().supplierId(UUID.randomUUID()).supplierName("A").orderedBy("b")
                .addLine(aLine(1, "3.00", CurrencyCode.EUR)).build();
        assertEquals("EUR", eur.getCurrencyCode());

        assertThrows(PurchaseOrderValidationException.class, () -> PurchaseOrder.builder().supplierId(UUID.randomUUID())
                .supplierName("A").orderedBy("b").currencyCode("MAD").addLine(aLine(1, "3.00", CurrencyCode.EUR)).build());
    }

    @Test
    @DisplayName("builder: tax and shipping are added to the grand total; in another currency they are refused")
    void builder_taxAndShipping() {
        PurchaseOrder order = minimal().taxAmount(Money.of("1.00", CurrencyCode.MAD))
                .shippingCost(Money.of("2.00", CurrencyCode.MAD)).build();
        assertEquals(0, new java.math.BigDecimal("13").compareTo(order.getGrandTotal().getAmount()));

        assertThrows(PurchaseOrderValidationException.class,
                () -> minimal().taxAmount(Money.of("1.00", CurrencyCode.EUR)).build());
    }

    @Test
    @DisplayName("currency: with no order currency, a line total or a line currency also decides it")
    void currency_fromLineTotalOrCode() {
        PurchaseOrder order = anOrder().withLine(2, "4.00").build();
        order.setCurrencyCode(null);
        PurchaseOrderLine line = order.getLines().get(0);
        line.setUnitPrice(null);
        line.setLineTotal(Money.of("8.00", CurrencyCode.USD));

        order.recalculateTotals();
        assertEquals("USD", order.getCurrencyCode());
    }

    @Test
    @DisplayName("totals: an order whose lines were all removed totals zero")
    void totals_withoutLines_areZero() {
        PurchaseOrder order = anOrder().build();
        order.setLines(new ArrayList<>());

        order.recalculateTotals();

        assertEquals(0, order.getTotalAmount().getAmount().signum());
        assertEquals(0, order.getGrandTotal().getAmount().signum());
        assertEquals(0, order.getTotalQuantity());
    }

    @Test
    @DisplayName("quantities: a line without quantity counts as zero towards the total quantity")
    void totalQuantity_ignoresNullQuantities() {
        PurchaseOrder order = anOrder().withLine(3, "1.00").withLine(4, "1.00").build();
        order.getLines().get(1).setQuantity(null);

        assertEquals(3, order.getTotalQuantity());
    }

    @Test
    @DisplayName("state: an order with no status is neither modifiable nor active")
    void noStatus_isNeitherModifiableNorActive() {
        PurchaseOrder order = new PurchaseOrder();

        assertFalse(order.isModifiable());
        assertFalse(order.isActive());
        assertFalse(order.isCompleted());
    }

    @Test
    @DisplayName("identity: orders are equal by id or by order code, never to another type or null")
    void identity() {
        PurchaseOrder a = anOrder().build();
        PurchaseOrder sameId = anOrder().build();
        sameId.setId(a.getId());
        PurchaseOrder sameCode = anOrder().build();
        sameCode.setOrderCode(a.getOrderCode());
        PurchaseOrder other = anOrder().build();

        assertEquals(a, a);
        assertEquals(a, sameId);
        assertEquals(a, sameCode);
        assertNotEquals(a, other);
        assertNotEquals(a, null);
        assertNotEquals(a, "order");
        assertEquals(a.hashCode(), a.hashCode());
        assertTrue(a.toString().contains(a.getOrderCode().getValue()));
    }

    @Test
    @DisplayName("lines: lines are equal by id only; a line without id equals only itself")
    void lineIdentity() {
        PurchaseOrderLine a = aLine(1, "1.00", CurrencyCode.MAD);
        PurchaseOrderLine sameId = aLine(5, "9.00", CurrencyCode.MAD);
        sameId.setId(a.getId());
        PurchaseOrderLine noId = new PurchaseOrderLine();

        assertEquals(a, sameId);
        assertEquals(a.hashCode(), sameId.hashCode());
        assertNotEquals(a, aLine(1, "1.00", CurrencyCode.MAD));
        assertNotEquals(noId, new PurchaseOrderLine());
        assertEquals(0, noId.hashCode());
        assertNotEquals(a, null);
        assertNotEquals(a, "line");
        assertNotNull(a.toString());
    }

    @Test
    @DisplayName("lines: the line total is recomputed only when both price and quantity are known; currency codes are normalised")
    void lineTotal_andCurrency() {
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.calculateLineTotal();
        assertNull(line.getLineTotal());

        line.setQuantity(3);
        line.setUnitPrice(Money.of("2.00", CurrencyCode.MAD));
        line.calculateLineTotal();
        assertEquals(0, new java.math.BigDecimal("6").compareTo(line.getLineTotal().getAmount()));

        line.setCurrencyCode(" eur ");
        assertEquals("EUR", line.getCurrencyCode());
        line.setCurrencyCode(null);
        assertNull(line.getCurrencyCode());
    }

    @Test
    @DisplayName("F-003: a cancel or reject reason that would push the notes past 1000 characters is refused and the order is unchanged")
    void reasonOverflowingNotes_isRefused_orderUnchanged() {
        PurchaseOrder draft = anOrder().withLine(1, "1.00").build();
        draft.setNotes("n".repeat(900));
        assertThrows(PurchaseOrderValidationException.class, () -> draft.cancel("buyer-1", "r".repeat(500)));
        assertEquals(OrderStatus.DRAFT, draft.getStatus());
        assertEquals("n".repeat(900), draft.getNotes());

        PurchaseOrder submitted = anOrder().withLine(1, "1.00").inStatus(OrderStatus.SUBMITTED).build();
        submitted.setNotes("n".repeat(900));
        assertThrows(PurchaseOrderValidationException.class, () -> submitted.reject("buyer-1", "r".repeat(500)));
        assertEquals(OrderStatus.SUBMITTED, submitted.getStatus());
        assertEquals("n".repeat(900), submitted.getNotes());
    }

    @Test
    @DisplayName("F-003: a reason that exactly fills the notes to 1000 characters is accepted")
    void reasonFillingNotesExactly_isAccepted() {
        PurchaseOrder draft = anOrder().withLine(1, "1.00").build();
        draft.setNotes("n".repeat(900));
        String label = " Annulee: ";
        String reason = "r".repeat(PurchaseOrder.MAX_NOTES_LENGTH - 900 - label.length());

        draft.cancel("buyer-1", reason);

        assertEquals(OrderStatus.CANCELLED, draft.getStatus());
        assertEquals(PurchaseOrder.MAX_NOTES_LENGTH, draft.getNotes().length());
    }
}
