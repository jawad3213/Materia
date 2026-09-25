package com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.auth.domain.entities.RefreshToken;
import com.materia.backend.contexts.auth.domain.ports.out.RefreshTokenRepository;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.entities.RefreshTokenJpaEntity;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.mappers.RefreshTokenPersistenceMapper;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.repositories.SpringDataRefreshTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 🔹 REFRESH TOKEN PERSISTENCE ADAPTER (OUTPUT ADAPTER)
 * 
 * Implements RefreshTokenRepository using Spring Data JPA.
 */
@Component
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository jpaRepository;
    private final RefreshTokenPersistenceMapper mapper;

    public RefreshTokenPersistenceAdapter(
            SpringDataRefreshTokenRepository jpaRepository,
            RefreshTokenPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity;
        if (refreshToken.getId() != null) {
            Optional<RefreshTokenJpaEntity> existing = jpaRepository.findById(refreshToken.getId());
            if (existing.isPresent()) {
                entity = existing.get();
                entity.setTokenHash(refreshToken.getToken());
                entity.setUserId(refreshToken.getUserId());
                entity.setExpiryDate(refreshToken.getExpiryDate());
                entity.setRevoked(refreshToken.isRevoked());
                entity.setDeviceInfo(refreshToken.getDeviceInfo());
                entity.setIpAddress(refreshToken.getIpAddress());
                if (refreshToken.getUpdatedAt() != null) {
                    entity.setUpdatedAt(refreshToken.getUpdatedAt());
                }
            } else {
                entity = mapper.toJpaEntity(refreshToken);
            }
        } else {
            entity = mapper.toJpaEntity(refreshToken);
        }
        RefreshTokenJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findById(UUID id) {
        if (id == null) return Optional.empty();
        return jpaRepository.findById(id).map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String tokenHash) {
        if (tokenHash == null || tokenHash.trim().isEmpty()) {
            return Optional.empty();
        }
        return jpaRepository.findByTokenHash(tokenHash.trim()).map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findByUserId(UUID userId) {
        if (userId == null) return List.of();
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeAllForUserId(UUID userId) {
        if (userId != null) {
            jpaRepository.revokeAllForUserId(userId);
        }
    }

    @Override
    @Transactional
    public void delete(RefreshToken refreshToken) {
        if (refreshToken != null && refreshToken.getId() != null) {
            jpaRepository.deleteById(refreshToken.getId());
        }
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        if (userId != null) {
            jpaRepository.deleteByUserId(userId);
        }
    }

    @Override
    @Transactional
    public void deleteByToken(String tokenHash) {
        if (tokenHash != null && !tokenHash.trim().isEmpty()) {
            jpaRepository.deleteByTokenHash(tokenHash.trim());
        }
    }

    @Override
    @Transactional
    public void deleteExpired() {
        jpaRepository.deleteExpired(LocalDateTime.now());
    }
}
