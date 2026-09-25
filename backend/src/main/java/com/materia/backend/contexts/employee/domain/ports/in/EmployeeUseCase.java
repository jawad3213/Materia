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
}
