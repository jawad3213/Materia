package com.materia.backend.contexts.purchaseOrder.domain.enums;

import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;

public enum DeliveryStatus {
    
    NOT_SHIPPED("NOT_SHIPPED", "Non expédié", "Commande non encore expédiée"),
    SHIPPED("SHIPPED", "Expédié", "Commande expédiée"),
    IN_TRANSIT("IN_TRANSIT", "En transit", "Commande en cours de transport"),
    PARTIAL("PARTIAL", "Partielle", "Commande partiellement livrée"),
    DELIVERED("DELIVERED", "Livrée", "Commande complètement livrée"),
    DELAYED("DELAYED", "Retardée", "Commande en retard");
    
    private final String code;
    private final String label;
    private final String description;
    
    DeliveryStatus(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    
    public static DeliveryStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new PurchaseOrderValidationException("Delivery status code is required");
        }
        for (DeliveryStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new PurchaseOrderValidationException("Unknown delivery status: " + code);
    }
}
