package com.materia.backend.contexts.purchaseRequisition.domain.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

/** [T081] Requisition code generation and parsing edge cases (feature 001 coverage). */
class RequisitionCodeCoverageTest {

    @Test
    @DisplayName("code: generation pads numbers and keeps the year; legacy codes roll into the current year")
    void generation() {
        int year = Year.now().getValue();
        assertEquals("REQ-" + year + "-0001", RequisitionCode.createDefault().getValue());
        assertEquals("PR-2026-0010", RequisitionCode.fromPrefixYearAndNumber("pr", 2026, 10).getValue());
        assertEquals("REQ-2026-0043", RequisitionCode.generateNext("REQ-2026-0042").getValue());
        assertEquals("REQ-" + year + "-0008", RequisitionCode.generateNext("REQ-0007").getValue());
        assertThrows(IllegalArgumentException.class, () -> RequisitionCode.fromPrefixYearAndNumber(" ", 2026, 1));
        assertThrows(IllegalArgumentException.class, () -> RequisitionCode.fromPrefixYearAndNumber(null, 2026, 1));
        assertThrows(IllegalArgumentException.class, () -> RequisitionCode.fromPrefixYearAndNumber("REQ", 2026, -1));
        assertThrows(IllegalArgumentException.class, () -> RequisitionCode.fromPrefixYearAndNumber("REQ", 2026, 10000));
    }

    @Test
    @DisplayName("code: parsing splits prefix, number and year; legacy codes have no year")
    void parsing() {
        RequisitionCode code = RequisitionCode.of(" REQ-2026-0042 ");
        assertEquals("REQ", code.getPrefix());
        assertEquals("0042", code.getNumber());
        assertEquals(42, code.getNumberAsInt());
        assertEquals(2026, code.getYear());
        assertEquals(-1, RequisitionCode.of("REQ-0042").getYear());
    }

    @Test
    @DisplayName("code: null, blank and malformed codes are refused; validity mirrors construction")
    void refusals() {
        assertThrows(IllegalArgumentException.class, () -> RequisitionCode.of(null));
        assertThrows(IllegalArgumentException.class, () -> RequisitionCode.of("  "));
        assertThrows(IllegalArgumentException.class, () -> RequisitionCode.of("PO-2026-0001"));
        assertFalse(RequisitionCode.isValid(null));
        assertFalse(RequisitionCode.isValid(" "));
        assertFalse(RequisitionCode.isValid("REQ-26-1"));
        assertTrue(RequisitionCode.isValid("PR-0001"));
    }
}
