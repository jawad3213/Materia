package com.materia.backend.contexts.purchaseRequisition.domain.entities;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionCurrencyMismatchException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionInvalidStatusTransitionException;
import com.materia.backend.support.TimeAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus.*;
import static com.materia.backend.support.fixtures.RequisitionFixtures.aLine;
import static com.materia.backend.support.fixtures.RequisitionFixtures.aRequisition;
import static org.junit.jupiter.api.Assertions.*;

/** Side effects of each transition, and the totals and line rules (T050-T054). */
class RequisitionTest {

    // ---- Transition side effects (US2 scenarios 1-5) ----

    @Test
    @DisplayName("submit: records the submission date and who submitted it")
    void submit_recordsDateAndSubmitter() {
        Requisition r = aRequisition().inStatus(DRAFT).build();

        LocalDateTime before = LocalDateTime.now();
        r.submit("user-7");
        LocalDateTime after = LocalDateTime.now();

        assertEquals(SUBMITTED, r.getStatus());
        TimeAssertions.assertWithin(r.getSubmittedDate(), before, after);
        assertEquals("user-7", r.getUpdatedBy());
    }

    @Test
    @DisplayName("submit: a requisition with no lines cannot be submitted, since there is nothing to approve")
    void submit_withoutLines_isRefused() {
        Requisition r = aRequisition().inStatus(DRAFT).withoutLines().build();

        assertThrows(RequisitionInvalidStatusTransitionException.class, () -> r.submit("user-7"));
        assertEquals(DRAFT, r.getStatus());
    }

    @Test
    @DisplayName("approve: records the approver, their notes and the approval date")
    void approve_recordsApprover() {
        Requisition r = aRequisition().inStatus(SUBMITTED).build();

        LocalDateTime before = LocalDateTime.now();
        r.approve("mgr-1", "Morgan", "within budget");
        LocalDateTime after = LocalDateTime.now();

        assertEquals("mgr-1", r.getApproverId());
        assertEquals("Morgan", r.getApproverName());
        assertEquals("within budget", r.getApprovalNotes());
        TimeAssertions.assertWithin(r.getApprovedDate(), before, after);
        assertTrue(r.isConvertible(), "an approved requisition becomes eligible for conversion");
    }

    @Test
    @DisplayName("reject: records who rejected it and why")
    void reject_recordsReason() {
        Requisition r = aRequisition().inStatus(SUBMITTED).build();

        r.reject("mgr-1", "Morgan", "over budget");

        assertEquals(REJECTED, r.getStatus());
        assertEquals("over budget", r.getRejectionReason());
        assertEquals("mgr-1", r.getApproverId());
    }

    @Test
    @DisplayName("convert: records the resulting purchase order and the conversion date")
    void convert_recordsPurchaseOrder() {
        Requisition r = aRequisition().inStatus(APPROVED).build();

        LocalDateTime before = LocalDateTime.now();
        r.convert("po-9", "PO-2026-0009", "buyer-1");
        LocalDateTime after = LocalDateTime.now();

        assertEquals(CONVERTED, r.getStatus());
        assertEquals("po-9", r.getPurchaseOrderId());
        assertEquals("PO-2026-0009", r.getPurchaseOrderCode());
        TimeAssertions.assertWithin(r.getConvertedDate(), before, after);
    }

    @Test
    @DisplayName("cancel: records the reason and the cancellation date")
    void cancel_recordsReason() {
        Requisition r = aRequisition().inStatus(APPROVED).build();

        LocalDateTime before = LocalDateTime.now();
        r.cancel("user-7", "supplier withdrew");
        LocalDateTime after = LocalDateTime.now();

        assertEquals("supplier withdrew", r.getCancellationReason());
        TimeAssertions.assertWithin(r.getCancelledDate(), before, after);
    }

    // ---- Status-derived permissions (US2 scenarios 5-7) ----

    @Test
    @DisplayName("modifiable: only draft and submitted requisitions may be edited")
    void isModifiable_onlyDraftAndSubmitted() {
        assertTrue(aRequisition().inStatus(DRAFT).build().isModifiable());
        assertTrue(aRequisition().inStatus(SUBMITTED).build().isModifiable());
        for (var status : new com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus[]{
                APPROVED, REJECTED, CANCELLED, CONVERTED}) {
            assertFalse(aRequisition().inStatus(status).build().isModifiable(), status + " must not be editable");
        }
    }

    @Test
    @DisplayName("terminal: converted, rejected and cancelled are final")
    void terminalStates_areFinal() {
        assertTrue(CONVERTED.isTerminal());
        assertTrue(REJECTED.isTerminal());
        assertTrue(CANCELLED.isTerminal());
        assertFalse(DRAFT.isTerminal());
        assertFalse(SUBMITTED.isTerminal());
        assertFalse(APPROVED.isTerminal());
    }

    // ---- Totals (T054) ----

    @Test
    @DisplayName("total: equals the sum of every line's quantity times unit price")
    void total_isSumOfLines() {
        Requisition r = aRequisition().withLine(3, "10.00").withLine(2, "7.25").build();

        assertEquals(Money.of("44.50", CurrencyCode.MAD), r.getTotalAmount());
    }

    @Test
    @DisplayName("total: is recomputed when a line is added or removed")
    void total_recomputesOnLineChange() {
        Requisition r = aRequisition().withLine(1, "10.00").build();

        r.addLine(aLine(2, "5.00"));
        assertEquals(Money.of("20.00", CurrencyCode.MAD), r.getTotalAmount());

        r.removeLine(0);
        assertEquals(Money.of("10.00", CurrencyCode.MAD), r.getTotalAmount());
    }

    @Test
    @DisplayName("total: lines in different currencies cannot be combined into one requisition")
    void total_mixedCurrencies_isRefused() {
        Requisition r = aRequisition().withLine(1, "10.00").build();

        assertThrows(RequisitionCurrencyMismatchException.class, () -> r.addLine(aLine(1, "10.00", CurrencyCode.EUR)));
    }

    @Test
    @Disabled("FINDING-019: addLine appends the line before validating currency, so a refused line is left behind")
    @DisplayName("total: a refused mixed-currency line is not left behind in the requisition")
    void total_mixedCurrencies_leavesNoTrace() {
        Requisition r = aRequisition().withLine(1, "10.00").build();
        int linesBefore = r.getLines().size();
        Money totalBefore = r.getTotalAmount();

        assertThrows(RequisitionCurrencyMismatchException.class, () -> r.addLine(aLine(1, "10.00", CurrencyCode.EUR)));

        assertEquals(linesBefore, r.getLines().size(), "the refused line must not remain");
        assertEquals(totalBefore, r.getTotalAmount());
    }

    @Test
    @DisplayName("lines: numbers are sequential, and renumbered when a line is removed")
    void lines_areRenumbered() {
        Requisition r = aRequisition().withLine(1, "1.00").withLine(1, "2.00").withLine(1, "3.00").build();

        r.removeLine(0);

        assertEquals(1, r.getLines().get(0).getLineNumber());
        assertEquals(2, r.getLines().get(1).getLineNumber());
    }
}
