package com.materia.backend.contexts.masterData.application.dtos.category;

import com.materia.backend.common.application.BaseInput;

/**
 * Request DTO for creating a new Category
 */
public class CreateCategoryInput extends BaseInput {

    private String code;
    private String name;
    private String description;
    private String shortDescription;
    private String parentId;
    private String categoryType;
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

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
