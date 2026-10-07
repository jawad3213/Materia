package com.materia.backend.contexts.masterData.application.dtos.material;

/**
 * Dedicated criteria for UI filtering (Category, Material Type, Status).
 */
public class MaterialFilterCriteria {

    private String categoryId;
    private String materialType;
    private String status;

    public MaterialFilterCriteria() {
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
