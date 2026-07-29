package com.materia.backend.contexts.masterdata.domain.entities;

import com.materia.backend.contexts.masterdata.domain.enums.CategoryType;


import com.materia.backend.common.domain.BaseEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Category Domain Entity
 * Architecture Hexagonale - Couche Domaine
 *
 * CatÃ©gorie de matÃ©riaux pour organisation hiÃ©rarchique
 *
 * @author SAP MM Team
 * @version 1.0
 */
public class Category extends BaseEntity {

    // ============================================================
    // CONSTANTES
    // ============================================================

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    // ============================================================
    // ATTRIBUTS
    // ============================================================

    // ---- IDENTIFICATION ----
    private String code;                // Code unique de la catÃ©gorie (ex: CAT-001)
    private String name;                // Nom de la catÃ©gorie (ex: MatiÃ¨res premiÃ¨res)
    private String description;         // Description dÃ©taillÃ©e
    private String shortDescription;    // Description courte pour les listes

    // ---- HIÃ‰RARCHIE ----
    private String parentId;            // ID de la catÃ©gorie parente (null pour racine)
    private String parentCode;          // Code de la catÃ©gorie parente (dÃ©normalisÃ©)
    private Integer level;              // Niveau dans l'arborescence (0 = racine)
    private String path;                // Chemin hiÃ©rarchique (ex: /CAT-001/CAT-002/)
    private List<String> childrenIds;   // IDs des sous-catÃ©gories

    // ---- CLASSIFICATION ----
    private CategoryType categoryType;      // TYPE: RAW_MATERIAL, FINISHED_GOOD, etc.
    private String status;              // ACTIVE / INACTIVE
    private String color;               // Couleur pour l'affichage
    private String icon;                // IcÃ´ne pour l'interface

    // ---- STATISTIQUES ----
    private Integer materialCount;      // Nombre de matÃ©riaux dans cette catÃ©gorie
    private Integer subCategoryCount;   // Nombre de sous-catÃ©gories
    private Integer totalItems;         // Nombre total d'items (matÃ©riaux + sous-catÃ©gories)


    // ============================================================
    // CONSTRUCTEUR DÃ‰FAUT
    // ============================================================

    public Category() {
        super();
        this.status = STATUS_ACTIVE;
        this.level = 0;
        this.childrenIds = new ArrayList<>();
        this.materialCount = 0;
        this.subCategoryCount = 0;
        this.totalItems = 0;
    }

    // ============================================================
    // CONSTRUCTEUR PRIVÃ‰ (via Builder)
    // ============================================================

