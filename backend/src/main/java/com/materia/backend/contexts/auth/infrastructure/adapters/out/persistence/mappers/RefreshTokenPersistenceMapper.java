package com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.auth.domain.entities.RefreshToken;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.entities.RefreshTokenJpaEntity;
import org.springframework.stereotype.Component;

/**
 * 🔹 REFRESH TOKEN PERSISTENCE MAPPER
 * 
 * Maps between RefreshToken domain entity and RefreshTokenJpaEntity.
 */
@Component
public class RefreshTokenPersistenceMapper {

    public RefreshToken toDomainEntity(RefreshTokenJpaEntity jpa) {
        if (jpa == null) return null;

        return RefreshToken.builder()
                .id(jpa.getId())
                .token(jpa.getTokenHash())
                .userId(jpa.getUserId())
                .expiryDate(jpa.getExpiryDate())
                .revoked(jpa.isRevoked())
                .deviceInfo(jpa.getDeviceInfo())
                .ipAddress(jpa.getIpAddress())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    public RefreshTokenJpaEntity toJpaEntity(RefreshToken domain) {
        if (domain == null) return null;

        RefreshTokenJpaEntity jpa = new RefreshTokenJpaEntity();
        if (domain.getId() != null) {
            jpa.setId(domain.getId());
        }
        jpa.setTokenHash(domain.getToken());
        jpa.setUserId(domain.getUserId());
        jpa.setExpiryDate(domain.getExpiryDate());
        jpa.setRevoked(domain.isRevoked());
        jpa.setDeviceInfo(domain.getDeviceInfo());
        jpa.setIpAddress(domain.getIpAddress());
        if (domain.getCreatedAt() != null) {
            jpa.setCreatedAt(domain.getCreatedAt());
        }
        if (domain.getUpdatedAt() != null) {
            jpa.setUpdatedAt(domain.getUpdatedAt());
        }
        return jpa;
    }
}
