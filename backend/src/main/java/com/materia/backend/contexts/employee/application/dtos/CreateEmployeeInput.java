package com.materia.backend.contexts.employee.application.dtos;

import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;

import java.time.LocalDate;

public record CreateEmployeeInput(
        String code,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate hireDate,
        EmploymentStatus status
) {}
