package com.materia.backend.contexts.goodsReceipt.domain.enums;


public enum QualityStatus {
    
    ACCEPTED("ACCEPTED", "Conforme", "Produits conformes aux spécifications"),
    REJECTED("REJECTED", "Non conforme", "Produits non conformes"),
    UNDER_REVIEW("UNDER_REVIEW", "À contrôler", "En attente de contrôle qualité"),
    PARTIAL("PARTIAL", "Partiellement conforme", "Partie conforme, partie non conforme");
    
    private final String code;
    private final String label;
    private final String description;
    
    QualityStatus(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    
    public static QualityStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }
        for (QualityStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Statut inconnu : " + code);
    }
    
    public boolean isAccepted() {
        return this == ACCEPTED;
    }
    
    public boolean isRejected() {
        return this == REJECTED;
    }
    
    public boolean isPending() {
        return this == UNDER_REVIEW;
    }
}