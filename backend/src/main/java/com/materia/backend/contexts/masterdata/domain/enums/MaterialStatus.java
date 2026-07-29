package com.materia.backend.contexts.masterdata.domain.enums;

/**
 * Possible statuses for a material
 * Material lifecycle management
 *
 * Location: masterdata/domain/enums/MaterialStatus.java
 */
public enum MaterialStatus {

    // ============================================================
    // MAIN STATUSES (5 ONLY)
    // ============================================================

    DRAFT("DRAFT", "Draft", "Material being created"),
    ACTIVE("ACTIVE", "Active", "Material available and usable"),
    INACTIVE("INACTIVE", "Inactive", "Material temporarily unavailable"),
    BLOCKED("BLOCKED", "Blocked", "Material blocked for safety or quality reasons"),
    OBSOLETE("OBSOLETE", "Obsolete", "Material replaced or discontinued");

    // ============================================================
    // ATTRIBUTES
    // ============================================================

    private final String code;
    private final String label;
    private final String description;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    MaterialStatus(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    /**
     * Retrieves a status by its code
     */
    public static MaterialStatus fromCode(String code) {
        if (code == null) return null;
        for (MaterialStatus status : values()) {
            if (status.getCode().equals(code)) { return status; }
        }
        throw new IllegalArgumentException("Unknown status: " + code);
    }

    /**
     * Checks if a status code exists
     */
    public static boolean isValid(String code) {
        if (code == null) return false;
        for (MaterialStatus status : values()) {
            if (status.getCode().equals(code)) { return true; }
        }
        return false;
    }

    /**
     * Retrieves all active (usable) statuses
     */
    public static MaterialStatus[] getActiveStatuses() {
        return new MaterialStatus[] { ACTIVE };
    }

    /**
     * Retrieves all blocking statuses
     */
    public static MaterialStatus[] getBlockingStatuses() {
        return new MaterialStatus[] { BLOCKED, OBSOLETE };
    }

    /**
     * Retrieves all modifiable statuses
     */
    public static MaterialStatus[] getModifiableStatuses() {
        return new MaterialStatus[] { DRAFT, ACTIVE, INACTIVE };
    }

    // ============================================================
    // INSTANCE METHODS
    // ============================================================

    /**
     * Checks if the material is usable
     */
    public boolean isUsable() { return this == ACTIVE; }

    /**
     * Checks if the material is under review
     */
    public boolean isInReview() { return this == DRAFT; }

    /**
     * Checks if the material is end-of-life
     */
    public boolean isEndOfLife() { return this == OBSOLETE; }

    /**
     * Checks if the material is blocked
     */
    public boolean isBlocked() { return this == BLOCKED || this == OBSOLETE; }

    /**
     * Checks if the status is valid for ordering
     */
    public boolean isOrderable() { return this == ACTIVE; }

    /**
     * Checks if the status is valid for receiving
     */
    public boolean isReceivable() { return this == ACTIVE; }

    /**
     * Checks if the material can be modified
     */
    public boolean isModifiable() { return this == DRAFT || this == ACTIVE || this == INACTIVE; }

    @Override
    public String toString() { return code + " - " + label; }
}
