package com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

/**
 * 🔹 USER PERSISTENCE MAPPER
 * 
 * Maps between domain User entity and JPA UserJpaEntity.
 */
@Component
public class UserPersistenceMapper {

    public User toDomainEntity(UserJpaEntity jpa) {
        if (jpa == null) return null;

        return User.builder()
                .id(jpa.getId())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .version(jpa.getVersion())
                .createdBy(jpa.getCreatedBy())
                .updatedBy(jpa.getUpdatedBy())
                .email(jpa.getEmail())
                .passwordHash(jpa.getPasswordHash())
                .role(jpa.getRole())
                .enabled(jpa.isEnabled())
                .firstName(jpa.getFirstName())
                .lastName(jpa.getLastName())
                .fullName(jpa.getFullName())
                .phone(jpa.getPhone())
                .status(jpa.getStatus())
                .department(jpa.getDepartment())
                .build();
    }

    public UserJpaEntity toJpaEntity(User domain) {
        if (domain == null) return null;

        UserJpaEntity jpa = new UserJpaEntity();
        jpa.setId(domain.getId());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setVersion(domain.getVersion());
        jpa.setCreatedBy(domain.getCreatedBy());
        jpa.setUpdatedBy(domain.getUpdatedBy());
        jpa.setEmail(domain.getEmail());
        jpa.setPasswordHash(domain.getPasswordHash());
        jpa.setRole(domain.getRole());
        jpa.setEnabled(domain.isEnabled());
        jpa.setFirstName(domain.getFirstName());
        jpa.setLastName(domain.getLastName());
        jpa.setFullName(domain.getFullName());
        jpa.setPhone(domain.getPhone());
        jpa.setStatus(domain.getStatus());
        jpa.setDepartment(domain.getDepartment());
        return jpa;
    }
}
