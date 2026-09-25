package com.materia.backend.contexts.employee.application.services;

import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.application.dtos.OffboardEmployeeInput;
import com.materia.backend.contexts.employee.application.mappers.EmployeeDtoMapper;
import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeNotFoundException;
import com.materia.backend.contexts.employee.domain.ports.out.CredentialPort;
import com.materia.backend.contexts.employee.domain.ports.out.EmployeeRepository;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeFullName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeOffboardingServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CredentialPort credentialPort;

    private EmployeeOffboardingService offboardingService;

    private final UUID employeeId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private Employee employee;

    @BeforeEach
    void setUp() {
        offboardingService = new EmployeeOffboardingService(employeeRepository, credentialPort, new EmployeeDtoMapper());

        employee = Employee.builder()
                .id(employeeId)
                .code(EmployeeCode.of("EMP-0001"))
                .fullName(EmployeeFullName.of("John", "Doe"))
                .email("john@example.com")
                .status(EmploymentStatus.ACTIVE)
                .userId(userId)
                .build();
    }

    private void stubRepository() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should terminate the employee and revoke access for an active account")
    void shouldRevokeAccessForActiveAccount() {
        stubRepository();
        when(credentialPort.isUserActive(userId)).thenReturn(true);

        EmployeeOutput output = offboardingService.offboard(
                new OffboardEmployeeInput(employeeId, LocalDate.of(2026, 3, 31), "Resignation", true));

        assertEquals(EmploymentStatus.TERMINATED, output.status());
        assertEquals(LocalDate.of(2026, 3, 31), output.terminationDate());
        verify(credentialPort).revokeUserAccess(userId);
    }

    @Test
    @DisplayName("Should skip revocation when the account is already revoked")
    void shouldSkipRevocationWhenAlreadyRevoked() {
        stubRepository();
        when(credentialPort.isUserActive(userId)).thenReturn(false);

        offboardingService.offboard(new OffboardEmployeeInput(employeeId, null, "Resignation", true));

        verify(credentialPort, never()).revokeUserAccess(any(UUID.class));
    }

    @Test
    @DisplayName("Should not touch credentials when revocation was not requested")
    void shouldNotRevokeWhenNotRequested() {
        stubRepository();

        offboardingService.offboard(new OffboardEmployeeInput(employeeId, null, "Resignation", false));

        verifyNoInteractions(credentialPort);
    }

    @Test
    @DisplayName("Should fail when the employee does not exist")
    void shouldFailForUnknownEmployee() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
                () -> offboardingService.offboard(new OffboardEmployeeInput(employeeId, null, null, true)));

        verifyNoInteractions(credentialPort);
    }
}
