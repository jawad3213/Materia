package com.materia.backend.contexts.auth.domain.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/** [T067] Password strength rules and email normalisation (feature 001 coverage). */
class AuthValueObjectsCoverageTest {

    @Test
    @DisplayName("password: a strong raw password is accepted, is not marked hashed, and never prints its value")
    void strongPassword_isAccepted() {
        Password password = Password.fromRaw("Str0ng!Pass");

        assertFalse(password.isHashed());
        assertEquals("********", password.toString());
        assertEquals(Password.fromRaw("Str0ng!Pass"), password);
        assertEquals(password.hashCode(), Password.fromRaw("Str0ng!Pass").hashCode());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"Sh0rt!", "nouppercase1!", "NOLOWERCASE1!", "NoDigitsHere!", "NoSpecial123"})
    @DisplayName("password: empty, short, or missing an uppercase, lowercase, digit or special character is refused")
    void weakPasswords_areRefused(String raw) {
        assertThrows(IllegalArgumentException.class, () -> Password.fromRaw(raw));
    }

    @Test
    @DisplayName("password: a stored hash is accepted as-is and compared by value")
    void hash_isAcceptedAsIs() {
        Password hash = Password.fromHash("$2a$10$abc");

        assertTrue(hash.isHashed());
        assertEquals("$2a$10$abc", hash.getHashedValue());
        assertNotEquals(hash, Password.fromHash("$2a$10$xyz"));
        assertNotEquals(hash, null);
        assertNotEquals(hash, "$2a$10$abc");
        assertEquals(hash, hash);
    }

    @Test
    @DisplayName("email: addresses are trimmed and lower-cased, and compared by value")
    void email_isNormalised() {
        Email email = Email.of("  Alice@Materia.COM ");

        assertEquals("alice@materia.com", email.getValue());
        assertEquals("alice@materia.com", email.toString());
        assertEquals(Email.of("alice@materia.com"), email);
        assertEquals(email.hashCode(), Email.of("ALICE@materia.com").hashCode());
        assertNotEquals(email, Email.of("bob@materia.com"));
        assertNotEquals(email, null);
        assertNotEquals(email, "alice@materia.com");
        assertEquals(email, email);
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "no-at-sign", "a@b", "a@@b.com"})
    @DisplayName("email: blank and malformed addresses are refused")
    void badEmails_areRefused(String raw) {
        assertThrows(IllegalArgumentException.class, () -> Email.of(raw));
    }
}
