package com.materia.backend.contexts.employee.domain.exceptions;

import com.materia.backend.common.application.exceptions.BusinessException;

public class EmailAlreadyInUseException extends BusinessException {

    public EmailAlreadyInUseException(String email) {
        super("Employee email is already in use: " + email, "EMPLOYEE_EMAIL_ALREADY_IN_USE");
    }
}
