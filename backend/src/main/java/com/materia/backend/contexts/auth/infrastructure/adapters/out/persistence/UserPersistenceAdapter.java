package com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.auth.domain.valueObjects.Email;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.mappers.UserPersistenceMapper;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.repositories.SpringDataUserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 🔹 USER PERSISTENCE ADAPTER (OUTPUT ADAPTER)
 * 
 * Implements the domain UserRepository output port using Spring Data JPA.
 * Queries are performed strictly by email and domain attributes.
 */
@Component
public class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    public UserPersistenceAdapter(SpringDataUserRepository jpaRepository, UserPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(UUID uuid) {
        return jpaRepository.findById(uuid).map(mapper::toDomainEntity);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAll(int page, int size) {
        if (page < 0 || size <= 0) {
            return Collections.emptyList();
        }
        return jpaRepository.findAll(PageRequest.of(page, size)).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        if (status == null) return Collections.emptyList();
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByRole(Role role) {
        if (role == null) return Collections.emptyList();
        return jpaRepository.findByRole(role).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) return Collections.emptyList();
        return jpaRepository.findByDepartment(department.trim()).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public User save(User entity) {
        UserJpaEntity jpaEntity = mapper.toJpaEntity(entity);
        UserJpaEntity savedJpa = jpaRepository.save(jpaEntity);
        return mapper.toDomainEntity(savedJpa);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        if (email == null) return Optional.empty();
        return findByEmail(email.getValue());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomainEntity);
    }

    @Override
    public void deleteById(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }

    @Override
    public void delete(User entity) {
        if (entity != null && entity.getId() != null) {
            jpaRepository.deleteById(entity.getId());
        }
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
