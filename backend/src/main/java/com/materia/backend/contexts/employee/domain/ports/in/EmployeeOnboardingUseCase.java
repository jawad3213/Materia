package com.materia.backend.contexts.employee.domain.ports.in;

import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.application.dtos.OnboardEmployeeInput;

/**
 * 🔹 EMPLOYEE ONBOARDING USE CASE (INPUT PORT)
 *
 * Orchestrates creating an employee, initializing profile, and optionally provisioning security credentials.
 */
public interface EmployeeOnboardingUseCase {

    EmployeeOutput onboard(OnboardEmployeeInput input);
}
