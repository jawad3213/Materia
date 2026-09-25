package com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record OffboardEmployeeWebRequest(
        @NotNull(message = "Employee ID is mandatory")
        UUID employeeId,

        LocalDate terminationDate,

        @Size(max = 500, message = "Reason must be at most 500 characters")
        String reason,

        boolean revokeUserAccess
) {}
