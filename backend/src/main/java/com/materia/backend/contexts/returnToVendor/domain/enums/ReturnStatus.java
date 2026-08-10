package com.materia.backend.contexts.returnToVendor.domain.enums;

import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorValidationException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Return Status Enum - Version Simplifiée
 * Statuts d'un retour fournisseur
 * 
 * @author SAP MM Team
 * @version 2.0
 */
public enum ReturnStatus {
    
    DRAFT("DRAFT", "Brouillon", "Retour en cours de saisie"),
    PENDING("PENDING", "En attente", "Retour soumis, en attente de traitement"),
    RESOLVED("RESOLVED", "Résolu", "Retour traité (remplacement ou avoir reçu)"),
    CANCELLED("CANCELLED", "Annulé", "Retour annulé");
    
    private final String code;
    private final String label;
    private final String description;
    
    ReturnStatus(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    
    public static ReturnStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new ReturnToVendorValidationException("Le code est obligatoire");
        }
        for (ReturnStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new ReturnToVendorValidationException("Statut inconnu : " + code);
    }
    
    public static boolean isValidCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(status -> status.code.equals(code));
    }
    
    public static List<String> getCodes() {
        return Arrays.stream(values())
                .map(ReturnStatus::getCode)
                .collect(Collectors.toList());
    }
    
    public boolean isActive() {
        return this != RESOLVED && this != CANCELLED;
    }
    
    public boolean isModifiable() {
        return this == DRAFT;
    }
    
    public boolean isClosed() {
        return this == RESOLVED || this == CANCELLED;
    }
}
