package com.materia.backend.contexts.employee.domain.valueObjects;

import java.util.Objects;

/**
 * 🔹 EMPLOYEE FULL NAME (VALUE OBJECT)
 *
 * Immutable representation of an employee's first and last name.
 */
public final class EmployeeFullName {

    private final String firstName;
    private final String lastName;

    private EmployeeFullName(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
    }

    public static EmployeeFullName of(String firstName, String lastName) {
        return new EmployeeFullName(firstName, lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeFullName that = (EmployeeFullName) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
