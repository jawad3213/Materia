package com.materia.backend.contexts.masterdata.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Material Type
 * Defines the nature of a material.
 */
public enum MaterialType {

    RAW_MATERIAL("RMT", "Raw Material", "Raw material used in manufacturing", "Production"),
    FINISHED_GOOD("FGD", "Finished Good", "Final product ready for sale", "Sales"),
    COMPONENT("CMP", "Component", "Part or element used in an assembly", "Production"),
    PACKAGING("PKG", "Packaging", "Packaging material", "Logistics"),
    SPARE_PART("SPR", "Spare Part", "Replacement part", "Maintenance"),
    CONSUMABLE("CNS", "Consumable", "Material consumed during use", "Production"),
    SERVICE("SRV", "Service", "Non-physical service or provision", "Administration"),
    TOOL("TOL", "Tool", "Equipment or work instrument", "Maintenance"),
    CHEMICAL("CHM", "Chemical", "Chemical substance or reagent", "Laboratory"),
    ELECTRONIC("ELC", "Electronic Component", "Electronic component or equipment", "Production");

    private final String prefix;
    private final String label;
    private final String description;
    private final String department;

    MaterialType(String prefix, String label, String description, String department) {
        this.prefix = prefix;
        this.label = label;
        this.description = description;
        this.department = department;
    }

    public String getPrefix() { return prefix; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public String getDepartment() { return department; }

    public static MaterialType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Material type code is required");
        }
        for (MaterialType type : values()) {
            if (type.prefix.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown material type: " + code);
    }

    public static MaterialType fromLabel(String label) {
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException("Material type label is required");
        }
        for (MaterialType type : values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown material type: " + label);
    }

    public static MaterialType fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Material type is required");
        }
        String normalized = value.trim();
        try {
            return MaterialType.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            // Fall through to code and label matching.
        }
        for (MaterialType type : values()) {
            if (type.prefix.equalsIgnoreCase(normalized) || type.label.equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown material type: " + value);
    }

    public static boolean isValidCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(type -> type.prefix.equals(code));
    }

    public static List<String> getPrefixes() {
        return Arrays.stream(values())
                .map(MaterialType::getPrefix)
                .collect(Collectors.toList());
    }

    public static List<String> getLabels() {
        return Arrays.stream(values())
                .map(MaterialType::getLabel)
                .collect(Collectors.toList());
    }

    public static List<MaterialType> findByDepartment(String department) {
        return Arrays.stream(values())
                .filter(type -> type.department.equals(department))
                .collect(Collectors.toList());
    }

    public static List<MaterialType> getProductionTypes() {
        return List.of(RAW_MATERIAL, COMPONENT, CONSUMABLE, ELECTRONIC);
    }

    public static List<MaterialType> getSalesTypes() {
        return List.of(FINISHED_GOOD, SPARE_PART);
    }

    public static List<MaterialType> getMaintenanceTypes() {
        return List.of(SPARE_PART, TOOL);
    }

    public boolean isPhysical() {
        return !SERVICE.equals(this);
    }

    public boolean isProduction() {
        return getProductionTypes().contains(this);
    }

    public boolean isSales() {
        return getSalesTypes().contains(this);
    }

    public boolean isMaintenance() {
        return getMaintenanceTypes().contains(this);
    }

    public boolean isRawMaterial() {
        return this == RAW_MATERIAL;
    }

    public boolean isFinishedGood() {
        return this == FINISHED_GOOD;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", label, prefix, description);
    }
}
