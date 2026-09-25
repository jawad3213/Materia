package com.materia.backend.contexts.auth.application.services;

import com.materia.backend.contexts.auth.application.dtos.AuthOutput;
import com.materia.backend.contexts.auth.application.dtos.ForgotPasswordInput;
import com.materia.backend.contexts.auth.application.dtos.LoginInput;
import com.materia.backend.contexts.auth.application.dtos.RefreshTokenInput;
import com.materia.backend.contexts.auth.application.dtos.ResetPasswordInput;
import com.materia.backend.contexts.auth.application.mappers.UserMapper;
import com.materia.backend.contexts.auth.domain.entities.PasswordResetToken;
import com.materia.backend.contexts.auth.domain.entities.RefreshToken;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.exceptions.AuthenticationFailedException;
import com.materia.backend.contexts.auth.domain.exceptions.InvalidTokenException;
import com.materia.backend.contexts.auth.domain.exceptions.TokenExpiredException;
import com.materia.backend.contexts.auth.domain.exceptions.UserAlreadyExistsException;
import com.materia.backend.contexts.auth.domain.exceptions.UserNotFoundException;
import com.materia.backend.contexts.auth.domain.ports.in.AuthUseCase;
import com.materia.backend.contexts.auth.domain.ports.out.PasswordResetTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.RefreshTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.gateway.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.materia.backend.contexts.auth.domain.ports.out.EmailSender;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 🔹 AUTH APPLICATION SERVICE
 * 
 * Implements the AuthUseCase input port.
 * Handles email-only login, registration, refresh token issuance, and password reset.
 */
