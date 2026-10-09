package com.materia.backend.contexts.masterData.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.util.UUID;

/**
 * The stock of a received receipt line was applied: the material's stock before and after. The goods receipt context
 * records these levels on its line.
 */
public class ReceiptStockAppliedEvent extends DomainEvent {

    private final UUID goodsReceiptId;
    private final UUID goodsReceiptLineId;
    private final int stockBefore;
    private final int stockAfter;

    public ReceiptStockAppliedEvent(UUID materialId, UUID goodsReceiptId, UUID goodsReceiptLineId,
                                    int stockBefore, int stockAfter) {
        super(materialId);
        this.goodsReceiptId = goodsReceiptId;
        this.goodsReceiptLineId = goodsReceiptLineId;
        this.stockBefore = stockBefore;
        this.stockAfter = stockAfter;
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

    public int getStockBefore() {
        return stockBefore;
    }

    public int getStockAfter() {
        return stockAfter;
    }
}
