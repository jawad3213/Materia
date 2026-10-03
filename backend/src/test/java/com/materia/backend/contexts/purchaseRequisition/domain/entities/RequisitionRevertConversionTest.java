package com.materia.backend.contexts.purchaseRequisition.domain.entities;

import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionInvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.materia.backend.support.TimeAssertions.assertWithin;
import static com.materia.backend.support.fixtures.RequisitionFixtures.aRequisition;
import static org.junit.jupiter.api.Assertions.*;

/** [T026] Releasing a requisition when its purchase order is withdrawn before receipt (US2-6, US2-7). */
class RequisitionRevertConversionTest {

    private final UUID orderId = UUID.randomUUID();

    @Test
    @DisplayName("release: a converted requisition linked to the order returns to approved with its order link cleared (US2-6)")
    void revert_linkedConversion_returnsToApproved() {
        Requisition requisition = aRequisition().convertedTo(orderId, "PO-2026-0001").build();
        LocalDateTime before = LocalDateTime.now();

        requisition.revertConversion(orderId.toString(), "buyer-2");

        assertEquals(RequisitionStatus.APPROVED, requisition.getStatus());
        assertNull(requisition.getPurchaseOrderId());
        assertNull(requisition.getPurchaseOrderCode());
        assertNull(requisition.getConvertedDate());
        assertEquals("buyer-2", requisition.getUpdatedBy());
        assertWithin(requisition.getUpdatedAt(), before, LocalDateTime.now());
    }

    @Test
    @DisplayName("release: a requisition can be converted again once released (US2-6)")
    void revert_thenConvertAgain() {
        Requisition requisition = aRequisition().convertedTo(orderId, "PO-2026-0001").build();
        requisition.revertConversion(orderId.toString(), "buyer-2");

        requisition.convert(UUID.randomUUID().toString(), "PO-2026-0002", "buyer-2");

        assertEquals(RequisitionStatus.CONVERTED, requisition.getStatus());
        assertEquals("PO-2026-0002", requisition.getPurchaseOrderCode());
    }

    @Test
    @DisplayName("release: only the order recorded on the requisition can release it (US2-7)")
    void revert_byAnotherOrder_isRefused() {
        Requisition requisition = aRequisition().convertedTo(orderId, "PO-2026-0001").build();

        assertThrows(RequisitionInvalidStatusTransitionException.class,
                () -> requisition.revertConversion(UUID.randomUUID().toString(), "buyer-2"));
        assertThrows(RequisitionInvalidStatusTransitionException.class,
                () -> requisition.revertConversion(null, "buyer-2"));
        assertEquals(RequisitionStatus.CONVERTED, requisition.getStatus());
        assertEquals(orderId.toString(), requisition.getPurchaseOrderId());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = RequisitionStatus.class, names = "CONVERTED", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("release: a requisition that is not converted cannot be released")
    void revert_fromNonConverted_isRefused(RequisitionStatus status) {
        Requisition requisition = aRequisition().inStatus(status).build();

        assertThrows(RequisitionInvalidStatusTransitionException.class,
                () -> requisition.revertConversion(orderId.toString(), "buyer-2"));
        assertEquals(status, requisition.getStatus());
    }
}
