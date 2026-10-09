package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 🔹 UPDATE PROFILE WEB REQUEST
 *
 * HTTP request body for PUT /api/v1/auth/me.
 */
public record UpdateProfileWebRequest(
        @NotBlank(message = "First name must not be blank")
        @Size(max = 100, message = "First name must be at most 100 characters")
        String firstName,

        @NotBlank(message = "Last name must not be blank")
        @Size(max = 100, message = "Last name must be at most 100 characters")
        String lastName,

        @Size(max = 30, message = "Phone must be at most 30 characters")
        @Pattern(regexp = "^$|^[+0-9 ()./-]{3,30}$", message = "Phone may only contain digits, spaces and + ( ) . / -")
        String phone
) {}
