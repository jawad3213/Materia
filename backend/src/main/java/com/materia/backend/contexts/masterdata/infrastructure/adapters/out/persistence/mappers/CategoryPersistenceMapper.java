package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.masterdata.domain.entities.Category;
import com.materia.backend.contexts.masterdata.domain.enums.CategoryType;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities.CategoryJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

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
        jpa.setChildrenIds(domain.getChildrenIds() != null ? new ArrayList<>(domain.getChildrenIds()) : new ArrayList<>());
        jpa.setCategoryType(domain.getCategoryType());
        jpa.setStatus(domain.getStatus());
        jpa.setColor(domain.getColor());
        jpa.setIcon(domain.getIcon());
        jpa.setMaterialCount(domain.getMaterialCount());
        jpa.setSubCategoryCount(domain.getSubCategoryCount());
        jpa.setTotalItems(domain.getTotalItems());

        // Audit fields
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setDeletedAt(domain.getDeletedAt());
        jpa.setCreatedBy(domain.getCreatedBy());
        jpa.setUpdatedBy(domain.getUpdatedBy());
        jpa.setDeletedBy(domain.getDeletedBy());

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
                .childrenIds(jpa.getChildrenIds() != null ? new ArrayList<>(jpa.getChildrenIds()) : new ArrayList<>())
                .categoryType(jpa.getCategoryType())
                .status(jpa.getStatus())
                .color(jpa.getColor())
                .icon(jpa.getIcon())
                .materialCount(jpa.getMaterialCount())
                .subCategoryCount(jpa.getSubCategoryCount())
                .totalItems(jpa.getTotalItems())
                .createdBy(jpa.getCreatedBy())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();

        domain.setDeletedAt(jpa.getDeletedAt());
        domain.setDeletedBy(jpa.getDeletedBy());
        domain.setUpdatedBy(jpa.getUpdatedBy());

        return domain;
    }
}
