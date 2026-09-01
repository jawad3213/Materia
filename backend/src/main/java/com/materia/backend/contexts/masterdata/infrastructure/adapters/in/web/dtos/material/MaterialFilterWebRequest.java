package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material;

/**
 * Dedicated request payload for UI filtering (Category, Material Type, Status).
 */
public class MaterialFilterWebRequest {

    private String categoryId;
    private String materialType;
    private String status;

    public MaterialFilterWebRequest() {
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
