package com.materia.backend.contexts.invoice.domain.enums;


public enum InvoiceType {
    
    STANDARD("STANDARD", "Standard", "Facture standard"),
    CREDIT_NOTE("CREDIT_NOTE", "Avoir", "Facture d'avoir (crédit)");
    
    private final String code;
    private final String label;
    private final String description;
    
    InvoiceType(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    
    public static InvoiceType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }
        for (InvoiceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Type inconnu : " + code);
    }
}
