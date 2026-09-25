package com.materia.backend.contexts.employee.domain.exceptions;

import com.materia.backend.common.application.exceptions.BusinessException;

import java.util.List;

public class InvalidRoleCodeException extends BusinessException {

    public InvalidRoleCodeException(String roleCode, List<String> allowedCodes) {
        super("Invalid role code: " + roleCode + ". Allowed values are " + String.join(", ", allowedCodes),
                "EMPLOYEE_INVALID_ROLE_CODE");
    }
}
