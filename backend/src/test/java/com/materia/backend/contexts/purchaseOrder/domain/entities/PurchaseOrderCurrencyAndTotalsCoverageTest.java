package com.materia.backend.contexts.purchaseOrder.domain.entities;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidLineException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.aLine;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;

/** [T084] Order currency resolution, totals on edge inputs, and builder required fields (US1). */
class PurchaseOrderCurrencyAndTotalsCoverageTest {

    private static PurchaseOrder.Builder minimal() {
        return PurchaseOrder.builder().supplierId(UUID.randomUUID()).supplierName("Acme").orderedBy("buyer-1")
                .addLine(aLine(2, "5.00", CurrencyCode.MAD));
    }

    @Test
    @DisplayName("builder: supplier name and orderer cannot be blank; each line needs a material and a positive quantity")
    void builder_requiredFields() {
        assertThrows(PurchaseOrderValidationException.class, () -> minimal().supplierName(" ").build());
        assertThrows(PurchaseOrderValidationException.class, () -> minimal().orderedBy(" ").build());

        PurchaseOrderLine noMaterial = aLine(1, "1.00", CurrencyCode.MAD);
        noMaterial.setMaterialCode(" ");
        assertThrows(PurchaseOrderInvalidLineException.class, () -> minimal().lines(List.of(noMaterial)).build());

        PurchaseOrderLine zero = aLine(1, "1.00", CurrencyCode.MAD);
        zero.setQuantity(0);
        assertThrows(PurchaseOrderInvalidLineException.class, () -> minimal().lines(List.of(zero)).build());
    }

    @Test
    @DisplayName("builder: an explicit incoterm is kept; without a currency the order takes its first line's currency")
    void builder_incotermAndCurrencyFromLines() {
        PurchaseOrder order = PurchaseOrder.builder().supplierId(UUID.randomUUID()).supplierName("Acme")
                .orderedBy("buyer-1").incoterm("FOB").addLine(aLine(2, "5.00", CurrencyCode.EUR)).build();

        assertEquals("FOB", order.getIncoterm());
        assertEquals("EUR", order.getCurrencyCode());
    }

    @Test
    @DisplayName("totals: an order with no lines totals zero in its currency")
    void recalculate_withoutLines_isZero() {
        PurchaseOrder order = anOrder().withLine(2, "5.00").build();
        order.setLines(new ArrayList<>());
        order.recalculateTotals();
        assertEquals(0, order.getTotalAmount().getAmount().signum());
        assertEquals(0, order.getGrandTotal().getAmount().signum());

        order.setLines(null);
        order.recalculateTotals();
        assertEquals(0, order.getTotalAmount().getAmount().signum());
        assertEquals(0, order.getTotalQuantity());
    }

    @Test
    @DisplayName("currency: without an order currency, it comes from a line's price, total or code, else MAD")
    void currency_resolvedFromLines() {
        PurchaseOrder byPrice = anOrder().withLine(2, "5.00").build();
        byPrice.setCurrencyCode(null);
        byPrice.recalculateTotals();
        assertEquals("MAD", byPrice.getCurrencyCode());

        PurchaseOrderLine totalOnly = aLine(1, "4.00", CurrencyCode.EUR);
        totalOnly.setUnitPrice(null);
        PurchaseOrder byTotal = anOrder().withLine(2, "5.00").build();
        byTotal.setLines(new ArrayList<>(List.of(totalOnly)));
        byTotal.setCurrencyCode(null);
        byTotal.recalculateTotals();
        assertEquals("EUR", byTotal.getCurrencyCode());

        PurchaseOrderLine codeOnly = aLine(1, "4.00", CurrencyCode.EUR);
        codeOnly.setUnitPrice(null);
        codeOnly.setLineTotal(null);
        codeOnly.setCurrencyCode("EUR");
        PurchaseOrder byCode = anOrder().withLine(2, "5.00").build();
        byCode.setLines(new ArrayList<>(List.of(codeOnly)));
        byCode.setCurrencyCode(null);
        byCode.recalculateTotals();
        assertEquals("EUR", byCode.getCurrencyCode());

        PurchaseOrderLine bare = aLine(1, "4.00", CurrencyCode.MAD);
        bare.setUnitPrice(null);
        bare.setLineTotal(null);
        bare.setCurrencyCode(null);
        PurchaseOrder fallback = anOrder().withLine(2, "5.00").build();
        fallback.setLines(new ArrayList<>(List.of(bare)));
        fallback.setCurrencyCode(null);
        fallback.recalculateTotals();
        assertEquals("MAD", fallback.getCurrencyCode());
    }

    @Test
    @DisplayName("currency: a line total in another currency is refused when totalling")
    void currency_lineTotalMismatch_isRefused() {
        PurchaseOrderLine eur = aLine(1, "4.00", CurrencyCode.EUR);
        PurchaseOrder order = anOrder().withLine(2, "5.00").build();
        order.getLines().add(eur);

        assertThrows(PurchaseOrderValidationException.class, order::recalculateTotals);
    }

    @Test
    @DisplayName("completed: a completed order counts as completed; a confirmed order does not")
    void isCompleted() {
        PurchaseOrder order = anOrder().withLine(2, "5.00").build();
        order.setStatus(OrderStatus.COMPLETED);
        assertTrue(order.isCompleted());
        order.setStatus(OrderStatus.CONFIRMED);
        assertFalse(order.isCompleted());
        order.setStatus(null);
        assertFalse(order.isModifiable());
        assertFalse(order.isActive());
    }

    @Test
    @DisplayName("tax and shipping: amounts in another currency are refused")
    void additionalAmounts_otherCurrency_isRefused() {
        PurchaseOrder order = anOrder().withLine(2, "5.00").build();
        order.setTaxAmount(Money.of("1.00", CurrencyCode.EUR));

        assertThrows(PurchaseOrderValidationException.class, order::recalculateTotals);
    }
}
