package com.materia.backend.contexts.goodsReceipt.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/** [T049] Receipt and quality status codes and predicates. */
class GoodsReceiptEnumsTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(ReceiptStatus.class)
    @DisplayName("receipt status: every constant round-trips through its code and carries a label")
    void receiptStatus_roundTrips(ReceiptStatus status) {
        assertEquals(status, ReceiptStatus.fromCode(status.getCode()));
        assertTrue(ReceiptStatus.isValidCode(status.getCode()));
        assertFalse(status.getLabel().isBlank());
        assertFalse(status.getDescription().isBlank());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"draft", "UNKNOWN"})
    @DisplayName("receipt status: blank and unknown codes are refused")
    void receiptStatus_unknown_isRefused(String code) {
        assertThrows(IllegalArgumentException.class, () -> ReceiptStatus.fromCode(code));
        assertFalse(ReceiptStatus.isValidCode(code));
    }

    @Test
    @DisplayName("receipt status: the code list names every constant; completed covers completed and partial")
    void receiptStatus_codesAndCompletion() {
        assertEquals(ReceiptStatus.values().length, ReceiptStatus.getCodes().size());
        assertTrue(ReceiptStatus.COMPLETED.isCompleted());
        assertTrue(ReceiptStatus.PARTIAL.isCompleted());
        assertFalse(ReceiptStatus.DRAFT.isCompleted());
        assertFalse(ReceiptStatus.CANCELLED.isCompleted());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(QualityStatus.class)
    @DisplayName("quality status: every constant round-trips and exactly one predicate family applies")
    void qualityStatus_roundTrips(QualityStatus status) {
        assertEquals(status, QualityStatus.fromCode(status.getCode()));
        assertFalse(status.getLabel().isBlank());
        assertFalse(status.getDescription().isBlank());
        assertEquals(status == QualityStatus.ACCEPTED, status.isAccepted());
        assertEquals(status == QualityStatus.REJECTED, status.isRejected());
        assertEquals(status == QualityStatus.UNDER_REVIEW, status.isPending());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"OK"})
    @DisplayName("quality status: blank and unknown codes are refused")
    void qualityStatus_unknown_isRefused(String code) {
        assertThrows(IllegalArgumentException.class, () -> QualityStatus.fromCode(code));
    }
}
