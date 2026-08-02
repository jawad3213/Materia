package com.materia.backend.contexts.masterdata.domain.entities;

import com.materia.backend.contexts.masterdata.domain.enums.StockMovementType;

import java.time.LocalDateTime;
import java.util.UUID;

public class StockMovement {

    private UUID id;
    private StockMovementType movementType;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String reason;
    private LocalDateTime occurredAt;

    public static StockMovement create(StockMovementType movementType,
                                       Integer quantity,
                                       Integer previousStock,
                                       Integer newStock,
                                       String reason) {
        StockMovement movement = new StockMovement();
        movement.id = UUID.randomUUID();
        movement.movementType = movementType;
        movement.quantity = quantity;
        movement.previousStock = previousStock;
        movement.newStock = newStock;
        movement.reason = reason;
        movement.occurredAt = LocalDateTime.now();
        return movement;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public StockMovementType getMovementType() { return movementType; }
    public void setMovementType(StockMovementType movementType) { this.movementType = movementType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getPreviousStock() { return previousStock; }
    public void setPreviousStock(Integer previousStock) { this.previousStock = previousStock; }

    public Integer getNewStock() { return newStock; }
    public void setNewStock(Integer newStock) { this.newStock = newStock; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
