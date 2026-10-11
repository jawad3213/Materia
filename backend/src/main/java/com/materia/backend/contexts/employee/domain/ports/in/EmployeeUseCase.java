package com.materia.backend.contexts.employee.domain.ports.in;

import com.materia.backend.contexts.employee.application.dtos.CreateEmployeeInput;
import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.application.dtos.UpdateEmployeeInput;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;

import java.util.List;
import java.util.UUID;

/**
 * 🔹 EMPLOYEE USE CASE (INPUT PORT)
 *
 * Core CRUD and query operations for Employee.
 */
public interface EmployeeUseCase {

    EmployeeOutput create(CreateEmployeeInput input);

    EmployeeOutput getById(UUID id);

    EmployeeOutput getByCode(String code);

    EmployeeOutput getByEmail(String email);

    List<EmployeeOutput> getAll();

    List<EmployeeOutput> getByStatus(EmploymentStatus status);

    EmployeeOutput update(UUID id, UpdateEmployeeInput input);

    void delete(UUID id);

    /**
     * Copies the name and phone a user set on their own account onto the employee linked to that account.
     * Does nothing when no employee is linked, or when the employee has left (their record is frozen).
     */
    void syncContactDetailsFromAccount(UUID userId, String firstName, String lastName, String phone);

    /**
     * Links an existing user account to the employee, for an employee onboarded without credentials whose account
     * already exists. Refused when the account is linked to another employee.
     */
    EmployeeOutput linkUserAccount(UUID employeeId, UUID userId);
}
