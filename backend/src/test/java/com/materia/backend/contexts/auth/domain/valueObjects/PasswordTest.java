package com.materia.backend.contexts.auth.domain.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {

    @Test
    @DisplayName("fromRaw: a password meeting every rule is accepted")
    void fromRaw_compliant_isAccepted() {
        assertDoesNotThrow(() -> Password.fromRaw("Str0ng!Pass"));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {
            "Sh0rt!",       // under eight characters
            "lower1!case",  // no uppercase
            "UPPER1!CASE",  // no lowercase
            "NoDigits!!",   // no digit
            "NoSpecial11"   // no special character
    })
    @DisplayName("fromRaw: a password breaking any single rule is refused")
    void fromRaw_eachRuleViolation_isRefused(String weak) {
        assertThrows(IllegalArgumentException.class, () -> Password.fromRaw(weak));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("fromRaw: a missing password is refused")
    void fromRaw_missing_isRefused(String missing) {
        assertThrows(IllegalArgumentException.class, () -> Password.fromRaw(missing));
    }

    @Test
    @DisplayName("fromRaw: exactly eight characters is the minimum accepted length")
    void fromRaw_eightCharacters_isAccepted() {
        assertDoesNotThrow(() -> Password.fromRaw("Abcd12!x"));
    }

    @Test
    @DisplayName("fromHash: an existing hash is wrapped without re-validating it against the raw-password rules")
    void fromHash_skipsValidation() {
        Password hashed = Password.fromHash("$2b$12$notARawPassword");
        assertTrue(hashed.isHashed());
    }

    @Test
    @DisplayName("toString: never exposes the password, so it cannot leak into logs")
    void toString_isMasked() {
        assertEquals("********", Password.fromRaw("Str0ng!Pass").toString());
    }
}
