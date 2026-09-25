package com.materia.backend.contexts.employee.domain.ports.in;

import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.application.dtos.OffboardEmployeeInput;

/**
 * 🔹 EMPLOYEE OFFBOARDING USE CASE (INPUT PORT)
 *
 * Orchestrates terminating an employee and immediately revoking authentication credentials / sessions.
 */
public interface EmployeeOffboardingUseCase {

    EmployeeOutput offboard(OffboardEmployeeInput input);
}
