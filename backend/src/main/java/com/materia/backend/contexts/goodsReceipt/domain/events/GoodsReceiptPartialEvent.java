package com.materia.backend.contexts.goodsReceipt.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/** Domain event raised when a goods receipt is completed with missing quantities. */
public class GoodsReceiptPartialEvent extends DomainEvent {

    private final String receiptCode;
    private final String purchaseOrderId;
    private final int totalQuantityOrdered;
    private final int totalQuantityReceived;
    private final int totalQuantityPending;
    private final String completedBy;

    public GoodsReceiptPartialEvent(UUID goodsReceiptId, String receiptCode,
                                    String purchaseOrderId, int totalQuantityOrdered,
                                    int totalQuantityReceived, int totalQuantityPending,
                                    String completedBy) {
        super(goodsReceiptId);
        this.receiptCode = receiptCode;
        this.purchaseOrderId = purchaseOrderId;
        this.totalQuantityOrdered = totalQuantityOrdered;
        this.totalQuantityReceived = totalQuantityReceived;
        this.totalQuantityPending = totalQuantityPending;
        this.completedBy = completedBy;
    }

    public GoodsReceiptPartialEvent(UUID eventId, LocalDateTime occurredOn,
                                    UUID goodsReceiptId, String receiptCode,
                                    String purchaseOrderId, int totalQuantityOrdered,
                                    int totalQuantityReceived, int totalQuantityPending,
                                    String completedBy) {
        super(eventId, occurredOn, goodsReceiptId);
        this.receiptCode = receiptCode;
        this.purchaseOrderId = purchaseOrderId;
        this.totalQuantityOrdered = totalQuantityOrdered;
        this.totalQuantityReceived = totalQuantityReceived;
        this.totalQuantityPending = totalQuantityPending;
        this.completedBy = completedBy;
    }

    public UUID getGoodsReceiptId() { return (UUID) getAggregateId(); }
    public String getReceiptCode() { return receiptCode; }
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public int getTotalQuantityOrdered() { return totalQuantityOrdered; }
    public int getTotalQuantityReceived() { return totalQuantityReceived; }
    public int getTotalQuantityPending() { return totalQuantityPending; }
    public String getCompletedBy() { return completedBy; }
}
