package com.materia.backend.contexts.employee.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.employee.application.dtos.CreateEmployeeInput;
import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.application.dtos.OffboardEmployeeInput;
import com.materia.backend.contexts.employee.application.dtos.OnboardEmployeeInput;
import com.materia.backend.contexts.employee.application.dtos.UpdateEmployeeInput;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeOffboardingUseCase;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeOnboardingUseCase;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeUseCase;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.request.CreateEmployeeWebRequest;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.request.OffboardEmployeeWebRequest;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.request.OnboardEmployeeWebRequest;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.request.UpdateEmployeeWebRequest;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.response.EmployeeWebResponse;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.mappers.EmployeeWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 🔹 EMPLOYEE REST CONTROLLER
 *
 * REST API for employee management, onboarding, and offboarding.
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeUseCase employeeUseCase;
    private final EmployeeOnboardingUseCase onboardingUseCase;
    private final EmployeeOffboardingUseCase offboardingUseCase;
    private final EmployeeWebMapper webMapper;

    public EmployeeController(
            EmployeeUseCase employeeUseCase,
            EmployeeOnboardingUseCase onboardingUseCase,
            EmployeeOffboardingUseCase offboardingUseCase,
            EmployeeWebMapper webMapper) {
        this.employeeUseCase = employeeUseCase;
        this.onboardingUseCase = onboardingUseCase;
        this.offboardingUseCase = offboardingUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping
    public ResponseEntity<EmployeeWebResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeWebRequest webRequest) {
        CreateEmployeeInput input = webMapper.toAppCreateInput(webRequest);
        EmployeeOutput output = employeeUseCase.create(input);
        return new ResponseEntity<>(webMapper.toWebResponse(output), HttpStatus.CREATED);
    }

    @PostMapping("/onboard")
    public ResponseEntity<EmployeeWebResponse> onboardEmployee(
            @Valid @RequestBody OnboardEmployeeWebRequest webRequest) {
        OnboardEmployeeInput input = webMapper.toAppOnboardInput(webRequest);
        EmployeeOutput output = onboardingUseCase.onboard(input);
        return new ResponseEntity<>(webMapper.toWebResponse(output), HttpStatus.CREATED);
    }

    @PostMapping("/offboard")
    public ResponseEntity<EmployeeWebResponse> offboardEmployee(
            @Valid @RequestBody OffboardEmployeeWebRequest webRequest) {
        OffboardEmployeeInput input = webMapper.toAppOffboardInput(webRequest);
        EmployeeOutput output = offboardingUseCase.offboard(input);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeWebResponse> getEmployeeById(@PathVariable UUID id) {
        EmployeeOutput output = employeeUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<EmployeeWebResponse> getEmployeeByCode(@PathVariable String code) {
        EmployeeOutput output = employeeUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<EmployeeWebResponse> getEmployeeByEmail(@PathVariable String email) {
        EmployeeOutput output = employeeUseCase.getByEmail(email);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeWebResponse>> getAllEmployees(
            @RequestParam(required = false) EmploymentStatus status) {
        List<EmployeeOutput> outputs;
        if (status != null) {
            outputs = employeeUseCase.getByStatus(status);
        } else {
            outputs = employeeUseCase.getAll();
        }
        return ResponseEntity.ok(webMapper.toWebResponseList(outputs));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeWebResponse> updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmployeeWebRequest webRequest) {
        UpdateEmployeeInput input = webMapper.toAppUpdateInput(webRequest);
        EmployeeOutput output = employeeUseCase.update(id, input);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        employeeUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
