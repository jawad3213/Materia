package com.materia.backend.contexts.employee.application.dtos;

import java.time.LocalDate;
import java.util.UUID;

public record OffboardEmployeeInput(
        UUID employeeId,
        LocalDate terminationDate,
        String reason,
        boolean revokeUserAccess
) {}
