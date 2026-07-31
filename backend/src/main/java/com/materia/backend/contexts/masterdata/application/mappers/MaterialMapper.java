package com.materia.backend.contexts.masterdata.application.mappers;

import com.materia.backend.contexts.masterdata.application.dtos.material.CreateMaterialInput;
import com.materia.backend.contexts.masterdata.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterdata.application.dtos.material.UpdateMaterialInput;
import com.materia.backend.contexts.masterdata.domain.entities.Material;
import com.materia.backend.contexts.masterdata.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterdata.domain.enums.MaterialType;
import com.materia.backend.contexts.masterdata.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterdata.domain.enums.UnitOfMeasure;
import com.materia.backend.contexts.masterdata.domain.valueObjects.Money;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import com.materia.backend.common.application.BaseMapper;

/**
 * Mapper for Material: Request DTO -> Entity and Entity -> Response DTO
 */
@Component
public class MaterialMapper implements BaseMapper<Material, CreateMaterialInput, UpdateMaterialInput, MaterialOutput> {

    // ============================================================
    // REQUEST DTO -> ENTITY
    // ============================================================

    /**
     * Convert CreateMaterialInput to Material entity
     */
    public Material toEntity(CreateMaterialInput request) {
        if (request == null) return null;

        CurrencyCode stdCurrency = request.getStandardPriceCurrency() != null
                ? CurrencyCode.fromCode(request.getStandardPriceCurrency())
                : CurrencyCode.MAD;

        CurrencyCode costCurrency = request.getCostPriceCurrency() != null
                ? CurrencyCode.fromCode(request.getCostPriceCurrency())
                : CurrencyCode.MAD;

        return Material.builder()
                .name(request.getName())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .searchKeywords(request.getSearchKeywords())
                .alternativeName(request.getAlternativeName())
                .categoryId(request.getCategoryId())
                .supplierId(request.getSupplierId())
                .materialType(request.getMaterialType() != null
                        ? MaterialType.fromValue(request.getMaterialType())
                        : null)
                .status(request.getStatus() != null
                        ? MaterialStatus.fromValue(request.getStatus())
                        : MaterialStatus.ACTIVE)
                .unitOfMeasure(request.getUnitOfMeasure() != null
                        ? UnitOfMeasure.fromValue(request.getUnitOfMeasure())
                        : null)
                .currentStock(request.getCurrentStock())
                .availableStock(request.getCurrentStock())
                .minimumStock(request.getMinimumStock())
                .maximumStock(request.getMaximumStock())
                .reorderPoint(request.getReorderPoint())
                .safetyStock(request.getSafetyStock())
                .economicOrderQuantity(request.getEconomicOrderQuantity())
                .standardPrice(request.getStandardPrice() != null
                        ? Money.of(request.getStandardPrice(), stdCurrency)
                        : null)
                .costPrice(request.getCostPrice() != null
                        ? Money.of(request.getCostPrice(), costCurrency)
                        : null)
                .createdBy(request.getCreatedBy())
                .build();
    }

    /**
     * Update existing entity from UpdateMaterialInput
     */
    public void updateEntity(Material entity, UpdateMaterialInput request) {
        if (entity == null || request == null) return;

        if (request.getName() != null) entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getShortDescription() != null) entity.setShortDescription(request.getShortDescription());
        if (request.getSearchKeywords() != null) entity.setSearchKeywords(request.getSearchKeywords());
        if (request.getAlternativeName() != null) entity.setAlternativeName(request.getAlternativeName());
        if (request.getCategoryId() != null) entity.setCategoryId(request.getCategoryId());
        if (request.getSupplierId() != null) entity.setSupplierId(request.getSupplierId());
        if (request.getMaterialType() != null) entity.setMaterialType(MaterialType.fromValue(request.getMaterialType()));
        if (request.getStatus() != null) entity.setStatus(MaterialStatus.fromValue(request.getStatus()));
        if (request.getUnitOfMeasure() != null) entity.setUnitOfMeasure(UnitOfMeasure.fromValue(request.getUnitOfMeasure()));
        if (request.getCurrentStock() != null) entity.setCurrentStock(request.getCurrentStock());
        if (request.getMinimumStock() != null) entity.setMinimumStock(request.getMinimumStock());
        if (request.getMaximumStock() != null) entity.setMaximumStock(request.getMaximumStock());
        if (request.getReorderPoint() != null) entity.setReorderPoint(request.getReorderPoint());
        if (request.getSafetyStock() != null) entity.setSafetyStock(request.getSafetyStock());
        if (request.getEconomicOrderQuantity() != null) entity.setEconomicOrderQuantity(request.getEconomicOrderQuantity());

