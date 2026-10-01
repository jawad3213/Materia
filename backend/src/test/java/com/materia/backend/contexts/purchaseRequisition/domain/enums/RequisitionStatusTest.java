package com.materia.backend.contexts.purchaseRequisition.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/** Status code parsing (T049). The transition rules themselves are in RequisitionTransitionMatrixTest. */
class RequisitionStatusTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(RequisitionStatus.class)
    @DisplayName("fromCode: every status round-trips through its code")
    void fromCode_roundTrips(RequisitionStatus status) {
        assertEquals(status, RequisitionStatus.fromCode(status.getCode()));
        assertTrue(RequisitionStatus.isValidCode(status.getCode()));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"NOT_A_STATUS", "draft", " DRAFT"})
    @DisplayName("fromCode: an unknown code is refused, and isValidCode agrees")
    void fromCode_unknown_isRefused(String code) {
        assertThrows(IllegalArgumentException.class, () -> RequisitionStatus.fromCode(code));
        assertFalse(RequisitionStatus.isValidCode(code));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("fromCode: a missing code is refused")
    void fromCode_missing_isRefused(String code) {
        assertThrows(IllegalArgumentException.class, () -> RequisitionStatus.fromCode(code));
        assertFalse(RequisitionStatus.isValidCode(code));
    }

    @Test
    @DisplayName("getCodes: lists every status exactly once")
    void getCodes_coversEveryStatus() {
        assertEquals(RequisitionStatus.values().length, RequisitionStatus.getCodes().size());
        assertTrue(RequisitionStatus.getCodes().containsAll(
                Arrays.stream(RequisitionStatus.values()).map(RequisitionStatus::getCode).toList()));
    }
}
