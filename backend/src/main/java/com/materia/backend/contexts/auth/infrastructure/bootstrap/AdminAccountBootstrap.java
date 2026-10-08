package com.materia.backend.contexts.auth.infrastructure.bootstrap;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.auth.domain.valueObjects.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 🔹 FIRST ADMINISTRATOR
 *
 * A fresh install has no account (migrations never seed passwords). When the users table is empty, this creates one
 * administrator from APP_BOOTSTRAP_ADMIN_EMAIL and APP_BOOTSTRAP_ADMIN_PASSWORD, who must choose a new password at the
 * first sign-in. Once any account exists it does nothing, so the variables can be removed after the first start.
 */
@Component
public class AdminAccountBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminAccountBootstrap(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 @Value("${app.bootstrap-admin.email:}") String email,
                                 @Value("${app.bootstrap-admin.password:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        createIfNoAccount();
    }

    /** @return true when the administrator was created */
    boolean createIfNoAccount() {
        if (userRepository.count() > 0) {
            return false;
        }
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            log.warn("[BOOTSTRAP] No user account exists. Set APP_BOOTSTRAP_ADMIN_EMAIL and APP_BOOTSTRAP_ADMIN_PASSWORD "
                    + "to create the first administrator.");
            return false;
        }
        try {
            Password.fromRaw(password);
        } catch (IllegalArgumentException e) {
            log.error("[BOOTSTRAP] APP_BOOTSTRAP_ADMIN_PASSWORD does not meet the password policy ({}); no account created.",
                    e.getMessage());
            return false;
        }

        User admin = User.builder()
                .email(email.trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .firstName("System")
                .lastName("Administrator")
                .mustChangePassword(true)
                .createdBy("bootstrap")
                .build();
        userRepository.save(admin);
        log.info("[BOOTSTRAP] Created the first administrator {}; a new password is required at first sign-in.", admin.getEmail());
        return true;
    }
}
