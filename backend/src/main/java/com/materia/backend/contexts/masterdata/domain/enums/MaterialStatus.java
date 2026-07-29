package com.materia.backend.contexts.masterdata.domain.enums;


/**
 * Statuts possibles pour un matÃ©riau
 * Gestion du cycle de vie d'un matÃ©riau
 *
 * ðŸ“ Position: masterData-service/domain/enums/MaterialStatus.java
 */
public enum MaterialStatus {

    // ============================================================
    // STATUTS PRINCIPAUX (5 SEULEMENT)
    // ============================================================

    DRAFT("DRAFT", "Brouillon", "MatÃ©riau en cours de crÃ©ation"),
    ACTIVE("ACTIVE", "Actif", "MatÃ©riau disponible et utilisable"),
    INACTIVE("INACTIVE", "Inactif", "MatÃ©riau temporairement indisponible"),
    BLOCKED("BLOCKED", "BloquÃ©", "MatÃ©riau bloquÃ© pour raison de sÃ©curitÃ© ou qualitÃ©"),
    OBSOLETE("OBSOLETE", "ObsolÃ¨te", "MatÃ©riau remplacÃ© ou abandonnÃ©");

    // ============================================================
    // ATTRIBUTS
    // ============================================================

    private final String code;
    private final String label;
    private final String description;

    // ============================================================
    // CONSTRUCTEUR
    // ============================================================

    MaterialStatus(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
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

    // ============================================================
    // MÃ‰THODES UTILITAIRES
    // ============================================================

    /**
     * RÃ©cupÃ¨re un statut par son code
     */
    public static MaterialStatus fromCode(String code) {
        if (code == null) return null;
        for (MaterialStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Statut inconnu: " + code);
    }

    /**
     * VÃ©rifie si un code de statut existe
     */
    public static boolean isValid(String code) {
        if (code == null) return false;
        for (MaterialStatus status : values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * RÃ©cupÃ¨re tous les statuts actifs (utilisables)
     */
    public static MaterialStatus[] getActiveStatuses() {
        return new MaterialStatus[] {
                ACTIVE
        };
    }

    /**
     * RÃ©cupÃ¨re tous les statuts bloquants
     */
    public static MaterialStatus[] getBlockingStatuses() {
        return new MaterialStatus[] {
                BLOCKED,
                OBSOLETE
        };
    }

    /**
     * RÃ©cupÃ¨re tous les statuts modifiables
     */
    public static MaterialStatus[] getModifiableStatuses() {
        return new MaterialStatus[] {
                DRAFT,
                ACTIVE,
                INACTIVE
        };
    }

    // ============================================================
    // MÃ‰THODES D'INSTANCE
    // ============================================================

    /**
     * VÃ©rifie si le matÃ©riau est utilisable
     */
    public boolean isUsable() {
        return this == ACTIVE;
    }

    /**
     * VÃ©rifie si le matÃ©riau est en cours de validation
     */
    public boolean isInReview() {
        return this == DRAFT;
    }

    /**
     * VÃ©rifie si le matÃ©riau est en fin de vie
     */
    public boolean isEndOfLife() {
        return this == OBSOLETE;
    }

    /**
     * VÃ©rifie si le matÃ©riau est bloquÃ©
     */
    public boolean isBlocked() {
        return this == BLOCKED || this == OBSOLETE;
    }

    /**
     * VÃ©rifie si le statut est valide pour une commande
     */
    public boolean isOrderable() {
        return this == ACTIVE;
    }

    /**
     * VÃ©rifie si le statut est valide pour une rÃ©ception
     */
    public boolean isReceivable() {
        return this == ACTIVE;
    }

    /**
     * VÃ©rifie si le matÃ©riau peut Ãªtre modifiÃ©
     */
    public boolean isModifiable() {
        return this == DRAFT || this == ACTIVE || this == INACTIVE;
    }

    @Override
    public String toString() {
        return code + " - " + label;
    }
}
