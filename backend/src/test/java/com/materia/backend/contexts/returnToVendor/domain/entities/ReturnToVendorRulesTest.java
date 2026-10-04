package com.materia.backend.contexts.returnToVendor.domain.entities;

import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidQuantityException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidStatusTransitionException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorLineRequiredException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorNotModifiableException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The return lifecycle: draft, shipped (pending), resolved by replacement or credit note, or cancelled. */
class ReturnToVendorRulesTest {

    private static ReturnToVendorLine.Builder aLine() {
        return ReturnToVendorLine.builder()
                .materialCode("MAT-2026-0001").materialName("Bolts")
                .rejectedQuantity(4).quantityToReturn(3)
                .unitPrice(new BigDecimal("5.00")).rejectionReason("Scratched");
    }

    private static ReturnToVendor aReturn() {
        return ReturnToVendor.builder()
                .goodsReceiptId("gr-1").supplierId("sup-1").supplierName("Acme")
                .returnReason("Damaged in transit")
                .addLine(aLine().build())
                .addLine(aLine().materialCode("MAT-2026-0002").unitPrice(new BigDecimal("2.50")).quantityToReturn(2).build())
                .build();
    }

    @Test
    @DisplayName("lifecycle: a draft is submitted, then resolved by a replacement that marks every line replaced")
    void draft_submit_resolveByReplacement() {
        ReturnToVendor rtv = aReturn();
        assertEquals(ReturnStatus.DRAFT, rtv.getStatus());
        assertTrue(rtv.isModifiable());

        rtv.submit("u1");
        assertEquals(ReturnStatus.PENDING, rtv.getStatus());
        assertTrue(rtv.getLines().stream().allMatch(ReturnToVendorLine::isFullyReturned));

        rtv.resolve("u1", ResolutionType.REPLACEMENT, " RMA-9 ", " New lot ");
        assertEquals(ReturnStatus.RESOLVED, rtv.getStatus());
        assertTrue(rtv.isReplacement());
        assertEquals("RMA-9", rtv.getReplacementPurchaseOrderReference());
        assertEquals("New lot", rtv.getSupplierResponse());
        assertNotNull(rtv.getResolutionDate());
        assertTrue(rtv.getLines().stream().allMatch(ReturnToVendorLine::isReplaced));
        assertTrue(rtv.isClosed());
    }

    @Test
    @DisplayName("credit note: the amount is quantity times order price over every line")
    void creditNote_amountIsTheValueOfTheGoods() {
        ReturnToVendor rtv = aReturn();
        rtv.submit("u1");

        rtv.resolve("u1", ResolutionType.CREDIT_NOTE, "AV-1", null);

        assertTrue(rtv.isCreditNote());
        assertEquals("AV-1", rtv.getCreditNoteReference());
        assertEquals(0, new BigDecimal("20.00").compareTo(new BigDecimal(rtv.getCreditNoteAmount())));
        assertEquals(5, rtv.totalQuantity());
        assertTrue(rtv.getLines().stream().allMatch(ReturnToVendorLine::isCreditNote));
    }

    @Test
    @DisplayName("transitions: only a draft is submitted and only a pending return is resolved")
    void transitions_areGuarded() {
        ReturnToVendor rtv = aReturn();
        assertThrows(ReturnToVendorInvalidStatusTransitionException.class,
                () -> rtv.resolve("u1", ResolutionType.CREDIT_NOTE, "AV-1", null));
        rtv.submit("u1");
        assertThrows(ReturnToVendorInvalidStatusTransitionException.class, () -> rtv.submit("u1"));
    }

    @Test
    @DisplayName("resolution: a type and a reference are required, and the supplier response is bounded")
    void resolution_requiresTypeAndReference() {
        ReturnToVendor rtv = aReturn();
        rtv.submit("u1");

        assertThrows(ReturnToVendorValidationException.class, () -> rtv.resolve("u1", null, "AV-1", null));
        ReturnToVendorValidationException noNumber = assertThrows(ReturnToVendorValidationException.class,
                () -> rtv.resolve("u1", ResolutionType.CREDIT_NOTE, " ", null));
        assertTrue(noNumber.getMessage().contains("credit note"));
        ReturnToVendorValidationException noReference = assertThrows(ReturnToVendorValidationException.class,
                () -> rtv.resolve("u1", ResolutionType.REPLACEMENT, null, null));
        assertTrue(noReference.getMessage().contains("replacement"));
        assertThrows(ReturnToVendorValidationException.class,
                () -> rtv.resolve("u1", ResolutionType.REPLACEMENT, "RMA-1", "x".repeat(501)));
        assertEquals(ReturnStatus.PENDING, rtv.getStatus());
    }

