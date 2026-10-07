package com.materia.backend.contexts.auth.application.services;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.domain.exceptions.EmailAlreadyExistsException;
import com.materia.backend.contexts.auth.domain.ports.in.AccountProvisioningUseCase;
import com.materia.backend.contexts.auth.domain.ports.out.EmailSender;
import com.materia.backend.contexts.auth.domain.ports.out.RefreshTokenRepository;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.auth.domain.valueObjects.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/** Provisions and revokes the user accounts of employees. */
@Service
@Transactional
public class AccountProvisioningService implements AccountProvisioningUseCase {

    private static final Logger log = LoggerFactory.getLogger(AccountProvisioningService.class);

    /** Role granted when the caller does not specify one. An unknown code is rejected, never downgraded to this. */
    private static final Role DEFAULT_ROLE = Role.PURCHASER;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;

    public AccountProvisioningService(UserRepository userRepository,
                                      RefreshTokenRepository refreshTokenRepository,
                                      PasswordEncoder passwordEncoder,
                                      EmailSender emailSender) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
    }

    @Override
    public UUID provisionAccount(String email, String firstName, String lastName, String roleCode, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new EmailAlreadyExistsException("A user account already exists with email: " + normalizedEmail);
        }

        Role role = roleCode == null || roleCode.isBlank() ? DEFAULT_ROLE : Role.fromCode(roleCode.trim().toUpperCase());

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
        log.info("[ACCOUNT-PROVISIONING] Provisioned User ID {} with role {} for {} (mustChangePassword=true)",
                savedUser.getId(), role.getCode(), normalizedEmail);

        // Deliver credentials via transactional email
        emailSender.sendTemporaryPasswordEmail(normalizedEmail, password);
        return savedUser.getId();
    }

    @Override
    public void revokeAccess(UUID userId) {
        if (userId == null) return;
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(UserStatus.INACTIVE);
            user.setEnabled(false);
            userRepository.save(user);
            // Invalidate all active refresh tokens immediately to force logout
            refreshTokenRepository.revokeAllForUserId(userId);
            log.info("[ACCOUNT-PROVISIONING] Revoked all tokens and disabled user ID: {}", userId);
        });
    }

    @Override
    public List<String> roleCodes() {
        return Role.getCodes();
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
