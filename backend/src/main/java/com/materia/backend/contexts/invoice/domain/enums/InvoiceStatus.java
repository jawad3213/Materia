package com.materia.backend.contexts.invoice.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Invoice Status Enum - Version MVP
 * 
 * @author SAP MM Team
 * @version 1.0
 */
public enum InvoiceStatus {
    
    DRAFT("DRAFT", "Brouillon", "Facture en cours de saisie"),
    SUBMITTED("SUBMITTED", "Soumise", "Facture soumise pour vérification"),
    VERIFIED("VERIFIED", "Vérifiée", "Facture vérifiée, en attente de paiement"),
    PAID("PAID", "Payée", "Facture payée"),
    CANCELLED("CANCELLED", "Annulée", "Facture annulée");
    
    private final String code;
    private final String label;
    private final String description;
    
    InvoiceStatus(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    
    public static InvoiceStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }
        for (InvoiceStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Statut inconnu : " + code);
    }
    
    public static List<String> getCodes() {
        return Arrays.stream(values())
                .map(InvoiceStatus::getCode)
                .collect(Collectors.toList());
    }
    
    public boolean isPayable() {
        return this == VERIFIED;
    }
    
    public boolean isModifiable() {
        return this == DRAFT || this == SUBMITTED;
    }
    
    public boolean isClosed() {
        return this == PAID || this == CANCELLED;
    }
}
