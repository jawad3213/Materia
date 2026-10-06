package com.materia.backend.contexts.auth.application.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 🔹 PROFILE OUTPUT DTO
 *
 * The signed-in user's own account, as shown on the profile page.
 */
public record ProfileOutput(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        String phone,
        String department,
        String role,
        String roleLabel,
        String status,
        boolean mustChangePassword,
        List<String> permissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
