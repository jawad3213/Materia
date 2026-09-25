package com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.request;

import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateEmployeeWebRequest(
        @Size(max = 30, message = "Code must be at most 30 characters")
        String code,

        @NotBlank(message = "First name is mandatory")
        @Size(max = 100, message = "First name must be at most 100 characters")
        String firstName,

        @NotBlank(message = "Last name is mandatory")
        @Size(max = 100, message = "Last name must be at most 100 characters")
        String lastName,

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email must be a valid format")
        String email,

        @Size(max = 50, message = "Phone must be at most 50 characters")
        String phone,

        LocalDate hireDate,
        EmploymentStatus status
) {}
