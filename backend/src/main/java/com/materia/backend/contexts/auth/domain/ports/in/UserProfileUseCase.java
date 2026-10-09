package com.materia.backend.contexts.auth.domain.ports.in;

import com.materia.backend.contexts.auth.application.dtos.UpdateProfileInput;
import com.materia.backend.contexts.auth.application.dtos.UserProfileOutput;

import java.util.UUID;

/** Self-service read and update of the signed-in user's own account. */
public interface UserProfileUseCase {

    UserProfileOutput getProfile(UUID userId);

    UserProfileOutput updateProfile(UUID userId, UpdateProfileInput input);
}
