package com.materia.backend.contexts.employee.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.infrastructure.adapters.out.persistence.entities.EmployeeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataEmployeeRepository extends JpaRepository<EmployeeJpaEntity, UUID> {

    Optional<EmployeeJpaEntity> findByCode(String code);

    Optional<EmployeeJpaEntity> findByEmail(String email);

    Optional<EmployeeJpaEntity> findByUserId(UUID userId);

    boolean existsByCode(String code);

    boolean existsByEmail(String email);

    List<EmployeeJpaEntity> findByStatus(EmploymentStatus status);

    @Query("SELECT e.code FROM EmployeeJpaEntity e ORDER BY e.createdAt DESC LIMIT 1")
    Optional<String> findLatestCode();
}
