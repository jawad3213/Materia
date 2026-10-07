package com.materia.backend.contexts.auth.application.dtos;

import java.util.UUID;

/**
 * What other contexts may know about a user account: identity, display name, role and whether it can sign in.
 *
 * @param displayName the full name, else the e-mail, else the id
 * @param statusCode  the account status code (ACTIVE, INACTIVE...); may be null on legacy accounts
 */
public record UserSummary(UUID id, String email, String displayName, String roleCode, boolean enabled,
                          String statusCode) {

    /** Enabled and not deactivated; an account without a status counts as active. */
    public boolean isActive() {
        return enabled && (statusCode == null || "ACTIVE".equals(statusCode));
    }
}
