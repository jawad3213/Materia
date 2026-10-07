package com.materia.backend.contexts.auth.application.services;

import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserDirectoryService implements UserDirectoryUseCase {

    private final UserRepository userRepository;

    public UserDirectoryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserSummary> findUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        try {
            return userRepository.findById(UUID.fromString(userId.trim())).map(UserDirectoryService::toSummary);
        } catch (IllegalArgumentException notAUuid) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserSummary> findUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim().toLowerCase()).map(UserDirectoryService::toSummary);
    }

    @Override
    public List<UserSummary> findUsersWithRole(String roleCode) {
        return userRepository.findByRole(Role.fromCode(roleCode)).stream().map(UserDirectoryService::toSummary).toList();
    }

    static UserSummary toSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getEmail(),
                displayName(user),
                user.getRole() != null ? user.getRole().getCode() : null,
                user.isEnabled(),
                user.getStatus() != null ? user.getStatus().getCode() : null);
    }

    private static String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        if (user.getEmail() != null) {
            return user.getEmail();
        }
        return user.getId() != null ? user.getId().toString() : null;
    }
}
