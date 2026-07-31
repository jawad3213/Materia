package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.masterdata.domain.entities.Material;
import com.materia.backend.contexts.masterdata.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterdata.domain.valueObjects.MaterialCode;
import com.materia.backend.contexts.masterdata.domain.valueObjects.Money;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities.MaterialJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
}
