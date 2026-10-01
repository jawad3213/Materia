package com.materia.backend.contexts.purchaseRequisition.domain.entities;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.materia.backend.support.fixtures.RequisitionFixtures.aLine;
import static org.junit.jupiter.api.Assertions.*;

/** Line-level rules (T053). */
class RequisitionLineTest {

    @Test
    @DisplayName("line total: equals quantity times unit price")
    void lineTotal_isQuantityTimesPrice() {
        assertEquals(Money.of("37.50", CurrencyCode.MAD), aLine(3, "12.50").getLineTotal());
    }

    @Test
    @DisplayName("line total: is recomputed when the quantity or price changes")
    void lineTotal_recomputes() {
        RequisitionLine line = aLine(2, "10.00");

        line.updateQuantity(5);
        assertEquals(Money.of("50.00", CurrencyCode.MAD), line.getLineTotal());

        line.updateUnitPrice(Money.of("4.00", CurrencyCode.MAD));
        assertEquals(Money.of("20.00", CurrencyCode.MAD), line.getLineTotal());
    }

    @ParameterizedTest(name = "quantity {0}")
    @ValueSource(ints = {0, -1})
    @DisplayName("quantity: must be strictly positive")
    void quantity_nonPositive_isRefused(int quantity) {
        RequisitionLine line = aLine(1, "10.00");
        assertThrows(IllegalArgumentException.class, () -> line.updateQuantity(quantity));
        assertEquals(1, line.getQuantity(), "a refused update leaves the quantity unchanged");
    }

    @Test
    @DisplayName("unit price: zero is refused, since a free line cannot be costed")
    void unitPrice_zero_isRefused() {
        assertThrows(IllegalArgumentException.class, () -> aLine(1, "10.00").updateUnitPrice(Money.zero(CurrencyCode.MAD)));
    }

    @Test
    @DisplayName("receipt: received plus rejected may equal but never exceed the ordered quantity")
    void receipt_cannotExceedQuantity() {
        RequisitionLine line = aLine(10, "1.00");

        assertDoesNotThrow(() -> line.receiveQuantity(7, 3));
        assertThrows(IllegalArgumentException.class, () -> line.receiveQuantity(8, 3));
    }

    @Test
    @DisplayName("receipt: negative received or rejected quantities are refused")
    void receipt_negative_isRefused() {
        RequisitionLine line = aLine(10, "1.00");
        assertThrows(IllegalArgumentException.class, () -> line.receiveQuantity(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> line.receiveQuantity(0, -1));
    }

    @Test
    @DisplayName("receipt: partial and full receipt are reported correctly")
    void receipt_progressIsReported() {
        RequisitionLine line = aLine(10, "1.00");

        line.receiveQuantity(4, 0);
        assertTrue(line.isPartiallyReceived());
        assertFalse(line.isFullyReceived());

        line.receiveQuantity(10, 0);
        assertTrue(line.isFullyReceived());
    }
}
