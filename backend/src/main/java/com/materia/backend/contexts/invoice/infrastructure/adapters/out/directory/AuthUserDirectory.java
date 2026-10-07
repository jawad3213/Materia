package com.materia.backend.contexts.invoice.infrastructure.adapters.out.directory;

import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.invoice.domain.ports.out.UserDirectory;
import org.springframework.stereotype.Component;

/** Resolves display names from the user accounts held by the auth context. */
@Component("invoiceUserDirectory")
public class AuthUserDirectory implements UserDirectory {

    private final UserDirectoryUseCase users;

    public AuthUserDirectory(UserDirectoryUseCase users) {
        this.users = users;
    }

    @Override
    public String displayName(String userId) {
        return users.findUser(userId).map(UserSummary::displayName).orElse(userId);
    }
}
