package com.materia.backend.contexts.goodsReceipt.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/** Domain event raised when a goods receipt is created. */
public class GoodsReceiptCreatedEvent extends DomainEvent {

    private final String receiptCode;
    private final String purchaseOrderId;
    private final String purchaseOrderCode;
    private final String receivedBy;
    private final String receivedByName;

    public GoodsReceiptCreatedEvent(UUID goodsReceiptId, String receiptCode,
                                    String purchaseOrderId, String purchaseOrderCode,
                                    String receivedBy, String receivedByName) {
        super(goodsReceiptId);
        this.receiptCode = receiptCode;
        this.purchaseOrderId = purchaseOrderId;
        this.purchaseOrderCode = purchaseOrderCode;
        this.receivedBy = receivedBy;
        this.receivedByName = receivedByName;
    }

    public GoodsReceiptCreatedEvent(UUID eventId, LocalDateTime occurredOn,
                                    UUID goodsReceiptId, String receiptCode,
                                    String purchaseOrderId, String purchaseOrderCode,
                                    String receivedBy, String receivedByName) {
        super(eventId, occurredOn, goodsReceiptId);
        this.receiptCode = receiptCode;
        this.purchaseOrderId = purchaseOrderId;
        this.purchaseOrderCode = purchaseOrderCode;
        this.receivedBy = receivedBy;
        this.receivedByName = receivedByName;
    }

    public UUID getGoodsReceiptId() { return (UUID) getAggregateId(); }
    public String getReceiptCode() { return receiptCode; }
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public String getPurchaseOrderCode() { return purchaseOrderCode; }
    public String getReceivedBy() { return receivedBy; }
    public String getReceivedByName() { return receivedByName; }
}
