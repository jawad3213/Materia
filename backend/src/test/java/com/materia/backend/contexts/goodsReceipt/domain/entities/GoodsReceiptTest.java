package com.materia.backend.contexts.goodsReceipt.domain.entities;

import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptInvalidQuantityException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptQualityInspectionRequiredException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoodsReceiptTest {

    @Test
    void rejectsARejectedQuantityGreaterThanTheReceivedQuantity() {
        assertThrows(GoodsReceiptInvalidQuantityException.class, () -> GoodsReceiptLine.builder()
                .materialCode("MAT-001")
                .quantityOrdered(10)
                .quantityReceived(4)
                .quantityRejected(5)
                .build());
    }

    @Test
    void completesAsPartialWhenAnyReceivedQuantityIsRejected() {
        GoodsReceiptLine line = GoodsReceiptLine.builder()
                .materialCode("MAT-001")
                .quantityOrdered(10)
                .quantityReceived(10)
                .quantityRejected(2)
                .qualityStatus(QualityStatus.PARTIAL)
                .rejectionReason("Damaged packaging")
                .build();
        GoodsReceipt receipt = GoodsReceipt.builder()
                .purchaseOrderId("00000000-0000-0000-0000-000000000001")
                .receivedBy("receiver-1")
                .receivedByName("Receiver")
                .addLine(line)
                .build();

        receipt.complete("receiver-1");

        assertEquals(ReceiptStatus.PARTIAL, receipt.getStatus());
    }

    @Test
    void cannotCompleteWhileQualityIsUnderReview() {
        GoodsReceiptLine line = GoodsReceiptLine.builder()
                .materialCode("MAT-001")
                .quantityOrdered(1)
                .quantityReceived(1)
                .quantityRejected(0)
                .qualityStatus(QualityStatus.UNDER_REVIEW)
                .build();
        GoodsReceipt receipt = GoodsReceipt.builder()
                .purchaseOrderId("00000000-0000-0000-0000-000000000001")
                .receivedBy("receiver-1")
                .receivedByName("Receiver")
                .addLine(line)
                .build();

        assertThrows(GoodsReceiptQualityInspectionRequiredException.class,
                () -> receipt.complete("receiver-1"));
    }
}
