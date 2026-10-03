package com.materia.backend.contexts.purchaseRequisition.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;

import static com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus.*;
import static org.junit.jupiter.api.Assertions.*;

/** [T080] Every requisition status predicate matches its documented group (feature 001 coverage). */
class RequisitionStatusCoverageTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(RequisitionStatus.class)
    @DisplayName("requisition status: each predicate matches its group, and display metadata is present")
    void predicates(RequisitionStatus s) {
        assertEquals(EnumSet.of(DRAFT, SUBMITTED).contains(s), s.isModifiable());
        assertEquals(s == APPROVED, s.isConvertible());
        assertEquals(s == SUBMITTED, s.isValidatable());
        assertEquals(EnumSet.of(DRAFT, REJECTED, CANCELLED).contains(s), s.isDeletable());
        assertEquals(EnumSet.of(SUBMITTED, APPROVED).contains(s), s.isCancellable());
        assertEquals(s == DRAFT, s.isSubmittable());
        assertEquals(s == SUBMITTED, s.isApprovable());
        assertEquals(s == SUBMITTED, s.isRejectable());
        boolean terminal = EnumSet.of(CONVERTED, REJECTED, CANCELLED).contains(s);
        assertEquals(terminal, s.isTerminal());
        assertEquals(terminal, s.isFinal());
        assertEquals(EnumSet.of(DRAFT, SUBMITTED).contains(s), s.isActive());
        assertFalse(s.getLabel().isBlank());
        assertFalse(s.getDescription().isBlank());
        assertFalse(s.getColor().isBlank());
    }
}
