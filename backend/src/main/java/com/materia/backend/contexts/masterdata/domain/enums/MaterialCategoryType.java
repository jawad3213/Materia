package com.materia.backend.contexts.masterdata.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Material Category Type
 * Defines the grouping type of a category.
 */
public enum MaterialCategoryType {

    MATERIAL("MAT", "Material Category", "Groups physical materials", "stock"),
    PRODUCT("PRD", "Product Category", "Groups finished products", "sales"),
    SERVICE("SRV", "Service Category", "Groups service offerings", "administration"),
    RAW_MATERIAL_CAT("RMC", "Raw Material Category", "Groups raw materials", "production"),
    COMPONENT_CAT("CMP", "Component Category", "Groups components", "production"),
    PACKAGING_CAT("PKG", "Packaging Category", "Groups packaging materials", "logistics"),
    SPARE_PART_CAT("SPR", "Spare Part Category", "Groups spare parts", "maintenance"),
    CONSUMABLE_CAT("CNS", "Consumable Category", "Groups consumables", "production"),
    TOOL_CAT("TOL", "Tool Category", "Groups tools", "maintenance"),
    CHEMICAL_CAT("CHM", "Chemical Category", "Groups chemical products", "laboratory"),
    ELECTRONIC_CAT("ELC", "Electronic Category", "Groups electronic components", "production"),
    FAMILY("FAM", "Family", "Product family grouping", "marketing"),
    BRAND("BRD", "Brand", "Brand-based category", "marketing"),
    DEPARTMENT("DEP", "Department", "Department grouping", "organization"),
    PROJECT("PRJ", "Project", "Project grouping", "organization"),
    GEOGRAPHIC("GEO", "Geographic", "Regional grouping", "distribution"),
    SEASONAL("SEA", "Seasonal", "Seasonal grouping", "sales");

    private final String code;
    private final String label;
    private final String description;
    private final String domain;

    MaterialCategoryType(String code, String label, String description, String domain) {
        this.code = code;
        this.label = label;
        this.description = description;
        this.domain = domain;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public String getDomain() { return domain; }

    public static MaterialCategoryType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Category code is required");
        }
        for (MaterialCategoryType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown material category type: " + code);
    }

    public static MaterialCategoryType fromLabel(String label) {
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException("Category label is required");
        }
        for (MaterialCategoryType type : values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown material category type: " + label);
    }

    public static MaterialCategoryType fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Category type is required");
        }
        String normalized = value.trim();
        try {
            return MaterialCategoryType.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            // Fall through to code and label matching.
        }
        for (MaterialCategoryType type : values()) {
            if (type.code.equalsIgnoreCase(normalized) || type.label.equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown material category type: " + value);
    }

    public static boolean isValidCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(type -> type.code.equals(code));
    }

    public static List<String> getCodes() {
        return Arrays.stream(values())
                .map(MaterialCategoryType::getCode)
                .collect(Collectors.toList());
    }

    public static List<String> getLabels() {
        return Arrays.stream(values())
                .map(MaterialCategoryType::getLabel)
                .collect(Collectors.toList());
    }

    public static List<MaterialCategoryType> getMaterialCategories() {
        return List.of(
                MATERIAL,
                RAW_MATERIAL_CAT,
                COMPONENT_CAT,
                PACKAGING_CAT,
                SPARE_PART_CAT,
                CONSUMABLE_CAT,
                TOOL_CAT,
                CHEMICAL_CAT,
                ELECTRONIC_CAT
        );
    }

    public static List<MaterialCategoryType> getCommercialCategories() {
        return List.of(PRODUCT, FAMILY, BRAND, SEASONAL);
    }

    public static List<MaterialCategoryType> getOrganizationalCategories() {
        return List.of(DEPARTMENT, PROJECT);
    }

    public boolean isMaterialCategory() {
        return getMaterialCategories().contains(this);
    }

    public boolean isCommercialCategory() {
        return getCommercialCategories().contains(this);
    }

    public boolean isOrganizationalCategory() {
        return getOrganizationalCategories().contains(this);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", label, code, description);
    }
}
