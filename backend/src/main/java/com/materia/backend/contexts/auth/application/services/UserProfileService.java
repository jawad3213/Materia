package com.materia.backend.contexts.auth.application.services;

import com.materia.backend.contexts.auth.application.dtos.UpdateProfileInput;
import com.materia.backend.contexts.auth.application.dtos.UserProfileOutput;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.events.UserProfileUpdatedEvent;
import com.materia.backend.contexts.auth.domain.exceptions.UserNotFoundException;
import com.materia.backend.contexts.auth.domain.ports.in.UserProfileUseCase;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserProfileService implements UserProfileUseCase {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserProfileService(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileOutput getProfile(UUID userId) {
        return toOutput(load(userId));
    }

    @Override
    public UserProfileOutput updateProfile(UUID userId, UpdateProfileInput input) {
        User user = load(userId);
        String firstName = trimToNull(input.firstName());
        String lastName = trimToNull(input.lastName());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        // Set explicitly: the setters only fill a blank full name, so it would keep part of the old name.
        String fullName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        user.setFullName(fullName.isEmpty() ? null : fullName);
        user.setPhone(trimToNull(input.phone()));
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserProfileUpdatedEvent(
                saved.getId(), saved.getFirstName(), saved.getLastName(), saved.getPhone()));
        return toOutput(saved);
    }

    private User load(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static UserProfileOutput toOutput(User user) {
        return new UserProfileOutput(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getDepartment(),
                user.getRole() != null ? user.getRole().getCode() : null,
                user.getStatus() != null ? user.getStatus().getCode() : null,
                user.getCreatedAt());
    }
}
