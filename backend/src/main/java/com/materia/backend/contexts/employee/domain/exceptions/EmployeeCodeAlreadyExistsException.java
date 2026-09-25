package com.materia.backend.contexts.employee.domain.exceptions;

import com.materia.backend.common.application.exceptions.BusinessException;

public class EmployeeCodeAlreadyExistsException extends BusinessException {

    public EmployeeCodeAlreadyExistsException(String code) {
        super("Employee code already exists: " + code, "EMPLOYEE_CODE_ALREADY_EXISTS");
    }
}
