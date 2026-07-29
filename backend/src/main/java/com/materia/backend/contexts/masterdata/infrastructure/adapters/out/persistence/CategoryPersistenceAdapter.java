package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.masterdata.domain.entities.Category;
import com.materia.backend.contexts.masterdata.domain.enums.CategoryType;
import com.materia.backend.contexts.masterdata.domain.ports.out.CategoryRepository;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities.CategoryJpaEntity;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.mappers.CategoryPersistenceMapper;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.repositories.SpringDataCategoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private final CategoryPersistenceMapper mapper;

    public CategoryPersistenceAdapter(SpringDataCategoryRepository jpaRepository,
                                       CategoryPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    // ============================================================
    // BaseRepository methods
    // ============================================================

    @Override
    public Optional<Category> findById(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::toDomainEntity);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Category save(Category entity) {
        CategoryJpaEntity jpaEntity = mapper.toJpaEntity(entity);
        CategoryJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    public List<Category> saveAll(List<Category> entities) {
        List<CategoryJpaEntity> jpaEntities = entities.stream()
                .map(mapper::toJpaEntity)
                .collect(Collectors.toList());
        return jpaRepository.saveAll(jpaEntities).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
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
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    // ============================================================
    // CategoryRepository-specific methods
    // ============================================================

    @Override
    public Optional<Category> findByCode(String code) {
        return jpaRepository.findByCode(code)
                .map(mapper::toDomainEntity);
    }

    @Override
    public List<Category> findRootCategories() {
        return jpaRepository.findByParentIdIsNull().stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByParentId(String parentId) {
        return jpaRepository.findByParentId(parentId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByCategoryType(CategoryType categoryType) {
        return jpaRepository.findByCategoryType(categoryType).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByStatus(String status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public List<Category> search(String keyword) {
        return jpaRepository.search(keyword).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }
}
