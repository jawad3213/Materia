package com.materia.backend.common.domain.ports.out;

import java.util.Optional;

/**
 * The name to show for a user id, provided by the identity context. Shared code (attachments, the change history)
 * asks this instead of reading the users table, so it keeps working once identity is a separate service.
 */
public interface UserDisplayNames {

    /** The user's full name, else their e-mail; empty when the id is not a known user. */
    Optional<String> displayName(String userId);
}
