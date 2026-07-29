package com.materia.backend.contexts.masterdata.application.dtos.material;

import com.materia.backend.common.application.BaseInput;
import java.util.UUID;

/**
 * Request DTO for updating an existing Material
 */
public class UpdateMaterialInput extends BaseInput {

    private UUID id;

    // ---- IDENTIFICATION ----
    private String name;
    private String description;
    private String shortDescription;
    private String searchKeywords;
    private String alternativeName;

    // ---- CLASSIFICATION ----
    private String categoryId;
    private String supplierId;
    private String materialType;
    private String status;

    // ---- UNITS ----
    private String unitOfMeasure;

    // ---- STOCK ----
    private Integer currentStock;
    private Integer minimumStock;
    private Integer maximumStock;
    private Integer reorderPoint;
    private Integer safetyStock;
    private Integer economicOrderQuantity;

    // ---- FINANCES ----
    private Double standardPrice;
    private Double costPrice;
    private String currencyCode;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

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

    public Double getStandardPrice() { return standardPrice; }
    public void setStandardPrice(Double standardPrice) { this.standardPrice = standardPrice; }

    public Double getCostPrice() { return costPrice; }
    public void setCostPrice(Double costPrice) { this.costPrice = costPrice; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
}
