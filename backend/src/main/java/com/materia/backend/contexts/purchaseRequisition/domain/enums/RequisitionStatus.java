package com.materia.backend.contexts.purchaseRequisition.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Requisition status enum.
 */
public enum RequisitionStatus {

    DRAFT("DRAFT", "Draft", "Requisition is being prepared", "#94a3b8"),
    SUBMITTED("SUBMITTED", "Submitted", "Requisition submitted for processing", "#2563eb"),
    APPROVED("APPROVED", "Approved", "Requisition approved for conversion", "#22c55e"),
    REJECTED("REJECTED", "Rejected", "Requisition rejected", "#ef4444"),
    CANCELLED("CANCELLED", "Cancelled", "Requisition cancelled", "#6b7280"),
    CONVERTED("CONVERTED", "Converted", "Requisition converted to purchase order", "#8b5cf6");

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

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }

    public static RequisitionStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Status code is required");
        }
        for (RequisitionStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown requisition status: " + code);
    }

    public static boolean isValidCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(status -> status.code.equals(code));
    }

    public static List<String> getCodes() {
        return Arrays.stream(values()).map(RequisitionStatus::getCode).collect(Collectors.toList());
    }

    public static List<RequisitionStatus> getModifiableStatuses() {
        return List.of(DRAFT, SUBMITTED);
    }

    public static List<RequisitionStatus> getConvertibleStatuses() {
        return List.of(APPROVED);
    }

    public static List<RequisitionStatus> getValidatableStatuses() {
        return List.of(SUBMITTED);
    }

    public static List<RequisitionStatus> getDeletableStatuses() {
        return List.of(DRAFT, REJECTED, CANCELLED);
    }

    public static List<RequisitionStatus> getCancellableStatuses() {
        return List.of(SUBMITTED, APPROVED);
    }

    public boolean isModifiable() {
        return getModifiableStatuses().contains(this);
    }

    public boolean isConvertible() {
        return getConvertibleStatuses().contains(this);
    }

    public boolean isValidatable() {
        return getValidatableStatuses().contains(this);
    }

    public boolean isDeletable() {
        return getDeletableStatuses().contains(this);
    }

    public boolean isCancellable() {
        return getCancellableStatuses().contains(this);
    }

    public boolean isSubmittable() {
        return DRAFT.equals(this);
    }

    public boolean isApprovable() {
        return SUBMITTED.equals(this);
    }

    public boolean isRejectable() {
        return SUBMITTED.equals(this);
    }

    public boolean isTerminal() {
        return CONVERTED.equals(this) || REJECTED.equals(this) || CANCELLED.equals(this);
    }

    public boolean isActive() {
        return DRAFT.equals(this) || SUBMITTED.equals(this);
    }

    public boolean isFinal() {
        return CONVERTED.equals(this) || REJECTED.equals(this) || CANCELLED.equals(this);
    }
}


