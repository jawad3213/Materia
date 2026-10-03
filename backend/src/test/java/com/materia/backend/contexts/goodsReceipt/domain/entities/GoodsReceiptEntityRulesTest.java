package com.materia.backend.contexts.goodsReceipt.domain.entities;

import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptInvalidLineException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptInvalidQuantityException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptLineRequiredException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptQualityInspectionRequiredException;
import com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;

/** Builder defaults, required fields, totals with missing values and identity of goods receipts and their lines (US6). */
class GoodsReceiptEntityRulesTest {

    private static GoodsReceiptLine accepted(int ordered, int received) {
        return GoodsReceiptLine.builder().materialCode("MAT-1").quantityOrdered(ordered)
                .quantityReceived(received).quantityRejected(0).qualityStatus(QualityStatus.ACCEPTED).build();
    }

    private static GoodsReceipt.Builder minimal() {
        return GoodsReceipt.builder().purchaseOrderId("po-1").receivedBy("r-1").receivedByName("Rita")
                .addLine(accepted(5, 5));
    }

    @Test
    @DisplayName("builder: explicit code, status, dates and author are kept")
    void builder_keepsExplicitValues() {
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        GoodsReceipt receipt = minimal().receiptCode("GR-2026-0042").status(ReceiptStatus.IN_PROGRESS)
                .createdAt(created).updatedAt(created).createdBy("r-1").build();

        assertEquals(ReceiptCode.of("GR-2026-0042"), receipt.getReceiptCode());
        assertEquals(ReceiptStatus.IN_PROGRESS, receipt.getStatus());
        assertEquals(created, receipt.getCreatedAt());
        assertEquals("r-1", receipt.getCreatedBy());
    }

    @Test
    @DisplayName("builder: a null line or line list is refused; a code given as a ReceiptCode is kept")
    void builder_lines() {
        assertThrows(GoodsReceiptInvalidLineException.class, () -> GoodsReceipt.builder().addLine(null));
        assertThrows(GoodsReceiptLineRequiredException.class, () -> GoodsReceipt.builder().lines(null));
        assertEquals("GR-2026-0007", minimal().receiptCode(ReceiptCode.of("GR-2026-0007")).build().getReceiptCode().getValue());
    }

    @Test
    @DisplayName("builder: lines with a blank material or negative quantities are refused")
    void builder_invalidLineData() {
        GoodsReceiptLine blank = accepted(1, 1);
        blank.setMaterialCode(" ");
        assertThrows(GoodsReceiptInvalidLineException.class, () -> GoodsReceipt.builder().purchaseOrderId("p")
                .receivedBy("r").receivedByName("R").lines(List.of(blank)).build());

        GoodsReceiptLine negativeReceived = accepted(1, 1);
        negativeReceived.setQuantityReceived(-1);
        assertThrows(GoodsReceiptInvalidQuantityException.class, () -> GoodsReceipt.builder().purchaseOrderId("p")
                .receivedBy("r").receivedByName("R").lines(List.of(negativeReceived)).build());

        GoodsReceiptLine negativeRejected = accepted(1, 1);
        negativeRejected.setQuantityRejected(-1);
        assertThrows(GoodsReceiptInvalidQuantityException.class, () -> GoodsReceipt.builder().purchaseOrderId("p")
                .receivedBy("r").receivedByName("R").lines(List.of(negativeRejected)).build());
    }

    @Test
    @DisplayName("totals: missing quantities count as zero; an empty receipt totals zero with no discrepancy")
    void totals_withMissingValues() {
        GoodsReceipt receipt = minimal().build();
        GoodsReceiptLine line = receipt.getLines().get(0);
        line.setQuantityOrdered(null);
        line.setQuantityRejected(null);

        receipt.recalculateTotals();
        assertEquals(0, receipt.getTotalQuantityOrdered());
        assertEquals(5, receipt.getTotalQuantityReceived());
        assertEquals(0, receipt.getTotalQuantityRejected());
        assertFalse(receipt.isHasDiscrepancy());

        receipt.setLines(new ArrayList<>());
        receipt.recalculateTotals();
        assertEquals(0, receipt.getTotalQuantityReceived());
        assertFalse(receipt.hasDiscrepancy());
    }

    @Test
    @DisplayName("validate: a line with no quality outcome at all blocks validation")
    void complete_withoutQualityStatus_isRefused() {
        GoodsReceipt receipt = minimal().build();
        receipt.getLines().get(0).setQualityStatus(null);

        assertThrows(GoodsReceiptQualityInspectionRequiredException.class, () -> receipt.complete("r-1"));
    }

    @Test
    @DisplayName("identity: receipts are equal by id or by code, never to another type or null")
    void identity() {
        var order = anOrder().inStatus(OrderStatus.READY_FOR_RECEIPT).build();
        GoodsReceipt a = aReceipt(order).build();
        GoodsReceipt sameId = aReceipt(order).build();
        sameId.setId(a.getId());
        GoodsReceipt sameCode = aReceipt(order).code(a.getReceiptCode().getValue()).build();
        GoodsReceipt other = aReceipt(order).build();

        assertEquals(a, a);
        assertEquals(a, sameId);
        assertEquals(a, sameCode);
        assertNotEquals(a, other);
        assertNotEquals(a, null);
        assertNotEquals(a, "receipt");
        assertEquals(a.hashCode(), a.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    @DisplayName("lines: lines are equal by id only; derived quantities stay unset until both inputs are known")
    void lineIdentityAndDerivation() {
        GoodsReceiptLine a = accepted(5, 5);
        GoodsReceiptLine sameId = accepted(3, 1);
        sameId.setId(a.getId());

        assertEquals(a, sameId);
        assertEquals(a.hashCode(), sameId.hashCode());
        assertNotEquals(a, accepted(5, 5));
        assertNotEquals(a, null);
        assertNotEquals(a, "line");
        assertNotNull(a.toString());

        GoodsReceiptLine blank = accepted(5, 5);
        blank.setQuantityReceived(null);
        blank.setQuantityAccepted(null);
        blank.setQuantityPending(null);
        blank.calculateAcceptedQuantity();
        blank.calculatePendingQuantity();
        assertNull(blank.getQuantityAccepted());
        assertNull(blank.getQuantityPending());
        assertFalse(blank.hasQuantityDiscrepancy());
        blank.setQuantityRejected(null);
        assertFalse(blank.hasRejection());
    }
}