        CurrencyCode stdCurrency = request.getStandardPriceCurrency() != null
                ? CurrencyCode.fromCode(request.getStandardPriceCurrency())
                : CurrencyCode.MAD;

        if (request.getStandardPrice() != null) {
            entity.setStandardPrice(Money.of(request.getStandardPrice(), stdCurrency));
        }

        CurrencyCode costCurrency = request.getCostPriceCurrency() != null
                ? CurrencyCode.fromCode(request.getCostPriceCurrency())
                : CurrencyCode.MAD;

        if (request.getCostPrice() != null) {
            entity.setCostPrice(Money.of(request.getCostPrice(), costCurrency));
        }

        if (request.getUpdatedBy() != null) {
            entity.setUpdatedBy(request.getUpdatedBy());
        }
    }

    // ============================================================
    // ENTITY -> RESPONSE DTO
    // ============================================================

    /**
     * Convert Material entity to MaterialOutput
     */
    public MaterialOutput toResponse(Material entity) {
        if (entity == null) return null;

        MaterialOutput response = new MaterialOutput();
        response.setId(entity.getId());
        response.setCode(entity.getCode() != null ? entity.getCode().getValue() : null);
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setShortDescription(entity.getShortDescription());
        response.setSearchKeywords(entity.getSearchKeywords());
        response.setAlternativeName(entity.getAlternativeName());
        response.setCategoryId(entity.getCategoryId());
        response.setCategoryName(entity.getCategoryName());
        response.setSupplierId(entity.getSupplierId());
        response.setSupplierName(entity.getSupplierName());
        response.setMaterialType(entity.getMaterialType() != null ? entity.getMaterialType().name() : null);
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        response.setUnitOfMeasure(entity.getUnitOfMeasure() != null ? entity.getUnitOfMeasure().name() : null);
        response.setCurrentStock(entity.getCurrentStock());
        response.setAvailableStock(entity.getAvailableStock());
        response.setMinimumStock(entity.getMinimumStock());
        response.setMaximumStock(entity.getMaximumStock());
        response.setReorderPoint(entity.getReorderPoint());
        response.setSafetyStock(entity.getSafetyStock());
        response.setEconomicOrderQuantity(entity.getEconomicOrderQuantity());
        response.setStandardPrice(entity.getStandardPrice() != null ? entity.getStandardPrice().format() : null);
        response.setCostPrice(entity.getCostPrice() != null ? entity.getCostPrice().format() : null);
        response.setLastPurchasePrice(entity.getLastPurchasePrice() != null ? entity.getLastPurchasePrice().format() : null);
        response.setAveragePurchasePrice(entity.getAveragePurchasePrice() != null ? entity.getAveragePurchasePrice().format() : null);
        response.setCurrencyCode(entity.getStandardPrice() != null ? entity.getStandardPrice().getCurrency().getCode() : null);
        response.setIsBelowMinimumStock(entity.isBelowMinimumStock());
        response.setIsReorderNeeded(entity.isBelowReorderPoint());
        response.setIsOutOfStock(entity.isOutOfStock());
        response.setObsoletedAt(entity.getObsoletedAt());
        response.setObsoletedBy(entity.getObsoletedBy());
        response.setObsoletedReason(entity.getObsoletedReason());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    /**
     * Convert list of Material entities to list of MaterialOutput
     */
    public List<MaterialOutput> toResponseList(List<Material> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
