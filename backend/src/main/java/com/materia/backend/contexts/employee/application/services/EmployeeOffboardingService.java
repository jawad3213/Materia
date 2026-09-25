package com.materia.backend.contexts.employee.application.services;

import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.application.dtos.OffboardEmployeeInput;
import com.materia.backend.contexts.employee.application.mappers.EmployeeDtoMapper;
import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeNotFoundException;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeOffboardingUseCase;
import com.materia.backend.contexts.employee.domain.ports.out.CredentialPort;
import com.materia.backend.contexts.employee.domain.ports.out.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 🔹 EMPLOYEE OFFBOARDING SERVICE
 *
 * Orchestrates:
 * 1. Terminating employee contract and status
 * 2. Logging termination reason and date
 * 3. Immediately revoking user security credentials / active sessions via CredentialPort
 */
@Service
@Transactional
public class EmployeeOffboardingService implements EmployeeOffboardingUseCase {

    private static final Logger log = LoggerFactory.getLogger(EmployeeOffboardingService.class);

    private final EmployeeRepository employeeRepository;
    private final CredentialPort credentialPort;
    private final EmployeeDtoMapper employeeDtoMapper;

    public EmployeeOffboardingService(
            EmployeeRepository employeeRepository,
            CredentialPort credentialPort,
            EmployeeDtoMapper employeeDtoMapper) {
        this.employeeRepository = employeeRepository;
        this.credentialPort = credentialPort;
        this.employeeDtoMapper = employeeDtoMapper;
    }

    @Override
    public EmployeeOutput offboard(OffboardEmployeeInput input) {
        if (input == null || input.employeeId() == null) {
            throw new IllegalArgumentException("OffboardEmployeeInput and employeeId are required");
        }

        Employee employee = employeeRepository.findById(input.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(input.employeeId()));

        LocalDate termDate = input.terminationDate() != null ? input.terminationDate() : LocalDate.now();
        String reason = input.reason() != null ? input.reason() : "Offboarding";

        employee.terminate(termDate, reason);

        // Immediate revocation of auth user account and active tokens.
        // Already issued access tokens stay valid until they expire (JWT_EXPIRATION_MS),
        // because the gateway validates them without a database round trip.
        if (input.revokeUserAccess() && employee.getUserId() != null) {
            if (credentialPort.isUserActive(employee.getUserId())) {
                log.warn("[OFFBOARDING] Revoking security access and active sessions for user ID: {}", employee.getUserId());
                credentialPort.revokeUserAccess(employee.getUserId());
            } else {
                log.info("[OFFBOARDING] Security access already revoked for user ID: {}", employee.getUserId());
            }
        }

        Employee saved = employeeRepository.save(employee);
        log.info("[OFFBOARDING] Successfully offboarded employee [{}] with ID {}", saved.getCode(), saved.getId());

        return employeeDtoMapper.toOutput(saved);
    }
}
