package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.auth.application.dtos.UpdateProfileInput;
import com.materia.backend.contexts.auth.application.dtos.UserProfileOutput;
import com.materia.backend.contexts.auth.domain.exceptions.AuthenticationFailedException;
import com.materia.backend.contexts.auth.domain.ports.in.UserProfileUseCase;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.UpdateProfileWebRequest;
import com.materia.backend.gateway.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 🔹 PROFILE REST CONTROLLER (INBOUND ADAPTER)
 *
 * Lets any signed-in user read and edit their own account. /api/v1/auth/** is a public route,
 * so the caller's identity is checked here rather than by the security chain.
 */
@RestController
@RequestMapping("/api/v1/auth/me")
public class ProfileController {

    private final UserProfileUseCase profileUseCase;

    public ProfileController(UserProfileUseCase profileUseCase) {
        this.profileUseCase = profileUseCase;
    }

    @GetMapping
    public ResponseEntity<UserProfileOutput> getProfile() {
        return ResponseEntity.ok(profileUseCase.getProfile(currentUserId()));
    }

    @PutMapping
    public ResponseEntity<UserProfileOutput> updateProfile(@Valid @RequestBody UpdateProfileWebRequest request) {
        UpdateProfileInput input = new UpdateProfileInput(request.firstName(), request.lastName(), request.phone());
        return ResponseEntity.ok(profileUseCase.updateProfile(currentUserId(), input));
    }

    private static UUID currentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AuthenticationFailedException("Authentication required", "AUTH_REQUIRED"));
    }
}
