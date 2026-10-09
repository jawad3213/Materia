package com.materia.backend.contexts.auth.infrastructure.adapters.in.directory;

import com.materia.backend.common.domain.ports.out.UserDisplayNames;
import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** User names for shared code (attachments, change history), from the identity context's own directory. */
@Component
public class AuthUserDisplayNames implements UserDisplayNames {

    private final UserDirectoryUseCase directory;

    public AuthUserDisplayNames(UserDirectoryUseCase directory) {
        this.directory = directory;
    }

    @Override
    public Optional<String> displayName(String userId) {
        return directory.findUser(userId).map(UserSummary::displayName);
    }
}
