package com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.auth.domain.entities.PasswordResetToken;
import com.materia.backend.contexts.auth.domain.ports.out.PasswordResetTokenRepository;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.entities.PasswordResetTokenJpaEntity;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.mappers.PasswordResetTokenPersistenceMapper;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.repositories.SpringDataPasswordResetTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔹 PASSWORD RESET TOKEN PERSISTENCE ADAPTER (OUTPUT ADAPTER)
 * 
 * Implements PasswordResetTokenRepository using Spring Data JPA.
 */
@Component
public class PasswordResetTokenPersistenceAdapter implements PasswordResetTokenRepository {

    private final SpringDataPasswordResetTokenRepository jpaRepository;
    private final PasswordResetTokenPersistenceMapper mapper;

    public PasswordResetTokenPersistenceAdapter(
            SpringDataPasswordResetTokenRepository jpaRepository,
            PasswordResetTokenPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenJpaEntity jpaEntity = mapper.toJpaEntity(token);
        PasswordResetTokenJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return jpaRepository.findByToken(token.trim()).map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findLatestByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return jpaRepository.findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(email.trim().toLowerCase())
                .map(mapper::toDomainEntity);
    }

    @Override
    @Transactional
    public void invalidateAllForUserId(UUID userId) {
        if (userId != null) {
            jpaRepository.invalidateAllForUserId(userId);
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
    public void deleteExpired() {
        jpaRepository.deleteExpiredOrUsed(LocalDateTime.now());
    }
}
