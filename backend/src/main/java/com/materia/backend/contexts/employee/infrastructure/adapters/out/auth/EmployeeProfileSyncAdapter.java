package com.materia.backend.contexts.employee.infrastructure.adapters.out.auth;

import com.materia.backend.contexts.auth.domain.ports.out.EmployeeProfileSync;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.ports.out.EmployeeRepository;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeFullName;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 🔹 EMPLOYEE PROFILE SYNC ADAPTER
 *
 * Applies a user's own profile change (name, phone) to the employee record linked to the account.
 * Accounts without an employee record, and terminated employees, are left alone.
 */
@Component
public class EmployeeProfileSyncAdapter implements EmployeeProfileSync {

    private final EmployeeRepository employeeRepository;

    public EmployeeProfileSyncAdapter(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void syncContactDetails(UUID userId, String firstName, String lastName, String phone) {
        if (userId == null) {
            return;
        }
        employeeRepository.findByUserId(userId)
                .filter(employee -> employee.getStatus() != EmploymentStatus.TERMINATED)
                .ifPresent(employee -> {
                    employee.updateDetails(EmployeeFullName.of(firstName, lastName), phone);
                    employeeRepository.save(employee);
                });
    }
}
