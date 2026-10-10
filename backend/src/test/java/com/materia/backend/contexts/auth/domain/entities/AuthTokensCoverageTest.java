package com.materia.backend.contexts.auth.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** [T068] Refresh and password-reset tokens (construction and identity; core validity rules are in AuthTokensTest): validity, single use and identity (feature 001 coverage). */
class AuthTokensCoverageTest {

    private static RefreshToken refresh(String token, LocalDateTime expiry) {
        return RefreshToken.builder().token(token).userId(UUID.randomUUID()).expiryDate(expiry)
                .deviceInfo("Firefox").ipAddress("10.0.0.1").build();
    }

    private static PasswordResetToken reset(String token, LocalDateTime expiry) {
        return PasswordResetToken.builder().token(token).userId(UUID.randomUUID()).email("a@materia.test")
                .expiryDate(expiry).build();
    }

    @Test
    @DisplayName("refresh token: valid until it expires or is revoked")
    void refresh_validity() {
        RefreshToken live = refresh("t1", LocalDateTime.now().plusHours(1));
        assertTrue(live.isValid());
        assertFalse(live.isExpired());

        live.revoke();
        assertTrue(live.isRevoked());
        assertFalse(live.isValid());
        assertNotNull(live.getUpdatedAt());

        RefreshToken expired = refresh("t2", LocalDateTime.now().minusSeconds(1));
        assertTrue(expired.isExpired());
        assertFalse(expired.isValid());

        assertFalse(refresh("t3", null).isExpired(), "a token without an expiry never expires");
    }

    @Test
    @DisplayName("refresh token: a new token is not revoked; the builder keeps id, audit dates and device details")
    void refresh_construction() {
        assertFalse(new RefreshToken().isRevoked());
        UUID id = UUID.randomUUID();
        LocalDateTime at = LocalDateTime.now().minusDays(1);
        RefreshToken token = RefreshToken.builder().id(id).token("t").revoked(true).createdAt(at).updatedAt(at).build();

        assertEquals(id, token.getId());
        assertTrue(token.isRevoked());
        assertEquals(at, token.getCreatedAt());
        assertEquals(at, token.getUpdatedAt());
    }

    @Test
    @DisplayName("refresh token: tokens are equal by token value (the id only when there is none), never to another type or null")
    void refresh_identity() {
        RefreshToken a = refresh("same", null);
        RefreshToken sameValue = refresh("same", null);
        RefreshToken other = refresh("other", null);
        RefreshToken sameId = refresh("x", null);
        sameId.setId(a.getId());

        assertEquals(a, a);
        assertEquals(a, sameValue);
        assertNotEquals(a, sameId, "a different token value is a different token, whatever its id");
        assertNotEquals(a, other);
        assertNotEquals(a, null);
        assertNotEquals(a, "same");
        assertEquals(a.hashCode(), a.hashCode());
    }

    @Test
    @DisplayName("reset token: valid until it is used or expires; using it is recorded")
    void reset_validity() {
        PasswordResetToken live = reset("r1", LocalDateTime.now().plusMinutes(15));
        assertTrue(live.isValid());

        live.markAsUsed();
        assertTrue(live.isUsed());
        assertFalse(live.isValid());
        assertNotNull(live.getUpdatedAt());

        PasswordResetToken expired = reset("r2", LocalDateTime.now().minusSeconds(1));
        assertTrue(expired.isExpired());
        assertFalse(expired.isValid());
        assertFalse(reset("r3", null).isExpired());

        PasswordResetToken flagged = reset("r4", null);
        flagged.setUsed(true);
        assertFalse(flagged.isValid());
    }

    @Test
    @DisplayName("reset token: a new token is unused; the builder keeps id and audit dates")
    void reset_construction() {
        assertFalse(new PasswordResetToken().isUsed());
        UUID id = UUID.randomUUID();
        LocalDateTime at = LocalDateTime.now().minusDays(1);
        PasswordResetToken token = PasswordResetToken.builder().id(id).token("t").used(true).createdAt(at).updatedAt(at).build();

        assertEquals(id, token.getId());
        assertTrue(token.isUsed());
        assertEquals(at, token.getCreatedAt());
        assertEquals(at, token.getUpdatedAt());
    }

    @Test
    @DisplayName("reset token: tokens are equal by token value (the id only when there is none), never to another type or null")
    void reset_identity() {
        PasswordResetToken a = reset("same", null);
        PasswordResetToken sameValue = reset("same", null);
        PasswordResetToken sameId = reset("x", null);
        sameId.setId(a.getId());

        assertEquals(a, a);
        assertEquals(a, sameValue);
        assertNotEquals(a, sameId, "a different token value is a different token, whatever its id");
        assertNotEquals(a, reset("other", null));
        assertNotEquals(a, null);
        assertNotEquals(a, "same");
        assertEquals(a.hashCode(), a.hashCode());
    }

    @Test
    @DisplayName("tokens: without a token value, a token is identified by its id, and hashes like it")
    void identity_withoutTokenValue_fallsBackToTheId() {
        RefreshToken a = refresh(null, null);
        RefreshToken sameId = refresh(null, null);
        sameId.setId(a.getId());
        RefreshToken otherId = refresh(null, null);

        assertEquals(a, sameId);
        assertEquals(a.hashCode(), sameId.hashCode());
        assertNotEquals(a, otherId);
        assertNotEquals(a, refresh("value", null), "a valued token is not an unvalued one");
        assertNotEquals(refresh("value", null), a);

        PasswordResetToken r = reset(null, null);
        PasswordResetToken rSameId = reset(null, null);
        rSameId.setId(r.getId());
        assertEquals(r, rSameId);
        assertEquals(r.hashCode(), rSameId.hashCode());
        assertNotEquals(r, reset(null, null));
        assertNotEquals(r, reset("value", null));
        assertNotEquals(reset("value", null), r);
    }

    @Test
    @DisplayName("tokens: two tokens with neither a value nor an id are not equal")
    void identity_withNothing_isNotEqual() {
        RefreshToken a = refresh(null, null);
        a.setId(null);
        RefreshToken b = refresh(null, null);
        b.setId(null);
        assertNotEquals(a, b);
        assertEquals(0, a.hashCode());

        PasswordResetToken r = reset(null, null);
        r.setId(null);
        PasswordResetToken s = reset(null, null);
        s.setId(null);
        assertNotEquals(r, s);
        assertEquals(0, r.hashCode());
    }
}
