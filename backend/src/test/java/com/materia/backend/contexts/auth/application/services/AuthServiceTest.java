package com.materia.backend.contexts.auth.application.services;

import com.materia.backend.contexts.auth.application.dtos.AuthOutput;
import com.materia.backend.contexts.auth.application.dtos.ForgotPasswordInput;
import com.materia.backend.contexts.auth.application.dtos.LoginInput;
import com.materia.backend.contexts.auth.application.dtos.RefreshTokenInput;
import com.materia.backend.contexts.auth.application.dtos.ResetPasswordInput;
import com.materia.backend.contexts.auth.application.mappers.UserMapper;
import com.materia.backend.contexts.auth.domain.entities.PasswordResetToken;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.exceptions.AuthenticationFailedException;
import com.materia.backend.contexts.auth.domain.exceptions.InvalidTokenException;
import com.materia.backend.contexts.auth.domain.exceptions.TokenExpiredException;
import com.materia.backend.contexts.auth.domain.exceptions.UserAlreadyExistsException;
import com.materia.backend.contexts.auth.domain.entities.RefreshToken;
import com.materia.backend.contexts.auth.domain.ports.out.PasswordResetTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.RefreshTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.gateway.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private com.materia.backend.contexts.auth.domain.ports.out.EmailSender emailSender;

    private UserMapper userMapper;
    private AuthService authService;

    private final UUID testUserId = UUID.randomUUID();
    private User testUser;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        authService = new AuthService(userRepository, refreshTokenRepository, passwordResetTokenRepository, passwordEncoder, jwtTokenProvider, userMapper, emailSender);

        testUser = User.builder()
                .id(testUserId)
                .email("john@example.com")
                .passwordHash("hashed_pwd")
                .role(Role.PURCHASER)
                .enabled(true)
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .status(com.materia.backend.contexts.auth.domain.enums.UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("login: successful authentication returns AuthOutput with tokens and permissions")
    void login_success() {
        LoginInput input = new LoginInput("john@example.com", "raw_password");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("raw_password", "hashed_pwd")).thenReturn(true);
        when(jwtTokenProvider.generateAccessTokenWithAuthorities(eq(testUserId), eq("john@example.com"), any())).thenReturn("access.token.jwt");
        when(jwtTokenProvider.generateRandomRefreshToken()).thenReturn("raw.random.refresh.token");
        when(jwtTokenProvider.hashToken("raw.random.refresh.token")).thenReturn("hashed-token-val");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthOutput output = authService.login(input);

        assertNotNull(output);
        assertEquals("access.token.jwt", output.getAccessToken());
        assertEquals("raw.random.refresh.token", output.getRefreshToken());
        assertEquals(testUserId, output.getUserId());
        assertEquals("john@example.com", output.getEmail());
        assertEquals("John", output.getFirstName());
        assertEquals("Doe", output.getLastName());
        assertEquals("John Doe", output.getFullName());
        assertEquals("+1234567890", output.getPhone());
        assertEquals(com.materia.backend.contexts.auth.domain.enums.UserStatus.ACTIVE, output.getStatus());
        assertEquals(Role.PURCHASER, output.getRole());
        assertNotNull(output.getPermissions());
        assertTrue(output.getPermissions().contains("order:read"));
    }

    @Test
    @DisplayName("login: throws AuthenticationFailedException when user does not exist")
    void login_userNotFound() {
        LoginInput input = new LoginInput("unknown@example.com", "password");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        AuthenticationFailedException ex = assertThrows(AuthenticationFailedException.class,
                () -> authService.login(input));

        assertEquals("AUTH_INVALID_CREDENTIALS", ex.getErrorCode());
    }

    @Test
    @DisplayName("login: throws AuthenticationFailedException when account is disabled")
    void login_userDisabled() {
        testUser.setEnabled(false);
        LoginInput input = new LoginInput("john@example.com", "raw_password");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        AuthenticationFailedException ex = assertThrows(AuthenticationFailedException.class,
                () -> authService.login(input));

        assertEquals("AUTH_USER_DISABLED", ex.getErrorCode());
    }

    @Test
    @DisplayName("login: throws AuthenticationFailedException on invalid password")
    void login_invalidPassword() {
        LoginInput input = new LoginInput("john@example.com", "wrong_password");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong_password", "hashed_pwd")).thenReturn(false);

        AuthenticationFailedException ex = assertThrows(AuthenticationFailedException.class,
                () -> authService.login(input));

        assertEquals("AUTH_INVALID_CREDENTIALS", ex.getErrorCode());
    }

    @Test
    @DisplayName("refreshToken: successfully issues new tokens for valid refresh token")
    void refreshToken_success() {
        RefreshTokenInput input = new RefreshTokenInput("valid.refresh.token");
        RefreshToken existingToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .token("hashed.token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(jwtTokenProvider.hashToken("valid.refresh.token")).thenReturn("hashed.token");
        when(refreshTokenRepository.findByToken("hashed.token")).thenReturn(Optional.of(existingToken));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessTokenWithAuthorities(eq(testUserId), eq("john@example.com"), any())).thenReturn("new.access.token");
        when(jwtTokenProvider.generateRandomRefreshToken()).thenReturn("new.raw.refresh.token");
        when(jwtTokenProvider.hashToken("new.raw.refresh.token")).thenReturn("new.hashed.token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthOutput output = authService.refreshToken(input);

        assertNotNull(output);
        assertEquals("new.access.token", output.getAccessToken());
        assertEquals("new.raw.refresh.token", output.getRefreshToken());
        assertEquals(testUserId, output.getUserId());
        assertTrue(existingToken.isRevoked(), "Old refresh token must be revoked upon rotation");
    }

    @Test
    @DisplayName("refreshToken: throws AuthenticationFailedException for invalid token")
    void refreshToken_invalidToken() {
        RefreshTokenInput input = new RefreshTokenInput("invalid.token");
        when(jwtTokenProvider.hashToken("invalid.token")).thenReturn("invalid.token.hash");
        when(refreshTokenRepository.findByToken("invalid.token.hash")).thenReturn(Optional.empty());

        AuthenticationFailedException ex = assertThrows(AuthenticationFailedException.class,
                () -> authService.refreshToken(input));

        assertEquals("AUTH_INVALID_REFRESH_TOKEN", ex.getErrorCode());
    }

    @Test
    @DisplayName("forgotPassword: generates reset token, invalidates prior tokens, and returns confirmation message")
    void forgotPassword_success() {
        ForgotPasswordInput input = new ForgotPasswordInput("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.hashToken(anyString())).thenReturn("hashed_reset_token");

        String message = authService.forgotPassword(input);

        assertNotNull(message);
        assertTrue(message.contains("john@example.com"));
        verify(passwordResetTokenRepository).invalidateAllForUserId(testUserId);

        // Only the hash is persisted; the raw token is what goes out by email.
        ArgumentCaptor<PasswordResetToken> savedToken = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(savedToken.capture());
        assertEquals("hashed_reset_token", savedToken.getValue().getToken());

        ArgumentCaptor<String> emailedToken = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendPasswordResetEmail(eq("john@example.com"), emailedToken.capture());
        assertNotEquals("hashed_reset_token", emailedToken.getValue());
    }

    @Test
    @DisplayName("resetPassword: successfully verifies valid token and updates password")
    void resetPassword_success() {
        ResetPasswordInput input = new ResetPasswordInput("john@example.com", "valid_token_xyz", "newPassword123", "newPassword123");
        PasswordResetToken validToken = PasswordResetToken.builder()
                .token("hashed_valid_token")
                .userId(testUserId)
                .email("john@example.com")
                .expiryDate(LocalDateTime.now().plusMinutes(20))
                .used(false)
                .build();

        when(jwtTokenProvider.hashToken("valid_token_xyz")).thenReturn("hashed_valid_token");
        when(passwordResetTokenRepository.findByToken("hashed_valid_token")).thenReturn(Optional.of(validToken));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded_new");

        String message = authService.resetPassword(input);

        assertNotNull(message);
        assertEquals("encoded_new", testUser.getPasswordHash());
        assertTrue(validToken.isUsed());
        verify(userRepository).save(testUser);
        verify(passwordResetTokenRepository).save(validToken);
        verify(refreshTokenRepository).revokeAllForUserId(testUserId);
    }

    @Test
    @DisplayName("resetPassword: throws InvalidTokenException when token does not exist")
    void resetPassword_tokenNotFound() {
        ResetPasswordInput input = new ResetPasswordInput("john@example.com", "unknown_token", "newPassword123", "newPassword123");
        when(jwtTokenProvider.hashToken("unknown_token")).thenReturn("hashed_unknown_token");
        when(passwordResetTokenRepository.findByToken("hashed_unknown_token")).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> authService.resetPassword(input));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword: throws TokenExpiredException when token is expired")
    void resetPassword_tokenExpired() {
        ResetPasswordInput input = new ResetPasswordInput("john@example.com", "expired_token", "newPassword123", "newPassword123");
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .token("hashed_expired_token")
                .userId(testUserId)
                .email("john@example.com")
                .expiryDate(LocalDateTime.now().minusMinutes(10))
                .used(false)
                .build();

        when(jwtTokenProvider.hashToken("expired_token")).thenReturn("hashed_expired_token");
        when(passwordResetTokenRepository.findByToken("hashed_expired_token")).thenReturn(Optional.of(expiredToken));

        assertThrows(TokenExpiredException.class, () -> authService.resetPassword(input));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword: throws InvalidTokenException when token has already been used")
    void resetPassword_tokenAlreadyUsed() {
        ResetPasswordInput input = new ResetPasswordInput("john@example.com", "used_token", "newPassword123", "newPassword123");
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .token("hashed_used_token")
                .userId(testUserId)
                .email("john@example.com")
                .expiryDate(LocalDateTime.now().plusMinutes(20))
                .used(true)
                .build();

        when(jwtTokenProvider.hashToken("used_token")).thenReturn("hashed_used_token");
        when(passwordResetTokenRepository.findByToken("hashed_used_token")).thenReturn(Optional.of(usedToken));

        assertThrows(InvalidTokenException.class, () -> authService.resetPassword(input));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword: throws exception when passwords mismatch")
    void resetPassword_mismatch() {
        ResetPasswordInput input = new ResetPasswordInput("john@example.com", "token123", "newPassword123", "differentPassword");

        AuthenticationFailedException ex = assertThrows(AuthenticationFailedException.class,
                () -> authService.resetPassword(input));

        assertEquals("AUTH_PASSWORDS_MISMATCH", ex.getErrorCode());
        verify(userRepository, never()).save(any());
    }
}
