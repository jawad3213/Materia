package com.materia.backend.contexts.auth.domain.entities;

import com.materia.backend.common.domain.BaseEntity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 🔹 PASSWORD RESET TOKEN DOMAIN ENTITY
 * 
 * Represents a password reset token in the auth bounded context.
 * Pure domain class — decoupled from persistence framework.
 */
public class PasswordResetToken extends BaseEntity {
    
    private String token;
    private UUID userId;
    private String email;
    private LocalDateTime expiryDate;
    private boolean used;
    
    public PasswordResetToken() {
        super();
        this.used = false;
    }
    
    public PasswordResetToken(Builder builder) {
        super();
        if (builder.id != null) this.id = builder.id;
        this.token = builder.token;
        this.userId = builder.userId;
        this.email = builder.email;
        this.expiryDate = builder.expiryDate;
        this.used = builder.used;
        
        if (builder.createdAt != null) this.setCreatedAt(builder.createdAt);
        if (builder.updatedAt != null) this.setUpdatedAt(builder.updatedAt);
    }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private UUID id;
        private String token;
        private UUID userId;
        private String email;
        private LocalDateTime expiryDate;
        private boolean used = false;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder token(String token) { this.token = token; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder expiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder used(boolean used) { this.used = used; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public PasswordResetToken build() {
            if (this.id == null) this.id = UUID.randomUUID();
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
            return new PasswordResetToken(this);
        }
    }
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }
    
    public boolean isValid() {
        return !used && !isExpired();
    }
    
    public void markAsUsed() {
        this.used = true;
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordResetToken that = (PasswordResetToken) o;
        return Objects.equals(getId(), that.getId()) ||
               Objects.equals(token, that.token);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), token);
    }
}
