package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.auth.application.dtos.ProfileOutput;
import com.materia.backend.contexts.auth.application.dtos.UpdateProfileInput;
import com.materia.backend.contexts.auth.domain.exceptions.AuthenticationFailedException;
import com.materia.backend.contexts.auth.domain.ports.in.ProfileUseCase;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.UpdateProfileWebRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 🔹 PROFILE REST CONTROLLER
 *
 * The signed-in user's own account. No permission beyond being signed in is needed: the account is always the
 * caller's, taken from the access token, never from the request.
 */
@RestController
@RequestMapping("/api/v1/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final ProfileUseCase profileUseCase;

    public ProfileController(ProfileUseCase profileUseCase) {
        this.profileUseCase = profileUseCase;
    }

    @GetMapping
    public ResponseEntity<ProfileOutput> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileUseCase.getProfile(currentUserId(authentication)));
    }

    @PutMapping
    public ResponseEntity<ProfileOutput> updateProfile(
            @Valid @RequestBody UpdateProfileWebRequest request,
            Authentication authentication) {
        UpdateProfileInput input = new UpdateProfileInput(request.firstName(), request.lastName(), request.phone());
        return ResponseEntity.ok(profileUseCase.updateProfile(currentUserId(authentication), input));
    }

    /** The JWT filter puts the user's id in the principal name. */
    private static UUID currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AuthenticationFailedException("No signed-in user");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new AuthenticationFailedException("No signed-in user");
        }
    }
}
