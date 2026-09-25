package com.materia.backend.contexts.employee.application.dtos;

import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 🔹 EMPLOYEE OUTPUT DTO
 */
public record EmployeeOutput(
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
