package com.materia.backend.contexts.purchaseRequisition.domain.entities;

import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;

import java.time.LocalDate;
import java.util.UUID;

public class RequisitionLine {

    private UUID id;
    private Integer lineNumber;

    private String materialCode;
    private UUID materialId;
    private String materialName;
    private String materialDescription;
    private String unitOfMeasure;
    private Money standardPrice;
    private Money unitPrice;
    private String currencyCode;

    private Integer quantity;
    private Integer quantityReceived;
    private Integer quantityRejected;

    private LocalDate requiredDate;

    private Money lineTotal;
    private String currencyCodeLine;

    private UUID supplierId;
    private String supplierName;
    private String supplierCode;

    private String notes;
    private String deliveryTerms;
    private String storageLocation;
    private String batchNumber;
    private LocalDate expiryDate;

    public RequisitionLine() {
        this.quantityReceived = 0;
        this.quantityRejected = 0;
    }

    public RequisitionLine(Material material, Integer quantity, LocalDate requiredDate) {
        this();
        if (material == null) {
            throw new IllegalArgumentException("Material is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        populateFromMaterial(material);
        this.quantity = quantity;
        this.requiredDate = requiredDate;
        this.currencyCodeLine = resolveCurrencyCode(material);
        calculateLineTotal();
    }

    public RequisitionLine(String materialCode, Integer quantity) {
        this();
        this.materialCode = materialCode;
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public UUID getMaterialId() {
        return materialId;
    }

    public void setMaterialId(UUID materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialDescription() {
        return materialDescription;
    }

    public void setMaterialDescription(String materialDescription) {
        this.materialDescription = materialDescription;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Money getStandardPrice() {
        return standardPrice;
    }

    public void setStandardPrice(Money standardPrice) {
        this.standardPrice = standardPrice;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Money unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public Integer getQuantityRejected() {
        return quantityRejected;
    }

    public void setQuantityRejected(Integer quantityRejected) {
        this.quantityRejected = quantityRejected;
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    public Money getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(Money lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getCurrencyCodeLine() {
        return currencyCodeLine;
    }

    public void setCurrencyCodeLine(String currencyCodeLine) {
        this.currencyCodeLine = currencyCodeLine;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDeliveryTerms() {
        return deliveryTerms;
    }

    public void setDeliveryTerms(String deliveryTerms) {
        this.deliveryTerms = deliveryTerms;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void populateFromMaterial(Material material) {
        if (material == null) {
            throw new IllegalArgumentException("Material is required");
        }
        this.materialId = material.getId();
        this.materialCode = material.getCode().getValue();
        this.materialName = material.getName();
        this.materialDescription = material.getDescription();
        this.unitOfMeasure = material.getUnitOfMeasure() != null
                ? material.getUnitOfMeasure().getCode()
                : null;
        this.standardPrice = material.getStandardPrice();
        this.unitPrice = material.getStandardPrice();
        this.currencyCode = resolveCurrencyCode(material);
        this.currencyCodeLine = this.currencyCode;
    }

    private String resolveCurrencyCode(Material material) {
        return material.getStandardPrice() != null
                ? material.getStandardPrice().getCurrencyCode()
                : null;
    }

    public void calculateLineTotal() {
        if (unitPrice != null && quantity != null) {
            this.lineTotal = unitPrice.multiply(quantity);
        }
    }

    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = newQuantity;
        calculateLineTotal();
    }

    public void updateUnitPrice(Money newUnitPrice) {
        if (newUnitPrice == null) {
            throw new IllegalArgumentException("Unit price is required");
        }
        if (!newUnitPrice.isPositive()) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        this.unitPrice = newUnitPrice;
        calculateLineTotal();
    }

    public void receiveQuantity(Integer received, Integer rejected) {
        if (received == null || received < 0) {
            throw new IllegalArgumentException("Received quantity cannot be negative");
        }
        if (rejected == null || rejected < 0) {
            throw new IllegalArgumentException("Rejected quantity cannot be negative");
        }
        if (received + rejected > this.quantity) {
            throw new IllegalArgumentException(
                    "Received and rejected quantities cannot exceed the total quantity"
            );
        }
        this.quantityReceived = received;
        this.quantityRejected = rejected;
    }

    public boolean isFullyReceived() {
        return quantityReceived != null && quantity != null &&
               quantityReceived >= quantity;
    }

    public boolean hasRejection() {
        return quantityRejected != null && quantityRejected > 0;
    }

    public boolean isPartiallyReceived() {
        return quantityReceived != null && quantity != null &&
               quantityReceived > 0 && quantityReceived < quantity;
    }

    public double getReceivedPercentage() {
        if (quantity == null || quantity == 0) {
            return 0.0;
        }
        int received = quantityReceived != null ? quantityReceived : 0;
        return (double) received / quantity * 100;
    }

    public double getRejectedPercentage() {
        if (quantity == null || quantity == 0) {
            return 0.0;
        }
        int rejected = quantityRejected != null ? quantityRejected : 0;
        return (double) rejected / quantity * 100;
    }

    public int getRemainingQuantity() {
        if (quantity == null) {
            return 0;
        }
        int received = quantityReceived != null ? quantityReceived : 0;
        return quantity - received;
    }

    public boolean isValid() {
        return materialCode != null &&
               !materialCode.isEmpty() &&
               quantity != null &&
               quantity > 0 &&
               unitPrice != null;
    }

    public boolean isDraft() {
        return materialCode == null || materialCode.isEmpty();
    }

    public boolean hasSpecificSupplier() {
        return supplierId != null;
    }

    public RequisitionLine copy() {
        RequisitionLine copy = new RequisitionLine();
        copy.id = this.id;
        copy.lineNumber = this.lineNumber;
        copy.materialCode = this.materialCode;
        copy.materialId = this.materialId;
        copy.materialName = this.materialName;
        copy.materialDescription = this.materialDescription;
        copy.unitOfMeasure = this.unitOfMeasure;
        copy.standardPrice = this.standardPrice;
        copy.unitPrice = this.unitPrice;
        copy.currencyCode = this.currencyCode;
        copy.quantity = this.quantity;
        copy.quantityReceived = this.quantityReceived;
        copy.quantityRejected = this.quantityRejected;
        copy.requiredDate = this.requiredDate;
        copy.lineTotal = this.lineTotal;
        copy.currencyCodeLine = this.currencyCodeLine;
        copy.supplierId = this.supplierId;
        copy.supplierName = this.supplierName;
        copy.supplierCode = this.supplierCode;
        copy.notes = this.notes;
        copy.deliveryTerms = this.deliveryTerms;
        copy.storageLocation = this.storageLocation;
        copy.batchNumber = this.batchNumber;
        copy.expiryDate = this.expiryDate;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequisitionLine that = (RequisitionLine) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "RequisitionLine{" +
                "id=" + id +
                ", lineNumber=" + lineNumber +
                ", materialCode='" + materialCode + '\'' +
                ", materialName='" + materialName + '\'' +
                ", quantity=" + quantity +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", unitPrice=" + unitPrice +
                ", lineTotal=" + lineTotal +
                ", requiredDate=" + requiredDate +
                '}';
    }
}
