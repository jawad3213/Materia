package com.materia.backend.contexts.employee.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.ports.out.EmployeeRepository;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.employee.infrastructure.adapters.out.persistence.entities.EmployeeJpaEntity;
import com.materia.backend.contexts.employee.infrastructure.adapters.out.persistence.mappers.EmployeePersistenceMapper;
import com.materia.backend.contexts.employee.infrastructure.adapters.out.persistence.repositories.SpringDataEmployeeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 🔹 EMPLOYEE PERSISTENCE ADAPTER
 *
 * Implements the outbound port EmployeeRepository using Spring Data JPA.
 */
@Component
public class EmployeePersistenceAdapter implements EmployeeRepository {

    private final SpringDataEmployeeRepository springDataRepository;
    private final EmployeePersistenceMapper mapper;

    public EmployeePersistenceAdapter(
            SpringDataEmployeeRepository springDataRepository,
            EmployeePersistenceMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Employee save(Employee employee) {
        EmployeeJpaEntity jpaEntity = mapper.toJpaEntity(employee);
        EmployeeJpaEntity saved = springDataRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Employee> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<EmployeeCode> findNextCode() {
        Optional<String> latestCodeOpt = springDataRepository.findLatestCode();
        if (latestCodeOpt.isPresent()) {
            String latest = latestCodeOpt.get();
            try {
                if (latest.startsWith("EMP-")) {
                    int seq = Integer.parseInt(latest.substring(4));
                    return Optional.of(EmployeeCode.of(String.format("EMP-%04d", seq + 1)));
                }
            } catch (NumberFormatException ignored) {}
        }
        return Optional.of(EmployeeCode.of(String.format("EMP-%04d", count() + 1)));
    }

    @Override
    public Optional<Employee> findByCode(EmployeeCode code) {
        if (code == null) return Optional.empty();
        return springDataRepository.findByCode(code.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return springDataRepository.findByEmail(email.trim().toLowerCase()).map(mapper::toDomain);
    }

    @Override
    public Optional<Employee> findByUserId(UUID userId) {
        if (userId == null) return Optional.empty();
        return springDataRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public List<Employee> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> findByStatus(EmploymentStatus status) {
        return springDataRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(EmployeeCode code) {
        if (code == null) return false;
        return springDataRepository.existsByCode(code.getValue());
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        return springDataRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
