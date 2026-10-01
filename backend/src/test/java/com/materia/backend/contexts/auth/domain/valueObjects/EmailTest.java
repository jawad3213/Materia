package com.materia.backend.contexts.auth.domain.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    @DisplayName("of: the address is stored trimmed and lower-cased")
    void of_normalises() {
        assertEquals("jane.doe@example.com", Email.of("  Jane.Doe@Example.COM  ").getValue());
    }

    @Test
    @DisplayName("equality: addresses differing only in case or whitespace are equal")
    void equality_ignoresCaseAndWhitespace() {
        assertEquals(Email.of("jane@example.com"), Email.of(" JANE@example.com "));
        assertEquals(Email.of("jane@example.com").hashCode(), Email.of("JANE@EXAMPLE.COM").hashCode());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"plainaddress", "@example.com", "jane@", "jane@example", "jane@example.c", "jane doe@example.com"})
    @DisplayName("of: a malformed address is refused")
    void of_malformed_isRefused(String malformed) {
        assertThrows(IllegalArgumentException.class, () -> Email.of(malformed));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("of: a missing address is refused")
    void of_missing_isRefused(String missing) {
        assertThrows(IllegalArgumentException.class, () -> Email.of(missing));
    }

    @Test
    @DisplayName("of: plus-addressing and subdomains are accepted")
    void of_commonValidForms_areAccepted() {
        assertDoesNotThrow(() -> Email.of("jane+orders@mail.example.co"));
    }
}
