package com.materia.backend.contexts.masterData.domain.valueObjects;

import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;

/**
 * Value object representing criteria for searching/filtering materials.
 */
public class MaterialSearchFilter {
    // ---- Search fields ----
    private final String code;
    private final String name;
    private final String description;
    private final String shortDescription;
    private final String searchKeywords;
    private final String alternativeName;

    // ---- Filter fields ----
    private final String categoryId;
    private final MaterialType materialType;
    private final MaterialStatus status;

    private MaterialSearchFilter(Builder builder) {
        this.code = builder.code;
        this.name = builder.name;
        this.description = builder.description;
        this.shortDescription = builder.shortDescription;
        this.searchKeywords = builder.searchKeywords;
        this.alternativeName = builder.alternativeName;
        this.categoryId = builder.categoryId;
        this.materialType = builder.materialType;
        this.status = builder.status;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getShortDescription() { return shortDescription; }
    public String getSearchKeywords() { return searchKeywords; }
    public String getAlternativeName() { return alternativeName; }
    public String getCategoryId() { return categoryId; }
    public MaterialType getMaterialType() { return materialType; }
    public MaterialStatus getStatus() { return status; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String code;
        private String name;
        private String description;
        private String shortDescription;
        private String searchKeywords;
        private String alternativeName;
        private String categoryId;
        private MaterialType materialType;
        private MaterialStatus status;

        public Builder code(String code) { this.code = code; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder shortDescription(String shortDescription) { this.shortDescription = shortDescription; return this; }
        public Builder searchKeywords(String searchKeywords) { this.searchKeywords = searchKeywords; return this; }
        public Builder alternativeName(String alternativeName) { this.alternativeName = alternativeName; return this; }
        public Builder categoryId(String categoryId) { this.categoryId = categoryId; return this; }
        public Builder materialType(MaterialType materialType) { this.materialType = materialType; return this; }
        public Builder status(MaterialStatus status) { this.status = status; return this; }

        public MaterialSearchFilter build() {
            return new MaterialSearchFilter(this);
        }
    }
}
