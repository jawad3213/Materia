package com.materia.backend.contexts.employee.domain.ports.out;

import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔹 EMPLOYEE REPOSITORY (OUTBOUND PORT)
 *
 * Contract for Employee persistence operations. Pure domain port.
 */
public interface EmployeeRepository {

    Employee save(Employee employee);

    Optional<Employee> findById(UUID id);

    Optional<EmployeeCode> findNextCode();

    Optional<Employee> findByCode(EmployeeCode code);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByUserId(UUID userId);

    List<Employee> findAll();

    List<Employee> findByStatus(EmploymentStatus status);

    boolean existsByCode(EmployeeCode code);

    boolean existsByEmail(String email);

    void deleteById(UUID id);

    long count();
}