    @Test
    @DisplayName("cancel: needs a reason, keeps earlier notes, releases quantities; resolved or cancelled returns cannot be cancelled")
    void cancel_rules() {
        ReturnToVendor rtv = aReturn();
        rtv.setNotes("Call before pick-up");
        assertThrows(ReturnToVendorValidationException.class, () -> rtv.cancel("u1", " "));

        rtv.cancel("u1", "Duplicate");
        assertEquals(ReturnStatus.CANCELLED, rtv.getStatus());
        assertEquals("Call before pick-up\nCancelled: Duplicate", rtv.getNotes());
        assertFalse(rtv.holdsQuantities());
        assertThrows(ReturnToVendorInvalidStatusTransitionException.class, () -> rtv.cancel("u1", "Again"));

        ReturnToVendor resolved = aReturn();
        resolved.submit("u1");
        resolved.resolve("u1", ResolutionType.CREDIT_NOTE, "AV-1", null);
        assertThrows(ReturnToVendorNotModifiableException.class, () -> resolved.cancel("u1", "Too late"));
        assertTrue(resolved.holdsQuantities());
    }

    @Test
    @DisplayName("building: a return needs its receipt, supplier, reason and at least one line")
    void building_requiredFields() {
        assertThrows(ReturnToVendorValidationException.class, () -> ReturnToVendor.builder()
                .supplierId("s").supplierName("Acme").returnReason("r").addLine(aLine().build()).build());
        assertThrows(ReturnToVendorValidationException.class, () -> ReturnToVendor.builder()
                .goodsReceiptId("g").supplierName("Acme").returnReason("r").addLine(aLine().build()).build());
        assertThrows(ReturnToVendorValidationException.class, () -> ReturnToVendor.builder()
                .goodsReceiptId("g").supplierId("s").returnReason("r").addLine(aLine().build()).build());
        assertThrows(ReturnToVendorValidationException.class, () -> ReturnToVendor.builder()
                .goodsReceiptId("g").supplierId("s").supplierName("Acme").addLine(aLine().build()).build());
        assertThrows(ReturnToVendorValidationException.class, () -> ReturnToVendor.builder()
                .goodsReceiptId("g").supplierId("s").supplierName("Acme").returnReason("x".repeat(501))
                .addLine(aLine().build()).build());
        assertThrows(ReturnToVendorLineRequiredException.class, () -> ReturnToVendor.builder()
                .goodsReceiptId("g").supplierId("s").supplierName("Acme").returnReason("r").lines(List.of()).build());
        assertThrows(ReturnToVendorLineRequiredException.class, () -> ReturnToVendor.builder().lines(null));
    }

    @Test
    @DisplayName("lines: the quantity is positive and within the rejection, and a line needs its material and reason")
    void lines_validation() {
        assertThrows(ReturnToVendorInvalidQuantityException.class, () -> aLine().quantityToReturn(5).build());
        assertThrows(ReturnToVendorInvalidQuantityException.class, () -> aLine().quantityToReturn(0).build());
        assertThrows(ReturnToVendorInvalidQuantityException.class, () -> aLine().quantityAlreadyReturned(4).build());
        assertThrows(ReturnToVendorValidationException.class, () -> aLine().materialCode(" ").build());
        assertThrows(ReturnToVendorValidationException.class, () -> aLine().materialName(null).build());
        assertThrows(ReturnToVendorValidationException.class, () -> aLine().rejectionReason("").build());

        ReturnToVendorLine line = aLine().build();
        assertEquals(3, line.getRemainingQuantity());
        assertEquals(0, new BigDecimal("15.00").compareTo(line.lineValue()));
        assertEquals(BigDecimal.ZERO, aLine().unitPrice(null).build().lineValue());
    }
}
