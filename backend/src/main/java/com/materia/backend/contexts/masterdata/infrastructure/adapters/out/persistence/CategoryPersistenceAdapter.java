package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.masterData.domain.entities.Category;
import com.materia.backend.contexts.masterData.domain.enums.MaterialCategoryType;
import com.materia.backend.contexts.masterData.domain.ports.out.CategoryRepository;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.CategoryJpaEntity;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.mappers.CategoryPersistenceMapper;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.repositories.SpringDataCategoryRepository;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.repositories.SpringDataMaterialRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence Adapter for Category
 * Hexagonal Architecture - Implements the domain output port (CategoryRepository)
 * and delegates to Spring Data JPA
 */
@Component
public class CategoryPersistenceAdapter implements CategoryRepository {

    private final SpringDataCategoryRepository jpaRepository;
    private final SpringDataMaterialRepository materialJpaRepository;
    private final CategoryPersistenceMapper mapper;

    public CategoryPersistenceAdapter(SpringDataCategoryRepository jpaRepository,
                                      SpringDataMaterialRepository materialJpaRepository,
                                       CategoryPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.materialJpaRepository = materialJpaRepository;
        this.mapper = mapper;
    }

    // ============================================================
    // BaseRepository methods
    // ============================================================

    @Override
    public Optional<Category> findById(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(jpaEntity -> toDomainEntities(List.of(jpaEntity)).get(0));
    }

    @Override
    public List<Category> findAll() {
        return toDomainEntities(jpaRepository.findAll());
    }

    @Override
    public Category save(Category entity) {
        CategoryJpaEntity jpaEntity = mapper.toJpaEntity(entity);
        CategoryJpaEntity saved = jpaRepository.save(jpaEntity);
        return toDomainEntities(List.of(saved)).get(0);
    }

    @Override
    public List<Category> saveAll(List<Category> entities) {
        List<CategoryJpaEntity> jpaEntities = entities.stream()
                .map(mapper::toJpaEntity)
                .collect(Collectors.toList());
        return toDomainEntities(jpaRepository.saveAll(jpaEntities));
    }

    @Override
    public void deleteById(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }

    @Override
    public void delete(Category entity) {
        jpaRepository.deleteById(entity.getId());
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<Category> findAllById(List<UUID> ids) {
        return toDomainEntities(jpaRepository.findAllById(ids));
    }

    // ============================================================
    // CategoryRepository-specific methods
    // ============================================================

    @Override
    public Optional<Category> findByCode(String code) {
        return jpaRepository.findByCode(code)
                .map(jpaEntity -> toDomainEntities(List.of(jpaEntity)).get(0));
    }

    @Override
    public List<Category> findRootCategories() {
        return toDomainEntities(jpaRepository.findByParentIdIsNull());
    }

    @Override
    public List<Category> findByParentId(String parentId) {
        return toDomainEntities(jpaRepository.findByParentId(parentId));
    }

    @Override
    public List<Category> findByCategoryType(MaterialCategoryType categoryType) {
        return toDomainEntities(jpaRepository.findByCategoryType(categoryType));
    }

    @Override
    public List<Category> findByStatus(String status) {
        return toDomainEntities(jpaRepository.findByStatus(status));
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByParentId(String parentId) {
        return jpaRepository.existsByParentId(parentId);
    }

    @Override
    public List<Category> search(String keyword) {
        return toDomainEntities(jpaRepository.search(keyword));
    }

    private List<Category> toDomainEntities(List<CategoryJpaEntity> jpaEntities) {
        if (jpaEntities.isEmpty()) {
            return List.of();
        }

        List<String> categoryIds = jpaEntities.stream()
                .map(CategoryJpaEntity::getId)
                .map(UUID::toString)
                .toList();

        Map<String, List<String>> childIdsByParentId = new LinkedHashMap<>();
        for (Object[] row : jpaRepository.findChildRelationsByParentIds(categoryIds)) {
            String parentId = (String) row[0];
            String childId = ((UUID) row[1]).toString();
            childIdsByParentId.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(childId);
        }

        Map<String, Integer> materialCountByCategoryId = new LinkedHashMap<>();
        for (Object[] row : materialJpaRepository.countMaterialsByCategoryIds(categoryIds)) {
            materialCountByCategoryId.put((String) row[0], ((Long) row[1]).intValue());
        }

        return jpaEntities.stream()
                .map(jpaEntity -> {
                    String categoryId = jpaEntity.getId().toString();
                    Category domain = mapper.toDomainEntity(jpaEntity);
                    List<String> childIds = childIdsByParentId.getOrDefault(categoryId, List.of());
                    int materialCount = materialCountByCategoryId.getOrDefault(categoryId, 0);
                    int subCategoryCount = childIds.size();

                    domain.setChildrenIds(childIds);
                    domain.setMaterialCount(materialCount);
                    domain.setSubCategoryCount(subCategoryCount);
                    domain.setTotalItems(materialCount + subCategoryCount);
                    domain.setUpdatedAt(jpaEntity.getUpdatedAt());
                    return domain;
                })
                .collect(Collectors.toList());
    }
}
