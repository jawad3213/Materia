package com.materia.backend.contexts.purchaseOrder.domain.enums;

import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum OrderStatus {

    DRAFT("DRAFT", "Brouillon", "Commande en cours de saisie", "#94a3b8"),
    SUBMITTED("SUBMITTED", "Soumise", "Commande soumise au fournisseur", "#f59e0b"),
    CONFIRMED("CONFIRMED", "Confirmee", "Commande confirmee par le fournisseur", "#3b82f6"),
    READY_FOR_RECEIPT("READY_FOR_RECEIPT", "Prete pour reception", "Commande assignee pour reception", "#0ea5e9"),
    RECEIVED("RECEIVED", "Recue", "Commande receptionnee", "#10b981"),
    IN_PROGRESS("IN_PROGRESS", "En cours", "Commande en cours de traitement", "#8b5cf6"),
    PARTIALLY_RECEIVED("PARTIALLY_RECEIVED", "Partiellement recue", "Commande partiellement livree", "#f59e0b"),
    COMPLETED("COMPLETED", "Terminee", "Commande completement livree", "#22c55e"),
    CANCELLED("CANCELLED", "Annulee", "Commande annulee", "#ef4444"),
    REJECTED("REJECTED", "Rejetee", "Commande rejetee par le fournisseur", "#dc2626");

    private final String code;
    private final String label;
    private final String description;
    private final String color;

    OrderStatus(String code, String label, String description, String color) {
        this.code = code;
        this.label = label;
        this.description = description;
        this.color = color;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public String getColor() { return color; }

    public static OrderStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new PurchaseOrderValidationException("Order status code is required");
        }
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new PurchaseOrderValidationException("Unknown order status: " + code);
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
                .map(OrderStatus::getCode)
                .collect(Collectors.toList());
    }

    public boolean isActive() {
        return this != CANCELLED && this != REJECTED && this != COMPLETED && this != RECEIVED;
    }

    public boolean isModifiable() {
        return this == DRAFT || this == SUBMITTED;
    }

    public boolean isCancellable() {
        return isActive() && this != COMPLETED && this != RECEIVED;
    }
}
