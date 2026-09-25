package com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.request;

import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeWebRequest(
        @Size(max = 100, message = "First name must be at most 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must be at most 100 characters")
        String lastName,

        @Size(max = 50, message = "Phone must be at most 50 characters")
        String phone,

        EmploymentStatus status
) {}
