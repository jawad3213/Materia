package com.materia.backend.contexts.purchaseOrder.domain.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

/** [T021] Order code format, generation and equality. */
class OrderCodeTest {

    @Test
    @DisplayName("format: a year-based code PO-YYYY-NNNN is accepted and split into its parts")
    void yearBasedCode_isParsed() {
        OrderCode code = OrderCode.of("PO-2026-0042");

        assertEquals("PO-2026-0042", code.getValue());
        assertEquals("PO", code.getPrefix());
        assertEquals("0042", code.getNumber());
        assertEquals(42, code.getNumberAsInt());
        assertEquals(2026, code.getYear());
        assertEquals("PO-2026-0042", code.toString());
    }

    @Test
    @DisplayName("format: a legacy code PO-NNNN is accepted and has no year")
    void legacyCode_hasNoYear() {
        OrderCode code = OrderCode.of(" PO-0007 ");

        assertEquals("PO-0007", code.getValue());
        assertEquals(-1, code.getYear());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "PO-26-0001", "PR-2026-0001", "PO-2026-001", "PO2026-0001"})
    @DisplayName("format: blank and malformed codes are refused")
    void malformedCodes_areRefused(String value) {
        assertThrows(IllegalArgumentException.class, () -> OrderCode.of(value));
        assertFalse(OrderCode.isValid(value));
    }

    @Test
    @DisplayName("generation: a code is built from prefix, year and a zero-padded number")
    void generation_padsNumber() {
        assertEquals("PO-2026-0005", OrderCode.fromPrefixYearAndNumber("po", 2026, 5).getValue());
        assertEquals("PO-" + Year.now().getValue() + "-0001", OrderCode.createDefault().getValue());
    }

    @Test
    @DisplayName("generation: a blank prefix or a number outside 0-9999 is refused")
    void generation_refusesBadInput() {
        assertThrows(IllegalArgumentException.class, () -> OrderCode.fromPrefixYearAndNumber(" ", 2026, 1));
        assertThrows(IllegalArgumentException.class, () -> OrderCode.fromPrefixYearAndNumber(null, 2026, 1));
        assertThrows(IllegalArgumentException.class, () -> OrderCode.fromPrefixYearAndNumber("PO", 2026, -1));
        assertThrows(IllegalArgumentException.class, () -> OrderCode.fromPrefixYearAndNumber("PO", 2026, 10000));
    }

    @Test
    @DisplayName("generation: incrementing keeps the year for year-based codes and works for legacy codes")
    void increment_keepsYear() {
        assertEquals("PO-2026-0043", OrderCode.of("PO-2026-0042").increment().getValue());
        assertEquals("PO-" + Year.now().getValue() + "-0008", OrderCode.of("PO-0007").increment().getValue());
    }

    @Test
    @DisplayName("equality: codes are equal by value")
    void equality_isByValue() {
        OrderCode a = OrderCode.of("PO-2026-0001");

        assertEquals(a, OrderCode.of("PO-2026-0001"));
        assertEquals(a.hashCode(), OrderCode.of("PO-2026-0001").hashCode());
        assertNotEquals(a, OrderCode.of("PO-2026-0002"));
        assertNotEquals(a, "PO-2026-0001");
        assertEquals(a, a);
    }
}
