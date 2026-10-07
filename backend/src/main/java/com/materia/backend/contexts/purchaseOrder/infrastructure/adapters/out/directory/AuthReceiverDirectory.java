package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.directory;

import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Resolves assignable receivers from the user accounts held by the auth context. */
@Component
public class AuthReceiverDirectory implements ReceiverDirectory {

    private static final String RECEIVER_ROLE = "RECEIVER";

    private final UserDirectoryUseCase users;

    public AuthReceiverDirectory(UserDirectoryUseCase users) {
        this.users = users;
    }

    @Override
    public List<Receiver> findAssignableReceivers() {
        return users.findUsersWithRole(RECEIVER_ROLE).stream()
                .filter(AuthReceiverDirectory::isAssignable)
                .map(AuthReceiverDirectory::toReceiver)
                .sorted(Comparator.comparing(Receiver::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public Optional<Receiver> findAssignableReceiver(String userId) {
        return users.findUser(userId)
                .filter(user -> RECEIVER_ROLE.equals(user.roleCode()))
                .filter(AuthReceiverDirectory::isAssignable)
                .map(AuthReceiverDirectory::toReceiver);
    }

    /** Only an enabled account with the ACTIVE status can be assigned; a missing status does not qualify. */
    private static boolean isAssignable(UserSummary user) {
        return user.enabled() && "ACTIVE".equals(user.statusCode());
    }

    private static Receiver toReceiver(UserSummary user) {
        return new Receiver(user.id().toString(), user.displayName(), user.email());
    }
}
