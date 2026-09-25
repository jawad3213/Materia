package com.materia.backend.contexts.employee.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeAlreadyTerminatedException;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeFullName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 🔹 EMPLOYEE DOMAIN ENTITY
 *
 * Core business actor within the enterprise.
 * Decoupled from security credentials, holding a reference to auth User UUID.
 */
public class Employee extends BaseEntity {

    private EmployeeCode code;
    private EmployeeFullName fullName;
    private String email;
    private String phone;
    private EmploymentStatus status;
    private UUID userId; // Link to auth context User ID (nullable)
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private String terminationReason;

    public Employee() {
        super();
        this.status = EmploymentStatus.ACTIVE;
    }

    private Employee(Builder builder) {
        super();
        if (builder.id != null) {
            this.id = builder.id;
        }
        if (builder.createdAt != null) {
            this.createdAt = builder.createdAt;
        }
        if (builder.updatedAt != null) {
            this.updatedAt = builder.updatedAt;
        }
        this.version = builder.version;
        this.createdBy = builder.createdBy;
        this.updatedBy = builder.updatedBy;
        this.code = builder.code;
        this.fullName = builder.fullName;
        this.email = builder.email != null ? builder.email.trim().toLowerCase() : null;
        this.phone = builder.phone;
        this.status = builder.status != null ? builder.status : EmploymentStatus.ACTIVE;
        this.userId = builder.userId;
        this.hireDate = builder.hireDate != null ? builder.hireDate : LocalDate.now();
        this.terminationDate = builder.terminationDate;
        this.terminationReason = builder.terminationReason;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ============================================================
    // DOMAIN BUSINESS METHODS
    // ============================================================

    public void updateDetails(EmployeeFullName fullName, String phone) {
        if (this.status == EmploymentStatus.TERMINATED) {
            throw new EmployeeAlreadyTerminatedException(this.code != null ? this.code.getValue() : "UNKNOWN");
        }
        if (fullName != null) {
            this.fullName = fullName;
        }
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(EmploymentStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Employment status cannot be null");
        }
        if (this.status == EmploymentStatus.TERMINATED && newStatus != EmploymentStatus.TERMINATED) {
            throw new EmployeeAlreadyTerminatedException(this.code != null ? this.code.getValue() : "UNKNOWN");
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void terminate(LocalDate terminationDate, String reason) {
        if (this.status == EmploymentStatus.TERMINATED) {
            throw new EmployeeAlreadyTerminatedException(this.code != null ? this.code.getValue() : "UNKNOWN");
        }
        this.status = EmploymentStatus.TERMINATED;
        this.terminationDate = terminationDate != null ? terminationDate : LocalDate.now();
        this.terminationReason = reason != null ? reason.trim() : "Standard Offboarding";
        this.updatedAt = LocalDateTime.now();
    }

    public void linkUserId(UUID userId) {
        this.userId = userId;
        this.updatedAt = LocalDateTime.now();
    }

    public void unlinkUserId() {
        this.userId = null;
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public EmployeeCode getCode() {
        return code;
    }

    public void setCode(EmployeeCode code) {
        this.code = code;
    }

    public EmployeeFullName getFullName() {
        return fullName;
    }

    public void setFullName(EmployeeFullName fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public EmploymentStatus getStatus() {
        return status;
    }

    public void setStatus(EmploymentStatus status) {
        this.status = status;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }

    // ============================================================
    // BUILDER
    // ============================================================

    public static class Builder {
        private UUID id;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long version;
        private String createdBy;
        private String updatedBy;
        private EmployeeCode code;
        private EmployeeFullName fullName;
        private String email;
        private String phone;
        private EmploymentStatus status = EmploymentStatus.ACTIVE;
        private UUID userId;
        private LocalDate hireDate = LocalDate.now();
        private LocalDate terminationDate;
        private String terminationReason;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder code(EmployeeCode code) {
            this.code = code;
            return this;
        }

        public Builder fullName(EmployeeFullName fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder status(EmploymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder hireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
            return this;
        }

        public Builder terminationDate(LocalDate terminationDate) {
            this.terminationDate = terminationDate;
            return this;
        }

        public Builder terminationReason(String terminationReason) {
            this.terminationReason = terminationReason;
            return this;
        }

        public Employee build() {
            return new Employee(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(code, employee.code) || Objects.equals(email, employee.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), code, email);
    }
}
