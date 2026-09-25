package com.materia.backend.contexts.employee.application.services;

import com.materia.backend.contexts.employee.application.dtos.CreateEmployeeInput;
import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.application.dtos.UpdateEmployeeInput;
import com.materia.backend.contexts.employee.application.mappers.EmployeeDtoMapper;
import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.exceptions.EmailAlreadyInUseException;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeCodeAlreadyExistsException;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeNotFoundException;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeUseCase;
import com.materia.backend.contexts.employee.domain.ports.out.EmployeeRepository;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeFullName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 🔹 EMPLOYEE APPLICATION SERVICE
 *
 * Handles standard Employee CRUD, searching, and profile updates.
 */
@Service
@Transactional
public class EmployeeService implements EmployeeUseCase {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeDtoMapper employeeDtoMapper;
    private final EmployeeCodeGeneratorService codeGeneratorService;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            EmployeeDtoMapper employeeDtoMapper,
            EmployeeCodeGeneratorService codeGeneratorService) {
        this.employeeRepository = employeeRepository;
        this.employeeDtoMapper = employeeDtoMapper;
        this.codeGeneratorService = codeGeneratorService;
    }

    @Override
    public EmployeeOutput create(CreateEmployeeInput input) {
        if (input == null) {
            throw new IllegalArgumentException("CreateEmployeeInput cannot be null");
        }

        EmployeeCode code = resolveOrGenerateCode(input.code());

        if (employeeRepository.existsByCode(code)) {
            throw new EmployeeCodeAlreadyExistsException(code.getValue());
        }

        String email = input.email() != null ? input.email().trim().toLowerCase() : null;
        if (email != null && employeeRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email);
        }

        Employee employee = employeeDtoMapper.toDomain(input);
        employee.setCode(code);

        Employee saved = employeeRepository.save(employee);
        log.info("[EMPLOYEE-SERVICE] Created employee with ID {} and code {}", saved.getId(), saved.getCode());

        return employeeDtoMapper.toOutput(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeOutput getById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return employeeDtoMapper.toOutput(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeOutput getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be empty");
        }
        Employee employee = employeeRepository.findByCode(EmployeeCode.of(code))
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with code: " + code));
        return employeeDtoMapper.toOutput(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeOutput getByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        Employee employee = employeeRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with email: " + email));
        return employeeDtoMapper.toOutput(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeOutput> getAll() {
        return employeeDtoMapper.toOutputList(employeeRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeOutput> getByStatus(EmploymentStatus status) {
        if (status == null) {
            return getAll();
        }
        return employeeDtoMapper.toOutputList(employeeRepository.findByStatus(status));
    }

    @Override
    public EmployeeOutput update(UUID id, UpdateEmployeeInput input) {
        if (id == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        if (input == null) {
            throw new IllegalArgumentException("UpdateEmployeeInput cannot be null");
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        EmployeeFullName fullName = (input.firstName() != null && input.lastName() != null)
                ? EmployeeFullName.of(input.firstName(), input.lastName())
                : employee.getFullName();

        employee.updateDetails(fullName, input.phone());
        if (input.status() != null) {
            employee.updateStatus(input.status());
        }

        Employee updated = employeeRepository.save(employee);
        log.info("[EMPLOYEE-SERVICE] Updated employee ID {}", updated.getId());

        return employeeDtoMapper.toOutput(updated);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        if (!employeeRepository.findById(id).isPresent()) {
            throw new EmployeeNotFoundException(id);
        }
        employeeRepository.deleteById(id);
        log.info("[EMPLOYEE-SERVICE] Deleted employee ID {}", id);
    }

    private EmployeeCode resolveOrGenerateCode(String codeString) {
        if (codeString != null && !codeString.trim().isEmpty()) {
            return EmployeeCode.of(codeString);
        }
        return codeGeneratorService.generateCode();
    }
}
