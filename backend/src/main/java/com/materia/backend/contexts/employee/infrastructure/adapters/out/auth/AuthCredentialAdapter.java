package com.materia.backend.contexts.employee.infrastructure.adapters.out.auth;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.domain.exceptions.EmailAlreadyExistsException;
import com.materia.backend.contexts.auth.domain.ports.out.EmailSender;
import com.materia.backend.contexts.auth.domain.ports.out.RefreshTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.auth.domain.valueObjects.Password;
import com.materia.backend.contexts.employee.domain.exceptions.InvalidRoleCodeException;
import com.materia.backend.contexts.employee.domain.ports.out.CredentialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * 🔹 AUTH CREDENTIAL ADAPTER
 *
 * Implements the CredentialPort outbound interface.
 * Connects the Employee context to the Auth context to provision security credentials
 * and revoke access upon employee termination.
 */
@Component
public class AuthCredentialAdapter implements CredentialPort {

    private static final Logger log = LoggerFactory.getLogger(AuthCredentialAdapter.class);

    /** Role granted when the caller does not specify one. An unknown code is rejected, never downgraded to this. */
    private static final Role DEFAULT_ROLE = Role.PURCHASER;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;

    public AuthCredentialAdapter(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailSender emailSender) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
    }

    @Override
    public UUID provisionUserAccount(String email, String firstName, String lastName, String roleCode, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new EmailAlreadyExistsException("A user account already exists with email: " + normalizedEmail);
        }

        Role role = resolveRole(roleCode);

        // A caller-supplied password must satisfy the same policy as one chosen through
        // the reset flow, so provisioning cannot become a way around the password rules.
        boolean passwordWasGenerated = rawPassword == null || rawPassword.trim().isEmpty();
        String password = passwordWasGenerated ? generateSecureTemporaryPassword() : rawPassword.trim();
        Password.fromRaw(password);

        User user = User.builder()
                .email(normalizedEmail)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .passwordHash(passwordEncoder.encode(password))
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .mustChangePassword(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("[CREDENTIAL-PORT] Successfully provisioned User ID {} with role {} for employee email {} (mustChangePassword=true)",
                savedUser.getId(), role.getCode(), normalizedEmail);

        // Deliver credentials via transactional email
        emailSender.sendTemporaryPasswordEmail(normalizedEmail, password);

        return savedUser.getId();
    }

    private Role resolveRole(String roleCode) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            return DEFAULT_ROLE;
        }
        try {
            return Role.fromCode(roleCode.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Falling back here would silently hand out the wrong permissions on a typo.
            throw new InvalidRoleCodeException(roleCode, Role.getCodes());
        }
    }

    @Override
    public void revokeUserAccess(UUID userId) {
        if (userId == null) return;

        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(UserStatus.INACTIVE);
            user.setEnabled(false);
            userRepository.save(user);

            // Invalidate all active refresh tokens immediately to force logout
            refreshTokenRepository.revokeAllForUserId(userId);
            log.info("[CREDENTIAL-PORT] Revoked all tokens and disabled user ID: {}", userId);
        });
    }

    @Override
    public boolean isUserActive(UUID userId) {
        if (userId == null) return false;
        return userRepository.findById(userId)
                .map(user -> user.isEnabled() && (user.getStatus() == null || user.getStatus().isActive()))
                .orElse(false);
    }

    @Override
    public String getUserRoleCode(UUID userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(user -> user.getRole() != null ? user.getRole().getCode() : null)
                .orElse(null);
    }

    @Override
    public String getUserRoleCodeByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        return userRepository.findByEmail(email.trim().toLowerCase())
                .map(user -> user.getRole() != null ? user.getRole().getCode() : null)
                .orElse(null);
    }

    /**
     * Builds a temporary password that satisfies the {@link Password} policy by construction:
     * the prefix carries the upper case, lower case and special character, and a digit is
     * appended because the random Base64 segment is not guaranteed to contain one.
     */
    private String generateSecureTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[9];
        random.nextBytes(bytes);
        return "Tmp!" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) + random.nextInt(10);
    }
}
