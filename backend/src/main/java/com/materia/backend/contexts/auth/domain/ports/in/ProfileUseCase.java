package com.materia.backend.contexts.auth.domain.ports.in;

import com.materia.backend.contexts.auth.application.dtos.ProfileOutput;
import com.materia.backend.contexts.auth.application.dtos.UpdateProfileInput;

import java.util.UUID;

/**
 * 🔹 PROFILE USE CASE (INPUT PORT)
 *
 * Reading and editing the signed-in user's own account.
 */
public interface ProfileUseCase {

    ProfileOutput getProfile(UUID userId);

    ProfileOutput updateProfile(UUID userId, UpdateProfileInput input);
}
