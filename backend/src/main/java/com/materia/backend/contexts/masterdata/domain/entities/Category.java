package com.materia.backend.contexts.masterdata.domain.entities;

import com.materia.backend.contexts.masterdata.domain.enums.MaterialCategoryType;

import com.materia.backend.common.domain.BaseEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Category Domain Entity
 * Hexagonal Architecture - Domain Layer
 *
 * Material category for hierarchical organization
 *
 * @author SAP MM Team
 * @version 1.0
 */
public class Category extends BaseEntity {

    // ============================================================
    // CONSTANTS
    // ============================================================

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    // ============================================================
    // ATTRIBUTES
    // ============================================================

    // ---- IDENTIFICATION ----
    private String code;                // Unique category code (e.g. CAT-001)
    private String name;                // Category name (e.g. Raw Materials)
    private String description;         // Detailed description
    private String shortDescription;    // Short description for lists

    // ---- HIERARCHY ----
    private String parentId;            // Parent category ID (null for root)
    private String parentCode;          // Parent category code (denormalized)
    private Integer level;              // Level in the tree (0 = root)
    private String path;                // Hierarchical path (e.g. /CAT-001/CAT-002/)
    private List<String> childrenIds;   // Sub-category IDs

    // ---- CLASSIFICATION ----
    private MaterialCategoryType categoryType;
    private String status;              // ACTIVE / INACTIVE

    // ---- STATISTICS ----
    private Integer materialCount;      // Number of materials in this category
    private Integer subCategoryCount;   // Number of sub-categories
    private Integer totalItems;         // Total items (materials + sub-categories)


    // ============================================================
    // DEFAULT CONSTRUCTOR
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
    // PRIVATE CONSTRUCTOR (via Builder)
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
        private MaterialCategoryType categoryType;
        private String status = STATUS_ACTIVE;
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

        // ---- HIERARCHY ----
        public Builder parentId(String parentId) { this.parentId = parentId; return this; }
        public Builder parentCode(String parentCode) { this.parentCode = parentCode; return this; }
        public Builder level(Integer level) { this.level = level; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder childrenIds(List<String> childrenIds) {
            this.childrenIds = childrenIds != null ? new ArrayList<>(childrenIds) : new ArrayList<>();
            return this;
        }

        // ---- CLASSIFICATION ----
        public Builder categoryType(MaterialCategoryType categoryType) { this.categoryType = categoryType; return this; }
        public Builder status(String status) { this.status = status; return this; }

        // ---- STATISTICS ----
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

            // Calculate path if parentId is provided
            if (this.parentId != null && this.path == null) {
                this.path = "/" + this.code + "/";
            }

            // Validation
            if (this.code == null || this.code.trim().isEmpty()) {
                throw new IllegalArgumentException("Category code is required");
            }
            if (this.name == null || this.name.trim().isEmpty()) {
                throw new IllegalArgumentException("Category name is required");
            }

            return new Category(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // ============================================================
    // DOMAIN METHODS
    // ============================================================

    /**
     * Checks if the category is active
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(this.status);
    }

    /**
     * Activates the category
     */
    public void activate() {
        this.status = STATUS_ACTIVE;
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Deactivates the category
     */
    public void deactivate() {
        this.status = STATUS_INACTIVE;
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Checks if this is a root category
     */
    public boolean isRoot() {
        return this.parentId == null || this.parentId.isEmpty();
    }

    /**
     * Checks if the category has sub-categories
     */
    public boolean hasChildren() {
        return this.childrenIds != null && !this.childrenIds.isEmpty();
    }

    /**
     * Adds a sub-category
     */
    public void addChild(String childId) {
        if (childId == null || childId.isEmpty()) {
            throw new IllegalArgumentException("Sub-category ID is required");
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
     * Removes a sub-category
     */
    public void removeChild(String childId) {
        if (childId == null || childId.isEmpty()) {
            throw new IllegalArgumentException("Sub-category ID is required");
        }
        if (this.childrenIds != null) {
            this.childrenIds.remove(childId);
            this.subCategoryCount = this.childrenIds.size();
            this.totalItems = this.materialCount + this.subCategoryCount;
            this.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * Updates the statistics
     */
    public void updateStatistics(int materialCount, int subCategoryCount) {
        this.materialCount = materialCount;
        this.subCategoryCount = subCategoryCount;
        this.totalItems = materialCount + subCategoryCount;
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Increments the material count
     */
    public void incrementMaterialCount() {
        this.materialCount = (this.materialCount == null ? 0 : this.materialCount) + 1;
        this.totalItems = this.materialCount + this.subCategoryCount;
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Decrements the material count
     */
    public void decrementMaterialCount() {
        if (this.materialCount != null && this.materialCount > 0) {
            this.materialCount--;
            this.totalItems = this.materialCount + this.subCategoryCount;
            this.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * Calculates the depth level
     */
    public int getDepth() {
        if (this.path == null || this.path.isEmpty()) {
            return 0;
        }
        return (int) this.path.chars().filter(ch -> ch == '/').count() - 1;
    }

    /**
     * Gets the full name with path
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
            throw new IllegalArgumentException("Category code is required");
        }
        this.code = code;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
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

    // ---- HIERARCHY ----
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
    public MaterialCategoryType getCategoryType() { return categoryType; }
    public void setCategoryType(MaterialCategoryType categoryType) {
        this.categoryType = categoryType;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ---- STATISTICS ----
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
                .materialCount(this.materialCount)
                .subCategoryCount(this.subCategoryCount)
                .totalItems(this.totalItems)
                .createdBy(this.getCreatedBy())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }
}
