package com.materia.backend.contexts.employee.domain.entities;

import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeAlreadyTerminatedException;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeFullName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** [T073] Employee defaults, termination rules and identity (feature 001 coverage). */
class EmployeeBehaviourTest {

    private static Employee.Builder base() {
        return Employee.builder().code(EmployeeCode.of("EMP-1001")).fullName(EmployeeFullName.of("Ada", "Lovelace"))
                .email(" Ada@Materia.Test ");
    }

    @Test
    @DisplayName("employee: a new employee is ACTIVE, hired today, with a normalised email")
    void defaults() {
        Employee e = base().build();

        assertEquals(EmploymentStatus.ACTIVE, e.getStatus());
        assertEquals(LocalDate.now(), e.getHireDate());
        assertEquals("ada@materia.test", e.getEmail());
        e.setEmail(null);
        assertNull(e.getEmail());
        assertNull(Employee.builder().code(EmployeeCode.of("EMP-1")).build().getEmail());
    }

    @Test
    @DisplayName("employee: a terminated employee's details and status cannot change, even without a code")
    void terminated_isFrozen() {
        Employee noCode = Employee.builder().fullName(EmployeeFullName.of("A", "B")).status(EmploymentStatus.TERMINATED).build();

        assertThrows(EmployeeAlreadyTerminatedException.class, () -> noCode.updateDetails(null, "+212"));
        assertThrows(EmployeeAlreadyTerminatedException.class, () -> noCode.updateStatus(EmploymentStatus.ACTIVE));
        assertThrows(EmployeeAlreadyTerminatedException.class, () -> noCode.terminate(null, null));
        assertDoesNotThrow(() -> noCode.updateStatus(EmploymentStatus.TERMINATED));
    }

    @Test
    @DisplayName("employee: updating details keeps the name when none is given; a null status is refused")
    void updates() {
        Employee e = base().build();

        e.updateDetails(null, "+212600");
        assertEquals("Ada Lovelace", e.getFullName().getFullName());
        assertEquals("+212600", e.getPhone());
        assertThrows(IllegalArgumentException.class, () -> e.updateStatus(null));
    }

    @Test
    @DisplayName("employee: termination defaults to today and a standard reason; given values are kept and trimmed")
    void termination() {
        Employee defaults = base().build();
        defaults.terminate(null, null);
        assertEquals(LocalDate.now(), defaults.getTerminationDate());
        assertEquals("Standard Offboarding", defaults.getTerminationReason());

        Employee given = base().build();
        given.terminate(LocalDate.of(2026, 1, 31), "  Contract end  ");
        assertEquals(LocalDate.of(2026, 1, 31), given.getTerminationDate());
        assertEquals("Contract end", given.getTerminationReason());
        assertEquals(EmploymentStatus.TERMINATED, given.getStatus());
    }

    @Test
    @DisplayName("employee: employees with the same id are equal when code or email match; never to another type or null")
    void identity() {
        UUID id = UUID.randomUUID();
        Employee a = base().id(id).build();
        Employee sameCode = base().id(id).email("other@materia.test").build();
        Employee sameEmail = Employee.builder().id(id).code(EmployeeCode.of("EMP-9999")).email("ada@materia.test").build();
        Employee different = Employee.builder().id(id).code(EmployeeCode.of("EMP-8888")).email("zed@materia.test").build();

        assertEquals(a, a);
        assertEquals(a, sameCode);
        assertEquals(a, sameEmail);
        assertNotEquals(a, different);
        assertNotEquals(a, base().id(UUID.randomUUID()).build());
        assertNotEquals(a, null);
        assertNotEquals(a, "EMP-1001");
        assertEquals(a.hashCode(), base().id(id).build().hashCode());
    }
}
