package com.materia.backend.contexts.employee.application.services;

import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.application.dtos.OnboardEmployeeInput;
import com.materia.backend.contexts.employee.application.mappers.EmployeeDtoMapper;
import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.exceptions.EmailAlreadyInUseException;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeCodeAlreadyExistsException;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeOnboardingUseCase;
import com.materia.backend.contexts.employee.domain.ports.out.CredentialPort;
import com.materia.backend.contexts.employee.domain.ports.out.EmployeeRepository;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeFullName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 🔹 EMPLOYEE ONBOARDING SERVICE
 *
 * Orchestrates:
 * 1. Validating new employee parameters
 * 2. Creating Employee enterprise record
 * 3. Optionally provisioning login credentials in the auth bounded context via CredentialPort
 * 4. Linking generated auth userId to the employee record
 */
@Service
@Transactional
public class EmployeeOnboardingService implements EmployeeOnboardingUseCase {

    private static final Logger log = LoggerFactory.getLogger(EmployeeOnboardingService.class);

    private final EmployeeRepository employeeRepository;
    private final CredentialPort credentialPort;
    private final EmployeeDtoMapper employeeDtoMapper;
    private final EmployeeCodeGeneratorService codeGeneratorService;

    public EmployeeOnboardingService(
            EmployeeRepository employeeRepository,
            CredentialPort credentialPort,
            EmployeeDtoMapper employeeDtoMapper,
            EmployeeCodeGeneratorService codeGeneratorService) {
        this.employeeRepository = employeeRepository;
        this.credentialPort = credentialPort;
        this.employeeDtoMapper = employeeDtoMapper;
        this.codeGeneratorService = codeGeneratorService;
    }

    @Override
    public EmployeeOutput onboard(OnboardEmployeeInput input) {
        if (input == null) {
            throw new IllegalArgumentException("OnboardEmployeeInput cannot be null");
        }

        EmployeeCode code = resolveOrGenerateCode(input.code());
        if (employeeRepository.existsByCode(code)) {
            throw new EmployeeCodeAlreadyExistsException(code.getValue());
        }

        String email = input.email() != null ? input.email().trim().toLowerCase() : null;
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Employee email is mandatory for onboarding");
        }
        if (employeeRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email);
        }

        EmployeeFullName fullName = EmployeeFullName.of(input.firstName(), input.lastName());

        Employee employee = Employee.builder()
                .code(code)
                .fullName(fullName)
                .email(email)
                .phone(input.phone())
                .hireDate(input.hireDate())
                .status(input.status() != null ? input.status() : EmploymentStatus.ACTIVE)
                .build();

        // Optional provision of user credentials in Auth system
        if (input.provisionCredentials()) {
            // The role code is resolved and validated by the credential port, which owns the role catalog.
            log.info("[ONBOARDING] Provisioning login account in Auth context for email {} with role {}", email, input.roleCode());
            UUID userId = credentialPort.provisionUserAccount(
                    email,
                    input.firstName(),
                    input.lastName(),
                    input.roleCode(),
                    input.initialPassword()
            );
            employee.linkUserId(userId);
            log.info("[ONBOARDING] Linked auth User ID {} to employee code {}", userId, code.getValue());
        }

        Employee saved = employeeRepository.save(employee);
        log.info("[ONBOARDING] Successfully onboarded employee [{}] with ID {}", saved.getCode(), saved.getId());

        return employeeDtoMapper.toOutput(saved);
    }

    private EmployeeCode resolveOrGenerateCode(String codeString) {
        if (codeString != null && !codeString.trim().isEmpty()) {
            return EmployeeCode.of(codeString);
        }
        return codeGeneratorService.generateCode();
    }
}
