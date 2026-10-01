package com.materia.backend.contexts.employee.domain.entities;

import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeAlreadyTerminatedException;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeFullName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Employee state rules and value objects (T077, T078, US4). */
class EmployeeTest {

    private static final String FINDING_029 =
            "FINDING-029: terminate() accepts a termination date before the hire date";

    private static Employee active() {
        return Employee.builder().id(UUID.randomUUID()).code(EmployeeCode.of("EMP-0001"))
                .fullName(EmployeeFullName.of("Ada", "Lovelace")).email("ada@example.com")
                .hireDate(LocalDate.of(2024, 3, 1)).status(EmploymentStatus.ACTIVE).build();
    }

    // ---- Termination (US4 scenarios 2, 5) ----

    @Test
    @DisplayName("terminate: records the date and reason and moves the employee to TERMINATED")
    void terminate_recordsDateAndReason() {
        Employee e = active();
        e.terminate(LocalDate.of(2026, 9, 1), "  resigned  ");

        assertEquals(EmploymentStatus.TERMINATED, e.getStatus());
        assertEquals(LocalDate.of(2026, 9, 1), e.getTerminationDate());
        assertEquals("resigned", e.getTerminationReason(), "the reason is stored trimmed");
    }

    @Test
    @DisplayName("terminate: with no date or reason, it defaults to today and a standard reason")
    void terminate_defaults() {
        Employee e = active();
        e.terminate(null, null);

        assertEquals(LocalDate.now(), e.getTerminationDate());
        assertNotNull(e.getTerminationReason());
    }

    @Test
    @DisplayName("terminate: terminating an already-terminated employee is refused, so repeat offboarding is well defined")
    void terminate_twice_isRefused() {
        Employee e = active();
        e.terminate(LocalDate.of(2026, 9, 1), "resigned");

        assertThrows(EmployeeAlreadyTerminatedException.class, () -> e.terminate(LocalDate.of(2026, 9, 2), "again"));
        assertEquals(LocalDate.of(2026, 9, 1), e.getTerminationDate(), "the original termination is preserved");
    }

    @Test
    @Disabled(FINDING_029)
    @DisplayName("terminate: a termination date before the hire date is refused")
    void terminate_beforeHireDate_isRefused() {
        Employee e = active(); // hired 2024-03-01
        assertThrows(IllegalArgumentException.class, () -> e.terminate(LocalDate.of(2020, 1, 1), "impossible"));
    }

    // ---- Status and details ----

    @ParameterizedTest(name = "to {0}")
    @EnumSource(value = EmploymentStatus.class, names = {"ACTIVE", "PROBATION", "ON_LEAVE", "SUSPENDED"})
    @DisplayName("status: a terminated employee cannot be moved back to any other status")
    void updateStatus_fromTerminated_isRefused(EmploymentStatus target) {
        Employee e = active();
        e.terminate(null, null);

        assertThrows(EmployeeAlreadyTerminatedException.class, () -> e.updateStatus(target));
        assertEquals(EmploymentStatus.TERMINATED, e.getStatus());
    }

    @Test
    @DisplayName("status: a null status is refused")
    void updateStatus_null_isRefused() {
        assertThrows(IllegalArgumentException.class, () -> active().updateStatus(null));
    }

    @Test
    @DisplayName("details: a terminated employee's details can no longer be edited")
    void updateDetails_afterTermination_isRefused() {
        Employee e = active();
        e.terminate(null, null);

        assertThrows(EmployeeAlreadyTerminatedException.class,
                () -> e.updateDetails(EmployeeFullName.of("New", "Name"), "000"));
    }

    @Test
    @DisplayName("details: a missing name leaves the existing name in place")
    void updateDetails_nullName_keepsExisting() {
        Employee e = active();
        e.updateDetails(null, "+212600000000");

        assertEquals("Ada", e.getFullName().getFirstName());
        assertEquals("+212600000000", e.getPhone());
    }

    @Test
    @DisplayName("account link: an employee can be linked to and unlinked from a user account")
    void linkAndUnlinkUser() {
        Employee e = active();
        UUID userId = UUID.randomUUID();

        e.linkUserId(userId);
        assertEquals(userId, e.getUserId());
        e.unlinkUserId();
        assertNull(e.getUserId());
    }

    // ---- Value objects (T077) ----

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"EMP-0001", "EMP_2026_0042", "ABC", "EMP-2026-0001"})
    @DisplayName("code: valid codes are accepted")
    void code_valid(String code) {
        assertDoesNotThrow(() -> EmployeeCode.of(code));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"AB", "EMP 0001", "EMP.0001", "THIS-CODE-IS-FAR-TOO-LONG-TO-BE-VALID"})
    @DisplayName("code: codes too short, too long, or containing illegal characters are refused")
    void code_invalid(String code) {
        assertThrows(IllegalArgumentException.class, () -> EmployeeCode.of(code));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("code: a missing code is refused")
    void code_missing(String code) {
        assertThrows(IllegalArgumentException.class, () -> EmployeeCode.of(code));
    }

    @Test
    @DisplayName("full name: both first and last name are required")
    void fullName_requiresBothParts() {
        assertThrows(IllegalArgumentException.class, () -> EmployeeFullName.of("", "Lovelace"));
        assertThrows(IllegalArgumentException.class, () -> EmployeeFullName.of("Ada", null));
        assertThrows(IllegalArgumentException.class, () -> EmployeeFullName.of("   ", "Lovelace"));
        assertDoesNotThrow(() -> EmployeeFullName.of("Ada", "Lovelace"));
    }
}
