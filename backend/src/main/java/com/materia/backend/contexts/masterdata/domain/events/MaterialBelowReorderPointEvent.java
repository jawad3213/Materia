package com.materia.backend.contexts.masterData.domain.events;

import com.materia.backend.common.domain.DomainEvent;
import com.materia.backend.contexts.masterData.domain.enums.StockStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement : Matériau en dessous du point de réapprovisionnement
 * Déclenché lorsqu'un matériau atteint ou dépasse le point de réapprovisionnement
 * 
 * @author SAP MM Team
 * @version 1.0
 */
public class MaterialBelowReorderPointEvent extends DomainEvent {
    
    private final UUID entityId;
    private final String materialCode;
    private final String materialName;
    private final int currentStock;
    private final int reorderPoint;
    private final int safetyStock;
    private final String supplierId;
    private final StockStatus stockStatus;
    
    public MaterialBelowReorderPointEvent(UUID entityId, String materialCode, String materialName,
                                          int currentStock, int reorderPoint, int safetyStock,
                                          String supplierId, StockStatus stockStatus) {
        super(entityId);
        this.entityId = entityId;
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.currentStock = currentStock;
        this.reorderPoint = reorderPoint;
        this.safetyStock = safetyStock;
        this.supplierId = supplierId;
        this.stockStatus = stockStatus;
    }
    
    public UUID getEntityId() { return entityId; }
    public LocalDateTime getOccurredAt() { return getOccurredOn(); }
    
    @Override
    public String getEventType() { return "MATERIAL_BELOW_REORDER_POINT"; }
    
    public int getVersion() { return 1; }
    
    public String getMaterialCode() { return materialCode; }
    public String getMaterialName() { return materialName; }
    public int getCurrentStock() { return currentStock; }
    public int getReorderPoint() { return reorderPoint; }
    public int getSafetyStock() { return safetyStock; }
    public String getSupplierId() { return supplierId; }
    public StockStatus getStockStatus() { return stockStatus; }
    
    public boolean isCritical() {
        return stockStatus == StockStatus.CRITICAL || stockStatus == StockStatus.OUT_OF_STOCK;
    }
    
    public int getReorderQuantity() {
        // Si stock < safetyStock, urgent = commander plus
        if (currentStock < safetyStock) {
            return (reorderPoint - currentStock) + safetyStock;
        }
        return reorderPoint - currentStock;
    }
}
