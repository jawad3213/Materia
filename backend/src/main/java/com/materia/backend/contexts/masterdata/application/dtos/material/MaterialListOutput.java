package com.materia.backend.contexts.masterData.application.dtos.material;

import com.materia.backend.common.application.BaseOutput;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * Lightweight Response DTO for Material lists
 */
public class MaterialListOutput extends BaseOutput {

    private UUID id;
    private String code;
    private String name;
    private String shortDescription;
    private String materialType;
    private String status;
    private String unitOfMeasure;
    private Integer currentStock;
    private BigDecimal standardPrice;
    private String standardPriceCurrency;
    private String categoryId;
    private String categoryName;
    private String supplierId;
    private String description;
    private String alternativeName;
    private String searchKeywords;
    private String stockStatus;
    private Integer stockOnOrder;
    private Integer reorderPoint;
    private Integer safetyStock;
    private Boolean isBelowMinimumStock;
    private Boolean isReorderNeeded;
    private Boolean isOutOfStock;

    public MaterialListOutput() { super(); }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public BigDecimal getStandardPrice() { return standardPrice; }
    public void setStandardPrice(BigDecimal standardPrice) { this.standardPrice = standardPrice; }

    public String getStandardPriceCurrency() { return standardPriceCurrency; }
    public void setStandardPriceCurrency(String standardPriceCurrency) { this.standardPriceCurrency = standardPriceCurrency; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAlternativeName() { return alternativeName; }
    public void setAlternativeName(String alternativeName) { this.alternativeName = alternativeName; }

    public String getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(String searchKeywords) { this.searchKeywords = searchKeywords; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }

    public Integer getStockOnOrder() { return stockOnOrder; }
    public void setStockOnOrder(Integer stockOnOrder) { this.stockOnOrder = stockOnOrder; }

    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }

    public Integer getSafetyStock() { return safetyStock; }
    public void setSafetyStock(Integer safetyStock) { this.safetyStock = safetyStock; }

    public Boolean getIsBelowMinimumStock() { return isBelowMinimumStock; }
    public void setIsBelowMinimumStock(Boolean isBelowMinimumStock) { this.isBelowMinimumStock = isBelowMinimumStock; }

    public Boolean getIsReorderNeeded() { return isReorderNeeded; }
    public void setIsReorderNeeded(Boolean isReorderNeeded) { this.isReorderNeeded = isReorderNeeded; }

    public Boolean getIsOutOfStock() { return isOutOfStock; }
    public void setIsOutOfStock(Boolean isOutOfStock) { this.isOutOfStock = isOutOfStock; }
}
