package com.materia.backend.contexts.employee.infrastructure.adapters.out.auth;

import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.ports.out.EmployeeRepository;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeFullName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** A user's own profile change reaches the linked employee record, unless there is none or it is terminated. */
@ExtendWith(MockitoExtension.class)
class EmployeeProfileSyncAdapterTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeProfileSyncAdapter adapter;

    private final UUID userId = UUID.randomUUID();

    private Employee employee(EmploymentStatus status) {
        return Employee.builder()
                .id(UUID.randomUUID())
                .fullName(EmployeeFullName.of("Sara", "Alami"))
                .email("sara@materia.ma")
                .phone("0600")
                .status(status)
                .userId(userId)
                .build();
    }

    @Test
    @DisplayName("rule: the linked employee takes the new name and phone")
    void sync_updatesLinkedEmployee() {
        Employee linked = employee(EmploymentStatus.ACTIVE);
        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.of(linked));

        adapter.syncContactDetails(userId, "Salma", "Bennani", "0522");

        assertThat(linked.getFullName()).isEqualTo(EmployeeFullName.of("Salma", "Bennani"));
        assertThat(linked.getPhone()).isEqualTo("0522");
        verify(employeeRepository).save(linked);
    }

    @Test
    @DisplayName("rule: accounts without an employee record and terminated employees are left alone")
    void sync_skipsMissingOrTerminated() {
        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.empty());
        adapter.syncContactDetails(userId, "Salma", "Bennani", null);

        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.of(employee(EmploymentStatus.TERMINATED)));
        adapter.syncContactDetails(userId, "Salma", "Bennani", null);

        adapter.syncContactDetails(null, "Salma", "Bennani", null);
        verify(employeeRepository, never()).save(any());
    }
}
