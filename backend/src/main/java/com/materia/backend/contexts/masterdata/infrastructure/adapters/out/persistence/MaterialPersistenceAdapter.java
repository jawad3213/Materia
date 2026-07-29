package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.masterdata.domain.entities.Material;
import com.materia.backend.contexts.masterdata.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterdata.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities.MaterialJpaEntity;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.mappers.MaterialPersistenceMapper;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.repositories.SpringDataMaterialRepository;
import org.springframework.stereotype.Component;

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
    public Optional<Material> findById(UUID uuid) {
        return jpaRepository.findById(uuid).map(mapper::toDomainEntity);
    }

    @Override
    public List<Material> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public Material save(Material entity) {
        MaterialJpaEntity jpa = mapper.toJpaEntity(entity);
        return mapper.toDomainEntity(jpaRepository.save(jpa));
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
        return jpaRepository.findByCategoryId(categoryId).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Material> findBySupplierId(String supplierId) {
        return jpaRepository.findBySupplierId(supplierId).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Material> findByStatus(MaterialStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) { return jpaRepository.existsByCode(code); }

    @Override
    public List<Material> search(String keyword) {
        return jpaRepository.search(keyword).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Material> findBelowMinimumStock() {
        return jpaRepository.findBelowMinimumStock().stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Material> findBelowReorderPoint() {
        return jpaRepository.findBelowReorderPoint().stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Material> findAvailableStock() {
        return jpaRepository.findAvailableStock().stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Material> findOutOfStock() {
        return jpaRepository.findOutOfStock().stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public com.materia.backend.common.application.PageResponse<Material> searchAdvanced(
            com.materia.backend.contexts.masterdata.domain.valueObjects.MaterialSearchFilter filter, 
            int page, 
            int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<MaterialJpaEntity> jpaPage = jpaRepository.searchAdvanced(
                filter.getKeyword(),
                filter.getCategoryId(),
                filter.getSupplierId(),
                filter.getStatus(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.getLowStockOnly(),
                pageable
        );
        
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
}
