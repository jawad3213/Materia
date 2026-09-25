package com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.auth.domain.entities.PasswordResetToken;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.entities.PasswordResetTokenJpaEntity;
import org.springframework.stereotype.Component;

/**
 * 🔹 PASSWORD RESET TOKEN PERSISTENCE MAPPER
 * 
 * Maps between PasswordResetToken domain entity and PasswordResetTokenJpaEntity.
 */
@Component
public class PasswordResetTokenPersistenceMapper {

    public PasswordResetToken toDomainEntity(PasswordResetTokenJpaEntity jpa) {
        if (jpa == null) return null;

        return PasswordResetToken.builder()
                .id(jpa.getId())
                .token(jpa.getToken())
                .userId(jpa.getUserId())
                .email(jpa.getEmail())
                .expiryDate(jpa.getExpiryDate())
                .used(jpa.isUsed())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    public PasswordResetTokenJpaEntity toJpaEntity(PasswordResetToken domain) {
        if (domain == null) return null;

        PasswordResetTokenJpaEntity jpa = new PasswordResetTokenJpaEntity();
        if (domain.getId() != null) {
            jpa.setId(domain.getId());
        }
        jpa.setToken(domain.getToken());
        jpa.setUserId(domain.getUserId());
        jpa.setEmail(domain.getEmail());
        jpa.setExpiryDate(domain.getExpiryDate());
        jpa.setUsed(domain.isUsed());
        if (domain.getCreatedAt() != null) {
            jpa.setCreatedAt(domain.getCreatedAt());
        }
        if (domain.getUpdatedAt() != null) {
            jpa.setUpdatedAt(domain.getUpdatedAt());
        }
        return jpa;
    }
}
