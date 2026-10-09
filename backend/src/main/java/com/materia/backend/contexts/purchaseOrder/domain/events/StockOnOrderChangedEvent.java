package com.materia.backend.contexts.purchaseOrder.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.util.UUID;

/**
 * An order changed how much of a material is on order: positive when ordered, negative when an order is reduced or
 * withdrawn. The master data context keeps the material's stock on order from these events.
 *
 * <p>Keyed by the material, so the changes of one material are applied in the order they were made.
 */
public class StockOnOrderChangedEvent extends DomainEvent {

    private final String materialCode;
    private final int quantityChange;

    public StockOnOrderChangedEvent(UUID materialId, String materialCode, int quantityChange) {
        super(materialId);
        this.materialCode = materialCode;
        this.quantityChange = quantityChange;
    }

    public UUID getMaterialId() {
        return (UUID) getAggregateId();
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public int getQuantityChange() {
        return quantityChange;
    }
}
