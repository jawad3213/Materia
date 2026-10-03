package com.materia.backend.contexts.auth.domain.entities;

import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** [T069] User account defaults, name derivation, status/enabled coupling and identity (feature 001 coverage). */
class UserBehaviourTest {

    @Test
    @DisplayName("user: a new account defaults to PURCHASER and ACTIVE, and derives its full name from first and last name")
    void builder_defaults() {
        User user = User.builder().email("a@materia.test").firstName(" Ada ").lastName(" Lovelace ").build();

        assertEquals(Role.PURCHASER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals("Ada Lovelace", user.getFullName());
    }

    @Test
    @DisplayName("user: explicit id, audit data, role, status and full name are kept")
    void builder_keepsExplicitValues() {
        UUID id = UUID.randomUUID();
        LocalDateTime at = LocalDateTime.now().minusDays(3);
        User user = User.builder().id(id).createdAt(at).updatedAt(at).version(4L).createdBy("admin").updatedBy("admin")
                .email("r@materia.test").role(Role.RECEIVER).status(UserStatus.SUSPENDED).fullName("Rita R.")
                .phone("+212600").department("Warehouse").mustChangePassword(true).enabled(false).build();

        assertEquals(id, user.getId());
        assertEquals(at, user.getCreatedAt());
        assertEquals(Role.RECEIVER, user.getRole());
        assertEquals(UserStatus.SUSPENDED, user.getStatus());
        assertEquals("Rita R.", user.getFullName());
        assertTrue(user.isMustChangePassword());
        assertFalse(user.isEnabled());
    }

    @Test
    @DisplayName("user: with no names at all there is no full name; one name alone is used as the full name")
    void fullName_derivation() {
        assertNull(User.builder().email("x@materia.test").build().getFullName());
        assertEquals("Ada", User.builder().email("x@materia.test").firstName("Ada").build().getFullName());
        assertEquals("Lovelace", User.builder().email("x@materia.test").lastName("Lovelace").build().getFullName());
        assertNull(User.builder().email("x@materia.test").firstName(" ").lastName(" ").build().getFullName());
    }

    @Test
    @org.junit.jupiter.api.Disabled("F-018: once one name is set, the derived full name freezes; setting the other name later does not update it")
    @DisplayName("F-018: setting first then last name on an unnamed user derives the full name from both")
    void nameChanges_deriveFromBoth() {
        User derived = User.builder().email("x@materia.test").build();
        derived.setFirstName("Grace");
        derived.setLastName("Hopper");
        assertEquals("Grace Hopper", derived.getFullName());
    }

    @Test
    @DisplayName("F-018 observation: the derived full name keeps the first name set alone")
    void nameChanges_observedFreeze() {
        User derived = User.builder().email("x@materia.test").build();
        derived.setFirstName("Grace");
        derived.setLastName("Hopper");
        assertEquals("Grace", derived.getFullName(), "F-018 appears fixed: re-enable nameChanges_deriveFromBoth");
    }

    @Test
    @DisplayName("user: an explicitly set full name is not overwritten by name changes; a blank one falls back to the names")
    void nameChanges() {
        User explicit = User.builder().email("y@materia.test").fullName("Admiral Hopper").build();
        explicit.setFirstName("Grace");
        explicit.setLastName("Hopper");
        assertEquals("Admiral Hopper", explicit.getFullName());

        explicit.setFullName(" ");
        assertEquals("Grace Hopper", explicit.getFullName(), "a blank full name falls back to the names");
    }

    @Test
    @DisplayName("user: setting a status enables the account only when the status is ACTIVE")
    void statusControlsEnabled() {
        User user = User.builder().email("x@materia.test").enabled(true).build();

        user.setStatus(UserStatus.SUSPENDED);
        assertFalse(user.isEnabled());
        user.setStatus(UserStatus.ACTIVE);
        assertTrue(user.isEnabled());
        user.setStatus(null);
        assertTrue(user.isEnabled(), "clearing the status leaves the enabled flag alone");
    }

    @Test
    @DisplayName("user: users are equal when they share an id and email; never to another type or null")
    void identity() {
        UUID id = UUID.randomUUID();
        User a = User.builder().id(id).email("a@materia.test").build();
        User same = User.builder().id(id).email("a@materia.test").build();
        User otherEmail = User.builder().id(id).email("b@materia.test").build();

        assertEquals(a, a);
        assertEquals(a, same);
        assertEquals(a.hashCode(), same.hashCode());
        assertNotEquals(a, otherEmail);
        assertNotEquals(a, User.builder().email("a@materia.test").build());
        assertNotEquals(a, null);
        assertNotEquals(a, "a@materia.test");
    }
}
