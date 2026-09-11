package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.enums.StockStatus;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;
import com.materia.backend.contexts.masterData.domain.valueObjects.ReorderQuantity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Material Stock Domain Service
 * Service métier pour la gestion des stocks
 * 
 * @author SAP MM Team
 * @version 1.0
 */
@Service
public class MaterialStockDomainService {
    
    /**
     * Obtient le statut du stock d'un matériau
     */
    public StockStatus getStockStatus(Material material) {
        if (material == null) {
            return null;
        }
        return material.getStockStatus();
    }
    
    /**
     * Obtient la quantité de réapprovisionnement recommandée
     */
    public ReorderQuantity getRecommendedReorderQuantity(Material material) {
        if (material == null || !material.isBelowReorderPoint()) {
            return null;
        }
        
        int currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : 0;
        int reorderPoint = material.getReorderPoint() != null ? material.getReorderPoint() : 0;
        int safetyStock = material.getSafetyStock() != null ? material.getSafetyStock() : 0;
        int eoq = material.getEconomicOrderQuantity() != null ? material.getEconomicOrderQuantity() : 100;
        
        // Calcul de la quantité à commander
        int quantity = 0;
        String reason;
        boolean isUrgent;
        
        // Si stock en dessous du safety stock → URGENT
        if (currentStock <= safetyStock) {
            quantity = Math.max(eoq, (reorderPoint - currentStock) + safetyStock);
            reason = "Stock critique (en dessous du stock de sécurité)";
            isUrgent = true;
        } else {
            quantity = eoq;
            reason = "Point de réapprovisionnement atteint";
            isUrgent = false;
        }
        
        // Estimation du coût
        Money estimatedCost = null;
        if (material.getStandardPrice() != null) {
            estimatedCost = material.getStandardPrice().multiply(quantity);
        }
        
        return new ReorderQuantity(quantity, estimatedCost, reason, isUrgent);
    }
    
    /**
     * Obtient les matériaux qui nécessitent un réapprovisionnement
     */
    public List<Material> getMaterialsNeedingReorder(List<Material> materials) {
        if (materials == null || materials.isEmpty()) {
            return List.of();
        }
        return materials.stream()
                .filter(material -> material.getStockStatus() == StockStatus.REORDER_NEEDED)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les matériaux en stock critique
     */
    public List<Material> getCriticalMaterials(List<Material> materials) {
        if (materials == null || materials.isEmpty()) {
            return List.of();
        }
        return materials.stream()
                .filter(material -> material.getStockStatus() == StockStatus.CRITICAL)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les matériaux en rupture de stock
     */
    public List<Material> getOutOfStockMaterials(List<Material> materials) {
        if (materials == null || materials.isEmpty()) {
            return List.of();
        }
        return materials.stream()
                .filter(material -> material.getStockStatus() == StockStatus.OUT_OF_STOCK)
                .collect(Collectors.toList());
    }
    
    /**
     * Calcule la valeur totale du stock
     */
    public Money calculateStockValue(Material material) {
        if (material == null || material.getStandardPrice() == null) {
            return null;
        }
        int currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : 0;
        return material.getStandardPrice().multiply(currentStock);
    }
}
