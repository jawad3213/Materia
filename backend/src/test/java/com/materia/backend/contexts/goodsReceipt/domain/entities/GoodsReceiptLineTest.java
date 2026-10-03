package com.materia.backend.contexts.goodsReceipt.domain.entities;

import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptInvalidLineException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptInvalidQuantityException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptLineRequiredException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;

/** [T048] Receipt line arithmetic and validation (US6, data-model.md). */
class GoodsReceiptLineTest {

    private static GoodsReceiptLine.Builder line(int ordered, int received, int rejected) {
        return GoodsReceiptLine.builder().materialCode("MAT-1")
                .quantityOrdered(ordered).quantityReceived(received).quantityRejected(rejected);
    }

    @Test
    @DisplayName("line: accepted = received − rejected and pending = ordered − received")
    void acceptedAndPending_areDerived() {
        GoodsReceiptLine l = line(10, 7, 2).build();

        assertEquals(5, l.getQuantityAccepted());
        assertEquals(3, l.getQuantityPending());
        assertTrue(l.hasQuantityDiscrepancy());
        assertTrue(l.hasRejection());
    }

    @Test
    @DisplayName("line: a full, clean line has no discrepancy and no rejection")
    void fullCleanLine_hasNoDiscrepancy() {
        GoodsReceiptLine l = line(10, 10, 0).build();

        assertFalse(l.hasQuantityDiscrepancy());
        assertFalse(l.hasRejection());
        assertEquals(0, l.getQuantityPending());
    }

    @Test
    @DisplayName("line: a blank material, negative quantities, rejected > received or received > ordered are refused")
    void invalidLines_areRefused() {
        assertThrows(GoodsReceiptInvalidLineException.class, () -> line(1, 1, 0).materialCode(" ").build());
        assertThrows(GoodsReceiptInvalidQuantityException.class, () -> line(5, -1, 0).build());
        assertThrows(GoodsReceiptInvalidQuantityException.class, () -> line(5, 1, -1).build());
        assertThrows(GoodsReceiptInvalidQuantityException.class, () -> line(5, 2, 3).build());
        assertThrows(GoodsReceiptInvalidQuantityException.class, () -> line(5, 6, 0).build());
        assertThrows(GoodsReceiptInvalidQuantityException.class, () -> GoodsReceiptLine.builder().materialCode("M").build());
    }

    @Test
    @DisplayName("line: the quality predicates follow the quality status")
    void qualityPredicates() {
        assertTrue(line(1, 1, 0).qualityStatus(QualityStatus.ACCEPTED).build().isAccepted());
        assertTrue(line(1, 1, 1).qualityStatus(QualityStatus.REJECTED).build().isRejected());
        assertTrue(line(1, 1, 0).qualityStatus(QualityStatus.UNDER_REVIEW).build().isPending());
        assertTrue(line(2, 2, 1).qualityStatus(QualityStatus.PARTIAL).build().isPartial());
        assertFalse(line(1, 1, 0).qualityStatus(QualityStatus.ACCEPTED).build().isPending());
    }

    @Test
    @DisplayName("receipt: removing a line at an index that does not exist is refused")
    void removeLine_badIndex_isRefused() {
        GoodsReceipt receipt = aReceipt(anOrder().inStatus(OrderStatus.READY_FOR_RECEIPT).build()).build();

        assertThrows(GoodsReceiptInvalidLineException.class, () -> receipt.removeLine(3));
        assertThrows(GoodsReceiptInvalidLineException.class, () -> receipt.removeLine(-1));
        assertThrows(GoodsReceiptInvalidLineException.class, () -> receipt.addLine(null));
    }

    @Test
    @DisplayName("receipt: a receipt without an order, a receiver, a receiver name or any line cannot be built")
    void receipt_requiredFields() {
        GoodsReceiptLine ok = line(1, 1, 0).qualityStatus(QualityStatus.ACCEPTED).build();

        assertThrows(GoodsReceiptValidationException.class, () -> GoodsReceipt.builder()
                .receivedBy("r").receivedByName("R").lines(List.of(ok)).build());
        assertThrows(GoodsReceiptValidationException.class, () -> GoodsReceipt.builder()
                .purchaseOrderId("po").receivedByName("R").lines(List.of(ok)).build());
        assertThrows(GoodsReceiptValidationException.class, () -> GoodsReceipt.builder()
                .purchaseOrderId("po").receivedBy("r").lines(List.of(ok)).build());
        assertThrows(GoodsReceiptLineRequiredException.class, () -> GoodsReceipt.builder()
                .purchaseOrderId("po").receivedBy("r").receivedByName("R").lines(List.of()).build());
    }

    @Test
    @DisplayName("receipt: a new receipt starts as a draft with today's receipt date and a default code")
    void receipt_defaults() {
        GoodsReceipt receipt = GoodsReceipt.builder().purchaseOrderId("po").receivedBy("r").receivedByName("R")
                .lines(List.of(line(1, 1, 0).qualityStatus(QualityStatus.ACCEPTED).build())).build();

        assertEquals(ReceiptStatus.DRAFT, receipt.getStatus());
        assertNotNull(receipt.getReceiptDate());
        assertNotNull(receipt.getReceiptCode());
        assertNotNull(receipt.getId());
    }
}
