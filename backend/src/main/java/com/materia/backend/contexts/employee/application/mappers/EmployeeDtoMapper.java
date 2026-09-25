package com.materia.backend.contexts.employee.application.mappers;

import com.materia.backend.contexts.employee.application.dtos.CreateEmployeeInput;
import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.ports.out.CredentialPort;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeFullName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🔹 EMPLOYEE DTO MAPPER
 *
 * Converts between domain entities and application layer DTOs.
 */
@Component
public class EmployeeDtoMapper {

    private final CredentialPort credentialPort;

    public EmployeeDtoMapper() {
        this.credentialPort = null;
    }

    @Autowired
    public EmployeeDtoMapper(CredentialPort credentialPort) {
        this.credentialPort = credentialPort;
    }

    public EmployeeOutput toOutput(Employee employee) {
        if (employee == null) {
            return null;
        }

        String firstName = employee.getFullName() != null ? employee.getFullName().getFirstName() : null;
        String lastName = employee.getFullName() != null ? employee.getFullName().getLastName() : null;
        String fullName = employee.getFullName() != null ? employee.getFullName().getFullName() : null;
        String code = employee.getCode() != null ? employee.getCode().getValue() : null;

        String role = null;
        if (credentialPort != null) {
            if (employee.getUserId() != null) {
                role = credentialPort.getUserRoleCode(employee.getUserId());
            }
            if (role == null && employee.getEmail() != null) {
                role = credentialPort.getUserRoleCodeByEmail(employee.getEmail());
            }
        }

        return new EmployeeOutput(
                employee.getId(),
                code,
                firstName,
                lastName,
                fullName,
                employee.getEmail(),
                employee.getPhone(),
                employee.getStatus(),
                employee.getUserId(),
                role,
                employee.getHireDate(),
                employee.getTerminationDate(),
                employee.getTerminationReason(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }

    public List<EmployeeOutput> toOutputList(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return Collections.emptyList();
        }
        return employees.stream()
                .map(this::toOutput)
                .collect(Collectors.toList());
    }

    public Employee toDomain(CreateEmployeeInput input) {
        if (input == null) {
            return null;
        }

        EmployeeCode code = input.code() != null ? EmployeeCode.of(input.code()) : null;
        EmployeeFullName fullName = (input.firstName() != null && input.lastName() != null)
                ? EmployeeFullName.of(input.firstName(), input.lastName())
                : null;

        return Employee.builder()
                .code(code)
                .fullName(fullName)
                .email(input.email())
                .phone(input.phone())
                .hireDate(input.hireDate())
                .status(input.status())
                .build();
    }
}
