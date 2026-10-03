package com.materia.backend.contexts.purchaseOrder.domain.entities;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidLineException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderLineRequiredException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotModifiableException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.aLine;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;

/** [T019] Line, total and currency rules of a purchase order (US1, edge cases). */
class PurchaseOrderLinesAndTotalsTest {

    /** Compares amounts numerically, so 50 and 50.00 are equal. */
    private static void assertAmount(String expected, Money actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual.getAmount()),
                () -> "expected " + expected + " but was " + actual.getAmount());
    }

    @Test
    @DisplayName("lines: a line total is quantity times unit price")
    void lineTotal_isQuantityTimesUnitPrice() {
        PurchaseOrderLine line = aLine(4, "12.50", CurrencyCode.MAD);

        assertAmount("50", line.getLineTotal());
    }

    @Test
    @DisplayName("totals: total is the sum of line totals; grand total adds tax and shipping")
    void totals_sumLinesTaxAndShipping() {
        PurchaseOrder order = anOrder().withLine(2, "10.00").withLine(3, "5.00").build();
        order.setTaxAmount(Money.of("7.00", CurrencyCode.MAD));
        order.setShippingCost(Money.of("3.00", CurrencyCode.MAD));

        order.recalculateTotals();

        assertAmount("35", order.getTotalAmount());
        assertAmount("45", order.getGrandTotal());
    }

    @Test
    @DisplayName("totals: missing tax and shipping count as zero")
    void totals_missingTaxAndShippingAreZero() {
        PurchaseOrder order = anOrder().withLine(2, "10.00").build();

        order.recalculateTotals();

        assertAmount(order.getTotalAmount().getAmount().toPlainString(), order.getGrandTotal());
    }

    @Test
    @DisplayName("currency: a line in a different currency from the order is refused (edge case)")
    void lineCurrencyMismatch_isRefused() {
        PurchaseOrder order = anOrder().withLine(2, "10.00").build();
        order.getLines().add(aLine(1, "4.00", CurrencyCode.EUR));

        assertThrows(PurchaseOrderValidationException.class, order::recalculateTotals);
    }

    @Test
    @DisplayName("currency: tax in a different currency from the order is refused (edge case)")
    void taxCurrencyMismatch_isRefused() {
        PurchaseOrder order = anOrder().withLine(2, "10.00").build();
        order.setTaxAmount(Money.of("1.00", CurrencyCode.USD));

        assertThrows(PurchaseOrderValidationException.class, order::recalculateTotals);
    }

    @Test
    @DisplayName("currency: shipping in a different currency from the order is refused (edge case)")
    void shippingCurrencyMismatch_isRefused() {
        PurchaseOrder order = anOrder().withLine(2, "10.00").build();
        order.setShippingCost(Money.of("1.00", CurrencyCode.EUR));

        assertThrows(PurchaseOrderValidationException.class, order::recalculateTotals);
    }

    @Test
    @DisplayName("currency: with no order currency set, the first priced line decides it")
    void orderCurrency_fallsBackToFirstLine() {
        PurchaseOrder order = anOrder().currency(CurrencyCode.EUR).withLine(1, "9.00").build();
        order.setCurrencyCode(null);

        order.recalculateTotals();

        assertEquals("EUR", order.getCurrencyCode());
    }

    @Test
    @DisplayName("lines: an order with no lines cannot be built (edge case)")
    void orderWithoutLines_isRefused() {
        assertThrows(PurchaseOrderLineRequiredException.class, () -> PurchaseOrder.builder()
                .supplierId(UUID.randomUUID()).supplierName("Acme").orderedBy("buyer-1")
                .lines(List.of()).build());
    }

    @Test
    @DisplayName("lines: a line without material, with zero quantity, or without price is refused")
    void invalidLines_areRefused() {
        assertThrows(PurchaseOrderInvalidLineException.class, () -> PurchaseOrderLine.builder()
                .quantity(1).unitPrice(Money.of("1.00", CurrencyCode.MAD)).build());
        assertThrows(PurchaseOrderInvalidLineException.class, () -> PurchaseOrderLine.builder()
                .materialCode("MAT-1").quantity(0).unitPrice(Money.of("1.00", CurrencyCode.MAD)).build());
        assertThrows(PurchaseOrderInvalidLineException.class, () -> PurchaseOrderLine.builder()
                .materialCode("MAT-1").quantity(1).build());
        assertThrows(PurchaseOrderInvalidLineException.class, () -> PurchaseOrderLine.builder()
                .materialCode("MAT-1").quantity(1).unitPrice(Money.of("1.00", CurrencyCode.MAD)).currencyCode("EUR").build());
    }

    @Test
    @DisplayName("lines: adding and removing lines recalculates the total while the order is a draft")
    void addAndRemoveLine_inDraft_recalculate() {
        PurchaseOrder order = anOrder().withLine(1, "10.00").build();

        order.addLine(aLine(2, "5.00", CurrencyCode.MAD));
        assertAmount("20", order.getTotalAmount());

        order.removeLine(0);
        assertAmount("10", order.getTotalAmount());
    }

    @Test
    @DisplayName("lines: lines cannot be added or removed once the order is submitted")
    void addAndRemoveLine_afterDraft_areRefused() {
        PurchaseOrder order = anOrder().withLines(2).inStatus(OrderStatus.SUBMITTED).build();

        assertThrows(PurchaseOrderNotModifiableException.class, () -> order.addLine(aLine(1, "1.00", CurrencyCode.MAD)));
        assertThrows(PurchaseOrderNotModifiableException.class, () -> order.removeLine(0));
    }

    @Test
    @DisplayName("lines: a null line or an out-of-range index is refused")
    void nullLineAndBadIndex_areRefused() {
        PurchaseOrder order = anOrder().build();

        assertThrows(PurchaseOrderInvalidLineException.class, () -> order.addLine(null));
        assertThrows(PurchaseOrderInvalidLineException.class, () -> order.removeLine(5));
        assertThrows(PurchaseOrderInvalidLineException.class, () -> order.removeLine(-1));
    }

    @Test
    @DisplayName("reject: a supplier rejection without a reason is refused (US1-4)")
    void reject_withoutReason_isRefused() {
        PurchaseOrder order = anOrder().inStatus(OrderStatus.SUBMITTED).build();

        assertThrows(PurchaseOrderValidationException.class, () -> order.reject("buyer-1", " "));
        assertThrows(PurchaseOrderValidationException.class, () -> order.reject("buyer-1", null));
        assertEquals(OrderStatus.SUBMITTED, order.getStatus());
    }

    @Test
    @DisplayName("assign: assigning without naming a receiver is refused")
    void assign_withoutReceiver_isRefused() {
        PurchaseOrder order = anOrder().inStatus(OrderStatus.CONFIRMED).build();

        assertThrows(PurchaseOrderValidationException.class, () -> order.assignReceiver("buyer-1", "Bob", " ", "Nobody"));
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertNull(order.getAssignedTo());
    }

    @Test
    @DisplayName("quantities: the total quantity is the sum of line quantities")
    void totalQuantity_sumsLines() {
        PurchaseOrder order = anOrder().withLine(3, "1.00").withLine(4, "1.00").build();

        assertEquals(7, order.getTotalQuantity());
    }

    @Test
    @DisplayName("state: completed orders report completed; active orders report active")
    void completedAndActive_predicates() {
        assertTrue(anOrder().inStatus(OrderStatus.COMPLETED).build().isCompleted());
        assertFalse(anOrder().inStatus(OrderStatus.CONFIRMED).build().isCompleted());
        assertTrue(anOrder().inStatus(OrderStatus.CONFIRMED).build().isActive());
        assertFalse(anOrder().inStatus(OrderStatus.CANCELLED).build().isActive());
    }
}
