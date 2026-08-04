package com.materia.backend.contexts.masterData.application.dtos.material;

import java.math.BigDecimal;

/**
 * Criteria for advanced material search.
 */
public class MaterialSearchCriteria {
    
    private String keyword;
    private String categoryId;
    private String supplierId;
    private String status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean lowStockOnly;

    public MaterialSearchCriteria() {
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Boolean getLowStockOnly() {
        return lowStockOnly;
    }

    public void setLowStockOnly(Boolean lowStockOnly) {
        this.lowStockOnly = lowStockOnly;
    }
}
