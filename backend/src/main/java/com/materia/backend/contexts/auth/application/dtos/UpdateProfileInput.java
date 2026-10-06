package com.materia.backend.contexts.auth.application.dtos;

/**
 * 🔹 UPDATE PROFILE INPUT DTO
 *
 * What users may change about themselves. E-mail, role, department and status are managed by an administrator.
 */
public record UpdateProfileInput(
        String firstName,
        String lastName,
        String phone
) {}
