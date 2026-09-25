package com.materia.backend.contexts.auth.domain.enums;

/**
 * 🔹 USER STATUS ENUM
 * 
 * Defines the lifecycle state of a user account in the auth bounded context.
 */
public enum UserStatus {

    ACTIVE("ACTIVE", "Active", "Compte actif et autorisé"),
    INACTIVE("INACTIVE", "Inactif", "Compte désactivé"),
    SUSPENDED("SUSPENDED", "Suspendu", "Compte temporairement suspendu"),
    PENDING("PENDING", "En attente", "Compte en attente d'activation");

    private final String code;
    private final String label;
    private final String description;

    UserStatus(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public static UserStatus fromCode(String code) {
        if (code == null) return null;
        for (UserStatus status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown UserStatus: " + code);
    }
}
