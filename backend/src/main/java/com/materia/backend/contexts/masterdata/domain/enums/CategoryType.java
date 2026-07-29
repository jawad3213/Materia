package com.materia.backend.contexts.masterdata.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Category Type
 * Classification of categories by their nature and usage
 *
 * @author SAP MM Team
 * @version 1.0
 */
public enum CategoryType {

    // ============================================================
    // CATEGORY TYPES
    // ============================================================

    RAW_MATERIAL("RAW_MATERIAL", "Raw Material", "Raw material used in manufacturing", "Production"),
    COMPONENT("COMPONENT", "Component", "Part or element used in an assembly", "Production"),
    FINISHED_GOOD("FINISHED_GOOD", "Finished Good", "Final product ready for sale", "Sales"),
    PACKAGING("PACKAGING", "Packaging", "Packaging material", "Logistics"),
    SPARE_PART("SPARE_PART", "Spare Part", "Replacement part", "Maintenance"),
    CONSUMABLE("CONSUMABLE", "Consumable", "Material consumed during use", "Production"),
    SERVICE("SERVICE", "Service", "Non-physical service or provision", "Administration"),
    TOOL("TOOL", "Tool", "Equipment or work instrument", "Maintenance"),
    CHEMICAL("CHEMICAL", "Chemical", "Chemical substance or reagent", "Laboratory"),
    ELECTRONIC("ELECTRONIC", "Electronic Component", "Electronic component or equipment", "Production");

    // ============================================================
    // ATTRIBUTES
    // ============================================================

    private final String code;
    private final String label;
    private final String description;
    private final String department;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    CategoryType(String code, String label, String description, String department) {
        this.code = code;
        this.label = label;
        this.description = description;
        this.department = department;
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public String getDepartment() { return department; }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    /**
     * Retrieves a CategoryType by its code
     */
    public static CategoryType fromCode(String code) {
        for (CategoryType type : values()) {
            if (type.code.equals(code)) { return type; }
        }
        throw new IllegalArgumentException("Unknown category type: " + code);
    }

    /**
     * Retrieves a CategoryType by its label
     */
    public static CategoryType fromLabel(String label) {
        for (CategoryType type : values()) {
            if (type.label.equals(label)) { return type; }
        }
        throw new IllegalArgumentException("Unknown category type: " + label);
    }

    /**
     * Checks if a code exists
     */
    public static boolean isValidCode(String code) {
        return Arrays.stream(values()).anyMatch(type -> type.code.equals(code));
    }

    /**
     * Retrieves all codes
     */
    public static List<String> getCodes() {
        return Arrays.stream(values()).map(CategoryType::getCode).collect(Collectors.toList());
    }

    /**
     * Retrieves all labels
     */
    public static List<String> getLabels() {
        return Arrays.stream(values()).map(CategoryType::getLabel).collect(Collectors.toList());
    }

    /**
     * Retrieves types by department
     */
    public static List<CategoryType> findByDepartment(String department) {
        return Arrays.stream(values()).filter(type -> type.department.equals(department)).collect(Collectors.toList());
    }

    /**
     * Checks if the type is production-related
     */
    public boolean isProductionRelated() {
        return "Production".equals(this.department) || "Maintenance".equals(this.department);
    }

    /**
     * Checks if the type is a physical material
     */
    public boolean isPhysical() { return !SERVICE.equals(this); }

    /**
     * Checks if the type is a finished good
     */
    public boolean isFinishedGood() { return FINISHED_GOOD.equals(this); }

    /**
     * Checks if the type is a component
     */
    public boolean isComponent() {
        return COMPONENT.equals(this) || SPARE_PART.equals(this) || ELECTRONIC.equals(this);
    }

    // ============================================================
    // TOSTRING
    // ============================================================

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", label, code, description);
    }
}
