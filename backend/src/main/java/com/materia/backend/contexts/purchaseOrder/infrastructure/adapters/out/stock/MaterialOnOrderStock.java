package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.stock;

import com.materia.backend.contexts.purchaseOrder.domain.events.StockOnOrderChangedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.OnOrderStock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Announces changes of stock on order as {@link StockOnOrderChangedEvent}s; the master data context applies them
 * after the order committed. Nothing here calls master data, so the two contexts can run as separate services.
 */
@Component
public class MaterialOnOrderStock implements OnOrderStock {

    private final ApplicationEventPublisher events;

    public MaterialOnOrderStock(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Override
    public void add(UUID materialId, String materialCode, int quantity) {
        if (quantity > 0) {
            events.publishEvent(new StockOnOrderChangedEvent(materialId, materialCode, quantity));
        }
    }

    @Override
    public void release(UUID materialId, String materialCode, int quantity) {
        if (quantity > 0) {
            events.publishEvent(new StockOnOrderChangedEvent(materialId, materialCode, -quantity));
        }
    }
}