@Service
@Transactional
public class AuthService implements AuthUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final EmailSender emailSender;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            UserMapper userMapper,
            EmailSender emailSender) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
        this.emailSender = emailSender;
    }

    @Override
    @Transactional
    public AuthOutput login(LoginInput input) {
        if (input == null || !StringUtils.hasText(input.getEmail()) || !StringUtils.hasText(input.getPassword())) {
            throw new AuthenticationFailedException("Email and password must not be empty", "AUTH_CREDENTIALS_REQUIRED");
        }

        String email = input.getEmail().trim().toLowerCase();
        log.debug("[AUTH-SERVICE] Attempting login for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[AUTH-SERVICE] Login failed: email '{}' not found", email);
                    return new AuthenticationFailedException("Invalid email or password", "AUTH_INVALID_CREDENTIALS");
                });

        if (!user.isEnabled() || (user.getStatus() != null && !user.getStatus().isActive())) {
            log.warn("[AUTH-SERVICE] Login failed: account '{}' is disabled or inactive", email);
            throw new AuthenticationFailedException("User account is disabled", "AUTH_USER_DISABLED");
        }

        if (!passwordEncoder.matches(input.getPassword(), user.getPasswordHash())) {
            log.warn("[AUTH-SERVICE] Login failed: incorrect password for email '{}'", email);
            throw new AuthenticationFailedException("Invalid email or password", "AUTH_INVALID_CREDENTIALS");
        }

        log.info("[AUTH-SERVICE] Login successful for user '{}' with role {}", email, user.getRole().getCode());

        List<GrantedAuthority> authorities = buildAuthorities(user);
        String accessToken = jwtTokenProvider.generateAccessTokenWithAuthorities(user.getId(), user.getEmail(), authorities);

        // Generate cryptographically secure random 256-bit refresh token
        String rawRefreshToken = jwtTokenProvider.generateRandomRefreshToken();
        String tokenHash = jwtTokenProvider.hashToken(rawRefreshToken);

        // Store SHA-256 hash in DB with 7-day expiration
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .token(tokenHash)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return userMapper.toAuthOutput(user, accessToken, rawRefreshToken);
    }

    @Override
    @Transactional
    public AuthOutput refreshToken(RefreshTokenInput input) {
        if (input == null || !StringUtils.hasText(input.getRefreshToken())) {
            throw new AuthenticationFailedException("Refresh token is required", "AUTH_REFRESH_TOKEN_REQUIRED");
        }

        String rawToken = input.getRefreshToken().trim();
        String tokenHash = jwtTokenProvider.hashToken(rawToken);

        RefreshToken tokenEntity = refreshTokenRepository.findByToken(tokenHash)
                .orElseThrow(() -> {
                    log.warn("[AUTH-SERVICE] Refresh token hash not found in database");
                    return new AuthenticationFailedException("Invalid or expired refresh token", "AUTH_INVALID_REFRESH_TOKEN");
                });

        if (!tokenEntity.isValid()) {
            log.warn("[AUTH-SERVICE] Refresh token expired or revoked for user ID: {}", tokenEntity.getUserId());
            throw new AuthenticationFailedException("Invalid or expired refresh token", "AUTH_INVALID_REFRESH_TOKEN");
        }

        // Revoke the old token (Token Rotation pattern)
        tokenEntity.revoke();
        refreshTokenRepository.save(tokenEntity);

        User user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new AuthenticationFailedException("User not found for token", "AUTH_USER_NOT_FOUND"));

        if (!user.isEnabled() || (user.getStatus() != null && !user.getStatus().isActive())) {
            throw new AuthenticationFailedException("User account is disabled", "AUTH_USER_DISABLED");
        }

        log.debug("[AUTH-SERVICE] Rotating tokens for user email: {}", user.getEmail());

        List<GrantedAuthority> authorities = buildAuthorities(user);
        String newAccessToken = jwtTokenProvider.generateAccessTokenWithAuthorities(user.getId(), user.getEmail(), authorities);

        // Generate new rotated refresh token & save its SHA-256 hash
        String newRawRefreshToken = jwtTokenProvider.generateRandomRefreshToken();
        String newTokenHash = jwtTokenProvider.hashToken(newRawRefreshToken);

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .token(newTokenHash)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        return userMapper.toAuthOutput(user, newAccessToken, newRawRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        if (!StringUtils.hasText(rawRefreshToken)) {
            return;
        }
        String tokenHash = jwtTokenProvider.hashToken(rawRefreshToken.trim());
        refreshTokenRepository.findByToken(tokenHash).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
            log.info("[AUTH-SERVICE] Successfully revoked refresh token for user ID: {}", token.getUserId());
        });
    }

    @Override
    public String forgotPassword(ForgotPasswordInput input) {
        if (input == null || !StringUtils.hasText(input.getEmail())) {
            throw new AuthenticationFailedException("Email address is required", "AUTH_EMAIL_REQUIRED");
        }

        String email = input.getEmail().trim().toLowerCase();
        log.info("[AUTH-SERVICE] Processing forgot password request for email: {}", email);

        userRepository.findByEmail(email).ifPresent(user -> {
            log.info("[AUTH-SERVICE] Found user id {} for password reset. Verification email dispatch triggered.", user.getId());

            // Invalidate any existing unused reset tokens for this user
            passwordResetTokenRepository.invalidateAllForUserId(user.getId());

            // Generate secure reset token. Only the SHA-256 hash is persisted, so a
            // leaked database dump cannot be replayed to take over accounts.
            String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

            // Build and persist PasswordResetToken valid for 30 minutes
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(jwtTokenProvider.hashToken(rawToken))
                    .userId(user.getId())
                    .email(user.getEmail())
                    .expiryDate(LocalDateTime.now().plusMinutes(30))
                    .used(false)
                    .build();

            passwordResetTokenRepository.save(resetToken);
            log.info("[AUTH-SERVICE] Generated password reset token for user {}", user.getEmail());

            // Dispatch transactional reset password email with the raw token
            emailSender.sendPasswordResetEmail(user.getEmail(), rawToken);
        });

        // Always return generic success message to prevent user enumeration attacks
        return "Password reset link sent to " + email;
    }

    @Override
    public String resetPassword(ResetPasswordInput input) {
        if (input == null || !StringUtils.hasText(input.getPassword())) {
            throw new AuthenticationFailedException("New password is required", "AUTH_FIELDS_REQUIRED");
        }

        if (StringUtils.hasText(input.getConfirmPassword()) && !input.getPassword().equals(input.getConfirmPassword())) {
            throw new AuthenticationFailedException("Passwords do not match", "AUTH_PASSWORDS_MISMATCH");
        }

        if (input.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new AuthenticationFailedException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters", "AUTH_PASSWORD_TOO_SHORT");
        }

        if (!StringUtils.hasText(input.getToken())) {
            throw new AuthenticationFailedException("Reset token is required", "AUTH_TOKEN_REQUIRED");
        }

        log.info("[AUTH-SERVICE] Verifying password reset token");

        String tokenHash = jwtTokenProvider.hashToken(input.getToken().trim());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        if (!resetToken.isValid()) {
            if (resetToken.isExpired()) {
                throw new TokenExpiredException("Password reset token has expired");
            }
            throw new InvalidTokenException("Password reset token has already been used");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found for token: " + resetToken.getUserId()));

        if (StringUtils.hasText(input.getEmail()) && !user.getEmail().equalsIgnoreCase(input.getEmail().trim())) {
            throw new AuthenticationFailedException("Email does not match reset token", "AUTH_EMAIL_MISMATCH");
        }

        user.setPasswordHash(passwordEncoder.encode(input.getPassword()));
        userRepository.save(user);

        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        // A password reset must end every other session: otherwise a stolen
        // refresh token survives the very reset meant to lock the thief out.
        refreshTokenRepository.revokeAllForUserId(user.getId());

        log.info("[AUTH-SERVICE] Successfully updated password for user email: {}", user.getEmail());
        return "Your password has been successfully reset. You can now sign in with your new password.";
    }

    private List<GrantedAuthority> buildAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getCode()));
            if (user.getRole().getPermissions() != null) {
                user.getRole().getPermissions().forEach(perm ->
                        authorities.add(new SimpleGrantedAuthority(perm))
                );
            }
        }
        return authorities;
    }
}
