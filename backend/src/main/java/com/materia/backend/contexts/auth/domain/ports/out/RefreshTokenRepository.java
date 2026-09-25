package com.materia.backend.contexts.auth.domain.ports.out;

import com.materia.backend.contexts.auth.domain.entities.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔹 REFRESH TOKEN REPOSITORY PORT (OUTPUT PORT)
 * 
 * Defines the contract for persisting, finding, and revoking refresh tokens.
 * Pure domain interface — decoupled from Spring Data or any persistence framework.
 */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findById(UUID id);

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(UUID userId);

    void revokeAllForUserId(UUID userId);

    void delete(RefreshToken refreshToken);

    void deleteByUserId(UUID userId);

    void deleteByToken(String token);

    void deleteExpired();
}
