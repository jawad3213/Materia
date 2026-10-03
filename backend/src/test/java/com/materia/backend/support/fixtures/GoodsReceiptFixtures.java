package com.materia.backend.support.fixtures;

import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceiptLine;
import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builds goods receipts against a given purchase order (FR-022).
 *
 * <pre>{@code
 * PurchaseOrder po = anOrder().withLine(10, "5.00").inStatus(READY_FOR_RECEIPT).build();
 * GoodsReceipt gr = aReceipt(po).receiving(0, 4, 1, "Damaged").build();
 * }</pre>
 *
 * By default every order line is received in full, accepted, by the order's assigned receiver.
 * Completed, partial and cancelled receipts are reached through the real {@code complete} and
 * {@code cancel} methods. {@code IN_PROGRESS} is set directly: no operation produces it (F-004).
 */
public final class GoodsReceiptFixtures {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(5000);

    private GoodsReceiptFixtures() {
    }

    public static Builder aReceipt(PurchaseOrder order) {
        return new Builder(order);
    }

    public static String uniqueCode() {
        return String.format("GR-%d-%04d", LocalDate.now().getYear(), 5000 + (SEQUENCE.getAndIncrement() - 5000) % 5000);
    }

    public static final class Builder {
        private final PurchaseOrder order;
        private final Map<Integer, int[]> quantities = new HashMap<>();
        private final Map<Integer, String> reasons = new HashMap<>();
        private final Map<Integer, Integer> earlier = new HashMap<>();
        private ReceiptStatus status = ReceiptStatus.DRAFT;
        private String code = uniqueCode();
        private String receivedBy;

        private Builder(PurchaseOrder order) {
            this.order = order;
            this.receivedBy = order.getAssignedTo() != null ? order.getAssignedTo() : PurchaseOrderFixtures.DEFAULT_RECEIVER_ID;
        }

        /** Overrides one line's quantities. A rejection needs a reason, as validation requires. */
        public Builder receiving(int lineIndex, int received, int rejected, String reason) {
            quantities.put(lineIndex, new int[]{received, rejected});
            if (reason != null) {
                reasons.put(lineIndex, reason);
            }
            return this;
        }

        /** Receives nothing on the line; it is still included so the receipt references every order line. */
        public Builder skipping(int lineIndex) {
            return receiving(lineIndex, 0, 0, null);
        }

        /**
         * Records that {@code quantity} of the line arrived on earlier receipts. The line then expects only the
         * outstanding remainder, as the service sets it, and by default receives exactly that remainder.
         */
        public Builder alreadyReceived(int lineIndex, int quantity) {
            earlier.put(lineIndex, quantity);
            return this;
        }

        public Builder receivedBy(String userId) {
            this.receivedBy = userId;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder inStatus(ReceiptStatus status) {
            this.status = status;
            return this;
        }

        public GoodsReceipt build() {
            if (status == ReceiptStatus.PARTIAL && quantities.isEmpty()) {
                // A partial receipt needs a discrepancy: receive one unit short on the first line.
                int ordered = order.getLines().get(0).getQuantity();
                receiving(0, Math.max(0, ordered - 1), 0, null);
            }

            List<GoodsReceiptLine> lines = new ArrayList<>();
            List<PurchaseOrderLine> orderLines = order.getLines();
            for (int i = 0; i < orderLines.size(); i++) {
                PurchaseOrderLine ol = orderLines.get(i);
                int expected = ol.getQuantity() - earlier.getOrDefault(i, 0);
                int[] q = quantities.getOrDefault(i, new int[]{expected, 0});
                lines.add(GoodsReceiptLine.builder()
                        .id(UUID.randomUUID())
                        .lineNumber(i + 1)
                        .purchaseOrderLineId(ol.getId().toString())
                        .materialCode(ol.getMaterialCode())
                        .materialId(ol.getMaterialId())
                        .materialName(ol.getMaterialName())
                        .unitOfMeasure(ol.getUnitOfMeasure())
                        .quantityOrdered(expected)
                        .quantityReceived(q[0])
                        .quantityRejected(q[1])
                        .qualityStatus(quality(q[0], q[1]))
                        .rejectionReason(reasons.get(i))
                        .unitPrice(ol.getUnitPrice())
                        .build());
            }

            GoodsReceipt receipt = GoodsReceipt.builder()
                    .id(UUID.randomUUID())
                    .receiptCode(ReceiptCode.of(code))
                    .purchaseOrderId(order.getId().toString())
                    .purchaseOrderCode(order.getOrderCode().getValue())
                    .supplierId(order.getSupplierId().toString())
                    .supplierName(order.getSupplierName())
                    .receivedBy(receivedBy)
                    .receivedByName(order.getAssignedToName() != null ? order.getAssignedToName() : "Rita Receiver")
                    .status(status == ReceiptStatus.IN_PROGRESS ? ReceiptStatus.IN_PROGRESS : ReceiptStatus.DRAFT)
                    .createdBy(receivedBy)
                    .lines(lines)
                    .build();
            receipt.recalculateTotals();

            switch (status) {
                case COMPLETED -> {
                    receipt.complete(receivedBy);
                    if (receipt.getStatus() != ReceiptStatus.COMPLETED) {
                        throw new IllegalStateException("Requested COMPLETED but the quantities carry a discrepancy");
                    }
                }
                case PARTIAL -> {
                    receipt.complete(receivedBy);
                    if (receipt.getStatus() != ReceiptStatus.PARTIAL) {
                        throw new IllegalStateException("Requested PARTIAL but the quantities carry no discrepancy");
                    }
                }
                case CANCELLED -> receipt.cancel(receivedBy, "Recorded in error");
                case DRAFT, IN_PROGRESS -> { }
            }
            return receipt;
        }

        private static QualityStatus quality(int received, int rejected) {
            if (rejected <= 0) {
                return QualityStatus.ACCEPTED;
            }
            return rejected >= received ? QualityStatus.REJECTED : QualityStatus.PARTIAL;
        }
    }
}
