package com.materia.backend.contexts.employee.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.exceptions.EmailAlreadyInUseException;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeCodeAlreadyExistsException;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeNotFoundException;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeOffboardingUseCase;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeOnboardingUseCase;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeUseCase;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.mappers.EmployeeWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.materia.backend.support.ErrorResponseAssertions.assertError;
import static com.materia.backend.support.ErrorResponseAssertions.assertValidationError;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * [T084] Employee HTTP contract – all 9 endpoints through the real security chain.
 *
 * <p>Status codes verified: 200 (read/update/delete), 201 (create/onboard), 204 (delete),
 * 400 (validation failure), 404 (not found), 409 (conflict on code or email).
 */
@WebMvcTest(EmployeeController.class)
@Import(EmployeeWebMapper.class)
@WithMockUser(username = "alice")
class EmployeeControllerTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/employees";

    @MockBean private EmployeeUseCase employeeUseCase;
    @MockBean private EmployeeOnboardingUseCase onboardingUseCase;
    @MockBean private EmployeeOffboardingUseCase offboardingUseCase;

    private final UUID id = UUID.randomUUID();

    // ---- Helper: canonical EmployeeOutput ----

    private EmployeeOutput output() {
        return new EmployeeOutput(
                id, "EMP-0001", "Alice", "Smith", "Alice Smith",
                "alice@example.com", "+212600000001",
                EmploymentStatus.ACTIVE, null, null,
                LocalDate.of(2026, 1, 10), null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Map<String, Object> createBody() {
        return Map.of(
                "firstName", "Alice",
                "lastName", "Smith",
                "email", "alice@example.com"
        );
    }

    private Map<String, Object> onboardBody() {
        return Map.of(
                "firstName", "Alice",
                "lastName", "Smith",
                "email", "alice@example.com",
                "provisionCredentials", false
        );
    }

    private Map<String, Object> offboardBody() {
        return Map.of(
                "employeeId", id.toString(),
                "revokeUserAccess", true
        );
    }

    // ---- POST /employees ----

    @Test
    @DisplayName("POST /employees: valid body returns 201 with employee payload")
    void create_is201() throws Exception {
        when(employeeUseCase.create(any())).thenReturn(output());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(createBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("EMP-0001"))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /employees: missing firstName returns 400 with validation error")
    void create_missingFirstName_is400() throws Exception {
        Map<String, Object> bad = Map.of("lastName", "Smith", "email", "alice@example.com");

        assertValidationError(
                mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(bad))),
                "firstName");
    }

    @Test
    @DisplayName("POST /employees: duplicate code returns 409 CONFLICT")
    void create_duplicateCode_is409() throws Exception {
        when(employeeUseCase.create(any())).thenThrow(new EmployeeCodeAlreadyExistsException("EMP-0001"));

        assertError(
                mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(createBody()))),
                HttpStatus.CONFLICT, "EMPLOYEE_CODE_ALREADY_EXISTS");
    }

    @Test
    @DisplayName("POST /employees: duplicate email returns 409 CONFLICT")
    void create_duplicateEmail_is409() throws Exception {
        when(employeeUseCase.create(any())).thenThrow(new EmailAlreadyInUseException("alice@example.com"));

        assertError(
                mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(createBody()))),
                HttpStatus.CONFLICT);
    }

    // ---- POST /employees/onboard ----

    @Test
    @DisplayName("POST /onboard: valid body returns 201 with employee payload")
    void onboard_is201() throws Exception {
        when(onboardingUseCase.onboard(any())).thenReturn(output());

        mockMvc.perform(post(BASE + "/onboard").contentType(MediaType.APPLICATION_JSON).content(json(onboardBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    // ---- POST /employees/offboard ----

    @Test
    @DisplayName("POST /offboard: valid body returns 200 with terminated employee")
    void offboard_is200() throws Exception {
        EmployeeOutput terminated = new EmployeeOutput(
                id, "EMP-0001", "Alice", "Smith", "Alice Smith",
                "alice@example.com", "+212600000001",
                EmploymentStatus.TERMINATED, null, null,
                LocalDate.of(2026, 1, 10), LocalDate.now(), "Resignation",
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(offboardingUseCase.offboard(any())).thenReturn(terminated);

        mockMvc.perform(post(BASE + "/offboard").contentType(MediaType.APPLICATION_JSON).content(json(offboardBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TERMINATED"));
    }

    // ---- GET /employees/{id} ----

    @Test
    @DisplayName("GET /employees/{id}: returns 200 with the employee")
    void getById_is200() throws Exception {
        when(employeeUseCase.getById(id)).thenReturn(output());

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("GET /employees/{id}: unknown id returns 404 NOT FOUND")
    void getById_notFound_is404() throws Exception {
        when(employeeUseCase.getById(any())).thenThrow(new EmployeeNotFoundException(id));

        assertError(mockMvc.perform(get(BASE + "/" + id)), HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND");
    }

    // ---- GET /employees/code/{code} ----

    @Test
    @DisplayName("GET /employees/code/{code}: returns 200 with the employee")
    void getByCode_is200() throws Exception {
        when(employeeUseCase.getByCode("EMP-0001")).thenReturn(output());

        mockMvc.perform(get(BASE + "/code/EMP-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("EMP-0001"));
    }

    @Test
    @DisplayName("GET /employees/code/{code}: unknown code returns 404")
    void getByCode_notFound_is404() throws Exception {
        when(employeeUseCase.getByCode("EMP-9999")).thenThrow(new EmployeeNotFoundException("Employee not found with code: EMP-9999"));

        assertError(mockMvc.perform(get(BASE + "/code/EMP-9999")), HttpStatus.NOT_FOUND);
    }

    // ---- GET /employees/email/{email} ----

    @Test
    @DisplayName("GET /employees/email/{email}: returns 200 with the employee")
    void getByEmail_is200() throws Exception {
        when(employeeUseCase.getByEmail("alice@example.com")).thenReturn(output());

        mockMvc.perform(get(BASE + "/email/alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    // ---- GET /employees ----

    @Test
    @DisplayName("GET /employees: returns 200 with a list of all employees")
    void getAll_is200() throws Exception {
        when(employeeUseCase.getAll()).thenReturn(List.of(output()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /employees?status=ACTIVE: filters by status and returns 200")
    void getByStatus_is200() throws Exception {
        when(employeeUseCase.getByStatus(EmploymentStatus.ACTIVE)).thenReturn(List.of(output()));

        mockMvc.perform(get(BASE).param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    // ---- PUT /employees/{id} ----

    @Test
    @DisplayName("PUT /employees/{id}: valid body returns 200 with updated employee")
    void update_is200() throws Exception {
        when(employeeUseCase.update(eq(id), any())).thenReturn(output());

        Map<String, Object> body = Map.of("firstName", "Betty", "lastName", "Baker");
        mockMvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("PUT /employees/{id}: unknown id returns 404")
    void update_notFound_is404() throws Exception {
        when(employeeUseCase.update(eq(id), any())).thenThrow(new EmployeeNotFoundException(id));

        Map<String, Object> body = Map.of("firstName", "Betty", "lastName", "Baker");
        assertError(mockMvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content(json(body))),
                HttpStatus.NOT_FOUND);
    }

    // ---- DELETE /employees/{id} ----

    @Test
    @DisplayName("DELETE /employees/{id}: returns 204 NO CONTENT on success")
    void delete_is204() throws Exception {
        doNothing().when(employeeUseCase).delete(id);

        mockMvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /employees/{id}: unknown id returns 404")
    void delete_notFound_is404() throws Exception {
        doThrow(new EmployeeNotFoundException(id)).when(employeeUseCase).delete(id);

        assertError(mockMvc.perform(delete(BASE + "/" + id)), HttpStatus.NOT_FOUND);
    }

    // ---- Contract: T085 – response field completeness ----

    @Test
    @DisplayName("contract: response contains all fields expected by the frontend users view")
    void responseContainsAllExpectedFields() throws Exception {
        when(employeeUseCase.getById(id)).thenReturn(output());

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.lastName").exists())
                .andExpect(jsonPath("$.fullName").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.hireDate").exists());
    }
}
