package com.materia.backend.support.fixtures;

import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeFullName;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builds Employee domain objects for unit and integration tests (T025, US4).
 *
 * <pre>{@code
 * Employee e = anEmployee().status(EmploymentStatus.PROBATION).userId(someId).build();
 * }</pre>
 *
 * Every field has a sensible default so tests only declare what is relevant to
 * the scenario under scrutiny. Codes are unique per JVM run to prevent DB
 * collisions in integration tests (SC-005).
 */
public final class EmployeeFixtures {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    private EmployeeFixtures() {
    }

    /** Entry point. Returns a Builder with production-safe defaults. */
    public static Builder anEmployee() {
        return new Builder();
    }

    /**
     * A code unique within the run, in the {@code EMP-NNNN} format the domain accepts.
     * Use {@code uniqueEmail()} together with this for DB uniqueness constraints.
     */
    public static String uniqueCode() {
        return String.format("EMP-%04d", SEQUENCE.getAndIncrement() % 10000);
    }

    /** A unique email safe to insert without violating the {@code unique(email)} constraint. */
    public static String uniqueEmail(String seq) {
        return "employee-" + seq + "@materia.test";
    }

    public static final class Builder {
        private UUID id = UUID.randomUUID();
        private String code = uniqueCode();
        private String firstName = "Ada";
        private String lastName = "Lovelace";
        private String email = "employee-" + SEQUENCE.get() + "@materia.test";
        private String phone = "+212600000001";
        private EmploymentStatus status = EmploymentStatus.ACTIVE;
        private UUID userId = null;
        private LocalDate hireDate = LocalDate.of(2024, 1, 15);
        private LocalDate terminationDate = null;
        private String terminationReason = null;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder status(EmploymentStatus status) { this.status = status; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder hireDate(LocalDate hireDate) { this.hireDate = hireDate; return this; }
        public Builder terminated(LocalDate terminationDate, String reason) {
            this.status = EmploymentStatus.TERMINATED;
            this.terminationDate = terminationDate;
            this.terminationReason = reason;
            return this;
        }

        public Employee build() {
            return Employee.builder()
                    .id(id)
                    .code(EmployeeCode.of(code))
                    .fullName(EmployeeFullName.of(firstName, lastName))
                    .email(email)
                    .phone(phone)
                    .status(status)
                    .userId(userId)
                    .hireDate(hireDate)
                    .terminationDate(terminationDate)
                    .terminationReason(terminationReason)
                    .build();
        }
    }
}
