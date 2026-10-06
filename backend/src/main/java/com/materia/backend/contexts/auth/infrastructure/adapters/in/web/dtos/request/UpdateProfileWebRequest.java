package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileWebRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be at most 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must be at most 100 characters")
        String lastName,

        @Size(max = 50, message = "Phone must be at most 50 characters")
        @Pattern(regexp = "^$|^[+0-9 ()./-]+$", message = "Phone may only contain digits, spaces and + ( ) . / -")
        String phone
) {}
