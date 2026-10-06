package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence;

import com.materia.backend.common.infrastructure.persistence.PersistenceIds;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.MaterialJpaEntity;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.MaterialStockMovementJpaEntity;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.mappers.MaterialPersistenceMapper;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.repositories.SpringDataMaterialRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence Adapter for Material
 * Hexagonal Architecture - Implements the domain output port (MaterialRepository)
 */
@Component
public class MaterialPersistenceAdapter implements MaterialRepository {

    private final SpringDataMaterialRepository jpaRepository;
    private final MaterialPersistenceMapper mapper;

    public MaterialPersistenceAdapter(SpringDataMaterialRepository jpaRepository,
                                       MaterialPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    // ============================================================
    // BaseRepository methods
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public Optional<Material> findById(UUID uuid) {
        return jpaRepository.findById(uuid).map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Material save(Material entity) {
        MaterialJpaEntity jpa = mapper.toJpaEntity(entity);
        MaterialJpaEntity saved;
        if (entity.getId() != null && jpaRepository.existsById(entity.getId())) {
            MaterialJpaEntity existing = jpaRepository.findById(entity.getId()).orElseThrow();
            // FINDING-026 fix: preserve existing stock movements across saves
            List<MaterialStockMovementJpaEntity> currentMovements = existing.getStockMovements();
            java.util.Set<UUID> existingIds = currentMovements.stream()
                    .map(com.materia.backend.common.infrastructure.persistence.BaseJpaEntity::getId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            for (MaterialStockMovementJpaEntity newMov : jpa.getStockMovements()) {
                if (newMov.getId() == null || !existingIds.contains(newMov.getId())) {
                    newMov.setMaterial(existing);
                    currentMovements.add(newMov);
                }
            }
            org.springframework.beans.BeanUtils.copyProperties(jpa, existing, "id", "createdAt", "createdBy", "stockMovements");
            saved = jpaRepository.save(existing);
        } else {
            saved = jpaRepository.save(jpa);
        }
        jpaRepository.flush();
        return mapper.toDomainEntity(jpaRepository.findById(saved.getId()).orElse(saved));
    }

    @Override
    public List<Material> saveAll(List<Material> entities) {
        List<MaterialJpaEntity> jpaEntities = entities.stream()
                .map(mapper::toJpaEntity).collect(Collectors.toList());
        return jpaRepository.saveAll(jpaEntities).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID uuid) { jpaRepository.deleteById(uuid); }

    @Override
    public void delete(Material entity) { jpaRepository.deleteById(entity.getId()); }

    @Override
    public boolean existsById(UUID id) { return jpaRepository.existsById(id); }

    @Override
    public long count() { return jpaRepository.count(); }

    @Override
    public List<Material> findAllById(List<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    // ============================================================
    // MaterialRepository-specific methods
    // ============================================================

    @Override
    public Optional<Material> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomainEntity);
    }


    @Override
    public List<Material> findByCategoryId(String categoryId) {
        UUID id = PersistenceIds.toUuidOrNull(categoryId);
        if (id == null) return List.of();
        return jpaRepository.findByCategoryId(id).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Material> findBySupplierId(String supplierId) {
        UUID id = PersistenceIds.toUuidOrNull(supplierId);
        if (id == null) return List.of();
        return jpaRepository.findBySupplierId(id).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Material> findByStatus(MaterialStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Material> findByMaterialType(com.materia.backend.contexts.masterData.domain.enums.MaterialType materialType) {
        return jpaRepository.findByMaterialType(materialType).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) { return jpaRepository.existsByCode(code); }

    @Override
    public boolean existsByCategoryId(String categoryId) {
        UUID id = PersistenceIds.toUuidOrNull(categoryId);
        return id != null && jpaRepository.existsByCategoryId(id);
    }

    @Override
    public boolean existsBySupplierId(String supplierId) {
        UUID id = PersistenceIds.toUuidOrNull(supplierId);
        return id != null && jpaRepository.existsBySupplierId(id);
    }

    @Override
    public List<Material> search(String keyword) {
        com.materia.backend.contexts.masterData.domain.valueObjects.MaterialSearchFilter filter =
            com.materia.backend.contexts.masterData.domain.valueObjects.MaterialSearchFilter.builder()
                .code(keyword)
                .name(keyword)
                .description(keyword)
                .shortDescription(keyword)
                .searchKeywords(keyword)
                .alternativeName(keyword)
                .build();
        return jpaRepository.findAll(buildSearchSpecification(filter)).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Material> findBelowReorderPoint() {
        return jpaRepository.findBelowReorderPoint().stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Material> findOutOfStock() {
        return jpaRepository.findOutOfStock().stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public com.materia.backend.common.application.PageResponse<Material> searchAdvanced(
            com.materia.backend.contexts.masterData.domain.valueObjects.MaterialSearchFilter filter,
            int page, 
            int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        
        Specification<MaterialJpaEntity> spec = Specification.where(buildSearchSpecification(filter))
                .and((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    
                    if (StringUtils.hasText(filter.getCategoryId())) {
                        UUID categoryId = PersistenceIds.toUuidOrNull(filter.getCategoryId());
                        // An identifier that is not a UUID matches no category.
                        predicates.add(categoryId != null ? cb.equal(root.get("categoryId"), categoryId) : cb.disjunction());
                    }
                    if (filter.getMaterialType() != null) {
                        predicates.add(cb.equal(root.get("materialType"), filter.getMaterialType()));
                    }
                    if (filter.getStatus() != null) {
                        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                    }
                    
                    return cb.and(predicates.toArray(new Predicate[0]));
                });
        
        org.springframework.data.domain.Page<MaterialJpaEntity> jpaPage = jpaRepository.findAll(spec, pageable);
        
        List<Material> domainList = jpaPage.getContent().stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
                
        return new com.materia.backend.common.application.PageResponse<>(
                domainList,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements(),
                jpaPage.getTotalPages(),
                jpaPage.isLast()
        );
    }

    /**
     * Builds a JPA Specification that searches individual fields using LIKE.
     * Each provided field is matched independently and all matches are combined with OR,
     * meaning a material will be returned if ANY of the provided search fields match.
     */
    private Specification<MaterialJpaEntity> buildSearchSpecification(
            com.materia.backend.contexts.masterData.domain.valueObjects.MaterialSearchFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> searchPredicates = new ArrayList<>();

            if (StringUtils.hasText(filter.getCode())) {
                searchPredicates.add(cb.like(cb.lower(root.get("code")), "%" + filter.getCode().trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getName())) {
                searchPredicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getDescription())) {
                searchPredicates.add(cb.like(cb.lower(root.get("description")), "%" + filter.getDescription().trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getShortDescription())) {
                searchPredicates.add(cb.like(cb.lower(root.get("shortDescription")), "%" + filter.getShortDescription().trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getSearchKeywords())) {
                searchPredicates.add(cb.like(cb.lower(root.get("searchKeywords")), "%" + filter.getSearchKeywords().trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getAlternativeName())) {
                searchPredicates.add(cb.like(cb.lower(root.get("alternativeName")), "%" + filter.getAlternativeName().trim().toLowerCase() + "%"));
            }

            if (searchPredicates.isEmpty()) {
                return cb.conjunction();
            }

            // OR logic: match if ANY of the provided fields match
            return cb.or(searchPredicates.toArray(new Predicate[0]));
        };
    }
}

