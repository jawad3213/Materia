package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.directory;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequesterDirectory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves requester names from the user accounts held by the auth context. */
@Component
public class AuthRequesterDirectory implements RequesterDirectory {

    private final UserRepository userRepository;

    public AuthRequesterDirectory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String displayName(String userId) {
        if (userId == null || userId.isBlank()) {
            return userId;
        }
        try {
            return userRepository.findById(UUID.fromString(userId.trim())).map(AuthRequesterDirectory::nameOf).orElse(userId);
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
