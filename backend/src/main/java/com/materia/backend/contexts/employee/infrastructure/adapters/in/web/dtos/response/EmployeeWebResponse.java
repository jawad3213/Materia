package com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeWebResponse(
        UUID id,
        String code,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        EmploymentStatus status,
        UUID userId,
        String role,
        LocalDate hireDate,
        LocalDate terminationDate,
        String terminationReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
