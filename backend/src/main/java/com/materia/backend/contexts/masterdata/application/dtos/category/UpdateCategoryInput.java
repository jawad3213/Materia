package com.materia.backend.contexts.masterdata.application.dtos.category;

import com.materia.backend.common.application.BaseInput;
import java.util.UUID;

/**
 * Request DTO for updating an existing Category
 */
public class UpdateCategoryInput extends BaseInput {

    private UUID id;
    private String name;
    private String description;
    private String shortDescription;
    private String parentId;
    private String categoryType;
    private String status;
    private String color;
    private String icon;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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
}
