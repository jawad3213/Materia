package com.materia.backend.contexts.auth.application.services;

import com.materia.backend.contexts.auth.application.dtos.ProfileOutput;
import com.materia.backend.contexts.auth.application.dtos.UpdateProfileInput;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.exceptions.UserNotFoundException;
import com.materia.backend.contexts.auth.domain.ports.in.ProfileUseCase;
import com.materia.backend.contexts.auth.domain.ports.out.EmployeeProfileSync;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 🔹 PROFILE SERVICE
 *
 * The signed-in user's own account: read it, and change the name and phone number.
 * A change is mirrored on the linked employee record when there is one.
 */
@Service
@Transactional
public class ProfileService implements ProfileUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);
    static final int MAX_NAME_LENGTH = 100;
    static final int MAX_PHONE_LENGTH = 50;

    private final UserRepository userRepository;
    private EmployeeProfileSync employeeProfileSync;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired(required = false)
    public void setEmployeeProfileSync(EmployeeProfileSync employeeProfileSync) {
        this.employeeProfileSync = employeeProfileSync;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileOutput getProfile(UUID userId) {
        return toOutput(load(userId));
    }

    @Override
    public ProfileOutput updateProfile(UUID userId, UpdateProfileInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Profile details are required");
        }
        User user = load(userId);
        String firstName = requiredName(input.firstName(), "First name");
        String lastName = requiredName(input.lastName(), "Last name");
        String phone = optionalPhone(input.phone());

        user.updateProfile(firstName, lastName, phone);
        User saved = userRepository.save(user);

        if (employeeProfileSync != null) {
            employeeProfileSync.syncContactDetails(saved.getId(), firstName, lastName, phone);
        }
        log.info("[PROFILE] User {} updated their profile", saved.getId());
        return toOutput(saved);
    }

    private User load(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    }

    private static String requiredName(String value, String label) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(label + " must be at most " + MAX_NAME_LENGTH + " characters");
        }
        return trimmed;
    }

    private static String optionalPhone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_PHONE_LENGTH) {
            throw new IllegalArgumentException("Phone must be at most " + MAX_PHONE_LENGTH + " characters");
        }
        if (!trimmed.matches("[+0-9 ()./-]+")) {
            throw new IllegalArgumentException("Phone may only contain digits, spaces and + ( ) . / -");
        }
        return trimmed;
    }

    private static ProfileOutput toOutput(User user) {
        return new ProfileOutput(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getDepartment(),
                user.getRole() != null ? user.getRole().getCode() : null,
                user.getRole() != null ? user.getRole().getLabel() : null,
                user.getStatus() != null ? user.getStatus().name() : null,
                user.isMustChangePassword(),
                user.getRole() != null ? user.getRole().getPermissions().stream().sorted().toList() : java.util.List.of(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
