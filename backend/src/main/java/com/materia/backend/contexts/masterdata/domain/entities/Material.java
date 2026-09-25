package com.materia.backend.contexts.masterData.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.common.domain.DomainEvent;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.enums.StockMovementType;
import com.materia.backend.contexts.masterData.domain.enums.StockStatus;
import com.materia.backend.contexts.masterData.domain.enums.UnitOfMeasure;
import com.materia.backend.contexts.masterData.domain.events.MaterialBelowReorderPointEvent;
import com.materia.backend.contexts.masterData.domain.valueObjects.MaterialCode;
import com.materia.backend.common.domain.valueObjects.Money;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
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

    // ✅ NOUVEAU : Stock en commande (pour virtual stock)
    private Integer stockOnOrder;

    // ✅ NOUVEAU : Date de dernière vérification de stock
    private LocalDateTime lastStockCheckDate;

    // ---- HISTORY & DOMAIN EVENTS ----
    private List<StockMovement> stockMovements;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public Material() {
        super();
        this.currentStock = 0;
        this.availableStock = 0;
        this.minimumStock = 10;
        this.maximumStock = 1000;
        this.reorderPoint = 20;
        this.safetyStock = 5;
        this.economicOrderQuantity = 100;
        this.stockOnOrder = 0;
        this.standardPrice = Money.zero(CurrencyCode.MAD);
        this.costPrice = Money.zero(CurrencyCode.MAD);
        this.lastPurchasePrice = Money.zero(CurrencyCode.MAD);
        this.averagePurchasePrice = Money.zero(CurrencyCode.MAD);
        this.status = MaterialStatus.ACTIVE;
        this.stockMovements = new ArrayList<>();
    }

    private Material(Builder builder) {
        super();
        this.id = builder.id;
        this.code = builder.code;
        this.name = builder.name;
        this.description = builder.description;
        this.shortDescription = builder.shortDescription;
        this.searchKeywords = builder.searchKeywords;
        this.alternativeName = builder.alternativeName;

        this.categoryId = builder.categoryId;
        this.categoryName = builder.categoryName;
        this.supplierId = builder.supplierId;
        this.supplierName = builder.supplierName;
        this.materialType = builder.materialType;
        this.status = builder.status;

        this.unitOfMeasure = builder.unitOfMeasure;

        this.currentStock = builder.currentStock;
        this.availableStock = builder.availableStock;
        this.minimumStock = builder.minimumStock;
        this.maximumStock = builder.maximumStock;
        this.reorderPoint = builder.reorderPoint;
        this.safetyStock = builder.safetyStock;
        this.economicOrderQuantity = builder.economicOrderQuantity;
        this.stockOnOrder = builder.stockOnOrder != null ? builder.stockOnOrder : 0;
        this.lastStockCheckDate = builder.lastStockCheckDate;

        this.standardPrice = builder.standardPrice;
        this.costPrice = builder.costPrice;
        this.lastPurchasePrice = builder.lastPurchasePrice;
        this.averagePurchasePrice = builder.averagePurchasePrice;

        this.obsoletedAt = builder.obsoletedAt;
        this.obsoletedBy = builder.obsoletedBy;
        this.obsoletedReason = builder.obsoletedReason;

        this.stockMovements = builder.stockMovements != null
                ? new ArrayList<>(builder.stockMovements)
                : new ArrayList<>();

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
        private UUID id;
        private MaterialCode code;
        private String name;
        private String description;
        private String shortDescription;
        private String searchKeywords;
        private String alternativeName;
        private String categoryId;
        private String categoryName;
        private String supplierId;
        private String supplierName;
        private MaterialType materialType;
        private MaterialStatus status;
        private UnitOfMeasure unitOfMeasure;
        private Integer currentStock;
        private Integer availableStock;
        private Integer minimumStock;
        private Integer maximumStock;
        private Integer reorderPoint;
        private Integer safetyStock;
        private Integer economicOrderQuantity;
        private Integer stockOnOrder = 0;
        private LocalDateTime lastStockCheckDate;
        private Money standardPrice;
        private Money costPrice;
        private Money lastPurchasePrice;
        private Money averagePurchasePrice;
        private LocalDateTime obsoletedAt;
        private String obsoletedBy;
        private String obsoletedReason;
        private List<StockMovement> stockMovements = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder code(MaterialCode code) { this.code = code; return this; }
        public Builder code(String code) {
            this.code = (code != null && !code.trim().isEmpty()) ? MaterialCode.of(code) : null;
            return this;
        }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder shortDescription(String shortDescription) { this.shortDescription = shortDescription; return this; }
        public Builder searchKeywords(String searchKeywords) { this.searchKeywords = searchKeywords; return this; }
        public Builder alternativeName(String alternativeName) { this.alternativeName = alternativeName; return this; }
        public Builder categoryId(String categoryId) { this.categoryId = categoryId; return this; }
        public Builder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public Builder supplierId(String supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        public Builder materialType(MaterialType materialType) { this.materialType = materialType; return this; }
        public Builder status(MaterialStatus status) { this.status = status; return this; }
        public Builder unitOfMeasure(UnitOfMeasure unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; return this; }
        public Builder currentStock(Integer currentStock) { this.currentStock = currentStock; return this; }
        public Builder availableStock(Integer availableStock) { this.availableStock = availableStock; return this; }
        public Builder minimumStock(Integer minimumStock) { this.minimumStock = minimumStock; return this; }
        public Builder maximumStock(Integer maximumStock) { this.maximumStock = maximumStock; return this; }
        public Builder reorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; return this; }
        public Builder safetyStock(Integer safetyStock) { this.safetyStock = safetyStock; return this; }
        public Builder economicOrderQuantity(Integer economicOrderQuantity) { this.economicOrderQuantity = economicOrderQuantity; return this; }
        public Builder stockOnOrder(Integer stockOnOrder) { this.stockOnOrder = stockOnOrder; return this; }
        public Builder lastStockCheckDate(LocalDateTime lastStockCheckDate) { this.lastStockCheckDate = lastStockCheckDate; return this; }
        public Builder standardPrice(Money standardPrice) { this.standardPrice = standardPrice; return this; }
        public Builder costPrice(Money costPrice) { this.costPrice = costPrice; return this; }
        public Builder lastPurchasePrice(Money lastPurchasePrice) { this.lastPurchasePrice = lastPurchasePrice; return this; }
        public Builder averagePurchasePrice(Money averagePurchasePrice) { this.averagePurchasePrice = averagePurchasePrice; return this; }
        public Builder obsoletedAt(LocalDateTime obsoletedAt) { this.obsoletedAt = obsoletedAt; return this; }
        public Builder obsoletedBy(String obsoletedBy) { this.obsoletedBy = obsoletedBy; return this; }
        public Builder obsoletedReason(String obsoletedReason) { this.obsoletedReason = obsoletedReason; return this; }
        public Builder stockMovements(List<StockMovement> stockMovements) {
            this.stockMovements = stockMovements != null ? new ArrayList<>(stockMovements) : new ArrayList<>();
            return this;
        }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }

        public Material build() {
            if (this.currentStock == null) this.currentStock = 0;
            if (this.availableStock == null) this.availableStock = this.currentStock;
            if (this.minimumStock == null) this.minimumStock = 10;
            if (this.maximumStock == null) this.maximumStock = 1000;
            if (this.reorderPoint == null) this.reorderPoint = 20;
            if (this.safetyStock == null) this.safetyStock = 5;
            if (this.economicOrderQuantity == null) this.economicOrderQuantity = 100;
            if (this.stockOnOrder == null) this.stockOnOrder = 0;

            if (this.standardPrice == null) this.standardPrice = Money.zero(CurrencyCode.MAD);
            if (this.costPrice == null) this.costPrice = Money.zero(CurrencyCode.MAD);
            if (this.lastPurchasePrice == null) this.lastPurchasePrice = Money.zero(CurrencyCode.MAD);
            if (this.averagePurchasePrice == null) this.averagePurchasePrice = Money.zero(CurrencyCode.MAD);
            if (this.status == null) this.status = MaterialStatus.ACTIVE;

            if (this.createdAt == null) { this.createdAt = LocalDateTime.now(); }
            if (this.updatedAt == null) { this.updatedAt = LocalDateTime.now(); }

            validateRequiredFields();
            validateStockConsistency();

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
            if (this.currentStock < 0) {
                throw new IllegalArgumentException("Current stock cannot be negative");
            }
            if (this.availableStock < 0) {
                throw new IllegalArgumentException("Available stock cannot be negative");
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // ============================================================
    // DOMAIN EVENTS HANDLING
    // ============================================================

    public void addDomainEvent(DomainEvent event) {
        if (event != null) {
            this.domainEvents.add(event);
        }
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    // ============================================================
    // MÉTHODES MÉTIER - GESTION DES STOCKS
    // ============================================================

    public boolean isBelowMinimumStock() {
        return currentStock != null && minimumStock != null && currentStock < minimumStock;
    }

    public void increaseStock(Integer quantity) {
        increaseStock(quantity, "Stock increase");
    }

    public void increaseStock(Integer quantity, String reason) {
        if (isObsolete()) {
            throw new IllegalStateException("Impossible d'augmenter le stock d'un matériau obsolète");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }
        int previousStock = this.currentStock != null ? this.currentStock : 0;
        this.currentStock = previousStock + quantity;
        this.availableStock = (this.availableStock != null ? this.availableStock : 0) + quantity;
        recordStockMovement(StockMovementType.RECEIPT, quantity, previousStock, this.currentStock,
                reason != null && !reason.isBlank() ? reason : "Stock increase");
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void decreaseStock(Integer quantity) {
        if (isObsolete()) {
            throw new IllegalStateException("Impossible de diminuer le stock d'un matériau obsolète");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }
        int available = this.currentStock != null ? this.currentStock : 0;
        if (available < quantity) {
            throw new IllegalStateException(
                    "Stock insuffisant. Disponible: " + available +
                            ", Demandé: " + quantity
            );
        }
        int previousStock = available;
        this.currentStock = available - quantity;
        this.availableStock = (this.availableStock != null ? this.availableStock : available) - quantity;
        recordStockMovement(StockMovementType.ISSUE, quantity, previousStock, this.currentStock, "Stock decrease");
        this.setUpdatedAt(LocalDateTime.now());

        // ✅ Déclencher la vérification du point de réapprovisionnement
        checkReorderPoint();
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
        checkReorderPoint();
    }

    public void recordOpeningBalance(String reason) {
        int openingStock = this.currentStock != null ? this.currentStock : 0;
        if (openingStock <= 0 || (this.stockMovements != null && !this.stockMovements.isEmpty())) {
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

    // ============================================================
    // ✅ NOUVELLES MÉTHODES - GESTION DES STOCKS
    // ============================================================

    /**
     * Vérifie si le stock est en dessous du point de réapprovisionnement
     */
    public boolean isBelowReorderPoint() {
        if (reorderPoint == null || currentStock == null) {
            return false;
        }
        return currentStock <= reorderPoint;
    }

    /**
     * Vérifie si le stock est en dessous du stock de sécurité
     */
    public boolean isBelowSafetyStock() {
        if (safetyStock == null || currentStock == null) {
            return false;
        }
        return currentStock <= safetyStock;
    }

    /**
     * Vérifie si le stock est épuisé
     */
    public boolean isOutOfStock() {
        return currentStock != null && currentStock <= 0;
    }

    /**
     * Calcule le stock virtuel (stock actuel + stock en commande)
     */
    public int getVirtualStock() {
        int current = currentStock != null ? currentStock : 0;
        int onOrder = stockOnOrder != null ? stockOnOrder : 0;
        return current + onOrder;
    }

    /**
     * Vérifie si le stock virtuel est en dessous du point de réapprovisionnement
     */
    public boolean isVirtualStockBelowReorderPoint() {
        if (reorderPoint == null) {
            return false;
        }
        return getVirtualStock() <= reorderPoint;
    }

    /**
     * Obtient le statut du stock
     */
    public StockStatus getStockStatus() {
        if (isOutOfStock()) {
            return StockStatus.OUT_OF_STOCK;
        }
        if (isBelowSafetyStock()) {
            return StockStatus.CRITICAL;
        }
        if (isBelowReorderPoint()) {
            return StockStatus.REORDER_NEEDED;
        }
        return StockStatus.IN_STOCK;
    }

    /**
     * Calcule la quantité de réapprovisionnement recommandée
     * Quantité = EOQ (Economic Order Quantity) ou maximumStock - currentStock
     */
    public int calculateReorderQuantity() {
        if (reorderPoint == null || currentStock == null) {
            return 0;
        }

        // Si on est au-dessus du point de réapprovisionnement, pas besoin de commander
        if (!isBelowReorderPoint()) {
            return 0;
        }

        // Si on a du stock en commande, ne pas commander
        if (getVirtualStock() > reorderPoint) {
            return 0;
        }

        // Quantité recommandée : EOQ ou la différence jusqu'au max
        if (economicOrderQuantity != null && economicOrderQuantity > 0) {
            return economicOrderQuantity;
        }

        if (maximumStock != null && maximumStock > 0) {
            return Math.min(maximumStock - currentStock, 500);
        }

        // Valeur par défaut
        return 100;
    }

    /**
     * Vérifie le point de réapprovisionnement et déclenche un événement si nécessaire
     */
    private void checkReorderPoint() {
        if (isBelowReorderPoint() && !isVirtualStockBelowReorderPoint()) {
            // 🔥 Ajouter un événement de réapprovisionnement
            this.addDomainEvent(new MaterialBelowReorderPointEvent(
                    this.getId(),
                    this.getCode() != null ? this.getCode().getValue() : null,
                    this.getName(),
                    this.currentStock,
                    this.reorderPoint,
                    this.safetyStock,
                    this.supplierId,
                    this.getStockStatus()
            ));
        }
    }

    /**
     * Met à jour le stock en commande (lors d'une commande)
     */
    public void addStockOnOrder(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }
        this.stockOnOrder = (this.stockOnOrder == null ? 0 : this.stockOnOrder) + quantity;
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Réduit le stock en commande (lors d'une réception)
     */
    public void reduceStockOnOrder(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }
        int currentOnOrder = this.stockOnOrder != null ? this.stockOnOrder : 0;
        if (currentOnOrder < quantity) {
            throw new IllegalStateException(
                    "Stock en commande insuffisant. Disponible: " + currentOnOrder +
                            ", Demandé: " + quantity
            );
        }
        this.stockOnOrder = currentOnOrder - quantity;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ============================================================
    // CONVENIENCE SETTERS
    // ============================================================

    public void setCode(MaterialCode code) {
        this.code = code;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void setCode(String code) {
        this.code = (code != null && !code.trim().isEmpty()) ? MaterialCode.of(code) : null;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        if (unitOfMeasure != null && !unitOfMeasure.isBlank()) {
            this.unitOfMeasure = UnitOfMeasure.fromValue(unitOfMeasure);
        } else {
            this.unitOfMeasure = null;
        }
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ============================================================
    // VERIFICATION METHODS
    // ============================================================

    public boolean isActive() { return MaterialStatus.ACTIVE.equals(this.status); }
    public boolean isObsolete() { return MaterialStatus.OBSOLETE.equals(this.status); }
    public boolean isOrderable() { return isActive() && !isObsolete(); }

    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return Objects.equals(getId(), material.getId()) ||
                Objects.equals(code, material.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), code);
    }
}
