package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.material;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class UpdateMaterialWebRequest {
    
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters if provided")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private String categoryId;
    private String supplierId;
    private String materialType;
    private String unitOfMeasure;
    
    @PositiveOrZero(message = "Minimum stock cannot be negative")
    private Integer minimumStock;
    
    @PositiveOrZero(message = "Maximum stock cannot be negative")
    private Integer maximumStock;
    
    @PositiveOrZero(message = "Safety stock cannot be negative")
    private Integer safetyStock;
    
    @PositiveOrZero(message = "Standard price cannot be negative")
    private BigDecimal standardPrice;
    
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters if provided")
    private String currencyCode;
    
    private String status;
    private String updatedBy;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
    public Integer getMaximumStock() { return maximumStock; }
    public void setMaximumStock(Integer maximumStock) { this.maximumStock = maximumStock; }
    public Integer getSafetyStock() { return safetyStock; }
    public void setSafetyStock(Integer safetyStock) { this.safetyStock = safetyStock; }
    public BigDecimal getStandardPrice() { return standardPrice; }
    public void setStandardPrice(BigDecimal standardPrice) { this.standardPrice = standardPrice; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
