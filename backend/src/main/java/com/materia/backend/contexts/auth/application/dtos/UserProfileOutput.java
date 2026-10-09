package com.materia.backend.contexts.auth.application.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The signed-in user's own account details, as shown on the profile page.
 */
public record UserProfileOutput(UUID id, String email, String firstName, String lastName, String fullName,
                                String phone, String department, String roleCode, String statusCode,
                                LocalDateTime createdAt) {
}
