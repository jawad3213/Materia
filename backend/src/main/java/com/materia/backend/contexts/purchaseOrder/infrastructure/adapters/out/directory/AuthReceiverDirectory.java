package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.directory;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Resolves assignable receivers from the user accounts held by the auth context. */
@Component
public class AuthReceiverDirectory implements ReceiverDirectory {

    private final UserRepository userRepository;

    public AuthReceiverDirectory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<Receiver> findAssignableReceivers() {
        return userRepository.findByRole(Role.RECEIVER).stream()
                .filter(AuthReceiverDirectory::isAssignable)
                .map(AuthReceiverDirectory::toReceiver)
                .sorted(Comparator.comparing(Receiver::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public Optional<Receiver> findAssignableReceiver(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        try {
            return userRepository.findById(UUID.fromString(userId.trim()))
                    .filter(user -> user.getRole() == Role.RECEIVER)
                    .filter(AuthReceiverDirectory::isAssignable)
                    .map(AuthReceiverDirectory::toReceiver);
        } catch (IllegalArgumentException invalidId) {
            return Optional.empty();
        }
    }

    private static boolean isAssignable(User user) {
        return user.isEnabled() && user.getStatus() == UserStatus.ACTIVE;
    }

    private static Receiver toReceiver(User user) {
        String name = user.getFullName() != null && !user.getFullName().isBlank()
                ? user.getFullName()
                : user.getEmail();
        return new Receiver(user.getId().toString(), name, user.getEmail());
    }
}
