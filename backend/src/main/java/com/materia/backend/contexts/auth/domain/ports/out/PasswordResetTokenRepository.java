package com.materia.backend.contexts.auth.domain.ports.out;

import com.materia.backend.contexts.auth.domain.entities.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

/**
 * 🔹 PASSWORD RESET TOKEN REPOSITORY PORT (OUTPUT PORT)
 * 
 * Defines the contract for persisting, finding, and invalidating password reset tokens.
 * Pure domain interface — no framework dependencies.
 */
public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findLatestByEmail(String email);

    void invalidateAllForUserId(UUID userId);

    void deleteByUserId(UUID userId);

    void deleteExpired();
}
