package com.materia.backend.contexts.requisition.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Requisition Status Enum
 * Définit les différents états d'une demande d'achat
 * 
 * @author SAP MM Team
 * @version 1.0
 */
public enum RequisitionStatus {
    
    DRAFT("DRAFT", "Brouillon", "Demande en cours de saisie", "#94a3b8"),
    UNDER_REVIEW("UNDER_REVIEW", "En révision", "Demande en cours d'analyse", "#3b82f6"),
    APPROVED("APPROVED", "Approuvée", "Demande approuvée pour conversion", "#22c55e"),
    REJECTED("REJECTED", "Rejetée", "Demande rejetée", "#ef4444"),
    CONVERTED("CONVERTED", "Convertie", "Demande convertie en commande", "#8b5cf6");
    
    private final String code;
    private final String label;
    private final String description;
    private final String color;
    
    RequisitionStatus(String code, String label, String description, String color) {
        this.code = code;
        this.label = label;
        this.description = description;
        this.color = color;
    }
    
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public String getColor() { return color; }
    
    /**
     * Récupère un statut par son code
     */
    public static RequisitionStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }
        for (RequisitionStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Statut inconnu : " + code);
    }
    
    /**
     * Vérifie si un code est valide
     */
    public static boolean isValidCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(status -> status.code.equals(code));
    }
    
    /**
     * Récupère tous les codes
     */
    public static List<String> getCodes() {
        return Arrays.stream(values())
                .map(RequisitionStatus::getCode)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les statuts modifiables
     */
    public static List<RequisitionStatus> getModifiableStatuses() {
        return List.of(DRAFT);
    }
    
    /**
     * Récupère les statuts convertibles
     */
    public static List<RequisitionStatus> getConvertibleStatuses() {
        return List.of(APPROVED);
    }
    
    /**
     * Récupère les statuts validables
     */
    public static List<RequisitionStatus> getValidatableStatuses() {
        return List.of(UNDER_REVIEW);
    }
    
    /**
     * Vérifie si la demande peut être modifiée
     */
    public boolean isModifiable() {
        return getModifiableStatuses().contains(this);
    }
    
    /**
     * Vérifie si la demande peut être convertie
     */
    public boolean isConvertible() {
        return getConvertibleStatuses().contains(this);
    }
    
    /**
     * Vérifie si la demande peut être validée
     */
    public boolean isValidatable() {
        return getValidatableStatuses().contains(this);
    }
    
    /**
     * Vérifie si la demande est annulable
     */
    public boolean isCancellable() {
        return !CONVERTED.equals(this) && !REJECTED.equals(this);
    }
    
    /**
     * Vérifie si la demande est supprimable
     */
    public boolean isDeletable() {
        return DRAFT.equals(this);
    }
    
    /**
     * Vérifie si la demande est active (en cours de traitement)
     */
    public boolean isActive() {
        return DRAFT.equals(this) || UNDER_REVIEW.equals(this);
    }
    
    /**
     * Vérifie si la demande est terminée (statut final)
     */
    public boolean isFinal() {
        return CONVERTED.equals(this) || REJECTED.equals(this);
    }
}
