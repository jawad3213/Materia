package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CreateMaterialWebRequest {
    
    @NotBlank(message = "Name is mandatory")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 200, message = "Short description must not exceed 200 characters")
    private String shortDescription;

    @Size(max = 500, message = "Search keywords must not exceed 500 characters")
    private String searchKeywords;

    @Size(max = 255, message = "Alternative name must not exceed 255 characters")
    private String alternativeName;
    
    @NotBlank(message = "Category ID is mandatory")
    private String categoryId;
    
    @NotBlank(message = "Supplier is mandatory")
    private String supplierId;
    
    @NotBlank(message = "Material Type is mandatory")
    private String materialType;

    private String status;
    
    @NotBlank(message = "Unit of Measure is mandatory")
    private String unitOfMeasure;
    
    @NotNull(message = "Current stock is mandatory")
    @PositiveOrZero(message = "Current stock cannot be negative")
    private Integer currentStock;
    
    @NotNull(message = "Minimum stock is mandatory")
    @PositiveOrZero(message = "Minimum stock cannot be negative")
    private Integer minimumStock;
    
    @NotNull(message = "Maximum stock is mandatory")
    @PositiveOrZero(message = "Maximum stock cannot be negative")
    private Integer maximumStock;

    @PositiveOrZero(message = "Reorder point cannot be negative")
    private Integer reorderPoint;
    
    @NotNull(message = "Safety stock is mandatory")
    @PositiveOrZero(message = "Safety stock cannot be negative")
    private Integer safetyStock;

    @PositiveOrZero(message = "Economic order quantity cannot be negative")
    private Integer economicOrderQuantity;
    
    @NotNull(message = "Standard price is mandatory")
    @PositiveOrZero(message = "Standard price cannot be negative")
    private BigDecimal standardPrice;
    
    @NotBlank(message = "Standard price currency is mandatory")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String standardPriceCurrency;

    @PositiveOrZero(message = "Cost price cannot be negative")
    private BigDecimal costPrice;
    
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String costPriceCurrency;
    
    @NotBlank(message = "Created by is mandatory")
    private String createdBy;

    // Getters and Setters
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

    public BigDecimal getStandardPrice() { return standardPrice; }
    public void setStandardPrice(BigDecimal standardPrice) { this.standardPrice = standardPrice; }

    public String getStandardPriceCurrency() { return standardPriceCurrency; }
    public void setStandardPriceCurrency(String standardPriceCurrency) { this.standardPriceCurrency = standardPriceCurrency; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public String getCostPriceCurrency() { return costPriceCurrency; }
    public void setCostPriceCurrency(String costPriceCurrency) { this.costPriceCurrency = costPriceCurrency; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
