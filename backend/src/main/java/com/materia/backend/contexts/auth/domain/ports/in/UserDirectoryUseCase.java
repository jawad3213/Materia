package com.materia.backend.contexts.auth.domain.ports.in;

import com.materia.backend.contexts.auth.application.dtos.UserSummary;

import java.util.List;
import java.util.Optional;

/** Read-only lookups of user accounts for the other contexts. */
public interface UserDirectoryUseCase {

    /** The account with this id; empty when the id is blank, not a UUID or unknown. */
    Optional<UserSummary> findUser(String userId);

    /** The account with this e-mail (case-insensitive); empty when the e-mail is blank or unknown. */
    Optional<UserSummary> findUserByEmail(String email);

    /** Every account holding this role code, whatever its status. */
    List<UserSummary> findUsersWithRole(String roleCode);
}
