package com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.auth.domain.entities.PasswordResetToken;
import com.materia.backend.contexts.auth.domain.entities.RefreshToken;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.domain.ports.out.PasswordResetTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.RefreshTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auth persistence adapters against real PostgreSQL with the migrated schema (T046, T047).
 * Each test rolls back, and fixtures use unique emails to avoid the seed accounts.
 */
class AuthPersistenceIT extends AbstractIntegrationTest {

    private static final String FINDING_018 =
            "FINDING-018: email uniqueness is case-sensitive in the database, so case variants can coexist";

    @Autowired private UserRepository users;
    @Autowired private RefreshTokenRepository refreshTokens;
    @Autowired private PasswordResetTokenRepository resetTokens;
    @PersistenceContext private EntityManager em;

    private static String uniqueEmail() {
        return "it-" + UUID.randomUUID() + "@example.com";
    }

    private User newUser(String email) {
        return User.builder().email(email).passwordHash("hash").role(Role.RECEIVER)
                .status(UserStatus.ACTIVE).enabled(true).firstName("Ada").lastName("Lovelace")
                .mustChangePassword(true).build();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("users: a saved user reads back with every field intact")
    void user_roundTrip() {
        String email = uniqueEmail();
        User saved = users.save(newUser(email));
        flushAndClear();

        User read = users.findById(saved.getId()).orElseThrow();
        assertEquals(email, read.getEmail());
        assertEquals(Role.RECEIVER, read.getRole());
        assertEquals(UserStatus.ACTIVE, read.getStatus());
        assertTrue(read.isEnabled());
        assertTrue(read.isMustChangePassword(), "the column restored by the drift fix must persist");
        assertEquals("Ada Lovelace", read.getFullName());
    }

    @Test
    @DisplayName("users: a second account with the same email is rejected by the database")
    void user_duplicateEmail_isRejected() {
        String email = uniqueEmail();
        users.save(newUser(email));
        flushAndClear();

        users.save(newUser(email));
        assertThrows(RuntimeException.class, this::flushAndClear);
    }

    @Test
    @DisplayName("users: lookup matches the stored form exactly, so callers must normalise first, as AuthService does")
    void user_lookupIsExactMatch() {
        String email = uniqueEmail();
        users.save(newUser(email));
        flushAndClear();

        assertTrue(users.findByEmail(email).isPresent());
        // Documents actual behaviour: the adapter does not normalise. AuthService trims and
        // lower-cases before calling, verified in AuthServiceBehaviourTest.
        assertTrue(users.findByEmail(email.toUpperCase()).isEmpty());
        assertTrue(users.existsByEmail(email));
    }

    @Test
    @Disabled(FINDING_018)
    @DisplayName("users: two accounts whose emails differ only by case are rejected as duplicates")
    void user_caseVariantEmail_isRejected() {
        String email = uniqueEmail();
        users.save(newUser(email));
        flushAndClear();

        users.save(newUser(email.toUpperCase()));
        assertThrows(RuntimeException.class, this::flushAndClear);
    }

    @Test
    @DisplayName("refresh tokens: revoking all for a user revokes every one of their tokens and nobody else's")
    void refreshTokens_revokeAllForUser() {
        User owner = users.save(newUser(uniqueEmail()));
        User other = users.save(newUser(uniqueEmail()));
        flushAndClear();

        LocalDateTime later = LocalDateTime.now().plusDays(1);
        refreshTokens.save(RefreshToken.builder().userId(owner.getId()).token("a-" + UUID.randomUUID()).expiryDate(later).build());
        refreshTokens.save(RefreshToken.builder().userId(owner.getId()).token("b-" + UUID.randomUUID()).expiryDate(later).build());
        RefreshToken bystander = refreshTokens.save(
                RefreshToken.builder().userId(other.getId()).token("c-" + UUID.randomUUID()).expiryDate(later).build());
        flushAndClear();

        refreshTokens.revokeAllForUserId(owner.getId());
        flushAndClear();

        assertTrue(refreshTokens.findByUserId(owner.getId()).stream().allMatch(RefreshToken::isRevoked));
        assertFalse(refreshTokens.findByToken(bystander.getToken()).orElseThrow().isRevoked(),
                "another user's session must be untouched");
    }

    @Test
    @DisplayName("refresh tokens: a token is found by its stored hash, and the revoked flag persists")
    void refreshToken_findByTokenAndRevoke() {
        User owner = users.save(newUser(uniqueEmail()));
        String hash = "sha256:" + UUID.randomUUID();
        RefreshToken token = refreshTokens.save(RefreshToken.builder().userId(owner.getId()).token(hash)
                .expiryDate(LocalDateTime.now().plusDays(1)).build());
        flushAndClear();

        RefreshToken read = refreshTokens.findByToken(hash).orElseThrow();
        read.revoke();
        refreshTokens.save(read);
        flushAndClear();

        assertTrue(refreshTokens.findByToken(hash).orElseThrow().isRevoked());
        assertEquals(token.getUserId(), read.getUserId());
    }

    @Test
    @DisplayName("reset tokens: invalidating all for a user spends every outstanding reset link")
    void resetTokens_invalidateAllForUser() {
        User owner = users.save(newUser(uniqueEmail()));
        String hash = "sha256:" + UUID.randomUUID();
        resetTokens.save(PasswordResetToken.builder().userId(owner.getId()).email(owner.getEmail())
                .token(hash).expiryDate(LocalDateTime.now().plusMinutes(30)).used(false).build());
        flushAndClear();

        resetTokens.invalidateAllForUserId(owner.getId());
        flushAndClear();

        assertFalse(resetTokens.findByToken(hash).orElseThrow().isValid());
    }
}
