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
import com.materia.backend.contexts.employee.domain.ports.out.EmployeeRepository;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.support.fixtures.EmployeeFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** [T081] Employee CRUD service: duplicate detection, lookups, update, delete (US4). */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeCodeGeneratorService codeGeneratorService;

    private EmployeeService employeeService;

    private final UUID employeeId = UUID.randomUUID();
    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, new EmployeeDtoMapper(), codeGeneratorService);
        employee = EmployeeFixtures.anEmployee()
                .id(employeeId)
                .code("EMP-0001")
                .email("alice@example.com")
                .build();
        lenient().when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---- Create ----

    @Test
    @DisplayName("create: persists and returns the employee when inputs are valid")
    void create_success() {
        when(employeeRepository.existsByCode(any())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);

        CreateEmployeeInput input = new CreateEmployeeInput(
                "EMP-0001", "Alice", "Smith", "alice@example.com",
                "+212600000001", LocalDate.of(2026, 1, 10), EmploymentStatus.ACTIVE);

        EmployeeOutput out = employeeService.create(input);

        assertEquals("EMP-0001", out.code());
        assertEquals("Alice", out.firstName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("create: auto-generates a code when none is provided")
    void create_autoGeneratesCode() {
        when(codeGeneratorService.generateCode()).thenReturn(EmployeeCode.of("EMP-0042"));
        when(employeeRepository.existsByCode(any())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);

        CreateEmployeeInput input = new CreateEmployeeInput(
                null, "Bob", "Jones", "bob@example.com",
                null, null, null);

        EmployeeOutput out = employeeService.create(input);

        assertEquals("EMP-0042", out.code());
        verify(codeGeneratorService).generateCode();
    }

    @Test
    @DisplayName("create: duplicate code is rejected before saving")
    void create_duplicateCode_isRejected() {
        when(employeeRepository.existsByCode(any())).thenReturn(true);

        CreateEmployeeInput input = new CreateEmployeeInput(
                "EMP-0001", "Alice", "Smith", "alice@example.com",
                null, null, null);

        assertThrows(EmployeeCodeAlreadyExistsException.class, () -> employeeService.create(input));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: duplicate email is rejected before saving")
    void create_duplicateEmail_isRejected() {
        when(employeeRepository.existsByCode(any())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(true);

        CreateEmployeeInput input = new CreateEmployeeInput(
                "EMP-0002", "Alice", "Smith", "alice@example.com",
                null, null, null);

        assertThrows(EmailAlreadyInUseException.class, () -> employeeService.create(input));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: null input throws IllegalArgumentException immediately")
    void create_nullInput_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(null));
    }

    // ---- Lookup ----

    @Test
    @DisplayName("getById: returns the employee when found")
    void getById_returnsEmployee() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        EmployeeOutput out = employeeService.getById(employeeId);

        assertEquals(employeeId, out.id());
    }

    @Test
    @DisplayName("getById: throws EmployeeNotFoundException for an unknown id")
    void getById_unknownId_throws() {
        when(employeeRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getById(UUID.randomUUID()));
    }

    @Test
    @DisplayName("getByCode: returns employee when code is known")
    void getByCode_returnsEmployee() {
        when(employeeRepository.findByCode(EmployeeCode.of("EMP-0001"))).thenReturn(Optional.of(employee));

        EmployeeOutput out = employeeService.getByCode("EMP-0001");

        assertEquals("alice@example.com", out.email());
    }

    @Test
    @DisplayName("getByCode: throws EmployeeNotFoundException for an unknown code")
    void getByCode_unknownCode_throws() {
        when(employeeRepository.findByCode(any())).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getByCode("EMP-9999"));
    }

    @Test
    @DisplayName("getByCode: blank code throws IllegalArgumentException")
    void getByCode_blankCode_throws() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.getByCode("  "));
    }

    @Test
    @DisplayName("getByEmail: returns employee when email is known")
    void getByEmail_returnsEmployee() {
        when(employeeRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(employee));

        EmployeeOutput out = employeeService.getByEmail("alice@example.com");

        assertNotNull(out);
    }

    @Test
    @DisplayName("getAll: returns all employees as a list")
    void getAll_returnsList() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<EmployeeOutput> list = employeeService.getAll();

        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("getByStatus: filters employees by status")
    void getByStatus_filtersCorrectly() {
        when(employeeRepository.findByStatus(EmploymentStatus.ACTIVE)).thenReturn(List.of(employee));

        List<EmployeeOutput> list = employeeService.getByStatus(EmploymentStatus.ACTIVE);

        assertEquals(1, list.size());
        assertEquals(EmploymentStatus.ACTIVE, list.get(0).status());
    }

    @Test
    @DisplayName("getByStatus: null status returns all employees")
    void getByStatus_null_returnsAll() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<EmployeeOutput> list = employeeService.getByStatus(null);

        assertEquals(1, list.size());
    }

    // ---- Update ----

    @Test
    @DisplayName("update: full name and phone are changed correctly")
    void update_changesDetails() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        UpdateEmployeeInput input = new UpdateEmployeeInput("Betty", "Baker", "+212600000099", null);
        EmployeeOutput out = employeeService.update(employeeId, input);

        assertEquals("Betty", out.firstName());
        assertEquals("Baker", out.lastName());
    }

    @Test
    @DisplayName("update: status transition is applied when provided")
    void update_statusTransition() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        UpdateEmployeeInput input = new UpdateEmployeeInput(null, null, null, EmploymentStatus.ON_LEAVE);
        EmployeeOutput out = employeeService.update(employeeId, input);

        assertEquals(EmploymentStatus.ON_LEAVE, out.status());
    }

    @Test
    @DisplayName("update: null id throws IllegalArgumentException")
    void update_nullId_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.update(null, new UpdateEmployeeInput(null, null, null, null)));
    }

    @Test
    @DisplayName("update: unknown id throws EmployeeNotFoundException")
    void update_unknownId_throws() {
        when(employeeRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.update(UUID.randomUUID(),
                        new UpdateEmployeeInput("A", "B", null, null)));
    }

    // ---- Delete ----

    @Test
    @DisplayName("delete: removes the employee by id")
    void delete_success() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        assertDoesNotThrow(() -> employeeService.delete(employeeId));
        verify(employeeRepository).deleteById(employeeId);
    }

    @Test
    @DisplayName("delete: throws EmployeeNotFoundException when employee does not exist")
    void delete_unknownId_throws() {
        when(employeeRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.delete(UUID.randomUUID()));
        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete: null id throws IllegalArgumentException")
    void delete_nullId_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.delete(null));
    }
}
