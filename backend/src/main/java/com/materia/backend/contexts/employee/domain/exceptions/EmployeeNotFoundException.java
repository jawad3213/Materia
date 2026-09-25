package com.materia.backend.contexts.employee.domain.exceptions;

import com.materia.backend.common.application.exceptions.NotFoundException;

import java.util.UUID;

public class EmployeeNotFoundException extends NotFoundException {

    public EmployeeNotFoundException(UUID id) {
        super("Employee not found with ID: " + id, "EMPLOYEE_NOT_FOUND");
    }

    public EmployeeNotFoundException(String message) {
        super(message, "EMPLOYEE_NOT_FOUND");
    }
}
