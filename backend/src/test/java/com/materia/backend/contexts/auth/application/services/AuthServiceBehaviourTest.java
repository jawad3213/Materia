package com.materia.backend.contexts.auth.application.services;

import com.materia.backend.contexts.auth.application.dtos.AuthOutput;
import com.materia.backend.contexts.auth.application.dtos.ChangePasswordInput;
import com.materia.backend.contexts.auth.application.dtos.ForgotPasswordInput;
import com.materia.backend.contexts.auth.application.dtos.LoginInput;
import com.materia.backend.contexts.auth.application.dtos.RefreshTokenInput;
import com.materia.backend.contexts.auth.application.dtos.ResetPasswordInput;
import com.materia.backend.contexts.auth.application.mappers.UserMapper;
import com.materia.backend.contexts.auth.domain.entities.PasswordResetToken;
import com.materia.backend.contexts.auth.domain.entities.RefreshToken;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.domain.exceptions.AuthenticationFailedException;
import com.materia.backend.contexts.auth.domain.ports.out.EmailSender;
import com.materia.backend.contexts.auth.domain.ports.out.PasswordResetTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.RefreshTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.gateway.security.JwtTokenProvider;
import com.materia.backend.support.TimeAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

/**
 * Security properties the spec requires of authentication (US1), complementing the
 * happy-path and basic-refusal coverage in {@link AuthServiceTest}.
 *
 * <p>Where the code contradicts a spec scenario, the case asserts the spec's behaviour and
 * is {@code @Disabled} with a reference to the finding, so the expectation is kept rather
 * than weakened (FR-019).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceBehaviourTest {

    private static final String FINDING_012 =
            "FINDING-012: login reveals a disabled account exists before checking the password";
    private static final String FINDING_013 =
            "FINDING-013: changePassword accepts an omitted current password and leaves other sessions alive";
    private static final String FINDING_017 =
            "FINDING-017: changePassword falls back to a caller-supplied email when the caller's own account is not found";
    private static final String FINDING_014 =
            "FINDING-014: resetPassword checks length only, skipping the complexity policy";

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private EmailSender emailSender;

    private AuthService authService;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, refreshTokenRepository, passwordResetTokenRepository,
                passwordEncoder, jwtTokenProvider, new UserMapper(), emailSender);
        // Hashing is deterministic and visibly distinct from its input, so a test can tell
        // a stored hash from a raw token.
        lenient().when(jwtTokenProvider.hashToken(anyString())).thenAnswer(i -> "sha256:" + i.getArgument(0));
    }

    private User user(Role role, UserStatus status, boolean enabled) {
        return User.builder().id(userId).email("jane@example.com").passwordHash("stored-hash")
                .role(role).status(status).enabled(enabled).build();
    }

    private User activeUser() {
        return user(Role.PURCHASER, UserStatus.ACTIVE, true);
    }

    private static String errorCode(Runnable action) {
        AuthenticationFailedException ex = assertThrows(AuthenticationFailedException.class, action::run);
        return ex.getErrorCode();
    }

    // =====================================================================
    // Sign-in (US1 scenarios 1-2)
    // =====================================================================

    @Test
    @DisplayName("login: an unknown email and a wrong password are refused identically, so neither reveals whether the account exists")
    void login_unknownEmailAndWrongPassword_areIndistinguishable() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        String unknown = errorCode(() -> authService.login(new LoginInput("ghost@example.com", "Whatever1!")));

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(activeUser()));
        when(passwordEncoder.matches("Wrong1!pass", "stored-hash")).thenReturn(false);
        String wrongPassword = errorCode(() -> authService.login(new LoginInput("jane@example.com", "Wrong1!pass")));

        assertEquals(unknown, wrongPassword);
        assertEquals("AUTH_INVALID_CREDENTIALS", wrongPassword);
    }

    @Test
    @Disabled(FINDING_012)
    @DisplayName("login: a disabled account with a wrong password is refused like any wrong password, without revealing it exists")
    void login_disabledAccountWrongPassword_doesNotRevealExistence() {
        when(userRepository.findByEmail("jane@example.com"))
                .thenReturn(Optional.of(user(Role.PURCHASER, UserStatus.ACTIVE, false)));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertEquals("AUTH_INVALID_CREDENTIALS",
                errorCode(() -> authService.login(new LoginInput("jane@example.com", "Wrong1!pass"))));
    }

    @Test
    @DisplayName("login: the email is trimmed and lower-cased before lookup, so case and whitespace never matter")
    void login_normalisesEmail() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(new LoginInput("  Jane@Example.COM  ", "Whatever1!")));
        verify(userRepository).findByEmail("jane@example.com");
    }

    @Test
    @DisplayName("login: a blank email or password is refused before any lookup")
    void login_blankCredentials_areRefusedWithoutLookup() {
        assertEquals("AUTH_CREDENTIALS_REQUIRED", errorCode(() -> authService.login(new LoginInput("  ", "x"))));
        assertEquals("AUTH_CREDENTIALS_REQUIRED", errorCode(() -> authService.login(new LoginInput("a@b.co", ""))));
        assertEquals("AUTH_CREDENTIALS_REQUIRED", errorCode(() -> authService.login(null)));
        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest(name = "status {0}")
    @EnumSource(value = UserStatus.class, names = {"INACTIVE", "SUSPENDED", "PENDING"})
    @DisplayName("login: any account status other than ACTIVE is refused, even with the correct password")
    void login_nonActiveStatus_isRefused(UserStatus status) {
        when(userRepository.findByEmail("jane@example.com"))
                .thenReturn(Optional.of(user(Role.PURCHASER, status, true)));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(new LoginInput("jane@example.com", "Correct1!")));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("login: only a hash of the refresh token is stored, never the token handed to the client")
    void login_storesRefreshTokenHashOnly() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(activeUser()));
        when(passwordEncoder.matches("Correct1!", "stored-hash")).thenReturn(true);
        when(jwtTokenProvider.generateRandomRefreshToken()).thenReturn("raw-refresh");

        LocalDateTime before = LocalDateTime.now();
        AuthOutput out = authService.login(new LoginInput("jane@example.com", "Correct1!"));
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        assertEquals("raw-refresh", out.getRefreshToken(), "the client receives the raw token");
        assertEquals("sha256:raw-refresh", saved.getValue().getToken(), "the database receives only its hash");
        assertFalse(saved.getValue().isRevoked());
        TimeAssertions.assertWithin(saved.getValue().getExpiryDate(), before.plusDays(7), after.plusDays(7));
    }

    // =====================================================================
    // Authorities (T041)
    // =====================================================================

    @ParameterizedTest(name = "{0}")
    @EnumSource(Role.class)
    @DisplayName("login: the access token is issued with the role and every permission that role holds")
    void login_grantsRoleAndAllItsPermissions(Role role) {
        when(userRepository.findByEmail("jane@example.com"))
                .thenReturn(Optional.of(user(role, UserStatus.ACTIVE, true)));
        when(passwordEncoder.matches("Correct1!", "stored-hash")).thenReturn(true);
        when(jwtTokenProvider.generateRandomRefreshToken()).thenReturn("raw");

        authService.login(new LoginInput("jane@example.com", "Correct1!"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Collection<org.springframework.security.core.GrantedAuthority>> granted =
                ArgumentCaptor.forClass(java.util.Collection.class);
        verify(jwtTokenProvider).generateAccessTokenWithAuthorities(eq(userId), eq("jane@example.com"), granted.capture());

        java.util.Set<String> authorities = granted.getValue().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(authorities.contains("ROLE_" + role.getCode()));
        assertTrue(authorities.containsAll(role.getPermissions()),
                () -> "missing: " + role.getPermissions().stream().filter(p -> !authorities.contains(p)).toList());
        assertEquals(role.getPermissions().size() + 1, authorities.size(), "nothing beyond the role's own grants");
    }

    // =====================================================================
    // Session renewal and sign-out (US1 scenarios 3-4)
    // =====================================================================

    private RefreshToken storedToken(boolean revoked, LocalDateTime expiry) {
        return RefreshToken.builder().userId(userId).token("sha256:raw").revoked(revoked).expiryDate(expiry).build();
    }

    @Test
    @DisplayName("refresh: renewal revokes the presented token, so it cannot be used again (rotation)")
    void refresh_revokesPresentedToken() {
        RefreshToken presented = storedToken(false, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("sha256:raw")).thenReturn(Optional.of(presented));
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser()));
        when(jwtTokenProvider.generateRandomRefreshToken()).thenReturn("next-raw");

        AuthOutput out = authService.refreshToken(new RefreshTokenInput("raw"));

        assertTrue(presented.isRevoked(), "the presented token is spent");
        assertEquals("next-raw", out.getRefreshToken());
    }

    @Test
    @DisplayName("refresh: replaying an already-used token is refused")
    void refresh_replayOfRevokedToken_isRefused() {
        when(refreshTokenRepository.findByToken("sha256:raw"))
                .thenReturn(Optional.of(storedToken(true, LocalDateTime.now().plusDays(1))));

        assertEquals("AUTH_INVALID_REFRESH_TOKEN",
                errorCode(() -> authService.refreshToken(new RefreshTokenInput("raw"))));
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("refresh: an expired token is refused")
    void refresh_expiredToken_isRefused() {
        when(refreshTokenRepository.findByToken("sha256:raw"))
                .thenReturn(Optional.of(storedToken(false, LocalDateTime.now().minusSeconds(1))));

        assertEquals("AUTH_INVALID_REFRESH_TOKEN",
                errorCode(() -> authService.refreshToken(new RefreshTokenInput("raw"))));
    }

    @Test
    @DisplayName("refresh: a valid token for a since-disabled account is refused")
    void refresh_disabledUser_isRefused() {
        when(refreshTokenRepository.findByToken("sha256:raw"))
                .thenReturn(Optional.of(storedToken(false, LocalDateTime.now().plusDays(1))));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(Role.PURCHASER, UserStatus.ACTIVE, false)));

        assertEquals("AUTH_USER_DISABLED", errorCode(() -> authService.refreshToken(new RefreshTokenInput("raw"))));
    }

    @Test
    @DisplayName("logout: signing out revokes the session's refresh token")
    void logout_revokesToken() {
        RefreshToken token = storedToken(false, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("sha256:raw")).thenReturn(Optional.of(token));

        authService.logout("raw");

        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    @DisplayName("logout: an unknown or blank token is a harmless no-op, never an error")
    void logout_unknownOrBlankToken_isNoOp() {
        when(refreshTokenRepository.findByToken("sha256:nope")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.logout("nope"));
        assertDoesNotThrow(() -> authService.logout("   "));
        assertDoesNotThrow(() -> authService.logout(null));
        verify(refreshTokenRepository, never()).save(any());
    }

    // =====================================================================
    // Password recovery (US1 scenario 5)
    // =====================================================================

    @Test
    @DisplayName("forgotPassword: an unregistered address gets the same response as a registered one, and nothing is sent")
    void forgotPassword_unregistered_isIndistinguishableAndSilent() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        String unregistered = authService.forgotPassword(new ForgotPasswordInput("ghost@example.com"));

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(activeUser()));
        String registered = authService.forgotPassword(new ForgotPasswordInput("jane@example.com"));

        assertEquals(unregistered.replace("ghost@example.com", "<email>"),
                registered.replace("jane@example.com", "<email>"),
                "the response must not differ by whether the account exists");
        verify(emailSender, times(1)).sendPasswordResetEmail(anyString(), anyString());
        verify(emailSender, never()).sendPasswordResetEmail(eq("ghost@example.com"), anyString());
    }

    @Test
    @DisplayName("forgotPassword: the email carries the raw token while only its hash is stored, valid for 30 minutes")
    void forgotPassword_emailsRawTokenStoresHash() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(activeUser()));

        LocalDateTime before = LocalDateTime.now();
        authService.forgotPassword(new ForgotPasswordInput("jane@example.com"));
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<String> emailed = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendPasswordResetEmail(eq("jane@example.com"), emailed.capture());
        ArgumentCaptor<PasswordResetToken> stored = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(stored.capture());

        assertEquals("sha256:" + emailed.getValue(), stored.getValue().getToken());
        assertTrue(emailed.getValue().length() >= 64, "the reset token must be long enough to resist guessing");
        verify(passwordResetTokenRepository).invalidateAllForUserId(userId);
        TimeAssertions.assertWithin(stored.getValue().getExpiryDate(), before.plusMinutes(30), after.plusMinutes(30));
    }

    // =====================================================================
    // Password reset and change (US1 scenarios 6-7)
    // =====================================================================

    private PasswordResetToken validResetToken() {
        return PasswordResetToken.builder().token("sha256:reset").userId(userId).email("jane@example.com")
                .expiryDate(LocalDateTime.now().plusMinutes(10)).used(false).build();
    }

    @Test
    @DisplayName("resetPassword: a successful reset ends every other session, so a stolen token cannot outlive it")
    void resetPassword_revokesAllSessions() {
        PasswordResetToken token = validResetToken();
        when(passwordResetTokenRepository.findByToken("sha256:reset")).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser()));
        when(passwordEncoder.encode("NewPass1!")).thenReturn("new-hash");

        authService.resetPassword(new ResetPasswordInput(null, "reset", "NewPass1!", "NewPass1!"));

        verify(refreshTokenRepository).revokeAllForUserId(userId);
        assertTrue(token.isUsed(), "the reset token is spent");
    }

    @Test
    @DisplayName("resetPassword: a token presented with a different account's email is refused")
    void resetPassword_emailMismatch_isRefused() {
        when(passwordResetTokenRepository.findByToken("sha256:reset")).thenReturn(Optional.of(validResetToken()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser()));

        assertEquals("AUTH_EMAIL_MISMATCH", errorCode(() -> authService.resetPassword(
                new ResetPasswordInput("someone@else.com", "reset", "NewPass1!", "NewPass1!"))));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword: a password shorter than eight characters is refused")
    void resetPassword_tooShort_isRefused() {
        assertEquals("AUTH_PASSWORD_TOO_SHORT",
                errorCode(() -> authService.resetPassword(new ResetPasswordInput(null, "reset", "Ab1!", "Ab1!"))));
    }

    @Test
    @Disabled(FINDING_014)
    @DisplayName("resetPassword: a password failing the complexity policy is refused, as it is everywhere else")
    void resetPassword_weakPassword_isRefused() {
        lenient().when(passwordResetTokenRepository.findByToken("sha256:reset")).thenReturn(Optional.of(validResetToken()));
        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser()));

        assertThrows(RuntimeException.class, () -> authService.resetPassword(
                new ResetPasswordInput(null, "reset", "aaaaaaaa", "aaaaaaaa")));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword: a password failing the complexity policy is refused")
    void changePassword_weakPassword_isRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword(userId, null, new ChangePasswordInput("Old1!pass", "aaaaaaaa", "aaaaaaaa")));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword: reusing the current password is refused")
    void changePassword_samePassword_isRefused() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser()));
        when(passwordEncoder.matches("Same1!pass", "stored-hash")).thenReturn(true);

        assertEquals("AUTH_PASSWORD_IDENTICAL", errorCode(() -> authService.changePassword(
                userId, null, new ChangePasswordInput("Same1!pass", "Same1!pass", "Same1!pass"))));
    }

    @Test
    @Disabled(FINDING_013)
    @DisplayName("changePassword: the current password must be supplied, so a stolen session alone cannot change it")
    void changePassword_missingCurrentPassword_isRefused() {
        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser()));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(AuthenticationFailedException.class, () -> authService.changePassword(
                userId, null, new ChangePasswordInput(null, "NewPass1!", "NewPass1!")));
        verify(userRepository, never()).save(any());
    }

    @Test
    @Disabled(FINDING_017)
    @DisplayName("changePassword: a caller-supplied email can never redirect the change to a different account")
    void changePassword_cannotTargetAnotherAccountByEmail() {
        // The caller's token names an account that no longer exists (deleted while the token is
        // still valid). The service must refuse, not fall back to an email the caller chose.
        UUID deletedCaller = UUID.randomUUID();
        User victim = User.builder().id(UUID.randomUUID()).email("victim@example.com")
                .passwordHash("victim-hash").role(Role.ADMIN).status(UserStatus.ACTIVE).enabled(true).build();
        lenient().when(userRepository.findById(deletedCaller)).thenReturn(Optional.empty());
        lenient().when(userRepository.findByEmail("victim@example.com")).thenReturn(Optional.of(victim));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("attacker-hash");

        assertThrows(RuntimeException.class, () -> authService.changePassword(
                deletedCaller, "victim@example.com", new ChangePasswordInput(null, "Takeover1!", "Takeover1!")));
        verify(userRepository, never()).save(victim);
    }

    @Test
    @Disabled(FINDING_013)
    @DisplayName("changePassword: a successful change ends every other session, as a reset does")
    void changePassword_revokesOtherSessions() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser()));
        when(passwordEncoder.matches("Old1!pass", "stored-hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPass1!", "stored-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("new-hash");

        authService.changePassword(userId, null, new ChangePasswordInput("Old1!pass", "NewPass1!", "NewPass1!"));

        verify(refreshTokenRepository).revokeAllForUserId(userId);
    }
}
