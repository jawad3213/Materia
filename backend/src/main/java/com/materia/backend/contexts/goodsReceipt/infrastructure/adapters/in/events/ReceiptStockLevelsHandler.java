package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.events;

import com.materia.backend.common.infrastructure.messaging.integration.IntegrationEvent;
import com.materia.backend.common.infrastructure.messaging.integration.IntegrationEventHandler;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.ReceiptStockLevelsUseCase;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Records on each receipt line the stock levels master data reports once it applied the line ({@code ReceiptStockApplied}). */
@Component
public class ReceiptStockLevelsHandler implements IntegrationEventHandler {

    private final ReceiptStockLevelsUseCase receipts;

    public ReceiptStockLevelsHandler(ReceiptStockLevelsUseCase receipts) {
        this.receipts = receipts;
    }

    @Override
    public String consumerName() {
        return "goodsReceipt.receipt-stock-levels";
    }

    @Override
    public boolean handles(IntegrationEvent event) {
        return event.is("masterData", "ReceiptStockApplied");
    }

    @Override
    public void handle(IntegrationEvent event) {
        var payload = event.payload();
        receipts.recordStockLevels(
                UUID.fromString(payload.path("goodsReceiptId").asText()),
                UUID.fromString(payload.path("goodsReceiptLineId").asText()),
                payload.path("stockBefore").asInt(),
                payload.path("stockAfter").asInt());
    }
}
