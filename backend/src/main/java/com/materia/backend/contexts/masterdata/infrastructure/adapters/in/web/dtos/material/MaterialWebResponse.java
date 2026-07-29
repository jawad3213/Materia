package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.material;

import java.time.LocalDateTime;
import java.util.UUID;

public class MaterialWebResponse {
    
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String shortDescription;
    private String searchKeywords;
    private String alternativeName;
    private String categoryId;
    private String categoryName;
    private String supplierId;
    private String supplierName;
    private String materialType;
    private String status;
    private String unitOfMeasure;
    private Integer currentStock;
    private Integer availableStock;
    private Integer minimumStock;
    private Integer maximumStock;
    private Integer reorderPoint;
    private Integer safetyStock;
    private Integer economicOrderQuantity;
    private String standardPrice;
    private String costPrice;
    private String lastPurchasePrice;
    private String averagePurchasePrice;
    private String currencyCode;
    private Boolean isBelowMinimumStock;
    private Boolean isReorderNeeded;
    private Boolean isOutOfStock;
    private LocalDateTime obsoletedAt;
    private String obsoletedBy;
    private String obsoletedReason;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
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
    public String getStandardPrice() { return standardPrice; }
    public void setStandardPrice(String standardPrice) { this.standardPrice = standardPrice; }
    public String getCostPrice() { return costPrice; }
    public void setCostPrice(String costPrice) { this.costPrice = costPrice; }
    public String getLastPurchasePrice() { return lastPurchasePrice; }
    public void setLastPurchasePrice(String lastPurchasePrice) { this.lastPurchasePrice = lastPurchasePrice; }
    public String getAveragePurchasePrice() { return averagePurchasePrice; }
    public void setAveragePurchasePrice(String averagePurchasePrice) { this.averagePurchasePrice = averagePurchasePrice; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public Boolean getIsBelowMinimumStock() { return isBelowMinimumStock; }
    public void setIsBelowMinimumStock(Boolean isBelowMinimumStock) { this.isBelowMinimumStock = isBelowMinimumStock; }
    public Boolean getIsReorderNeeded() { return isReorderNeeded; }
    public void setIsReorderNeeded(Boolean isReorderNeeded) { this.isReorderNeeded = isReorderNeeded; }
    public Boolean getIsOutOfStock() { return isOutOfStock; }
    public void setIsOutOfStock(Boolean isOutOfStock) { this.isOutOfStock = isOutOfStock; }
    public LocalDateTime getObsoletedAt() { return obsoletedAt; }
    public void setObsoletedAt(LocalDateTime obsoletedAt) { this.obsoletedAt = obsoletedAt; }
    public String getObsoletedBy() { return obsoletedBy; }
    public void setObsoletedBy(String obsoletedBy) { this.obsoletedBy = obsoletedBy; }
    public String getObsoletedReason() { return obsoletedReason; }
    public void setObsoletedReason(String obsoletedReason) { this.obsoletedReason = obsoletedReason; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
