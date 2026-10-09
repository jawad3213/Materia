package com.materia.backend.contexts.masterData.infrastructure.adapters.in.events;

import com.materia.backend.common.infrastructure.messaging.integration.IntegrationEvent;
import com.materia.backend.common.infrastructure.messaging.integration.IntegrationEventHandler;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialInventoryUseCase;
import org.springframework.stereotype.Component;

/** Keeps each material's stock on order from the purchase orders' {@code StockOnOrderChanged} events. */
@Component
public class StockOnOrderHandler implements IntegrationEventHandler {

    private final MaterialInventoryUseCase materials;

    public StockOnOrderHandler(MaterialInventoryUseCase materials) {
        this.materials = materials;
    }

    @Override
    public String consumerName() {
        return "masterData.stock-on-order";
    }

    @Override
    public boolean handles(IntegrationEvent event) {
        return event.is("purchaseOrder", "StockOnOrderChanged");
    }

    @Override
    public void handle(IntegrationEvent event) {
        var payload = event.payload();
        int change = payload.path("quantityChange").asInt();
        var materialId = EventFields.uuid(payload, "materialId");
        String materialCode = EventFields.text(payload, "materialCode");
        if (change > 0) {
            materials.addStockOnOrder(materialId, materialCode, change, null);
        } else if (change < 0) {
            materials.releaseStockOnOrder(materialId, materialCode, -change, null);
        }
    }
}
