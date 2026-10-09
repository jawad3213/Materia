package com.materia.backend.contexts.auth.infrastructure.seed;

import com.materia.backend.common.infrastructure.seed.DevSeed;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Dev only: the admin, purchaser and receiver accounts the login page offers in development, all with the password
 * {@code app.seed.password}. They are ready to use (no password change, no e-mail). Existing accounts are left alone.
 */
@Component
@Profile(DevSeed.PROFILE)
@ConditionalOnProperty(name = DevSeed.ENABLED_PROPERTY, havingValue = "true")
@Order(DevSeed.ACCOUNTS_ORDER)
public class DevAccountsSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevAccountsSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String password;

    public DevAccountsSeeder(UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             @Value("${app.seed.password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        Password.fromRaw(password); // a dev password must follow the same policy as any other
        create(DevSeed.ADMIN_EMAIL, "Amina", "Benali", Role.ADMIN);
        create(DevSeed.PURCHASER_EMAIL, "Youssef", "El Alami", Role.PURCHASER);
        create(DevSeed.RECEIVER_EMAIL, "Leila", "Amrani", Role.RECEIVER);
    }

    private void create(String email, String firstName, String lastName, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .firstName(firstName)
                .lastName(lastName)
                .mustChangePassword(false)
                .createdBy(DevSeed.SEEDER)
                .build());
        log.info("[DEV-SEED] Created the {} account {}", role.getCode(), email);
    }
}
