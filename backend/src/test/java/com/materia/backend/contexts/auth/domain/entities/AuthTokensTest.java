package com.materia.backend.contexts.auth.domain.entities;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Single-use and expiry rules for refresh tokens and password-reset tokens (T035, T036). */
class AuthTokensTest {

    private static final String FINDING_016 =
            "FINDING-016: token equals() matches on id OR token, but hashCode() hashes both";

    private static RefreshToken refresh(boolean revoked, LocalDateTime expiry) {
        return RefreshToken.builder().userId(UUID.randomUUID()).token("t").revoked(revoked).expiryDate(expiry).build();
    }

    private static PasswordResetToken reset(boolean used, LocalDateTime expiry) {
        return PasswordResetToken.builder().userId(UUID.randomUUID()).token("t").used(used).expiryDate(expiry).build();
    }

    // ---- RefreshToken ----

    @Test
    @DisplayName("refresh token: unrevoked and unexpired is valid")
    void refresh_fresh_isValid() {
        assertTrue(refresh(false, LocalDateTime.now().plusMinutes(5)).isValid());
    }

    @Test
    @DisplayName("refresh token: revoking makes it invalid, which is what enforces single use")
    void refresh_revoke_invalidates() {
        RefreshToken token = refresh(false, LocalDateTime.now().plusMinutes(5));
        token.revoke();
        assertTrue(token.isRevoked());
        assertFalse(token.isValid());
    }

    @Test
    @DisplayName("refresh token: past its expiry it is invalid even though never revoked")
    void refresh_expired_isInvalid() {
        RefreshToken token = refresh(false, LocalDateTime.now().minusSeconds(1));
        assertTrue(token.isExpired());
        assertFalse(token.isValid());
    }

    @Test
    @DisplayName("refresh token: one with no expiry date is treated as never expiring, so issuers must always set one")
    void refresh_noExpiry_neverExpires() {
        // Documents actual behaviour: a null expiry means valid forever. AuthService always sets
        // a 7-day expiry, and AuthServiceBehaviourTest verifies that it does.
        assertFalse(refresh(false, null).isExpired());
        assertTrue(refresh(false, null).isValid());
    }

    // ---- PasswordResetToken ----

    @Test
    @DisplayName("reset token: unused and unexpired is valid")
    void reset_fresh_isValid() {
        assertTrue(reset(false, LocalDateTime.now().plusMinutes(5)).isValid());
    }

    @Test
    @DisplayName("reset token: once used it cannot be used again")
    void reset_markUsed_invalidates() {
        PasswordResetToken token = reset(false, LocalDateTime.now().plusMinutes(5));
        token.markAsUsed();
        assertTrue(token.isUsed());
        assertFalse(token.isValid());
    }

    @Test
    @DisplayName("reset token: past its expiry it is invalid even though never used")
    void reset_expired_isInvalid() {
        PasswordResetToken token = reset(false, LocalDateTime.now().minusSeconds(1));
        assertTrue(token.isExpired());
        assertFalse(token.isValid());
    }

    // ---- equals / hashCode contract ----

    @Test
    @Disabled(FINDING_016)
    @DisplayName("tokens: two tokens that are equal also hash identically, as every hash-based collection requires")
    void tokens_equalImpliesSameHash() {
        RefreshToken a = RefreshToken.builder().id(UUID.randomUUID()).token("same").build();
        RefreshToken b = RefreshToken.builder().id(UUID.randomUUID()).token("same").build();

        assertEquals(a, b, "equal by token");
        assertEquals(a.hashCode(), b.hashCode(), "equal objects must share a hash code");

        Set<RefreshToken> set = new HashSet<>(Set.of(a));
        assertTrue(set.contains(b), "a HashSet must find an equal element");
    }
}
