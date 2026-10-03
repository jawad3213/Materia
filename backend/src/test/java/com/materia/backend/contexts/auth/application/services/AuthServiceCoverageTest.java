package com.materia.backend.contexts.auth.application.services;

import com.materia.backend.contexts.auth.application.dtos.ChangePasswordInput;
import com.materia.backend.contexts.auth.application.dtos.ForgotPasswordInput;
import com.materia.backend.contexts.auth.application.dtos.LoginInput;
import com.materia.backend.contexts.auth.application.dtos.RefreshTokenInput;
import com.materia.backend.contexts.auth.application.dtos.ResetPasswordInput;
import com.materia.backend.contexts.auth.application.mappers.UserMapper;
import com.materia.backend.contexts.auth.domain.entities.RefreshToken;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.domain.exceptions.AuthenticationFailedException;
import com.materia.backend.contexts.auth.domain.exceptions.UserNotFoundException;
import com.materia.backend.contexts.auth.domain.ports.out.EmailSender;
import com.materia.backend.contexts.auth.domain.ports.out.PasswordResetTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.RefreshTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.gateway.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.UserFixtures.aUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** [T070] Input checks and lookup paths of AuthService not covered by AuthServiceTest/AuthServiceBehaviourTest (feature 001 coverage). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceCoverageTest {

    @Mock private UserRepository users;
    @Mock private RefreshTokenRepository refreshTokens;
    @Mock private PasswordResetTokenRepository resetTokens;
    @Mock private PasswordEncoder encoder;
    @Mock private JwtTokenProvider jwt;
    @Mock private EmailSender email;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(users, refreshTokens, resetTokens, encoder, jwt, new UserMapper(), email);
        when(jwt.hashToken(anyString())).thenAnswer(i -> "sha256:" + i.getArgument(0));
    }

    private static String code(AuthenticationFailedException e) {
        return e.getErrorCode();
    }

    @Test
    @DisplayName("login: a missing request, email or password is refused before any lookup")
    void login_missingCredentials() {
        LoginInput noPassword = new LoginInput();
        noPassword.setEmail("a@materia.test");

        assertThrows(AuthenticationFailedException.class, () -> service.login(null));
        assertThrows(AuthenticationFailedException.class, () -> service.login(noPassword));
        verifyNoInteractions(users);
    }

    @Test
    @DisplayName("login: an account whose status is not active is refused even when enabled")
    void login_inactiveStatus_isRefused() {
        User inactive = aUser().status(UserStatus.SUSPENDED).enabled(true).build();
        when(users.findByEmail("a@materia.test")).thenReturn(Optional.of(inactive));
        LoginInput input = new LoginInput();
        input.setEmail(" A@Materia.test ");
        input.setPassword("whatever");

        AuthenticationFailedException e = assertThrows(AuthenticationFailedException.class, () -> service.login(input));
        assertEquals("AUTH_USER_DISABLED", code(e));
    }

    @Test
    @DisplayName("refresh: a missing token is refused; an unknown or revoked token is refused")
    void refresh_badTokens() {
        RefreshTokenInput blank = new RefreshTokenInput();
        blank.setRefreshToken(" ");
        assertEquals("AUTH_REFRESH_TOKEN_REQUIRED", code(assertThrows(AuthenticationFailedException.class, () -> service.refreshToken(null))));
        assertThrows(AuthenticationFailedException.class, () -> service.refreshToken(blank));

        RefreshTokenInput unknown = new RefreshTokenInput();
        unknown.setRefreshToken("nope");
        when(refreshTokens.findByToken("sha256:nope")).thenReturn(Optional.empty());
        assertEquals("AUTH_INVALID_REFRESH_TOKEN", code(assertThrows(AuthenticationFailedException.class, () -> service.refreshToken(unknown))));
    }

    @Test
    @DisplayName("refresh: a valid token for a missing or disabled account is refused after the old token is revoked")
    void refresh_accountProblems() {
        UUID userId = UUID.randomUUID();
        RefreshToken token = RefreshToken.builder().token("sha256:raw").userId(userId)
                .expiryDate(LocalDateTime.now().plusDays(1)).build();
        when(refreshTokens.findByToken("sha256:raw")).thenReturn(Optional.of(token));
        RefreshTokenInput input = new RefreshTokenInput();
        input.setRefreshToken("raw");

        when(users.findById(userId)).thenReturn(Optional.empty());
        assertEquals("AUTH_USER_NOT_FOUND", code(assertThrows(AuthenticationFailedException.class, () -> service.refreshToken(input))));
        assertTrue(token.isRevoked());

        RefreshToken token2 = RefreshToken.builder().token("sha256:raw2").userId(userId)
                .expiryDate(LocalDateTime.now().plusDays(1)).build();
        when(refreshTokens.findByToken("sha256:raw2")).thenReturn(Optional.of(token2));
        when(users.findById(userId)).thenReturn(Optional.of(aUser().enabled(false).build()));
        RefreshTokenInput input2 = new RefreshTokenInput();
        input2.setRefreshToken("raw2");
        assertEquals("AUTH_USER_DISABLED", code(assertThrows(AuthenticationFailedException.class, () -> service.refreshToken(input2))));
    }

    @Test
    @DisplayName("logout: a blank token is ignored; an unknown token changes nothing")
    void logout_blankOrUnknown() {
        service.logout(" ");
        verifyNoInteractions(refreshTokens);

        when(refreshTokens.findByToken(anyString())).thenReturn(Optional.empty());
        service.logout("unknown");
        verify(refreshTokens, never()).save(any());
    }

    @Test
    @DisplayName("forgot password: a missing email is refused")
    void forgot_missingEmail() {
        ForgotPasswordInput blank = new ForgotPasswordInput();
        blank.setEmail(" ");

        assertEquals("AUTH_EMAIL_REQUIRED", code(assertThrows(AuthenticationFailedException.class, () -> service.forgotPassword(null))));
        assertThrows(AuthenticationFailedException.class, () -> service.forgotPassword(blank));
    }

    @Test
    @DisplayName("reset password: missing password, mismatched confirmation, too short, or missing token are each refused")
    void reset_inputChecks() {
        assertEquals("AUTH_FIELDS_REQUIRED", code(assertThrows(AuthenticationFailedException.class, () -> service.resetPassword(null))));

        ResetPasswordInput mismatch = new ResetPasswordInput();
        mismatch.setPassword("Str0ng!Pass");
        mismatch.setConfirmPassword("Different1!");
        assertEquals("AUTH_PASSWORDS_MISMATCH", code(assertThrows(AuthenticationFailedException.class, () -> service.resetPassword(mismatch))));

        ResetPasswordInput shortOne = new ResetPasswordInput();
        shortOne.setPassword("S1!a");
        assertEquals("AUTH_PASSWORD_TOO_SHORT", code(assertThrows(AuthenticationFailedException.class, () -> service.resetPassword(shortOne))));

        ResetPasswordInput noToken = new ResetPasswordInput();
        noToken.setPassword("Str0ng!Pass");
        noToken.setConfirmPassword("Str0ng!Pass");
        assertEquals("AUTH_TOKEN_REQUIRED", code(assertThrows(AuthenticationFailedException.class, () -> service.resetPassword(noToken))));
    }

    @Test
    @DisplayName("change password: missing, mismatched or too-short new passwords are refused before any lookup")
    void change_inputChecks() {
        assertThrows(AuthenticationFailedException.class, () -> service.changePassword(UUID.randomUUID(), null, null));

        ChangePasswordInput mismatch = new ChangePasswordInput();
        mismatch.setNewPassword("Str0ng!Pass");
        mismatch.setConfirmPassword("Other1!pass");
        assertEquals("AUTH_PASSWORDS_MISMATCH", code(assertThrows(AuthenticationFailedException.class,
                () -> service.changePassword(UUID.randomUUID(), null, mismatch))));

        ChangePasswordInput shortOne = new ChangePasswordInput();
        shortOne.setNewPassword("S1!a");
        assertEquals("AUTH_PASSWORD_TOO_SHORT", code(assertThrows(AuthenticationFailedException.class,
                () -> service.changePassword(UUID.randomUUID(), null, shortOne))));
        verifyNoInteractions(users);
    }

    @Test
    @DisplayName("change password: with no id and no email, or no matching account, the user is not found")
    void change_userNotFound() {
        ChangePasswordInput input = new ChangePasswordInput();
        input.setNewPassword("Str0ng!Pass");

        assertThrows(UserNotFoundException.class, () -> service.changePassword(null, null, input));
        when(users.findById(any())).thenReturn(Optional.empty());
        when(users.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.changePassword(UUID.randomUUID(), "x@materia.test", input));
    }

    @Test
    @DisplayName("change password: a wrong current password or reusing the current password is refused")
    void change_currentPasswordRules() {
        User user = aUser().build();
        when(users.findById(user.getId())).thenReturn(Optional.of(user));

        ChangePasswordInput wrongCurrent = new ChangePasswordInput();
        wrongCurrent.setCurrentPassword("Wrong1!pass");
        wrongCurrent.setNewPassword("Str0ng!Pass");
        when(encoder.matches("Wrong1!pass", user.getPasswordHash())).thenReturn(false);
        assertEquals("AUTH_INVALID_CURRENT_PASSWORD", code(assertThrows(AuthenticationFailedException.class,
                () -> service.changePassword(user.getId(), null, wrongCurrent))));

        ChangePasswordInput same = new ChangePasswordInput();
        same.setNewPassword("Str0ng!Pass");
        when(encoder.matches("Str0ng!Pass", user.getPasswordHash())).thenReturn(true);
        assertEquals("AUTH_PASSWORD_IDENTICAL", code(assertThrows(AuthenticationFailedException.class,
                () -> service.changePassword(user.getId(), null, same))));
        verify(users, never()).save(any());
    }

    @Test
    @DisplayName("change password: a valid change is saved and clears the must-change flag; lookup by email works without an id")
    void change_success_byEmail() {
        User user = aUser().mustChangePassword(true).build();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);
        when(encoder.encode("Str0ng!Pass")).thenReturn("hashed");
        ChangePasswordInput input = new ChangePasswordInput();
        input.setNewPassword("Str0ng!Pass");
        input.setConfirmPassword("Str0ng!Pass");

        assertNotNull(service.changePassword(null, " " + user.getEmail().toUpperCase() + " ", input));

        assertEquals("hashed", user.getPasswordHash());
        assertFalse(user.isMustChangePassword());
        verify(users).save(user);
    }
}
