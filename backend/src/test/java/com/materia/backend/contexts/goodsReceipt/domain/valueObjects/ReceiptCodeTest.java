package com.materia.backend.contexts.goodsReceipt.domain.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

/** [T050] Receipt code format, generation and equality. */
class ReceiptCodeTest {

    @Test
    @DisplayName("format: a year-based code GR-YYYY-NNNN is accepted and split into its parts")
    void yearBasedCode_isParsed() {
        ReceiptCode code = ReceiptCode.of("GR-2026-0012");

        assertEquals("GR", code.getPrefix());
        assertEquals("0012", code.getNumber());
        assertEquals(12, code.getNumberAsInt());
        assertEquals(2026, code.getYear());
        assertEquals("GR-2026-0012", code.getValue());
    }

    @Test
    @DisplayName("format: a legacy code GR-NNNN is accepted and has no year")
    void legacyCode_hasNoYear() {
        assertEquals(-1, ReceiptCode.of("GR-0003").getYear());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "PO-2026-0001", "GR-26-0001", "GR-2026-01"})
    @DisplayName("format: blank and malformed codes are refused")
    void malformed_isRefused(String value) {
        assertThrows(IllegalArgumentException.class, () -> ReceiptCode.of(value));
        assertFalse(ReceiptCode.isValid(value));
    }

    @Test
    @DisplayName("generation: codes are zero-padded; the next code keeps the year; bad input is refused")
    void generation() {
        assertEquals("GR-2026-0007", ReceiptCode.fromPrefixYearAndNumber("GR", 2026, 7).getValue());
        assertEquals("GR-" + Year.now().getValue() + "-0001", ReceiptCode.createDefault().getValue());
        assertEquals("GR-2026-0013", ReceiptCode.generateNext("GR-2026-0012").getValue());
        assertEquals("GR-2026-0013", ReceiptCode.of("GR-2026-0012").increment().getValue());
        assertEquals("GR-" + Year.now().getValue() + "-0004", ReceiptCode.generateNext("GR-0003").getValue());
        assertThrows(IllegalArgumentException.class, () -> ReceiptCode.fromPrefixYearAndNumber(" ", 2026, 1));
        assertThrows(IllegalArgumentException.class, () -> ReceiptCode.fromPrefixYearAndNumber("GR", 2026, 10000));
    }

    @Test
    @DisplayName("equality: codes are equal by value")
    void equality() {
        ReceiptCode a = ReceiptCode.of("GR-2026-0001");

        assertEquals(a, ReceiptCode.of("GR-2026-0001"));
        assertEquals(a.hashCode(), ReceiptCode.of("GR-2026-0001").hashCode());
        assertNotEquals(a, ReceiptCode.of("GR-2026-0002"));
        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotNull(a.toString());
    }
}
