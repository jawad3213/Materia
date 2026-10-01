package com.materia.backend.contexts.purchaseRequisition.domain.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/** Requisition code format and sequence (T055). */
class RequisitionCodeTest {

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"REQ-2026-0001", "PR-2026-0042", "REQ-0001", "req-2026-0001"})
    @DisplayName("format: year-based and legacy codes are accepted, in any letter case")
    void validCodes_areAccepted(String code) {
        assertTrue(RequisitionCode.isValid(code));
        assertDoesNotThrow(() -> RequisitionCode.of(code));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"REQ-2026-001", "XYZ-2026-0001", "REQ2026-0001", "REQ-2026-00001", ""})
    @DisplayName("format: malformed codes are refused")
    void invalidCodes_areRefused(String code) {
        assertFalse(RequisitionCode.isValid(code));
        assertThrows(IllegalArgumentException.class, () -> RequisitionCode.of(code));
    }

    @Test
    @DisplayName("sequence: the next code increments the number and keeps the prefix and year")
    void next_incrementsNumber() {
        assertEquals("REQ-2026-0043", RequisitionCode.generateNext("REQ-2026-0042").getValue());
    }

    @Test
    @DisplayName("sequence: numbering is zero-padded to four digits")
    void next_isZeroPadded() {
        assertEquals("REQ-2026-0010", RequisitionCode.generateNext("REQ-2026-0009").getValue());
    }

    @Test
    @DisplayName("sequence: the year comes from the current code, so rolling over to a new year is the caller's job")
    void next_keepsYear() {
        assertEquals("REQ-2025-0006", RequisitionCode.generateNext("REQ-2025-0005").getValue());
    }

    @Test
    @DisplayName("capacity: after 9999 in a year no further code can be issued, a hard limit of 9999 per year")
    void next_after9999_isRefused() {
        // Documents a capacity ceiling rather than a defect: the 10,000th requisition in a
        // year cannot be numbered. See the findings register.
        assertThrows(IllegalArgumentException.class, () -> RequisitionCode.generateNext("REQ-2026-9999"));
    }
}
