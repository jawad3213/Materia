package com.materia.backend.contexts.employee.infrastructure.adapters.out.auth;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.domain.ports.out.EmailSender;
import com.materia.backend.contexts.auth.domain.ports.out.RefreshTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.auth.domain.valueObjects.Password;
import com.materia.backend.contexts.employee.domain.exceptions.InvalidRoleCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthCredentialAdapterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailSender emailSender;

    private AuthCredentialAdapter adapter;

    private final UUID testUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new AuthCredentialAdapter(userRepository, refreshTokenRepository, passwordEncoder, emailSender);
    }

    private void stubSuccessfulSave() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(testUserId);
            return user;
        });
    }

    @Test
    @DisplayName("Should reject an unknown role code instead of downgrading it")
    void shouldRejectUnknownRoleCode() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        InvalidRoleCodeException exception = assertThrows(InvalidRoleCodeException.class,
                () -> adapter.provisionUserAccount("john@example.com", "John", "Doe", "ADMNI", "Str0ng!Pass"));

        assertEquals("EMPLOYEE_INVALID_ROLE_CODE", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("ADMIN"));
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(emailSender);
    }

    @Test
    @DisplayName("Should provision the requested role when the code is valid")
    void shouldProvisionRequestedRole() {
        stubSuccessfulSave();

        UUID userId = adapter.provisionUserAccount("john@example.com", "John", "Doe", "admin", "Str0ng!Pass");

        assertEquals(testUserId, userId);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Role.ADMIN, captor.getValue().getRole());
        assertEquals(UserStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should fall back to the default role when no code is provided")
    void shouldFallBackToDefaultRoleWhenCodeIsBlank() {
        stubSuccessfulSave();

        adapter.provisionUserAccount("john@example.com", "John", "Doe", "  ", "Str0ng!Pass");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Role.PURCHASER, captor.getValue().getRole());
    }

    @Test
    @DisplayName("Should reject a caller supplied password that violates the password policy")
    void shouldRejectWeakInitialPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> adapter.provisionUserAccount("john@example.com", "John", "Doe", "PURCHASER", "abc"));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(emailSender);
    }

    @Test
    @DisplayName("Should email a policy compliant temporary password when none is supplied")
    void shouldEmailGeneratedTemporaryPassword() {
        stubSuccessfulSave();

        adapter.provisionUserAccount("john@example.com", "John", "Doe", "PURCHASER", null);

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendTemporaryPasswordEmail(eq("john@example.com"), passwordCaptor.capture());
        // The generated password must satisfy the same policy as a user chosen one.
        assertDoesNotThrow(() -> Password.fromRaw(passwordCaptor.getValue()));
        verify(passwordEncoder).encode(passwordCaptor.getValue());
    }

    @Test
    @DisplayName("Should email the supplied password to the new user, so they receive their credential regardless of who chose it")
    void shouldEmailSuppliedPasswordToRecipient() {
        // Intended behaviour: the credential email is sent whether the password was
        // generated or supplied by the caller. The plaintext exposure this implies is a
        // recorded, accepted risk — see FINDING-001 in specs/001-backend-module-tests/research.md.
        stubSuccessfulSave();

        adapter.provisionUserAccount("john@example.com", "John", "Doe", "PURCHASER", "Str0ng!Pass");

        verify(emailSender).sendTemporaryPasswordEmail(eq("john@example.com"), eq("Str0ng!Pass"));
    }

    @Test
    @DisplayName("Should report an account as inactive once it has been revoked")
    void shouldReportRevokedAccountAsInactive() {
        User user = User.builder()
                .id(testUserId)
                .email("john@example.com")
                .role(Role.PURCHASER)
                .status(UserStatus.INACTIVE)
                .enabled(false)
                .build();
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));

        assertFalse(adapter.isUserActive(testUserId));
    }

    @Test
    @DisplayName("Should disable the account and revoke every refresh token on revocation")
    void shouldRevokeUserAccess() {
        User user = User.builder()
                .id(testUserId)
                .email("john@example.com")
                .role(Role.PURCHASER)
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .build();
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));

        adapter.revokeUserAccess(testUserId);

        assertEquals(UserStatus.INACTIVE, user.getStatus());
        assertFalse(user.isEnabled());
        verify(userRepository).save(user);
        verify(refreshTokenRepository).revokeAllForUserId(testUserId);
    }
}
