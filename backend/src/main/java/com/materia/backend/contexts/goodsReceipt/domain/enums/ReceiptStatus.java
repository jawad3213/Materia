package com.materia.backend.contexts.goodsReceipt.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ReceiptStatus {
    
    DRAFT("DRAFT", "Brouillon", "Réception en cours de saisie"),
    IN_PROGRESS("IN_PROGRESS", "En cours", "Réception partielle en cours"),
    COMPLETED("COMPLETED", "Terminée", "Réception complète terminée"),
    PARTIAL("PARTIAL", "Partielle", "Réception partielle terminée"),
    CANCELLED("CANCELLED", "Annulée", "Réception annulée");
    
    private final String code;
    private final String label;
    private final String description;
    
    ReceiptStatus(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    
    public static ReceiptStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }
        for (ReceiptStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Statut inconnu : " + code);
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
                .map(ReceiptStatus::getCode)
                .collect(Collectors.toList());
    }
    
    public boolean isCompleted() {
        return this == COMPLETED || this == PARTIAL;
    }
    
    public boolean isCancellable() {
        return this == DRAFT || this == IN_PROGRESS;
    }
    
    public boolean isModifiable() {
        return this == DRAFT || this == IN_PROGRESS;
    }
}