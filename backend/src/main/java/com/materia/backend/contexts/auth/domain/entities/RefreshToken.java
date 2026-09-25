package com.materia.backend.contexts.auth.domain.entities;

import com.materia.backend.common.domain.BaseEntity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 🔹 REFRESH TOKEN DOMAIN ENTITY
 * 
 * Represents a refresh token entity within the auth bounded context.
 * Pure domain class — decoupled from persistence framework.
 */
public class RefreshToken extends BaseEntity {
    
    private String token;
    private UUID userId;
    private LocalDateTime expiryDate;
    private boolean revoked;
    private String deviceInfo;
    private String ipAddress;
    
    public RefreshToken() {
        super();
        this.revoked = false;
    }
    
    public RefreshToken(Builder builder) {
        super();
        if (builder.id != null) this.id = builder.id;
        this.token = builder.token;
        this.userId = builder.userId;
        this.expiryDate = builder.expiryDate;
        this.revoked = builder.revoked;
        this.deviceInfo = builder.deviceInfo;
        this.ipAddress = builder.ipAddress;
        
        if (builder.createdAt != null) this.setCreatedAt(builder.createdAt);
        if (builder.updatedAt != null) this.setUpdatedAt(builder.updatedAt);
    }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private UUID id;
        private String token;
        private UUID userId;
        private LocalDateTime expiryDate;
        private boolean revoked = false;
        private String deviceInfo;
        private String ipAddress;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder token(String token) { this.token = token; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder expiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder revoked(boolean revoked) { this.revoked = revoked; return this; }
        public Builder deviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public RefreshToken build() {
            if (this.id == null) this.id = UUID.randomUUID();
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
            return new RefreshToken(this);
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

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }
    
    public boolean isValid() {
        return !revoked && !isExpired();
    }
    
    public void revoke() {
        this.revoked = true;
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(getId(), that.getId()) ||
               Objects.equals(token, that.token);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), token);
    }
}
