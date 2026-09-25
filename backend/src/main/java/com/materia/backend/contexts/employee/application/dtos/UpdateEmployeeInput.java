package com.materia.backend.contexts.employee.application.dtos;

import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;

public record UpdateEmployeeInput(
        String firstName,
        String lastName,
        String phone,
        EmploymentStatus status
) {}
