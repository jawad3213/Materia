package com.materia.backend.contexts.employee.domain.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/** [T072] Employee name and code rules (feature 001 coverage). */
class EmployeeValueObjectsTest {

    @Test
    @DisplayName("name: first and last names are trimmed and joined; names compare by both parts")
    void fullName() {
        EmployeeFullName name = EmployeeFullName.of(" Ada ", " Lovelace ");

        assertEquals("Ada", name.getFirstName());
        assertEquals("Lovelace", name.getLastName());
        assertEquals("Ada Lovelace", name.getFullName());
        assertEquals("Ada Lovelace", name.toString());
        assertEquals(EmployeeFullName.of("Ada", "Lovelace"), name);
        assertEquals(name.hashCode(), EmployeeFullName.of("Ada", "Lovelace").hashCode());
        assertNotEquals(name, EmployeeFullName.of("Ada", "Byron"));
        assertNotEquals(name, EmployeeFullName.of("Grace", "Lovelace"));
        assertNotEquals(name, null);
        assertNotEquals(name, "Ada Lovelace");
        assertEquals(name, name);
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    @DisplayName("name: a blank first or last name is refused")
    void blankNames_areRefused(String blank) {
        assertThrows(IllegalArgumentException.class, () -> EmployeeFullName.of(blank, "Lovelace"));
        assertThrows(IllegalArgumentException.class, () -> EmployeeFullName.of("Ada", blank));
    }

    @Test
    @DisplayName("code: codes are trimmed and upper-cased; generated codes are zero-padded; codes compare by value")
    void code() {
        assertEquals("EMP-0007", EmployeeCode.of(" emp-0007 ").getValue());
        assertEquals("EMP-0042", EmployeeCode.fromPrefixAndNumber("EMP", 42).getValue());
        assertEquals("EMP-2026-0042", EmployeeCode.fromPrefixYearAndNumber("EMP", 2026, 42).toString());
        EmployeeCode code = EmployeeCode.of("EMP-1");
        assertEquals(EmployeeCode.of("emp-1"), code);
        assertEquals(code.hashCode(), EmployeeCode.of("EMP-1").hashCode());
        assertNotEquals(code, EmployeeCode.of("EMP-2"));
        assertNotEquals(code, null);
        assertNotEquals(code, "EMP-1");
        assertEquals(code, code);
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "AB", "EMP 001", "EMP@1"})
    @DisplayName("code: blank, too short or badly formed codes are refused")
    void badCodes_areRefused(String raw) {
        assertThrows(IllegalArgumentException.class, () -> EmployeeCode.of(raw));
    }
}
