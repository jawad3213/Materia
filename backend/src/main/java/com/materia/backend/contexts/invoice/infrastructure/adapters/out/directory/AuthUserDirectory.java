package com.materia.backend.contexts.invoice.infrastructure.adapters.out.directory;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.invoice.domain.ports.out.UserDirectory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves display names from the user accounts held by the auth context. */
@Component("invoiceUserDirectory")
public class AuthUserDirectory implements UserDirectory {

    private final UserRepository userRepository;

    public AuthUserDirectory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String displayName(String userId) {
        if (userId == null || userId.isBlank()) {
            return userId;
        }
        try {
            return userRepository.findById(UUID.fromString(userId.trim()))
                    .map(AuthUserDirectory::nameOf)
                    .orElse(userId);
        } catch (IllegalArgumentException notAUuid) {
            return userId;
        }
    }

    private static String nameOf(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        return user.getEmail() != null ? user.getEmail() : user.getId().toString();
    }
}
