package com.materia.backend.contexts.masterData.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when material stock decreases.
 */
public class MaterialStockDecreasedEvent extends DomainEvent {

    private final String materialCode;
    private final Integer decreasedQuantity;
    private final Integer previousStock;
    private final Integer newStock;
    private final String reason;

    public MaterialStockDecreasedEvent(UUID materialId,
                                       String materialCode,
                                       Integer decreasedQuantity,
                                       Integer previousStock,
                                       Integer newStock,
                                       String reason) {
        super(materialId);
        this.materialCode = materialCode;
        this.decreasedQuantity = decreasedQuantity;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.reason = reason;
    }

    public MaterialStockDecreasedEvent(UUID eventId,
                                       LocalDateTime occurredOn,
                                       UUID materialId,
                                       String materialCode,
                                       Integer decreasedQuantity,
                                       Integer previousStock,
                                       Integer newStock,
                                       String reason) {
        super(eventId, occurredOn, materialId);
        this.materialCode = materialCode;
        this.decreasedQuantity = decreasedQuantity;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.reason = reason;
    }

    public UUID getMaterialId() {
        return (UUID) getAggregateId();
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public Integer getDecreasedQuantity() {
        return decreasedQuantity;
    }

    public Integer getPreviousStock() {
        return previousStock;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public String getReason() {
        return reason;
    }
}
