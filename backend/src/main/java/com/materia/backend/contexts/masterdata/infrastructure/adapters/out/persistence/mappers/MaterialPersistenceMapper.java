package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.entities.StockMovement;
import com.materia.backend.contexts.masterData.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.domain.valueObjects.MaterialCode;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.MaterialJpaEntity;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.MaterialStockMovementJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper between Material domain entity and MaterialJpaEntity
 */
@Component
public class MaterialPersistenceMapper {

    /**
     * Domain Entity → JPA Entity
     */
    public MaterialJpaEntity toJpaEntity(Material domain) {
        if (domain == null) return null;

        MaterialJpaEntity jpa = new MaterialJpaEntity();
        jpa.setId(domain.getId());
        jpa.setCode(domain.getCode() != null ? domain.getCode().getValue() : null);
        jpa.setName(domain.getName());
        jpa.setDescription(domain.getDescription());
        jpa.setShortDescription(domain.getShortDescription());
        jpa.setSearchKeywords(domain.getSearchKeywords());
        jpa.setAlternativeName(domain.getAlternativeName());

        jpa.setCategoryId(domain.getCategoryId());
        jpa.setCategoryName(domain.getCategoryName());
        jpa.setSupplierId(domain.getSupplierId());
        jpa.setSupplierName(domain.getSupplierName());
        jpa.setMaterialType(domain.getMaterialType());
        jpa.setStatus(domain.getStatus());

        jpa.setUnitOfMeasure(domain.getUnitOfMeasure());

        jpa.setCurrentStock(domain.getCurrentStock());
        jpa.setAvailableStock(domain.getAvailableStock());
        jpa.setMinimumStock(domain.getMinimumStock());
        jpa.setMaximumStock(domain.getMaximumStock());
        jpa.setReorderPoint(domain.getReorderPoint());
        jpa.setSafetyStock(domain.getSafetyStock());
        jpa.setEconomicOrderQuantity(domain.getEconomicOrderQuantity());

        // Flatten Money value objects
        if (domain.getStandardPrice() != null) {
            jpa.setStandardPrice(domain.getStandardPrice().getAmount());
            jpa.setStandardPriceCurrency(domain.getStandardPrice().getCurrency());
        }
        if (domain.getCostPrice() != null) {
            jpa.setCostPrice(domain.getCostPrice().getAmount());
            jpa.setCostPriceCurrency(domain.getCostPrice().getCurrency());
        }
        if (domain.getLastPurchasePrice() != null) {
            jpa.setLastPurchasePrice(domain.getLastPurchasePrice().getAmount());
            jpa.setLastPurchasePriceCurrency(domain.getLastPurchasePrice().getCurrency());
        }
        if (domain.getAveragePurchasePrice() != null) {
            jpa.setAveragePurchasePrice(domain.getAveragePurchasePrice().getAmount());
            jpa.setAveragePurchasePriceCurrency(domain.getAveragePurchasePrice().getCurrency());
        }

        jpa.setObsoletedAt(domain.getObsoletedAt());
        jpa.setObsoletedBy(domain.getObsoletedBy());
        jpa.setObsoletedReason(domain.getObsoletedReason());
        jpa.setStockMovements(toMovementJpaEntities(domain.getStockMovements(), jpa));

        // Audit fields
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setVersion(domain.getVersion());
        jpa.setCreatedBy(domain.getCreatedBy());
        jpa.setUpdatedBy(domain.getUpdatedBy());

        return jpa;
    }

