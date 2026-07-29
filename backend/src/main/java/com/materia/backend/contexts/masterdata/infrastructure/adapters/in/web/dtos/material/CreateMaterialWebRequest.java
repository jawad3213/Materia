package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CreateMaterialWebRequest {
    
    @NotBlank(message = "Code is mandatory")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Name is mandatory")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "Category ID is mandatory")
    private String categoryId;
    
    @NotBlank(message = "Supplier ID is mandatory")
    private String supplierId;
    
    @NotBlank(message = "Material Type is mandatory")
    private String materialType;
    
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
    
    @NotNull(message = "Safety stock is mandatory")
    @PositiveOrZero(message = "Safety stock cannot be negative")
    private Integer safetyStock;
    
    @NotNull(message = "Standard price is mandatory")
    @PositiveOrZero(message = "Standard price cannot be negative")
    private BigDecimal standardPrice;
    
    @NotBlank(message = "Currency code is mandatory")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode;
    
    @NotBlank(message = "Created by is mandatory")
    private String createdBy;

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
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
    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }
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
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
