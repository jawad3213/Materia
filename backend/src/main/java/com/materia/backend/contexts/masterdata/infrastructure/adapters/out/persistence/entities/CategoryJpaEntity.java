package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import com.materia.backend.contexts.masterData.domain.enums.MaterialCategoryType;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for Category
 * Infrastructure Layer - Maps to the 'categories' database table
 */
@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_code", columnList = "code", unique = true),
        @Index(name = "idx_category_parent_id", columnList = "parent_id"),
        @Index(name = "idx_category_status", columnList = "status"),
        @Index(name = "idx_category_type", columnList = "category_type")
})
public class CategoryJpaEntity extends BaseJpaEntity {

    // ---- IDENTIFICATION ----
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "short_description", length = 200)
    private String shortDescription;

    // ---- HIERARCHY ----
    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "parent_code", length = 50)
    private String parentCode;

    @Column(name = "level")
    private Integer level;

    @Column(name = "path", length = 500)
    private String path;

    @Transient
    private List<String> childrenIds = new ArrayList<>();

    // ---- CLASSIFICATION ----
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", length = 30)
    private MaterialCategoryType categoryType;

    @Column(name = "status", nullable = false, length = 20)
    private String status;


    // ---- STATISTICS ----
    @Transient
    private Integer materialCount;

    @Transient
    private Integer subCategoryCount;

    @Transient
    private Integer totalItems;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public CategoryJpaEntity() {
        super();
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

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

    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<String> getChildrenIds() { return childrenIds; }
    public void setChildrenIds(List<String> childrenIds) { this.childrenIds = childrenIds; }

    public MaterialCategoryType getCategoryType() { return categoryType; }
    public void setCategoryType(MaterialCategoryType categoryType) { this.categoryType = categoryType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


    public Integer getMaterialCount() { return materialCount; }
    public void setMaterialCount(Integer materialCount) { this.materialCount = materialCount; }

    public Integer getSubCategoryCount() { return subCategoryCount; }
    public void setSubCategoryCount(Integer subCategoryCount) { this.subCategoryCount = subCategoryCount; }

    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
}