    /**
     * JPA Entity → Domain Entity
     */
    public Material toDomainEntity(MaterialJpaEntity jpa) {
        if (jpa == null) return null;

        Material domain = new Material();
        domain.setId(jpa.getId());
        if (jpa.getCode() != null) domain.setCode(MaterialCode.of(jpa.getCode()));
        domain.setName(jpa.getName());
        domain.setDescription(jpa.getDescription());
        domain.setShortDescription(jpa.getShortDescription());
        domain.setSearchKeywords(jpa.getSearchKeywords());
        domain.setAlternativeName(jpa.getAlternativeName());

        domain.setCategoryId(jpa.getCategoryId());
        domain.setCategoryName(jpa.getCategoryName());
        domain.setSupplierId(jpa.getSupplierId());
        domain.setSupplierName(jpa.getSupplierName());
        domain.setMaterialType(jpa.getMaterialType());
        domain.setStatus(jpa.getStatus());

        domain.setUnitOfMeasure(jpa.getUnitOfMeasure());

        domain.setCurrentStock(jpa.getCurrentStock());
        domain.setAvailableStock(jpa.getAvailableStock());
        domain.setMinimumStock(jpa.getMinimumStock());
        domain.setMaximumStock(jpa.getMaximumStock());
        domain.setReorderPoint(jpa.getReorderPoint());
        domain.setSafetyStock(jpa.getSafetyStock());
        domain.setEconomicOrderQuantity(jpa.getEconomicOrderQuantity());

        // Reconstruct Money value objects
        domain.setStandardPrice(toMoney(jpa.getStandardPrice(), jpa.getStandardPriceCurrency()));
        domain.setCostPrice(toMoney(jpa.getCostPrice(), jpa.getCostPriceCurrency()));
        domain.setLastPurchasePrice(toMoney(jpa.getLastPurchasePrice(), jpa.getLastPurchasePriceCurrency()));
        domain.setAveragePurchasePrice(toMoney(jpa.getAveragePurchasePrice(), jpa.getAveragePurchasePriceCurrency()));

        domain.setObsoletedAt(jpa.getObsoletedAt());
        domain.setObsoletedBy(jpa.getObsoletedBy());
        domain.setObsoletedReason(jpa.getObsoletedReason());
        domain.setStockMovements(toDomainMovements(jpa.getStockMovements()));

        // Audit fields
        domain.setCreatedAt(jpa.getCreatedAt());
        domain.setUpdatedAt(jpa.getUpdatedAt());
        domain.setVersion(jpa.getVersion());
        domain.setCreatedBy(jpa.getCreatedBy());
        domain.setUpdatedBy(jpa.getUpdatedBy());

        return domain;
    }

    private Money toMoney(BigDecimal amount, CurrencyCode currency) {
        if (amount == null) return null;
        CurrencyCode cur = currency != null ? currency : CurrencyCode.MAD;
        return Money.of(amount, cur);
    }

    private List<MaterialStockMovementJpaEntity> toMovementJpaEntities(List<StockMovement> movements,
                                                                       MaterialJpaEntity parent) {
        List<MaterialStockMovementJpaEntity> entities = new ArrayList<>();
        if (movements == null) {
            return entities;
        }

        for (StockMovement movement : movements) {
            if (movement == null) {
                continue;
            }

            MaterialStockMovementJpaEntity entity = new MaterialStockMovementJpaEntity();
            entity.setId(movement.getId());
            entity.setMaterial(parent);
            entity.setMovementType(movement.getMovementType());
            entity.setQuantity(movement.getQuantity());
            entity.setPreviousStock(movement.getPreviousStock());
            entity.setNewStock(movement.getNewStock());
            entity.setReason(movement.getReason());
            entity.setOccurredAt(movement.getOccurredAt());
            entities.add(entity);
        }

        return entities;
    }

    private List<StockMovement> toDomainMovements(List<MaterialStockMovementJpaEntity> entities) {
        List<StockMovement> movements = new ArrayList<>();
        if (entities == null) {
            return movements;
        }

        for (MaterialStockMovementJpaEntity entity : entities) {
            if (entity == null) {
                continue;
            }

            StockMovement movement = new StockMovement();
            movement.setId(entity.getId());
            movement.setMovementType(entity.getMovementType());
            movement.setQuantity(entity.getQuantity());
            movement.setPreviousStock(entity.getPreviousStock());
            movement.setNewStock(entity.getNewStock());
            movement.setReason(entity.getReason());
            movement.setOccurredAt(entity.getOccurredAt());
            movements.add(movement);
        }

        return movements;
    }
}
