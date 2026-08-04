package com.materia.backend.contexts.masterData.domain.valueObjects;

import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import java.math.BigDecimal;

/**
 * Value object representing criteria for filtering materials.
 */
public class MaterialSearchFilter {
    private final String keyword;
    private final String categoryId;
    private final String supplierId;
    private final MaterialStatus status;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final Boolean lowStockOnly;

    private MaterialSearchFilter(Builder builder) {
        this.keyword = builder.keyword;
        this.categoryId = builder.categoryId;
        this.supplierId = builder.supplierId;
        this.status = builder.status;
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.lowStockOnly = builder.lowStockOnly;
    }

    public String getKeyword() { return keyword; }
    public String getCategoryId() { return categoryId; }
    public String getSupplierId() { return supplierId; }
    public MaterialStatus getStatus() { return status; }
    public BigDecimal getMinPrice() { return minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public Boolean getLowStockOnly() { return lowStockOnly; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String keyword;
        private String categoryId;
        private String supplierId;
        private MaterialStatus status;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Boolean lowStockOnly;

        public Builder keyword(String keyword) { this.keyword = keyword; return this; }
        public Builder categoryId(String categoryId) { this.categoryId = categoryId; return this; }
        public Builder supplierId(String supplierId) { this.supplierId = supplierId; return this; }
        public Builder status(MaterialStatus status) { this.status = status; return this; }
        public Builder minPrice(BigDecimal minPrice) { this.minPrice = minPrice; return this; }
        public Builder maxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; return this; }
        public Builder lowStockOnly(Boolean lowStockOnly) { this.lowStockOnly = lowStockOnly; return this; }

        public MaterialSearchFilter build() {
            return new MaterialSearchFilter(this);
        }
    }
}
