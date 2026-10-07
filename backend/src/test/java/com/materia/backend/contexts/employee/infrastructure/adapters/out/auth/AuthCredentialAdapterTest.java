package com.materia.backend.contexts.employee.infrastructure.adapters.out.auth;

import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.AccountProvisioningUseCase;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.employee.domain.exceptions.InvalidRoleCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthCredentialAdapterTest {

    private static final List<String> ROLE_CODES = List.of("ADMIN", "PURCHASER", "RECEIVER");

    @Mock
    private AccountProvisioningUseCase accountProvisioning;

    @Mock
    private UserDirectoryUseCase userDirectory;

    private AuthCredentialAdapter adapter;

    private final UUID testUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new AuthCredentialAdapter(accountProvisioning, userDirectory);
    }

    @Test
    @DisplayName("Should reject an unknown role code instead of downgrading it")
    void shouldRejectUnknownRoleCode() {
        when(accountProvisioning.roleCodes()).thenReturn(ROLE_CODES);

        InvalidRoleCodeException exception = assertThrows(InvalidRoleCodeException.class,
                () -> adapter.provisionUserAccount("john@example.com", "John", "Doe", "ADMNI", "Str0ng!Pass"));

        assertEquals("EMPLOYEE_INVALID_ROLE_CODE", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("ADMIN"));
        verify(accountProvisioning, never()).provisionAccount(anyString(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should provision the account through the auth context when the role code is valid")
    void shouldProvisionThroughAuth() {
        when(accountProvisioning.roleCodes()).thenReturn(ROLE_CODES);
        when(accountProvisioning.provisionAccount("john@example.com", "John", "Doe", "admin", "Str0ng!Pass"))
                .thenReturn(testUserId);

        assertEquals(testUserId, adapter.provisionUserAccount("john@example.com", "John", "Doe", "admin", "Str0ng!Pass"));
    }

    @Test
    @DisplayName("Should leave a blank role code to the default role of the auth context")
    void shouldPassBlankRoleCodeThrough() {
        adapter.provisionUserAccount("john@example.com", "John", "Doe", "  ", null);

        verify(accountProvisioning).provisionAccount("john@example.com", "John", "Doe", "  ", null);
        verify(accountProvisioning, never()).roleCodes();
    }

    @Test
    @DisplayName("Should report an account as inactive once it has been revoked")
    void shouldReportRevokedAccountAsInactive() {
        when(userDirectory.findUser(testUserId.toString())).thenReturn(Optional.of(
                new UserSummary(testUserId, "john@example.com", "John Doe", "PURCHASER", false, "INACTIVE")));

        assertFalse(adapter.isUserActive(testUserId));
        assertEquals("PURCHASER", adapter.getUserRoleCode(testUserId));
    }

    @Test
    @DisplayName("Should revoke access through the auth context")
    void shouldRevokeUserAccess() {
        adapter.revokeUserAccess(testUserId);

        verify(accountProvisioning).revokeAccess(testUserId);
    }

    @Test
    @DisplayName("Should find nobody for a missing id or e-mail")
    void shouldFindNobodyWithoutIdentity() {
        assertFalse(adapter.isUserActive(null));
        assertNull(adapter.getUserRoleCode(null));
        assertNull(adapter.getUserRoleCodeByEmail("nobody@example.com"));
        verify(userDirectory, never()).findUser(any());
    }
}
