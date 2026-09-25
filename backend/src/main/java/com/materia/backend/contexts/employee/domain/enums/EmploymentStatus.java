package com.materia.backend.contexts.employee.domain.enums;

/**
 * 🔹 EMPLOYMENT STATUS
 * 
 * Represents the current employment lifecycle stage of an employee.
 */
public enum EmploymentStatus {
    ACTIVE("Active", true),
    PROBATION("Probation", true),
    ON_LEAVE("On Leave", false),
    SUSPENDED("Suspended", false),
    TERMINATED("Terminated", false);

    private final String label;
    private final boolean active;

    EmploymentStatus(String label, boolean active) {
        this.label = label;
        this.active = active;
    }

    public String getLabel() {
        return label;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isTerminated() {
        return this == TERMINATED;
    }

    public boolean canBeTerminated() {
        return this != TERMINATED;
    }
}
