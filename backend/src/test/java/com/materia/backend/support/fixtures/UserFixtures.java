package com.materia.backend.support.fixtures;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixture builder for User domain entity (T024, US1).
 * Supports role, enabled, status, and mustChangePassword configuration.
 */
public final class UserFixtures {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    private UserFixtures() {
    }

    public static Builder aUser() {
        return new Builder();
    }

    public static String uniqueEmail() {
        return String.format("user-%04d@materia.test", SEQUENCE.getAndIncrement() % 10000);
    }

    public static final class Builder {
        private UUID id = UUID.randomUUID();
        private String email = uniqueEmail();
        private String passwordHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"; // "password"
        private Role role = Role.PURCHASER;
        private boolean enabled = true;
        private String firstName = "John";
        private String lastName = "Doe";
        private UserStatus status = UserStatus.ACTIVE;
        private boolean mustChangePassword = false;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder status(UserStatus status) { this.status = status; return this; }
        public Builder mustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; return this; }

        public User build() {
            return User.builder()
                    .id(id)
                    .email(email)
                    .passwordHash(passwordHash)
                    .role(role)
                    .enabled(enabled)
                    .firstName(firstName)
                    .lastName(lastName)
                    .fullName(firstName + " " + lastName)
                    .status(status)
                    .mustChangePassword(mustChangePassword)
                    .build();
        }
    }
}
