package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.stock;

import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptLineReceivedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.ReceivedStock;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialInventoryUseCase;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Checks materials through the master data context's public lookup (a read, which can become an HTTP call), and
 * announces received lines as {@link GoodsReceiptLineReceivedEvent}s, which master data applies to its stock.
 */
@Component
public class MaterialReceivedStock implements ReceivedStock {

    private final MaterialInventoryUseCase materials;
    private final ApplicationEventPublisher events;

    public MaterialReceivedStock(MaterialInventoryUseCase materials, ApplicationEventPublisher events) {
        this.materials = materials;
        this.events = events;
    }

    @Override
    public boolean knows(UUID materialId, String materialCode) {
        return materialId != null
                ? materials.findMaterial(materialId).isPresent()
                : materialCode != null && materials.findMaterialByCode(materialCode).isPresent();
    }

    @Override
    public void received(UUID goodsReceiptId, UUID goodsReceiptLineId, UUID materialId, String materialCode,
                         int acceptedQuantity, int receivedQuantity, String reason, String userId) {
        events.publishEvent(new GoodsReceiptLineReceivedEvent(goodsReceiptId, goodsReceiptLineId, materialId,
                materialCode, acceptedQuantity, receivedQuantity, reason, userId));
    }
}
