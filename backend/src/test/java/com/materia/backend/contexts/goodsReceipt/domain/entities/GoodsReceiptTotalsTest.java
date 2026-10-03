package com.materia.backend.contexts.goodsReceipt.domain.entities;

import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptQualityInspectionRequiredException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;

/** [T034] Receipt totals, discrepancy detection and validation preconditions (US3, data-model.md). */
class GoodsReceiptTotalsTest {

    private PurchaseOrder order() {
        return anOrder().withLine(10, "5.00").withLine(4, "2.00").inStatus(OrderStatus.READY_FOR_RECEIPT).build();
    }

    @Test
    @DisplayName("totals: ordered, received, rejected and accepted are summed across lines; accepted = received − rejected")
    void totals_areSummed() {
        GoodsReceipt receipt = aReceipt(order()).receiving(0, 8, 2, "Damaged").receiving(1, 4, 0, null).build();

        assertEquals(14, receipt.getTotalQuantityOrdered());
        assertEquals(12, receipt.getTotalQuantityReceived());
        assertEquals(2, receipt.getTotalQuantityRejected());
        assertEquals(10, receipt.getTotalQuantityAccepted());
    }

    @Test
    @DisplayName("discrepancy: none when every line is received in full with no rejection")
    void noDiscrepancy_whenFullAndClean() {
        assertFalse(aReceipt(order()).build().isHasDiscrepancy());
    }

    @Test
    @DisplayName("discrepancy: a short line or any rejection is a discrepancy")
    void discrepancy_whenShortOrRejected() {
        assertTrue(aReceipt(order()).receiving(0, 9, 0, null).build().isHasDiscrepancy());
        assertTrue(aReceipt(order()).receiving(0, 10, 1, "Scratched").build().isHasDiscrepancy());
    }

    @Test
    @DisplayName("validate: a clean, complete receipt validates as completed and records the receipt date")
    void complete_clean_isCompleted() {
        GoodsReceipt receipt = aReceipt(order()).build();

        receipt.complete("receiver-1");

        assertEquals(ReceiptStatus.COMPLETED, receipt.getStatus());
        assertNotNull(receipt.getReceiptDate());
    }

    @Test
    @DisplayName("validate: a receipt with a discrepancy validates as partial")
    void complete_withDiscrepancy_isPartial() {
        GoodsReceipt receipt = aReceipt(order()).receiving(0, 6, 0, null).build();

        receipt.complete("receiver-1");

        assertEquals(ReceiptStatus.PARTIAL, receipt.getStatus());
    }

    @Test
    @DisplayName("validate: a line still awaiting quality inspection blocks validation")
    void complete_pendingQuality_isRefused() {
        GoodsReceipt receipt = aReceipt(order()).build();
        receipt.getLines().get(0).setQualityStatus(QualityStatus.UNDER_REVIEW);

        assertThrows(GoodsReceiptQualityInspectionRequiredException.class, () -> receipt.complete("receiver-1"));
        assertEquals(ReceiptStatus.DRAFT, receipt.getStatus());
    }

    @Test
    @DisplayName("validate: a line with rejected quantity but no reason blocks validation (US3-4)")
    void complete_rejectionWithoutReason_isRefused() {
        GoodsReceipt receipt = aReceipt(order()).receiving(0, 10, 3, null).build();

        assertThrows(GoodsReceiptValidationException.class, () -> receipt.complete("receiver-1"));
        assertEquals(ReceiptStatus.DRAFT, receipt.getStatus());
    }

    @Test
    @DisplayName("F-005: a receipt that receives exactly what remains validates as completed")
    void complete_remainderOnly_isCompleted() {
        PurchaseOrder po = anOrder().withLine(10, "5.00").inStatus(OrderStatus.PARTIALLY_RECEIVED).build();
        // 6 of 10 were received earlier; this receipt brings in the remaining 4.
        GoodsReceipt remainder = aReceipt(po).alreadyReceived(0, 6).receiving(0, 4, 0, null).build();

        remainder.complete("receiver-1");

        assertEquals(ReceiptStatus.COMPLETED, remainder.getStatus());
    }
}
