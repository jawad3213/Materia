package com.materia.backend.contexts.masterData.infrastructure.adapters.in.events;

import com.materia.backend.common.infrastructure.messaging.integration.IntegrationEvent;
import com.materia.backend.common.infrastructure.messaging.integration.IntegrationEventHandler;
import com.materia.backend.contexts.masterData.domain.events.ReceiptStockAppliedEvent;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialInventoryUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Applies validated receipt lines ({@code GoodsReceiptLineReceived}) to stock: the accepted quantity enters stock and
 * everything received leaves the stock on order. Answers with {@link ReceiptStockAppliedEvent} so the receipt line
 * records the stock before and after.
 */
@Component
public class ReceivedGoodsStockHandler implements IntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ReceivedGoodsStockHandler.class);

    private final MaterialInventoryUseCase materials;
    private final ApplicationEventPublisher events;

    public ReceivedGoodsStockHandler(MaterialInventoryUseCase materials, ApplicationEventPublisher events) {
        this.materials = materials;
        this.events = events;
    }

    @Override
    public String consumerName() {
        return "masterData.received-goods-stock";
    }

    @Override
    public boolean handles(IntegrationEvent event) {
        return event.is("goodsReceipt", "GoodsReceiptLineReceived");
    }

    @Override
    public void handle(IntegrationEvent event) {
        var payload = event.payload();
        UUID materialId = EventFields.uuid(payload, "materialId");
        String materialCode = EventFields.text(payload, "materialCode");
        int accepted = payload.path("quantityAccepted").asInt();
        int received = payload.path("quantityReceived").asInt();
        String userId = EventFields.text(payload, "userId");

        if (accepted <= 0) {
            if (received > 0) {
                materials.releaseStockOnOrder(materialId, materialCode, received, userId);
            }
            return;
        }
        materials.receiveGoods(materialId, materialCode, accepted, received, EventFields.text(payload, "reason"), userId)
                .ifPresentOrElse(
                        movement -> events.publishEvent(new ReceiptStockAppliedEvent(
                                materialId,
                                EventFields.uuid(payload, "goodsReceiptId"),
                                EventFields.uuid(payload, "goodsReceiptLineId"),
                                movement.stockBefore(), movement.stockAfter())),
                        () -> log.warn("Received goods for unknown material {} ({}) on receipt {}: stock unchanged",
                                materialCode, materialId, EventFields.text(payload, "goodsReceiptId")));
    }
}
