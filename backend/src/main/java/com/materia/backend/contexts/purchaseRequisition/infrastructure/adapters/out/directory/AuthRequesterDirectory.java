package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.directory;

import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequesterDirectory;
import org.springframework.stereotype.Component;

/** Resolves requester names from the user accounts held by the auth context. */
@Component
public class AuthRequesterDirectory implements RequesterDirectory {

    private final UserDirectoryUseCase users;

    public AuthRequesterDirectory(UserDirectoryUseCase users) {
        this.users = users;
    }

    @Override
    public String displayName(String userId) {
        return users.findUser(userId).map(UserSummary::displayName).orElse(userId);
    }
}
