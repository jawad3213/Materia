package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.masterdata.domain.entities.Material;
import com.materia.backend.contexts.masterdata.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterdata.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities.MaterialJpaEntity;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.mappers.MaterialPersistenceMapper;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.repositories.SpringDataMaterialRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
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
    public List<String> findCodesByPrefix(String prefix) {
        return jpaRepository.findCodesByPrefix(prefix);
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
    public boolean existsByCategoryId(String categoryId) {
        return jpaRepository.existsByCategoryId(categoryId);
    }

    @Override
    public boolean existsBySupplierId(String supplierId) {
        return jpaRepository.existsBySupplierId(supplierId);
    }

    @Override
    public List<Material> search(String keyword) {
        return jpaRepository.findAll(buildSearchSpecification(keyword)).stream()
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
        
        Specification<MaterialJpaEntity> spec = Specification.where(buildSearchSpecification(filter.getKeyword()))
                .and((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    
                    if (StringUtils.hasText(filter.getCategoryId())) {
                        predicates.add(cb.equal(root.get("categoryId"), filter.getCategoryId()));
                    }
                    if (StringUtils.hasText(filter.getSupplierId())) {
                        predicates.add(cb.equal(root.get("supplierId"), filter.getSupplierId()));
                    }
                    if (filter.getStatus() != null) {
                        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                    }
                    if (filter.getMinPrice() != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("standardPrice"), filter.getMinPrice()));
                    }
                    if (filter.getMaxPrice() != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("standardPrice"), filter.getMaxPrice()));
                    }
                    if (Boolean.TRUE.equals(filter.getLowStockOnly())) {
                        predicates.add(cb.lessThan(root.get("currentStock"), root.get("minimumStock")));
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

    private Specification<MaterialJpaEntity> buildSearchSpecification(String keywordString) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keywordString)) {
                return cb.conjunction();
            }

            String[] keywords = keywordString.split("[,\\s]+");
            List<Predicate> keywordPredicates = new ArrayList<>();

            for (String kw : keywords) {
                if (!StringUtils.hasText(kw)) continue;
                String searchPattern = "%" + kw.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate altNameMatch = cb.like(cb.lower(root.get("alternativeName")), searchPattern);
                Predicate shortDescMatch = cb.like(cb.lower(root.get("shortDescription")), searchPattern);
                Predicate searchKeywordMatch = cb.like(cb.lower(root.get("searchKeywords")), searchPattern);
                
                keywordPredicates.add(cb.or(nameMatch, descMatch, altNameMatch, shortDescMatch, searchKeywordMatch));
            }

            // Using AND across multiple keywords so that finding "acier 304L" implies finding both words in any of the fields
            return cb.and(keywordPredicates.toArray(new Predicate[0]));
        };
    }
}
