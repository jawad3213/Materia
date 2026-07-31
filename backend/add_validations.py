import os

src_root = r'c:\Users\elhai\OneDrive\Desktop\materia\backend\src\main\java\com\materia\backend\contexts\masterdata\infrastructure\adapters\in\web\dtos'

category_create = '''package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateCategoryWebRequest {
    
    @NotBlank(message = "Code is mandatory")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Name is mandatory")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 200, message = "Short description must not exceed 200 characters")
    private String shortDescription;
    
    private String parentId;
    
    @NotBlank(message = "Category Type is mandatory")
    private String categoryType;
    
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid hex code")
    private String color;
    
    private String icon;
    
    @NotBlank(message = "Created by is mandatory")
    private String createdBy;

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getCategoryType() { return categoryType; }
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
'''

category_update = '''package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.category;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateCategoryWebRequest {
    
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters if provided")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 200, message = "Short description must not exceed 200 characters")
    private String shortDescription;
    
    private String parentId;
    private String categoryType;
    private String status;
    
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid hex code")
    private String color;
    
    private String icon;
    private String updatedBy;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getCategoryType() { return categoryType; }
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
'''

supplier_create = '''package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateSupplierWebRequest {
    
    @NotBlank(message = "Code is mandatory")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Name is mandatory")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Contact person is mandatory")
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;
    
    @NotBlank(message = "Contact email is mandatory")
    @Email(message = "Contact email must be valid")
    private String contactEmail;
    
    @NotBlank(message = "Contact phone is mandatory")
    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;
    
    @NotBlank(message = "Address is mandatory")
    private String address;
    
    @NotBlank(message = "City is mandatory")
    private String city;
    
    @NotBlank(message = "Country is mandatory")
    private String country;
    
    @NotBlank(message = "Payment terms are mandatory")
    private String paymentTerms;
    
    @NotBlank(message = "Currency code is mandatory")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode;
    
    private String taxId;
    private String website;
    
    @NotBlank(message = "Created by is mandatory")
    private String createdBy;

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
'''

supplier_update = '''package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateSupplierWebRequest {
    
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters if provided")
    private String name;
    
    @Size(min = 1, max = 100, message = "Contact person must be between 1 and 100 characters if provided")
    private String contactPerson;
    
    @Email(message = "Contact email must be valid if provided")
    private String contactEmail;
    
    @Size(min = 1, max = 20, message = "Contact phone must be between 1 and 20 characters if provided")
    private String contactPhone;
    
    private String address;
    private String city;
    private String country;
    private String paymentTerms;
    
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters if provided")
    private String currencyCode;
    
    private String taxId;
    private String website;
    private String status;
    private String updatedBy;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
'''

material_create = '''package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.material;

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
'''

material_update = '''package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.material;

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
'''

files = {
    os.path.join(src_root, 'category', 'CreateCategoryWebRequest.java'): category_create,
    os.path.join(src_root, 'category', 'UpdateCategoryWebRequest.java'): category_update,
    os.path.join(src_root, 'supplier', 'CreateSupplierWebRequest.java'): supplier_create,
    os.path.join(src_root, 'supplier', 'UpdateSupplierWebRequest.java'): supplier_update,
    os.path.join(src_root, 'material', 'CreateMaterialWebRequest.java'): material_create,
    os.path.join(src_root, 'material', 'UpdateMaterialWebRequest.java'): material_update
}

for filepath, content in files.items():
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
        print(f"Updated {os.path.basename(filepath)}")
