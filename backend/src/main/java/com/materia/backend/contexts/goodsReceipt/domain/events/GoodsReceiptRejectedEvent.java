package com.materia.backend.contexts.goodsReceipt.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/** Domain event raised when quantities in a goods receipt are rejected. */
public class GoodsReceiptRejectedEvent extends DomainEvent {

    private final String receiptCode;
    private final String purchaseOrderId;
    private final int totalQuantityRejected;
    private final String rejectionReason;
    private final String rejectedBy;

    public GoodsReceiptRejectedEvent(UUID goodsReceiptId, String receiptCode,
                                     String purchaseOrderId, int totalQuantityRejected,
                                     String rejectionReason, String rejectedBy) {
        super(goodsReceiptId);
        this.receiptCode = receiptCode;
        this.purchaseOrderId = purchaseOrderId;
        this.totalQuantityRejected = totalQuantityRejected;
        this.rejectionReason = rejectionReason;
        this.rejectedBy = rejectedBy;
    }

    public GoodsReceiptRejectedEvent(UUID eventId, LocalDateTime occurredOn,
                                     UUID goodsReceiptId, String receiptCode,
                                     String purchaseOrderId, int totalQuantityRejected,
                                     String rejectionReason, String rejectedBy) {
        super(eventId, occurredOn, goodsReceiptId);
        this.receiptCode = receiptCode;
        this.purchaseOrderId = purchaseOrderId;
        this.totalQuantityRejected = totalQuantityRejected;
        this.rejectionReason = rejectionReason;
        this.rejectedBy = rejectedBy;
    }

    public UUID getGoodsReceiptId() { return (UUID) getAggregateId(); }
    public String getReceiptCode() { return receiptCode; }
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public int getTotalQuantityRejected() { return totalQuantityRejected; }
    public String getRejectionReason() { return rejectionReason; }
    public String getRejectedBy() { return rejectedBy; }
}
