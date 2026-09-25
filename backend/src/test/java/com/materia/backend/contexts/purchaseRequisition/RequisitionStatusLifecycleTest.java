package com.materia.backend.contexts.purchaseRequisition;

import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionInvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying the exact Requisition Status Lifecycle matrix specified:
 *
 * Statut    Modifier  Supprimer  Soumettre  Approuver  Rejeter  Convertir  Annuler
 * DRAFT     ✅ OUI    ✅ OUI     ✅ OUI     ❌ NON     ❌ NON   ❌ NON     ✅ OUI
 * SUBMITTED ✅ OUI*   ❌ NON     ❌ NON     ✅ OUI     ✅ OUI   ❌ NON     ✅ OUI
 * APPROVED  ❌ NON    ❌ NON     ❌ NON     ❌ NON     ❌ NON   ✅ OUI     ✅ OUI
 * REJECTED  ❌ NON    ✅ OUI     ❌ NON     ❌ NON     ❌ NON   ❌ NON     ❌ NON
 * CONVERTED ❌ NON    ❌ NON     ❌ NON     ❌ NON     ❌ NON   ❌ NON     ❌ NON
 * CANCELLED ❌ NON    ✅ OUI     ❌ NON     ❌ NON     ❌ NON   ❌ NON     ❌ NON
 */
class RequisitionStatusLifecycleTest {

    @Test
    @DisplayName("Matrix Rule: Modifier (Edit) permitted for DRAFT and SUBMITTED only")
    void testModifierRule() {
        assertTrue(RequisitionStatus.DRAFT.isModifiable(), "DRAFT must be modifiable");
        assertTrue(RequisitionStatus.SUBMITTED.isModifiable(), "SUBMITTED must be modifiable (OUI*)");
        assertFalse(RequisitionStatus.APPROVED.isModifiable(), "APPROVED must not be modifiable");
        assertFalse(RequisitionStatus.REJECTED.isModifiable(), "REJECTED must not be modifiable");
        assertFalse(RequisitionStatus.CONVERTED.isModifiable(), "CONVERTED must not be modifiable");
        assertFalse(RequisitionStatus.CANCELLED.isModifiable(), "CANCELLED must not be modifiable");
    }

    @Test
    @DisplayName("Matrix Rule: Supprimer (Delete) permitted for DRAFT and REJECTED (and CANCELLED)")
    void testSupprimerRule() {
        assertTrue(RequisitionStatus.DRAFT.isDeletable(), "DRAFT must be deletable");
        assertFalse(RequisitionStatus.SUBMITTED.isDeletable(), "SUBMITTED must not be deletable");
        assertFalse(RequisitionStatus.APPROVED.isDeletable(), "APPROVED must not be deletable");
        assertTrue(RequisitionStatus.REJECTED.isDeletable(), "REJECTED must be deletable");
        assertFalse(RequisitionStatus.CONVERTED.isDeletable(), "CONVERTED must not be deletable");
        assertTrue(RequisitionStatus.CANCELLED.isDeletable(), "CANCELLED is deletable");
    }

    @Test
    @DisplayName("Matrix Rule: Soumettre (Submit) permitted for DRAFT only")
    void testSoumettreRule() {
        assertTrue(RequisitionStatus.DRAFT.isSubmittable(), "DRAFT must be submittable");
        assertFalse(RequisitionStatus.SUBMITTED.isSubmittable(), "SUBMITTED must not be submittable");
        assertFalse(RequisitionStatus.APPROVED.isSubmittable(), "APPROVED must not be submittable");
        assertFalse(RequisitionStatus.REJECTED.isSubmittable(), "REJECTED must not be submittable");
        assertFalse(RequisitionStatus.CONVERTED.isSubmittable(), "CONVERTED must not be submittable");
        assertFalse(RequisitionStatus.CANCELLED.isSubmittable(), "CANCELLED must not be submittable");
    }

    @Test
    @DisplayName("Matrix Rule: Approuver / Rejeter permitted for SUBMITTED only")
    void testApprouverAndRejeterRule() {
        assertFalse(RequisitionStatus.DRAFT.isApprovable());
        assertFalse(RequisitionStatus.DRAFT.isRejectable());

        assertTrue(RequisitionStatus.SUBMITTED.isApprovable());
        assertTrue(RequisitionStatus.SUBMITTED.isRejectable());

        assertFalse(RequisitionStatus.APPROVED.isApprovable());
        assertFalse(RequisitionStatus.APPROVED.isRejectable());

        assertFalse(RequisitionStatus.REJECTED.isApprovable());
        assertFalse(RequisitionStatus.REJECTED.isRejectable());

        assertFalse(RequisitionStatus.CONVERTED.isApprovable());
        assertFalse(RequisitionStatus.CONVERTED.isRejectable());

        assertFalse(RequisitionStatus.CANCELLED.isApprovable());
        assertFalse(RequisitionStatus.CANCELLED.isRejectable());
    }

    @Test
    @DisplayName("Matrix Rule: Convertir (Convert) permitted for APPROVED only")
    void testConvertirRule() {
        assertFalse(RequisitionStatus.DRAFT.isConvertible());
        assertFalse(RequisitionStatus.SUBMITTED.isConvertible());
        assertTrue(RequisitionStatus.APPROVED.isConvertible());
        assertFalse(RequisitionStatus.REJECTED.isConvertible());
        assertFalse(RequisitionStatus.CONVERTED.isConvertible());
        assertFalse(RequisitionStatus.CANCELLED.isConvertible());
    }

    @Test
    @DisplayName("Matrix Rule: Annuler (Cancel) permitted for SUBMITTED and APPROVED only (DRAFT is deleted directly)")
    void testAnnulerRule() {
        assertFalse(RequisitionStatus.DRAFT.isCancellable(), "DRAFT must not be cancellable (deleted directly instead)");
        assertTrue(RequisitionStatus.SUBMITTED.isCancellable(), "SUBMITTED can be cancelled");
        assertTrue(RequisitionStatus.APPROVED.isCancellable(), "APPROVED can be cancelled");
        assertFalse(RequisitionStatus.REJECTED.isCancellable(), "REJECTED cannot be cancelled");
        assertFalse(RequisitionStatus.CONVERTED.isCancellable(), "CONVERTED cannot be cancelled");
        assertFalse(RequisitionStatus.CANCELLED.isCancellable(), "CANCELLED cannot be cancelled again");
    }

    @Test
    @DisplayName("Domain Behavior: cancel() sets status CANCELLED, records date and reason")
    void testRequisitionCancelBehavior() {
        Requisition req = new Requisition();
        req.setStatus(RequisitionStatus.SUBMITTED);

        assertTrue(req.isCancellable());
        req.cancel("user-123", "No longer required by department");

        assertEquals(RequisitionStatus.CANCELLED, req.getStatus());
        assertEquals("No longer required by department", req.getCancellationReason());
        assertNotNull(req.getCancelledDate());
        assertEquals("user-123", req.getUpdatedBy());

        // Attempting to cancel already cancelled requisition should fail
        assertThrows(RequisitionInvalidStatusTransitionException.class, () ->
            req.cancel("user-123", "Repeat cancel")
        );
    }
}
