package com.materia.backend.contexts.auth.application.dtos;

/**
 * The fields a user may change on their own account. E-mail, role and status stay admin-managed.
 */
public record UpdateProfileInput(String firstName, String lastName, String phone) {
}
