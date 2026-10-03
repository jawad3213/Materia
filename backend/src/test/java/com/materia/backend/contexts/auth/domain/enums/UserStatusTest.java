package com.materia.backend.contexts.auth.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/** [T066] Account status codes and which statuses allow sign-in (feature 001 coverage). */
class UserStatusTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(UserStatus.class)
    @DisplayName("user status: each status is found by its code, ignoring case and spaces; only ACTIVE is active")
    void lookupAndActivity(UserStatus status) {
        assertEquals(status, UserStatus.fromCode(" " + status.getCode().toLowerCase() + " "));
        assertEquals(status == UserStatus.ACTIVE, status.isActive());
        assertFalse(status.getLabel().isBlank());
        assertFalse(status.getDescription().isBlank());
    }

    @Test
    @DisplayName("user status: a null code means no status; an unknown code is refused")
    void nullAndUnknown() {
        assertNull(UserStatus.fromCode(null));
        assertThrows(IllegalArgumentException.class, () -> UserStatus.fromCode("LOCKED"));
    }
}
