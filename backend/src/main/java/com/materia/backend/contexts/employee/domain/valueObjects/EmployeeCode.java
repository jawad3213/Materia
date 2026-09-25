package com.materia.backend.contexts.employee.domain.valueObjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 🔹 EMPLOYEE CODE (VALUE OBJECT)
 *
 * Immutable enterprise identifier for an employee (e.g. EMP-0001, EMP-2026-0042).
 */
public final class EmployeeCode {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9_-]{3,30}$");

    private final String value;

    private EmployeeCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee code cannot be empty");
        }
        String normalized = value.trim().toUpperCase();
        if (!CODE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Employee code format invalid: " + normalized + ". Must match " + CODE_PATTERN.pattern());
        }
        this.value = normalized;
    }

    public static EmployeeCode of(String value) {
        return new EmployeeCode(value);
    }

    public static EmployeeCode fromPrefixAndNumber(String prefix, int number) {
        return new EmployeeCode(String.format("%s-%04d", prefix, number));
    }

    public static EmployeeCode fromPrefixYearAndNumber(String prefix, int year, int number) {
        return new EmployeeCode(String.format("%s-%d-%04d", prefix, year, number));
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeCode that = (EmployeeCode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
