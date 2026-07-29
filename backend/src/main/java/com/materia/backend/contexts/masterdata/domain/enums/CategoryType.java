package com.materia.backend.contexts.masterdata.domain.enums;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Type de catÃ©gorie
 * Classification des catÃ©gories selon leur nature et usage
 *
 * @author SAP MM Team
 * @version 1.0
 */
public enum CategoryType {

    // ============================================================
    // TYPES DE CATÃ‰GORIES
    // ============================================================

    RAW_MATERIAL("RAW_MATERIAL", "MatiÃ¨re premiÃ¨re", "MatÃ©riau brut utilisÃ© dans la fabrication", "Production"),

    COMPONENT("COMPONENT", "Composant", "PiÃ¨ce ou Ã©lÃ©ment entrant dans un assemblage", "Production"),

    FINISHED_GOOD("FINISHED_GOOD", "Produit fini", "Produit final prÃªt Ã  la vente", "Vente"),

    PACKAGING("PACKAGING", "Emballage", "MatÃ©riau d'emballage", "Logistique"),

    SPARE_PART("SPARE_PART", "PiÃ¨ce dÃ©tachÃ©e", "PiÃ¨ce de rechange", "Maintenance"),

    CONSUMABLE("CONSUMABLE", "Consommable", "MatÃ©riau consommÃ© lors de l'utilisation", "Production"),

    SERVICE("SERVICE", "Service", "Prestation ou service non physique", "Administration"),

    TOOL("TOOL", "Outil", "Ã‰quipement ou instrument de travail", "Maintenance"),

    CHEMICAL("CHEMICAL", "Produit chimique", "Substance chimique ou rÃ©actif", "Laboratoire"),

    ELECTRONIC("ELECTRONIC", "Composant Ã©lectronique", "Composant ou Ã©quipement Ã©lectronique", "Production");

    // ============================================================
    // ATTRIBUTS
    // ============================================================

    private final String code;
    private final String label;
    private final String description;
    private final String department;

    // ============================================================
    // CONSTRUCTEUR
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

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getDepartment() {
        return department;
    }

    // ============================================================
    // MÃ‰THODES UTILITAIRES
    // ============================================================

    /**
     * RÃ©cupÃ¨re un CategoryType par son code
     */
    public static CategoryType fromCode(String code) {
        for (CategoryType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Type de catÃ©gorie inconnu : " + code);
    }

    /**
     * RÃ©cupÃ¨re un CategoryType par son label
     */
    public static CategoryType fromLabel(String label) {
        for (CategoryType type : values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Type de catÃ©gorie inconnu : " + label);
    }

    /**
     * VÃ©rifie si le code existe
     */
    public static boolean isValidCode(String code) {
        return Arrays.stream(values())
                .anyMatch(type -> type.code.equals(code));
    }

    /**
     * RÃ©cupÃ¨re tous les codes
     */
    public static List<String> getCodes() {
        return Arrays.stream(values())
                .map(CategoryType::getCode)
                .collect(Collectors.toList());
    }

    /**
     * RÃ©cupÃ¨re tous les labels
     */
    public static List<String> getLabels() {
        return Arrays.stream(values())
                .map(CategoryType::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * RÃ©cupÃ¨re les types par dÃ©partement
     */
    public static List<CategoryType> findByDepartment(String department) {
        return Arrays.stream(values())
                .filter(type -> type.department.equals(department))
                .collect(Collectors.toList());
    }

    /**
     * VÃ©rifie si le type est liÃ© Ã  la production
     */
    public boolean isProductionRelated() {
        return "Production".equals(this.department) || "Maintenance".equals(this.department);
    }

    /**
     * VÃ©rifie si le type est un matÃ©riau physique
     */
    public boolean isPhysical() {
        return !SERVICE.equals(this);
    }

    /**
     * VÃ©rifie si le type est un produit fini
     */
    public boolean isFinishedGood() {
        return FINISHED_GOOD.equals(this);
    }

    /**
     * VÃ©rifie si le type est un composant
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

