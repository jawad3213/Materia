package com.materia.backend.contexts.masterData.domain.entities;

import com.materia.backend.contexts.masterData.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.StockMovementType;
import com.materia.backend.contexts.masterData.domain.enums.UnitOfMeasure;
import com.materia.backend.contexts.masterData.domain.valueObjects.MaterialCode;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;

import com.materia.backend.common.domain.BaseEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Material extends BaseEntity {

    // ============================================================
    // SPECIFIC ATTRIBUTES
    // ============================================================

    // ---- IDENTIFICATION ----
    private MaterialCode code;
    private String name;
    private String description;
    private String shortDescription;
    private String searchKeywords;
    private String alternativeName;

    // ---- CLASSIFICATION ----
    private String categoryId;
    private String categoryName;
    private String supplierId;
    private String supplierName;
    private MaterialType materialType;
    private MaterialStatus status;

    // ---- UNITS ----
    private UnitOfMeasure unitOfMeasure;

    // ---- STOCK ----
    private Integer currentStock;
    private Integer availableStock;
    private Integer minimumStock;
    private Integer maximumStock;
    private Integer reorderPoint;
    private Integer safetyStock;
    private Integer economicOrderQuantity;

    // ---- FINANCES ----
    private Money standardPrice;
    private Money costPrice;
    private Money lastPurchasePrice;
    private Money averagePurchasePrice;

    // ---- OBSOLESCENCE ----
    private LocalDateTime obsoletedAt;
    private String obsoletedBy;
    private String obsoletedReason;

    // ---- HISTORY ----
    private List<StockMovement> stockMovements;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    /**
     * Default constructor (for JPA / serialization)
     */
    public Material() {
        super();
        this.currentStock = 0;
        this.availableStock = 0;
        this.minimumStock = 10;
        this.maximumStock = 1000;
        this.reorderPoint = 20;
        this.safetyStock = 5;
        this.economicOrderQuantity = 100;
        this.standardPrice = Money.zero(CurrencyCode.MAD);
        this.costPrice = Money.zero(CurrencyCode.MAD);
        this.lastPurchasePrice = Money.zero(CurrencyCode.MAD);
        this.averagePurchasePrice = Money.zero(CurrencyCode.MAD);
        this.status = MaterialStatus.ACTIVE;
        this.stockMovements = new ArrayList<>();
    }

    /**
     * Private constructor (via Builder)
     */
    private Material(Builder builder) {
        super();

        // ---- IDENTIFICATION ----
        this.id = builder.id;
        this.code = builder.code;
        this.name = builder.name;
        this.description = builder.description;
        this.shortDescription = builder.shortDescription;
        this.searchKeywords = builder.searchKeywords;
        this.alternativeName = builder.alternativeName;

        // ---- CLASSIFICATION ----
        this.categoryId = builder.categoryId;
        this.categoryName = builder.categoryName;
        this.supplierId = builder.supplierId;
        this.supplierName = builder.supplierName;
        this.materialType = builder.materialType;
        this.status = builder.status;

        // ---- UNITS ----
        this.unitOfMeasure = builder.unitOfMeasure;

        // ---- STOCK ----
        this.currentStock = builder.currentStock;
        this.availableStock = builder.availableStock;
        this.minimumStock = builder.minimumStock;
        this.maximumStock = builder.maximumStock;
        this.reorderPoint = builder.reorderPoint;
        this.safetyStock = builder.safetyStock;
        this.economicOrderQuantity = builder.economicOrderQuantity;

        // ---- FINANCES ----
        this.standardPrice = builder.standardPrice;
        this.costPrice = builder.costPrice;
        this.lastPurchasePrice = builder.lastPurchasePrice;
        this.averagePurchasePrice = builder.averagePurchasePrice;

        // ---- OBSOLESCENCE ----
        this.obsoletedAt = builder.obsoletedAt;
        this.obsoletedBy = builder.obsoletedBy;
        this.obsoletedReason = builder.obsoletedReason;

        // ---- HISTORY ----
        this.stockMovements = builder.stockMovements != null
                ? new ArrayList<>(builder.stockMovements)
                : new ArrayList<>();

        // ---- AUDIT ----
        if (builder.createdAt != null) {
            this.setCreatedAt(builder.createdAt);
        }
        if (builder.updatedAt != null) {
            this.setUpdatedAt(builder.updatedAt);
        }
        if (builder.createdBy != null) {
            this.setCreatedBy(builder.createdBy);
            this.setUpdatedBy(builder.createdBy);
        }
    }

    // ============================================================
    // BUILDER PATTERN
    // ============================================================

    public static class Builder {
        // ---- IDENTIFICATION ----
        private UUID id;
        private MaterialCode code;
        private String name;
        private String description;
        private String shortDescription;
        private String searchKeywords;
        private String alternativeName;

        // ---- CLASSIFICATION ----
        private String categoryId;
        private String categoryName;
        private String supplierId;
        private String supplierName;
        private MaterialType materialType;
        private MaterialStatus status = MaterialStatus.ACTIVE;

        // ---- UNITS ----
        private UnitOfMeasure unitOfMeasure;

        // ---- STOCK ----
        private Integer currentStock = 0;
        private Integer availableStock;
        private Integer minimumStock = 10;
        private Integer maximumStock = 1000;
        private Integer reorderPoint = 20;
        private Integer safetyStock = 5;
        private Integer economicOrderQuantity = 100;

        // ---- FINANCES ----
        private Money standardPrice = Money.zero(CurrencyCode.MAD);
        private Money costPrice = Money.zero(CurrencyCode.MAD);
        private Money lastPurchasePrice = Money.zero(CurrencyCode.MAD);
        private Money averagePurchasePrice = Money.zero(CurrencyCode.MAD);

        // ---- OBSOLESCENCE ----
        private LocalDateTime obsoletedAt;
        private String obsoletedBy;
        private String obsoletedReason;

        // ---- HISTORY ----
        private List<StockMovement> stockMovements = new ArrayList<>();

        // ---- AUDIT ----
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // ============================================================
        // BUILDERS - IDENTIFICATION
        // ============================================================

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder code(MaterialCode code) { this.code = code; return this; }
        public Builder code(String code) {
            this.code = code != null && !code.trim().isEmpty() ? MaterialCode.of(code) : null;
            return this;
        }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder shortDescription(String shortDescription) { this.shortDescription = shortDescription; return this; }
        public Builder searchKeywords(String searchKeywords) { this.searchKeywords = searchKeywords; return this; }
        public Builder alternativeName(String alternativeName) { this.alternativeName = alternativeName; return this; }

        // ============================================================
        // BUILDERS - CLASSIFICATION
        // ============================================================

        public Builder categoryId(String categoryId) { this.categoryId = categoryId; return this; }
        public Builder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public Builder supplierId(String supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        public Builder materialType(MaterialType materialType) { this.materialType = materialType; return this; }
        public Builder status(MaterialStatus status) {
            this.status = status != null ? status : MaterialStatus.ACTIVE;
            return this;
        }

        // ============================================================
        // BUILDERS - UNITS
        // ============================================================

        public Builder unitOfMeasure(UnitOfMeasure unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; return this; }

        // ============================================================
        // BUILDERS - STOCK
        // ============================================================

        public Builder currentStock(Integer currentStock) { this.currentStock = currentStock != null ? currentStock : 0; return this; }
        public Builder availableStock(Integer availableStock) { this.availableStock = availableStock; return this; }
        public Builder minimumStock(Integer minimumStock) { this.minimumStock = minimumStock != null ? minimumStock : 10; return this; }
        public Builder maximumStock(Integer maximumStock) { this.maximumStock = maximumStock != null ? maximumStock : 1000; return this; }
        public Builder reorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint != null ? reorderPoint : 20; return this; }
        public Builder safetyStock(Integer safetyStock) { this.safetyStock = safetyStock != null ? safetyStock : 5; return this; }
        public Builder economicOrderQuantity(Integer economicOrderQuantity) { this.economicOrderQuantity = economicOrderQuantity != null ? economicOrderQuantity : 100; return this; }

        // ============================================================
        // BUILDERS - FINANCES
        // ============================================================

        public Builder standardPrice(Money standardPrice) { this.standardPrice = standardPrice != null ? standardPrice : Money.zero(CurrencyCode.MAD); return this; }
        public Builder costPrice(Money costPrice) { this.costPrice = costPrice != null ? costPrice : Money.zero(CurrencyCode.MAD); return this; }
        public Builder lastPurchasePrice(Money lastPurchasePrice) { this.lastPurchasePrice = lastPurchasePrice != null ? lastPurchasePrice : Money.zero(CurrencyCode.MAD); return this; }
        public Builder averagePurchasePrice(Money averagePurchasePrice) { this.averagePurchasePrice = averagePurchasePrice != null ? averagePurchasePrice : Money.zero(CurrencyCode.MAD); return this; }

        // ============================================================
        // BUILDERS - OBSOLESCENCE
        // ============================================================

        public Builder obsoletedAt(LocalDateTime obsoletedAt) { this.obsoletedAt = obsoletedAt; return this; }
        public Builder obsoletedBy(String obsoletedBy) { this.obsoletedBy = obsoletedBy; return this; }
        public Builder obsoletedReason(String obsoletedReason) { this.obsoletedReason = obsoletedReason; return this; }
        public Builder stockMovements(List<StockMovement> stockMovements) {
            this.stockMovements = stockMovements != null ? new ArrayList<>(stockMovements) : new ArrayList<>();
            return this;
        }

        // ============================================================
        // BUILDERS - AUDIT
        // ============================================================

        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        // ============================================================
        // BUILD
        // ============================================================

        public Material build() {
            // ---- ID GENERATION ----
            if (this.id == null) { this.id = UUID.randomUUID(); }

            // ---- DEFAULT VALUES ----
            if (this.currentStock == null) this.currentStock = 0;
            if (this.availableStock == null) this.availableStock = this.currentStock;
            if (this.minimumStock == null) this.minimumStock = 10;
            if (this.maximumStock == null) this.maximumStock = 1000;
            if (this.reorderPoint == null) this.reorderPoint = 20;
            if (this.safetyStock == null) this.safetyStock = 5;
            if (this.economicOrderQuantity == null) this.economicOrderQuantity = 100;
            if (this.standardPrice == null) this.standardPrice = Money.zero(CurrencyCode.MAD);
            if (this.costPrice == null) this.costPrice = Money.zero(CurrencyCode.MAD);
            if (this.lastPurchasePrice == null) this.lastPurchasePrice = Money.zero(CurrencyCode.MAD);
            if (this.averagePurchasePrice == null) this.averagePurchasePrice = Money.zero(CurrencyCode.MAD);
            if (this.status == null) this.status = MaterialStatus.ACTIVE;

            // ---- DATES ----
            if (this.createdAt == null) { this.createdAt = LocalDateTime.now(); }
            if (this.updatedAt == null) { this.updatedAt = LocalDateTime.now(); }

            // ---- VALIDATIONS ----
            validateRequiredFields();
            validateStockConsistency();

            // ---- OBSOLESCENCE ----
            if (MaterialStatus.OBSOLETE.equals(this.status)) {
                if (this.obsoletedAt == null) { this.obsoletedAt = LocalDateTime.now(); }
                if (this.obsoletedBy == null || this.obsoletedBy.trim().isEmpty()) {
                    throw new IllegalArgumentException("The user who marked this material as obsolete is required");
                }
                if (this.obsoletedReason == null || this.obsoletedReason.trim().isEmpty()) {
                    throw new IllegalArgumentException("The reason for obsolescence is required");
                }
            }

            return new Material(this);
        }

        private void validateRequiredFields() {
            if (this.code == null) {
                throw new IllegalArgumentException("Material code is required");
            }
            if (this.name == null || this.name.trim().isEmpty()) {
                throw new IllegalArgumentException("Material name is required");
            }
            if (this.materialType == null) {
                throw new IllegalArgumentException("Material type is required");
            }
            if (this.categoryId == null || this.categoryId.trim().isEmpty()) {
                throw new IllegalArgumentException("Category is required");
            }
            if (this.unitOfMeasure == null) {
                throw new IllegalArgumentException("Unit of measure is required");
            }
        }

        private void validateStockConsistency() {
            if (this.minimumStock > this.maximumStock) {
                throw new IllegalArgumentException(
                        "Minimum stock (" + this.minimumStock +
                                ") cannot be greater than maximum stock (" + this.maximumStock + ")"
                );
            }
            if (this.reorderPoint > this.minimumStock) {
                throw new IllegalArgumentException(
                        "Reorder point (" + this.reorderPoint +
                                ") cannot be greater than minimum stock (" + this.minimumStock + ")"
                );
            }
            if (this.safetyStock > this.minimumStock) {
                throw new IllegalArgumentException(
                        "Safety stock (" + this.safetyStock +
                                ") cannot be greater than minimum stock (" + this.minimumStock + ")"
                );
            }
            if (this.currentStock < 0) {
                throw new IllegalArgumentException("Current stock cannot be negative");
            }
            if (this.availableStock < 0) {
                throw new IllegalArgumentException("Available stock cannot be negative");
            }
            if (this.availableStock > this.currentStock) {
                throw new IllegalArgumentException(
                        "Available stock (" + this.availableStock +
                                ") cannot be greater than current stock (" + this.currentStock + ")"
                );
            }
        }
    }

    /**
     * Entry point to create a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    // ============================================================
    // DOMAIN METHODS (Business Behavior)
    // ============================================================

    /**
     * Calculates the available stock (currentStock - reservedStock)
     */
    public void calculateAvailableStock() {
        this.availableStock = this.currentStock;
    }

    /**
     * Checks if stock is below the alert threshold
     */
    public boolean isBelowMinimumStock() {
        return currentStock < minimumStock;
    }

    /**
     * Checks if stock is below the reorder point
     */
    public boolean isBelowReorderPoint() {
        return currentStock < reorderPoint;
    }

    /**
     * Checks if stock exceeds the maximum
     */
    public boolean isAboveMaximumStock() {
        return currentStock > maximumStock;
    }

    /**
     * Increases stock (e.g. after receipt)
     */
    public void increaseStock(Integer quantity) {
        if (isObsolete()) {
            throw new IllegalStateException("Cannot increase stock of an obsolete material");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        int previousStock = this.currentStock != null ? this.currentStock : 0;
        this.currentStock += quantity;
        this.availableStock += quantity;
        recordStockMovement(StockMovementType.RECEIPT, quantity, previousStock, this.currentStock, "Stock increase");
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Decreases stock (e.g. after issue)
     */
    public void decreaseStock(Integer quantity) {
        if (isObsolete()) {
            throw new IllegalStateException("Cannot decrease stock of an obsolete material");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (this.currentStock < quantity) {
            throw new IllegalStateException("Insufficient stock. Available: " + this.currentStock);
        }
        int previousStock = this.currentStock;
        this.currentStock -= quantity;
        this.availableStock -= quantity;
        recordStockMovement(StockMovementType.ISSUE, quantity, previousStock, this.currentStock, "Stock decrease");
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void adjustStock(Integer newCurrentStock, String reason) {
        if (isObsolete()) {
            throw new IllegalStateException("Cannot modify stock of an obsolete material");
        }
        if (newCurrentStock == null || newCurrentStock < 0) {
            throw new IllegalArgumentException("Current stock cannot be negative");
        }

        int previousStock = this.currentStock != null ? this.currentStock : 0;
        this.currentStock = newCurrentStock;
        this.availableStock = newCurrentStock;

        if (previousStock != newCurrentStock) {
            recordStockMovement(
                    StockMovementType.ADJUSTMENT,
                    Math.abs(newCurrentStock - previousStock),
                    previousStock,
                    newCurrentStock,
                    reason != null && !reason.trim().isEmpty() ? reason : "Manual stock adjustment"
            );
        }

        this.setUpdatedAt(LocalDateTime.now());
    }

    public void recordOpeningBalance(String reason) {
        int openingStock = this.currentStock != null ? this.currentStock : 0;
        if (openingStock <= 0 || !this.stockMovements.isEmpty()) {
            return;
        }

        recordStockMovement(
                StockMovementType.OPENING_BALANCE,
                openingStock,
                0,
                openingStock,
                reason != null && !reason.trim().isEmpty() ? reason : "Initial stock"
        );
    }

    private void recordStockMovement(StockMovementType type,
                                     Integer quantity,
                                     Integer previousStock,
                                     Integer newStock,
                                     String reason) {
        if (this.stockMovements == null) {
            this.stockMovements = new ArrayList<>();
        }

        this.stockMovements.add(0, StockMovement.create(type, quantity, previousStock, newStock, reason));
    }

    /**
     * Calculates the replenishment need
     */
    public int calculateReorderQuantity() {
        if (isObsolete()) { return 0; }
        if (currentStock < reorderPoint) {
            return reorderPoint - currentStock + safetyStock;
        }
        return 0;
    }

    /**
     * Calculates the stock turnover rate
     */
    public double calculateTurnoverRate(int annualConsumption) {
        if (currentStock == 0) { return 0; }
        return (double) annualConsumption / currentStock;
    }

    /**
     * Formats the standard price with the currency symbol
     */
    public String getFormattedPrice() {
        if (standardPrice == null) { return "0.00"; }
        return standardPrice.format();
    }

    /**
     * Formats the standard price with the currency code
     */
    public String getFormattedPriceWithCode() {
        if (standardPrice == null) { return "0.00"; }
        return standardPrice.formatWithCode();
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    // ---- IDENTIFICATION ----
    public MaterialCode getCode() { return code; }
    public void setCode(MaterialCode code) {
        if (code == null) { throw new IllegalArgumentException("Material code is required"); }
        this.code = code;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void setCode(String code) {
        if (code == null || code.trim().isEmpty()) { throw new IllegalArgumentException("Material code is required"); }
        this.code = MaterialCode.of(code);
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) { throw new IllegalArgumentException("Material name is required"); }
        this.name = name;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; this.setUpdatedAt(LocalDateTime.now()); }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; this.setUpdatedAt(LocalDateTime.now()); }

    public String getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(String searchKeywords) { this.searchKeywords = searchKeywords; this.setUpdatedAt(LocalDateTime.now()); }

    public String getAlternativeName() { return alternativeName; }
    public void setAlternativeName(String alternativeName) { this.alternativeName = alternativeName; this.setUpdatedAt(LocalDateTime.now()); }

    // ---- CLASSIFICATION ----
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) { throw new IllegalArgumentException("Category is required"); }
        this.categoryId = categoryId;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; this.setUpdatedAt(LocalDateTime.now()); }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; this.setUpdatedAt(LocalDateTime.now()); }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; this.setUpdatedAt(LocalDateTime.now()); }

    public MaterialType getMaterialType() { return materialType; }
    public void setMaterialType(MaterialType materialType) {
        if (materialType == null) { throw new IllegalArgumentException("Material type is required"); }
        this.materialType = materialType;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public MaterialStatus getStatus() { return status; }
    public void setStatus(MaterialStatus status) {
        if (status == null) { throw new IllegalArgumentException("Status is required"); }
        this.status = status;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ---- UNITS ----
    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        if (unitOfMeasure == null) { throw new IllegalArgumentException("Unit of measure is required"); }
        this.unitOfMeasure = unitOfMeasure;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ---- STOCK ----
    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) {
        if (isObsolete()) { throw new IllegalStateException("Cannot modify stock of an obsolete material"); }
        this.currentStock = currentStock != null ? currentStock : 0;
        this.availableStock = this.currentStock;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock != null ? availableStock : 0; this.setUpdatedAt(LocalDateTime.now()); }

    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock != null ? minimumStock : 10; this.setUpdatedAt(LocalDateTime.now()); }

    public Integer getMaximumStock() { return maximumStock; }
    public void setMaximumStock(Integer maximumStock) { this.maximumStock = maximumStock != null ? maximumStock : 1000; this.setUpdatedAt(LocalDateTime.now()); }

    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint != null ? reorderPoint : 20; this.setUpdatedAt(LocalDateTime.now()); }

    public Integer getSafetyStock() { return safetyStock; }
    public void setSafetyStock(Integer safetyStock) { this.safetyStock = safetyStock != null ? safetyStock : 5; this.setUpdatedAt(LocalDateTime.now()); }

    public Integer getEconomicOrderQuantity() { return economicOrderQuantity; }
    public void setEconomicOrderQuantity(Integer economicOrderQuantity) { this.economicOrderQuantity = economicOrderQuantity != null ? economicOrderQuantity : 100; this.setUpdatedAt(LocalDateTime.now()); }

    // ---- FINANCES ----
    public Money getStandardPrice() { return standardPrice; }
    public void setStandardPrice(Money standardPrice) { this.standardPrice = standardPrice != null ? standardPrice : Money.zero(CurrencyCode.MAD); this.setUpdatedAt(LocalDateTime.now()); }

    public Money getCostPrice() { return costPrice; }
    public void setCostPrice(Money costPrice) { this.costPrice = costPrice != null ? costPrice : Money.zero(CurrencyCode.MAD); this.setUpdatedAt(LocalDateTime.now()); }

    public Money getLastPurchasePrice() { return lastPurchasePrice; }
    public void setLastPurchasePrice(Money lastPurchasePrice) { this.lastPurchasePrice = lastPurchasePrice != null ? lastPurchasePrice : Money.zero(CurrencyCode.MAD); this.setUpdatedAt(LocalDateTime.now()); }

    public Money getAveragePurchasePrice() { return averagePurchasePrice; }
    public void setAveragePurchasePrice(Money averagePurchasePrice) { this.averagePurchasePrice = averagePurchasePrice != null ? averagePurchasePrice : Money.zero(CurrencyCode.MAD); this.setUpdatedAt(LocalDateTime.now()); }

    // ---- OBSOLESCENCE ----
    public LocalDateTime getObsoletedAt() { return obsoletedAt; }
    public void setObsoletedAt(LocalDateTime obsoletedAt) { this.obsoletedAt = obsoletedAt; this.setUpdatedAt(LocalDateTime.now()); }

    public String getObsoletedBy() { return obsoletedBy; }
    public void setObsoletedBy(String obsoletedBy) { this.obsoletedBy = obsoletedBy; this.setUpdatedAt(LocalDateTime.now()); }

    public String getObsoletedReason() { return obsoletedReason; }
    public void setObsoletedReason(String obsoletedReason) { this.obsoletedReason = obsoletedReason; this.setUpdatedAt(LocalDateTime.now()); }

    public List<StockMovement> getStockMovements() { return stockMovements; }
    public void setStockMovements(List<StockMovement> stockMovements) {
        this.stockMovements = stockMovements != null ? new ArrayList<>(stockMovements) : new ArrayList<>();
    }

    // ============================================================
    // VERIFICATION METHODS
    // ============================================================

    public boolean isActive() { return MaterialStatus.ACTIVE.equals(this.status); }
    public boolean isObsolete() { return MaterialStatus.OBSOLETE.equals(this.status); }
    public boolean isOrderable() { return isActive() && !isObsolete(); }
    public boolean isOutOfStock() { return this.currentStock <= 0; }

    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return Objects.equals(getId(), material.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
