package com.materia.backend.contexts.auth.domain.ports.out;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.domain.valueObjects.Email;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(Email email);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findAll(int page, int size);
    List<User> findByStatus(UserStatus status);
    List<User> findByRole(Role role);
    List<User> findByDepartment(String department);
    void delete(User user);
    void deleteById(UUID id);
    long count();
    boolean existsById(UUID id);
    boolean existsByEmail(String email);
}