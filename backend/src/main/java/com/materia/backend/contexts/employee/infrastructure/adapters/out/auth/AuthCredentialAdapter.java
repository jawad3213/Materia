package com.materia.backend.contexts.employee.infrastructure.adapters.out.auth;

import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.AccountProvisioningUseCase;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.employee.domain.exceptions.InvalidRoleCodeException;
import com.materia.backend.contexts.employee.domain.ports.out.CredentialPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 🔹 AUTH CREDENTIAL ADAPTER
 *
 * Implements the CredentialPort outbound interface through the public use cases of the auth context:
 * provisions the security credentials of an employee and revokes their access upon termination.
 */
@Component
public class AuthCredentialAdapter implements CredentialPort {

    private final AccountProvisioningUseCase accountProvisioning;
    private final UserDirectoryUseCase userDirectory;

    public AuthCredentialAdapter(AccountProvisioningUseCase accountProvisioning, UserDirectoryUseCase userDirectory) {
        this.accountProvisioning = accountProvisioning;
        this.userDirectory = userDirectory;
    }

    @Override
    public UUID provisionUserAccount(String email, String firstName, String lastName, String roleCode, String rawPassword) {
        rejectUnknownRole(roleCode);
        return accountProvisioning.provisionAccount(email, firstName, lastName, roleCode, rawPassword);
    }

    /** Falling back to the default role here would silently hand out the wrong permissions on a typo. */
    private void rejectUnknownRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return;
        }
        List<String> allowed = accountProvisioning.roleCodes();
        if (!allowed.contains(roleCode.trim().toUpperCase())) {
            throw new InvalidRoleCodeException(roleCode, allowed);
        }
    }

    @Override
    public void revokeUserAccess(UUID userId) {
        accountProvisioning.revokeAccess(userId);
    }

    @Override
    public boolean isUserActive(UUID userId) {
        if (userId == null) return false;
        return userDirectory.findUser(userId.toString()).map(UserSummary::isActive).orElse(false);
    }

    @Override
    public String getUserRoleCode(UUID userId) {
        if (userId == null) return null;
        return userDirectory.findUser(userId.toString()).map(UserSummary::roleCode).orElse(null);
    }

    @Override
    public String getUserRoleCodeByEmail(String email) {
        return userDirectory.findUserByEmail(email).map(UserSummary::roleCode).orElse(null);
    }
}