    private Category(Builder builder) {
        super();
        this.id = builder.id;
        this.code = builder.code;
        this.name = builder.name;
        this.description = builder.description;
        this.shortDescription = builder.shortDescription;
        this.parentId = builder.parentId;
        this.parentCode = builder.parentCode;
        this.level = builder.level;
        this.path = builder.path;
        this.childrenIds = builder.childrenIds;
        this.categoryType = builder.categoryType;
        this.status = builder.status;
        this.color = builder.color;
        this.icon = builder.icon;
        this.materialCount = builder.materialCount;
        this.subCategoryCount = builder.subCategoryCount;
        this.totalItems = builder.totalItems;

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
        private String code;
        private String name;
        private String description;
        private String shortDescription;
        private String parentId;
        private String parentCode;
        private Integer level = 0;
        private String path;
        private List<String> childrenIds = new ArrayList<>();
        private CategoryType categoryType;
        private String status = STATUS_ACTIVE;
        private String color;
        private String icon;
        private Integer materialCount = 0;
        private Integer subCategoryCount = 0;
        private Integer totalItems = 0;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // ---- IDENTIFICATION ----
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder shortDescription(String shortDescription) { this.shortDescription = shortDescription; return this; }

        // ---- HIÃ‰RARCHIE ----
        public Builder parentId(String parentId) { this.parentId = parentId; return this; }
        public Builder parentCode(String parentCode) { this.parentCode = parentCode; return this; }
        public Builder level(Integer level) { this.level = level; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder childrenIds(List<String> childrenIds) {
            this.childrenIds = childrenIds != null ? new ArrayList<>(childrenIds) : new ArrayList<>();
            return this;
        }

        // ---- CLASSIFICATION ----
        public Builder categoryType(CategoryType categoryType) { this.categoryType = categoryType; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder color(String color) { this.color = color; return this; }
        public Builder icon(String icon) { this.icon = icon; return this; }

        // ---- STATISTIQUES ----
        public Builder materialCount(Integer materialCount) { this.materialCount = materialCount; return this; }
        public Builder subCategoryCount(Integer subCategoryCount) { this.subCategoryCount = subCategoryCount; return this; }
        public Builder totalItems(Integer totalItems) { this.totalItems = totalItems; return this; }

        // ---- AUDIT ----
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Category build() {
            if (this.id == null) this.id = UUID.randomUUID();
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
            if (this.status == null) this.status = STATUS_ACTIVE;
            if (this.childrenIds == null) this.childrenIds = new ArrayList<>();
            if (this.materialCount == null) this.materialCount = 0;
            if (this.subCategoryCount == null) this.subCategoryCount = 0;
            if (this.totalItems == null) this.totalItems = 0;
            if (this.level == null) this.level = 0;

            // Calcul du chemin si parentId fourni
            if (this.parentId != null && this.path == null) {
                this.path = "/" + this.code + "/";
            }

            // Validation
            if (this.code == null || this.code.trim().isEmpty()) {
                throw new IllegalArgumentException("Le code de la catÃ©gorie est obligatoire");
            }
            if (this.name == null || this.name.trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom de la catÃ©gorie est obligatoire");
            }

            return new Category(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // ============================================================
    // MÃ‰THODES DOMAINE
    // ============================================================

    /**
     * VÃ©rifie si la catÃ©gorie est active
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(this.status);
    }

    /**
     * Active la catÃ©gorie
     */
    public void activate() {
        this.status = STATUS_ACTIVE;
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * DÃ©sactive la catÃ©gorie
     */
    public void deactivate() {
        this.status = STATUS_INACTIVE;
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * VÃ©rifie si c'est une catÃ©gorie racine
     */
    public boolean isRoot() {
        return this.parentId == null || this.parentId.isEmpty();
    }

    /**
     * VÃ©rifie si la catÃ©gorie a des sous-catÃ©gories
     */
    public boolean hasChildren() {
        return this.childrenIds != null && !this.childrenIds.isEmpty();
    }

    /**
     * Ajoute une sous-catÃ©gorie
     */
    public void addChild(String childId) {
        if (childId == null || childId.isEmpty()) {
            throw new IllegalArgumentException("L'ID de la sous-catÃ©gorie est obligatoire");
        }
        if (this.childrenIds == null) {
            this.childrenIds = new ArrayList<>();
        }
        if (!this.childrenIds.contains(childId)) {
            this.childrenIds.add(childId);
            this.subCategoryCount = this.childrenIds.size();
            this.totalItems = this.materialCount + this.subCategoryCount;
            this.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * Supprime une sous-catÃ©gorie
     */
    public void removeChild(String childId) {
        if (childId == null || childId.isEmpty()) {
            throw new IllegalArgumentException("L'ID de la sous-catÃ©gorie est obligatoire");
        }
        if (this.childrenIds != null) {
            this.childrenIds.remove(childId);
            this.subCategoryCount = this.childrenIds.size();
            this.totalItems = this.materialCount + this.subCategoryCount;
            this.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * Met Ã  jour les statistiques
     */
    public void updateStatistics(int materialCount, int subCategoryCount) {
        this.materialCount = materialCount;
        this.subCategoryCount = subCategoryCount;
        this.totalItems = materialCount + subCategoryCount;
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * IncrÃ©mente le nombre de matÃ©riaux
     */
    public void incrementMaterialCount() {
        this.materialCount = (this.materialCount == null ? 0 : this.materialCount) + 1;
        this.totalItems = this.materialCount + this.subCategoryCount;
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * DÃ©crÃ©mente le nombre de matÃ©riaux
     */
    public void decrementMaterialCount() {
        if (this.materialCount != null && this.materialCount > 0) {
            this.materialCount--;
            this.totalItems = this.materialCount + this.subCategoryCount;
            this.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * Calcule le niveau de profondeur
     */
    public int getDepth() {
        if (this.path == null || this.path.isEmpty()) {
            return 0;
        }
        return (int) this.path.chars().filter(ch -> ch == '/').count() - 1;
    }

    /**
     * Obtient le nom complet avec le chemin
     */
    public String getFullPathName() {
        if (isRoot()) {
            return this.name;
        }
        return (this.parentCode != null ? this.parentCode + " / " : "") + this.name;
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    // ---- IDENTIFICATION ----
    public String getCode() { return code; }
    public void setCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code de la catÃ©gorie est obligatoire");
        }
        this.code = code;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la catÃ©gorie est obligatoire");
        }
        this.name = name;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ---- HIÃ‰RARCHIE ----
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) {
        this.parentId = parentId;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) {
        this.level = level != null ? level : 0;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getPath() { return path; }
    public void setPath(String path) {
        this.path = path;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public List<String> getChildrenIds() { return childrenIds; }
    public void setChildrenIds(List<String> childrenIds) {
        this.childrenIds = childrenIds != null ? new ArrayList<>(childrenIds) : new ArrayList<>();
        this.subCategoryCount = this.childrenIds.size();
        this.totalItems = this.materialCount + this.subCategoryCount;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ---- CLASSIFICATION ----
    public CategoryType getCategoryType() { return categoryType; }
    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getColor() { return color; }
    public void setColor(String color) {
        this.color = color;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getIcon() { return icon; }
    public void setIcon(String icon) {
        this.icon = icon;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ---- STATISTIQUES ----
    public Integer getMaterialCount() { return materialCount; }
    public void setMaterialCount(Integer materialCount) {
        this.materialCount = materialCount != null ? materialCount : 0;
        this.totalItems = this.materialCount + this.subCategoryCount;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public Integer getSubCategoryCount() { return subCategoryCount; }
    public void setSubCategoryCount(Integer subCategoryCount) {
        this.subCategoryCount = subCategoryCount != null ? subCategoryCount : 0;
        this.totalItems = this.materialCount + this.subCategoryCount;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems != null ? totalItems : 0;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ============================================================
    // EQUALS, HASHCODE, TOSTRING
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(getId(), category.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", parentId='" + parentId + '\'' +
                ", level=" + level +
                ", status='" + status + '\'' +
                ", materialCount=" + materialCount +
                ", subCategoryCount=" + subCategoryCount +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }

    // ============================================================
    // COPY
    // ============================================================

    public Category copy() {
        return Category.builder()
                .id(this.getId())
                .code(this.code)
                .name(this.name)
                .description(this.description)
                .shortDescription(this.shortDescription)
                .parentId(this.parentId)
                .parentCode(this.parentCode)
                .level(this.level)
                .path(this.path)
                .childrenIds(this.childrenIds != null ? new ArrayList<>(this.childrenIds) : new ArrayList<>())
                .categoryType(this.categoryType)
                .status(this.status)
                .color(this.color)
                .icon(this.icon)
                .materialCount(this.materialCount)
                .subCategoryCount(this.subCategoryCount)
                .totalItems(this.totalItems)
                .createdBy(this.getCreatedBy())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }
}
