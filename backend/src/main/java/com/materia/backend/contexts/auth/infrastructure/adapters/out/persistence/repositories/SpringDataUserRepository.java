package com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔹 SPRING DATA USER REPOSITORY
 * 
 * Spring Data JPA repository for UserJpaEntity in the auth context.
 * Performs queries strictly by email and profile attributes.
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserJpaEntity> findByStatus(UserStatus status);

    List<UserJpaEntity> findByRole(Role role);

    List<UserJpaEntity> findByDepartment(String department);
}
