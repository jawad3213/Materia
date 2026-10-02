package com.materia.backend.contexts.employee.application.services;

import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.application.dtos.OnboardEmployeeInput;
import com.materia.backend.contexts.employee.application.mappers.EmployeeDtoMapper;
import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.exceptions.EmailAlreadyInUseException;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeCodeAlreadyExistsException;
import com.materia.backend.contexts.employee.domain.ports.out.CredentialPort;
import com.materia.backend.contexts.employee.domain.ports.out.EmployeeRepository;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** [T079] Onboarding service: credentials, user-id linkage and duplicate detection (US4). */
@ExtendWith(MockitoExtension.class)
class EmployeeOnboardingServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private CredentialPort credentialPort;
    @Mock private EmployeeCodeGeneratorService codeGeneratorService;

    private EmployeeOnboardingService onboardingService;

    private static final UUID GENERATED_USER_ID = UUID.randomUUID();

    private static OnboardEmployeeInput validInput(boolean provisionCredentials) {
        return new OnboardEmployeeInput(
                "EMP-9001",
                "Alice",
                "Smith",
                "alice@example.com",
                "+212600000099",
                LocalDate.of(2026, 1, 10),
                EmploymentStatus.ACTIVE,
                provisionCredentials,
                provisionCredentials ? "PURCHASER" : null,
                provisionCredentials ? "Str0ng!Pass" : null
        );
    }

    @BeforeEach
    void setUp() {
        onboardingService = new EmployeeOnboardingService(
                employeeRepository, credentialPort, new EmployeeDtoMapper(), codeGeneratorService);
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // ---- Happy paths ----

    @Test
    @DisplayName("onboard without credentials: persists employee with no userId")
    void onboard_withoutCredentials_savesEmployee() {
        when(employeeRepository.existsByCode(any())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);

        EmployeeOutput out = onboardingService.onboard(validInput(false));

        assertEquals("EMP-9001", out.code());
        assertEquals("Alice", out.firstName());
        assertEquals("alice@example.com", out.email());
        assertNull(out.userId(), "userId must remain null when credentials are not provisioned");
        verifyNoInteractions(credentialPort);
    }

    @Test
    @DisplayName("onboard with credentials: provisions account and links userId to the saved employee")
    void onboard_withCredentials_linksUserId() {
        when(employeeRepository.existsByCode(any())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(credentialPort.provisionUserAccount(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(GENERATED_USER_ID);

        EmployeeOutput out = onboardingService.onboard(validInput(true));

        assertEquals(GENERATED_USER_ID, out.userId());

        // Assert the auth port was called with the right email and role
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        verify(credentialPort).provisionUserAccount(
                emailCaptor.capture(), anyString(), anyString(), roleCaptor.capture(), anyString());
        assertEquals("alice@example.com", emailCaptor.getValue());
        assertEquals("PURCHASER", roleCaptor.getValue());
    }

    @Test
    @DisplayName("onboard: auto-generates a code when none is provided")
    void onboard_generatesCode_whenNoneProvided() {
        when(codeGeneratorService.generateCode()).thenReturn(EmployeeCode.of("EMP-0001"));
        when(employeeRepository.existsByCode(any())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);

        OnboardEmployeeInput noCode = new OnboardEmployeeInput(
                null, "Bob", "Jones", "bob@example.com",
                null, null, null, false, null, null);

        EmployeeOutput out = onboardingService.onboard(noCode);

        assertEquals("EMP-0001", out.code());
        verify(codeGeneratorService).generateCode();
    }

    // ---- Duplicate guards ----

    @Test
    @DisplayName("onboard: duplicate code is rejected with EmployeeCodeAlreadyExistsException")
    void onboard_duplicateCode_isRejected() {
        when(employeeRepository.existsByCode(any())).thenReturn(true);

        assertThrows(EmployeeCodeAlreadyExistsException.class,
                () -> onboardingService.onboard(validInput(false)));

        verify(employeeRepository, never()).save(any());
        verifyNoInteractions(credentialPort);
    }

    @Test
    @DisplayName("onboard: duplicate email is rejected with EmailAlreadyInUseException")
    void onboard_duplicateEmail_isRejected() {
        when(employeeRepository.existsByCode(any())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class,
                () -> onboardingService.onboard(validInput(false)));

        verify(employeeRepository, never()).save(any());
        verifyNoInteractions(credentialPort);
    }

    @Test
    @DisplayName("onboard: missing email is rejected, so the email is mandatory for onboarding")
    void onboard_missingEmail_isRejected() {
        OnboardEmployeeInput noEmail = new OnboardEmployeeInput(
                "EMP-9002", "Bob", "Jones", null,
                null, null, null, false, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> onboardingService.onboard(noEmail));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("onboard: null input is rejected with IllegalArgumentException")
    void onboard_nullInput_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> onboardingService.onboard(null));
    }

    // ---- Status passthrough ----

    @Test
    @DisplayName("onboard: PROBATION status from the request is honoured")
    void onboard_probationStatus_isHonoured() {
        when(employeeRepository.existsByCode(any())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);

        OnboardEmployeeInput probation = new OnboardEmployeeInput(
                "EMP-9003", "Carol", "Vance", "carol@example.com",
                null, LocalDate.of(2026, 4, 1),
                EmploymentStatus.PROBATION, false, null, null);

        EmployeeOutput out = onboardingService.onboard(probation);

        assertEquals(EmploymentStatus.PROBATION, out.status());
    }
}
