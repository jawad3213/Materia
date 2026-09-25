package com.materia.backend.contexts.employee.domain.ports.out;

import java.util.UUID;

/**
 * 🔹 CREDENTIAL PORT (OUTBOUND PORT)
 *
 * Outbound contract defining what the Employee context needs from the Auth / Security identity provider.
 * Allows provisioning login accounts and revoking security access without coupling domain logic to Auth entities.
 */
public interface CredentialPort {

    /**
     * Provisions a user account in the auth security system.
     *
     * @param email user email address (must be unique)
     * @param firstName employee first name
     * @param lastName employee last name
     * @param roleCode role code (e.g. ADMIN, PURCHASER, RECEIVER); blank falls back to the
     *                 default role, an unknown code is rejected
     * @param rawPassword initial password; when blank, a policy-compliant temporary password
     *                    is generated and emailed to the employee
     * @return generated User UUID
     */
    UUID provisionUserAccount(String email, String firstName, String lastName, String roleCode, String rawPassword);

    /**
     * Revokes user login access and terminates all active sessions / refresh tokens.
     *
     * @param userId UUID of the user account in auth system
     */
    void revokeUserAccess(UUID userId);

    /**
     * Checks if a user account currently exists and is active.
     * Used to keep revocation idempotent: an account that is already revoked is left untouched.
     */
    boolean isUserActive(UUID userId);

    /**
     * Retrieves the assigned role code (e.g. ADMIN, PURCHASER, RECEIVER) for a user account.
     * Returns null if userId is null or user not found.
     */
    String getUserRoleCode(UUID userId);

    /**
     * Retrieves the assigned role code by email address.
     * Returns null if email is null or user not found.
     */
    String getUserRoleCodeByEmail(String email);
}
