package com.materia.backend.contexts.auth.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 🔹 USER DOMAIN ENTITY
 * 
 * Core domain representation of a user identity within the auth bounded context.
 * Pure domain class — decoupled from JPA.
 * Authenticated strictly by email.
 */
public class User extends BaseEntity {

    private String email;
    private String passwordHash;
    private Role role;
    private boolean enabled;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private UserStatus status;
    private String department;

    public User() {
        super();
        this.enabled = true;
        this.role = Role.PURCHASER;
        this.status = UserStatus.ACTIVE;
    }

    private User(Builder builder) { 
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
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.role = builder.role != null ? builder.role : Role.PURCHASER;
        this.enabled = builder.enabled;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.fullName = builder.fullName != null ? builder.fullName : computeFullName(builder.firstName, builder.lastName);
        this.phone = builder.phone;
        this.status = builder.status != null ? builder.status : UserStatus.ACTIVE;
        this.department = builder.department;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static String computeFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) return null;
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? null : combined;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        if (this.fullName == null || this.fullName.isBlank()) {
            this.fullName = computeFullName(this.firstName, this.lastName);
        }
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        if (this.fullName == null || this.fullName.isBlank()) {
            this.fullName = computeFullName(this.firstName, this.lastName);
        }
    }

    public String getFullName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return computeFullName(firstName, lastName);
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
        if (status != null) {
            this.enabled = status.isActive();
        }
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    // Builder
    public static class Builder {
        private UUID id;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long version;
        private String createdBy;
        private String updatedBy;
        private String email;
        private String passwordHash;
        private Role role = Role.PURCHASER;
        private boolean enabled = true;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phone;
        private UserStatus status = UserStatus.ACTIVE;
        private String department;

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

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), email);
    }
}
