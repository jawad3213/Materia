package com.materia.backend.contexts.goodsReceipt.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/** Domain event raised when all goods in a receipt are accepted and completed. */
public class GoodsReceiptCompletedEvent extends DomainEvent {

    private final String receiptCode;
    private final String purchaseOrderId;
    private final int totalQuantityReceived;
    private final int totalQuantityAccepted;
    private final String completedBy;

    public GoodsReceiptCompletedEvent(UUID goodsReceiptId, String receiptCode,
                                      String purchaseOrderId, int totalQuantityReceived,
                                      int totalQuantityAccepted, String completedBy) {
        super(goodsReceiptId);
        this.receiptCode = receiptCode;
        this.purchaseOrderId = purchaseOrderId;
        this.totalQuantityReceived = totalQuantityReceived;
        this.totalQuantityAccepted = totalQuantityAccepted;
        this.completedBy = completedBy;
    }

    public GoodsReceiptCompletedEvent(UUID eventId, LocalDateTime occurredOn,
                                      UUID goodsReceiptId, String receiptCode,
                                      String purchaseOrderId, int totalQuantityReceived,
                                      int totalQuantityAccepted, String completedBy) {
        super(eventId, occurredOn, goodsReceiptId);
        this.receiptCode = receiptCode;
        this.purchaseOrderId = purchaseOrderId;
        this.totalQuantityReceived = totalQuantityReceived;
        this.totalQuantityAccepted = totalQuantityAccepted;
        this.completedBy = completedBy;
    }

    public UUID getGoodsReceiptId() { return (UUID) getAggregateId(); }
    public String getReceiptCode() { return receiptCode; }
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public int getTotalQuantityReceived() { return totalQuantityReceived; }
    public int getTotalQuantityAccepted() { return totalQuantityAccepted; }
    public String getCompletedBy() { return completedBy; }
}
