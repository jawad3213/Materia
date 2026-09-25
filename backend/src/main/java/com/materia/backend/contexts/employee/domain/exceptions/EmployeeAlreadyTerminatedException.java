package com.materia.backend.contexts.employee.domain.exceptions;

import com.materia.backend.common.application.exceptions.BusinessException;

public class EmployeeAlreadyTerminatedException extends BusinessException {

    public EmployeeAlreadyTerminatedException(String code) {
        super("Employee with code " + code + " is already terminated", "EMPLOYEE_ALREADY_TERMINATED");
    }
}
