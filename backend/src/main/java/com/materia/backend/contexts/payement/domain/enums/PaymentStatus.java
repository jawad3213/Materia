package com.materia.backend.contexts.payement.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Payment Status Enum - Version Simplifiée (MVP)
 * 
 * @author SAP MM Team
 * @version 1.0
 */
public enum PaymentStatus {
    
    DRAFT("DRAFT", "Brouillon", "Paiement en cours de saisie"),
    PENDING("PENDING", "En attente", "Paiement préparé, en attente de confirmation"),
    COMPLETED("COMPLETED", "Payé", "Paiement effectué et enregistré"),
    CANCELLED("CANCELLED", "Annulé", "Paiement annulé");
    
    private final String code;
    private final String label;
    private final String description;
    
    PaymentStatus(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    
    public static PaymentStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }
        for (PaymentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Statut inconnu : " + code);
    }
    
    public static List<String> getCodes() {
        return Arrays.stream(values())
                .map(PaymentStatus::getCode)
                .collect(Collectors.toList());
    }
    
    public boolean isExecutable() {
        return this == DRAFT || this == PENDING;
    }
    
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    public boolean isCancellable() {
        return this != COMPLETED && this != CANCELLED;
    }
    
    public boolean isModifiable() {
        return this == DRAFT || this == PENDING;
    }
}
