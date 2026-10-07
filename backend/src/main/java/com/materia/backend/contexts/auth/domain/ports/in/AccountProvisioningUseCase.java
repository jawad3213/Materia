package com.materia.backend.contexts.auth.domain.ports.in;

import java.util.List;
import java.util.UUID;

/** Creates and revokes user accounts on behalf of the other contexts (the employee context). */
public interface AccountProvisioningUseCase {

    /**
     * Creates an active account that must change its password at first sign-in, and e-mails its credential.
     *
     * @param roleCode    a role code; blank grants the default role, an unknown code is rejected
     * @param rawPassword the initial password; blank generates a policy-compliant temporary one
     * @return the new account id
     */
    UUID provisionAccount(String email, String firstName, String lastName, String roleCode, String rawPassword);

    /** Disables the account and revokes all its refresh tokens; unknown ids are ignored. */
    void revokeAccess(UUID userId);

    /** The role codes an account can be given. */
    List<String> roleCodes();
}
