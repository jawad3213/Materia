package com.materia.backend.contexts.goodsReceipt.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.util.UUID;

/**
 * A validated receipt line: its accepted quantity enters stock, and everything received (accepted or rejected) is no
 * longer on order. The master data context moves the stock from this event and answers with the stock levels.
 *
 * <p>Keyed by the material, so the movements of one material are applied in the order they were made.
 */
public class GoodsReceiptLineReceivedEvent extends DomainEvent {

    private final UUID goodsReceiptId;
    private final UUID goodsReceiptLineId;
    private final String materialCode;
    private final int quantityAccepted;
    private final int quantityReceived;
    private final String reason;
    private final String userId;

    public GoodsReceiptLineReceivedEvent(UUID goodsReceiptId, UUID goodsReceiptLineId, UUID materialId,
                                         String materialCode, int quantityAccepted, int quantityReceived,
                                         String reason, String userId) {
        super(materialId);
        this.goodsReceiptId = goodsReceiptId;
        this.goodsReceiptLineId = goodsReceiptLineId;
        this.materialCode = materialCode;
        this.quantityAccepted = quantityAccepted;
        this.quantityReceived = quantityReceived;
        this.reason = reason;
        this.userId = userId;
    }

    public UUID getMaterialId() {
        return (UUID) getAggregateId();
    }

    public UUID getGoodsReceiptId() {
        return goodsReceiptId;
    }

    public UUID getGoodsReceiptLineId() {
        return goodsReceiptLineId;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public int getQuantityAccepted() {
        return quantityAccepted;
    }

    public int getQuantityReceived() {
        return quantityReceived;
    }

    public String getReason() {
        return reason;
    }

    public String getUserId() {
        return userId;
    }
}
