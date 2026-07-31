package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.masterdata.domain.entities.Category;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities.CategoryJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Category domain entity and CategoryJpaEntity
 * Infrastructure Layer - Converts between domain and persistence representations
 */
@Component
public class CategoryPersistenceMapper {

    /**
     * Domain Entity → JPA Entity
     */
    public CategoryJpaEntity toJpaEntity(Category domain) {
        if (domain == null) return null;

        CategoryJpaEntity jpa = new CategoryJpaEntity();
        jpa.setId(domain.getId());
        jpa.setCode(domain.getCode());
        jpa.setName(domain.getName());
        jpa.setDescription(domain.getDescription());
        jpa.setShortDescription(domain.getShortDescription());
        jpa.setParentId(domain.getParentId());
        jpa.setParentCode(domain.getParentCode());
        jpa.setLevel(domain.getLevel());
        jpa.setPath(domain.getPath());
        jpa.setCategoryType(domain.getCategoryType());
        jpa.setStatus(domain.getStatus());

        // Audit fields
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setVersion(domain.getVersion());
        jpa.setCreatedBy(domain.getCreatedBy());
        jpa.setUpdatedBy(domain.getUpdatedBy());

        return jpa;
    }

    /**
     * JPA Entity → Domain Entity
     */
    public Category toDomainEntity(CategoryJpaEntity jpa) {
        if (jpa == null) return null;

        Category domain = Category.builder()
                .id(jpa.getId())
                .code(jpa.getCode())
                .name(jpa.getName())
                .description(jpa.getDescription())
                .shortDescription(jpa.getShortDescription())
                .parentId(jpa.getParentId())
                .parentCode(jpa.getParentCode())
                .level(jpa.getLevel())
                .path(jpa.getPath())
                .categoryType(jpa.getCategoryType())
                .status(jpa.getStatus())
                .createdBy(jpa.getCreatedBy())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();

        domain.setVersion(jpa.getVersion());
        domain.setUpdatedBy(jpa.getUpdatedBy());

        return domain;
    }
}
