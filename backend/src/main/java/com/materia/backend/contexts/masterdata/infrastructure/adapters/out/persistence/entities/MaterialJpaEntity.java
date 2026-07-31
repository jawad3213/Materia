package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import com.materia.backend.contexts.masterdata.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterdata.domain.enums.MaterialType;
import com.materia.backend.contexts.masterdata.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterdata.domain.enums.UnitOfMeasure;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for Material
 * Infrastructure Layer - Maps to the 'materials' database table
 */
@Entity
@Table(name = "materials", indexes = {
        @Index(name = "idx_material_code", columnList = "code", unique = true),
        @Index(name = "idx_material_category_id", columnList = "category_id"),
        @Index(name = "idx_material_supplier_id", columnList = "supplier_id"),
        @Index(name = "idx_material_status", columnList = "status")
})
public class MaterialJpaEntity extends BaseJpaEntity {

    // ---- IDENTIFICATION ----
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "short_description", length = 200)
    private String shortDescription;

    @Column(name = "search_keywords", length = 500)
    private String searchKeywords;

    @Column(name = "alternative_name", length = 255)
    private String alternativeName;

    // ---- CLASSIFICATION ----
    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "supplier_id")
    private String supplierId;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 30)
    private MaterialType materialType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MaterialStatus status;

    // ---- UNITS ----
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", length = 20)
    private UnitOfMeasure unitOfMeasure;

    // ---- STOCK ----
    @Column(name = "current_stock")
    private Integer currentStock;

    @Column(name = "available_stock")
    private Integer availableStock;

    @Column(name = "minimum_stock")
    private Integer minimumStock;

    @Column(name = "maximum_stock")
    private Integer maximumStock;

    @Column(name = "reorder_point")
    private Integer reorderPoint;

    @Column(name = "safety_stock")
    private Integer safetyStock;

    @Column(name = "economic_order_quantity")
    private Integer economicOrderQuantity;

    // ---- FINANCES (flattened from Money value object) ----
    @Column(name = "standard_price", precision = 19, scale = 4)
    private BigDecimal standardPrice;

    // ---- CALCULATED (NOT IN DOMAIN) ----
    @Enumerated(EnumType.STRING)
    @Column(name = "standard_price_currency", length = 10)
    private CurrencyCode standardPriceCurrency;

    @Column(name = "cost_price", precision = 19, scale = 4)
    private BigDecimal costPrice;

    // ---- CALCULATED (NOT IN DOMAIN) ----
    @Enumerated(EnumType.STRING)
    @Column(name = "cost_price_currency", length = 10)
    private CurrencyCode costPriceCurrency;

    @Column(name = "last_purchase_price", precision = 19, scale = 4)
    private BigDecimal lastPurchasePrice;

    // ---- CALCULATED (NOT IN DOMAIN) ----
    @Enumerated(EnumType.STRING)
    @Column(name = "last_purchase_price_currency", length = 10)
    private CurrencyCode lastPurchasePriceCurrency;

    @Column(name = "average_purchase_price", precision = 19, scale = 4)
    private BigDecimal averagePurchasePrice;

    // ---- CALCULATED (NOT IN DOMAIN) ----
    @Enumerated(EnumType.STRING)
    @Column(name = "average_purchase_price_currency", length = 10)
    private CurrencyCode averagePurchasePriceCurrency;

    // ---- OBSOLESCENCE ----
    @Column(name = "obsoleted_at")
    private LocalDateTime obsoletedAt;

    @Column(name = "obsoleted_by", length = 100)
    private String obsoletedBy;

    @Column(name = "obsoleted_reason", length = 500)
    private String obsoletedReason;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public MaterialJpaEntity() {
        super();
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(String searchKeywords) { this.searchKeywords = searchKeywords; }

    public String getAlternativeName() { return alternativeName; }
    public void setAlternativeName(String alternativeName) { this.alternativeName = alternativeName; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public MaterialType getMaterialType() { return materialType; }
    public void setMaterialType(MaterialType materialType) { this.materialType = materialType; }

    public MaterialStatus getStatus() { return status; }
    public void setStatus(MaterialStatus status) { this.status = status; }

    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }

    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }

    public Integer getMaximumStock() { return maximumStock; }
    public void setMaximumStock(Integer maximumStock) { this.maximumStock = maximumStock; }

    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }

    public Integer getSafetyStock() { return safetyStock; }
    public void setSafetyStock(Integer safetyStock) { this.safetyStock = safetyStock; }

    public Integer getEconomicOrderQuantity() { return economicOrderQuantity; }
    public void setEconomicOrderQuantity(Integer economicOrderQuantity) { this.economicOrderQuantity = economicOrderQuantity; }

    public BigDecimal getStandardPrice() { return standardPrice; }
    public void setStandardPrice(BigDecimal standardPrice) { this.standardPrice = standardPrice; }

    public CurrencyCode getStandardPriceCurrency() { return standardPriceCurrency; }
    public void setStandardPriceCurrency(CurrencyCode standardPriceCurrency) { this.standardPriceCurrency = standardPriceCurrency; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public CurrencyCode getCostPriceCurrency() { return costPriceCurrency; }
    public void setCostPriceCurrency(CurrencyCode costPriceCurrency) { this.costPriceCurrency = costPriceCurrency; }

    public BigDecimal getLastPurchasePrice() { return lastPurchasePrice; }
    public void setLastPurchasePrice(BigDecimal lastPurchasePrice) { this.lastPurchasePrice = lastPurchasePrice; }

    public CurrencyCode getLastPurchasePriceCurrency() { return lastPurchasePriceCurrency; }
    public void setLastPurchasePriceCurrency(CurrencyCode lastPurchasePriceCurrency) { this.lastPurchasePriceCurrency = lastPurchasePriceCurrency; }

    public BigDecimal getAveragePurchasePrice() { return averagePurchasePrice; }
    public void setAveragePurchasePrice(BigDecimal averagePurchasePrice) { this.averagePurchasePrice = averagePurchasePrice; }

    public CurrencyCode getAveragePurchasePriceCurrency() { return averagePurchasePriceCurrency; }
    public void setAveragePurchasePriceCurrency(CurrencyCode averagePurchasePriceCurrency) { this.averagePurchasePriceCurrency = averagePurchasePriceCurrency; }

    public LocalDateTime getObsoletedAt() { return obsoletedAt; }
    public void setObsoletedAt(LocalDateTime obsoletedAt) { this.obsoletedAt = obsoletedAt; }

    public String getObsoletedBy() { return obsoletedBy; }
    public void setObsoletedBy(String obsoletedBy) { this.obsoletedBy = obsoletedBy; }

    public String getObsoletedReason() { return obsoletedReason; }
    public void setObsoletedReason(String obsoletedReason) { this.obsoletedReason = obsoletedReason; }
}
