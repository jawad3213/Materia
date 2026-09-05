package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.masterData.domain.entities.Supplier;
import com.materia.backend.contexts.masterData.domain.ports.out.SupplierRepository;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.SupplierJpaEntity;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.mappers.SupplierPersistenceMapper;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.repositories.SpringDataSupplierRepository;
import org.springframework.stereotype.Component;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.contexts.masterData.domain.valueObjects.SupplierSearchFilter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence Adapter for Supplier
 * Hexagonal Architecture - Implements the domain output port (SupplierRepository)
 */
@Component
public class SupplierPersistenceAdapter implements SupplierRepository {

    private final SpringDataSupplierRepository jpaRepository;
    private final SupplierPersistenceMapper mapper;

    public SupplierPersistenceAdapter(SpringDataSupplierRepository jpaRepository,
                                       SupplierPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    // ============================================================
    // BaseRepository methods
    // ============================================================

    @Override
    public Optional<Supplier> findById(UUID uuid) {
        return jpaRepository.findById(uuid).map(mapper::toDomainEntity);
    }

    @Override
    public List<Supplier> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public Supplier save(Supplier entity) {
        SupplierJpaEntity jpa = mapper.toJpaEntity(entity);
        return mapper.toDomainEntity(jpaRepository.save(jpa));
    }

    @Override
    public List<Supplier> saveAll(List<Supplier> entities) {
        List<SupplierJpaEntity> jpaEntities = entities.stream()
                .map(mapper::toJpaEntity).collect(Collectors.toList());
        return jpaRepository.saveAll(jpaEntities).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID uuid) { jpaRepository.deleteById(uuid); }

    @Override
    public void delete(Supplier entity) { jpaRepository.deleteById(entity.getId()); }

    @Override
    public boolean existsById(UUID id) { return jpaRepository.existsById(id); }

    @Override
    public long count() { return jpaRepository.count(); }

    @Override
    public List<Supplier> findAllById(List<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    // ============================================================
    // SupplierRepository-specific methods
    // ============================================================

    @Override
    public Optional<Supplier> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomainEntity);
    }


    @Override
    public List<Supplier> findByStatus(String status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Supplier> findByCountry(String country) {
        return jpaRepository.findByCountry(country).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) { return jpaRepository.existsByCode(code); }

    @Override
    public List<Supplier> search(String keyword) {
        return jpaRepository.search(keyword).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public PageResponse<Supplier> searchAdvanced(
            SupplierSearchFilter filter,
            int page, 
            int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        Specification<SupplierJpaEntity> spec = Specification.where(buildSearchSpecification(filter))
                .and((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    if (StringUtils.hasText(filter.getStatus())) {
                        predicates.add(cb.equal(cb.upper(root.get("status")), filter.getStatus().trim().toUpperCase()));
                    }
                    if (filter.getCurrencyCode() != null) {
                        predicates.add(cb.equal(root.get("currencyCode"), filter.getCurrencyCode()));
                    }
                    if (StringUtils.hasText(filter.getCountry())) {
                        predicates.add(cb.equal(cb.lower(root.get("country")), filter.getCountry().trim().toLowerCase()));
                    }

                    return cb.and(predicates.toArray(new Predicate[0]));
                });

        org.springframework.data.domain.Page<SupplierJpaEntity> jpaPage = jpaRepository.findAll(spec, pageable);

        List<Supplier> domainList = jpaPage.getContent().stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());

        return new PageResponse<>(
                domainList,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements(),
                jpaPage.getTotalPages(),
                jpaPage.isLast()
        );
    }

    private Specification<SupplierJpaEntity> buildSearchSpecification(SupplierSearchFilter filter) {
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
            if (StringUtils.hasText(filter.getContactPerson())) {
                searchPredicates.add(cb.like(cb.lower(root.get("contactPerson")), "%" + filter.getContactPerson().trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getContactEmail())) {
                searchPredicates.add(cb.like(cb.lower(root.get("contactEmail")), "%" + filter.getContactEmail().trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getFullAddress())) {
                String addrPattern = "%" + filter.getFullAddress().trim().toLowerCase() + "%";
                searchPredicates.add(cb.or(
                        cb.like(cb.lower(root.get("address")), addrPattern),
                        cb.like(cb.lower(root.get("city")), addrPattern),
                        cb.like(cb.lower(root.get("country")), addrPattern),
                        cb.like(cb.lower(root.get("postalCode")), addrPattern)
                ));
            }

            if (searchPredicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.or(searchPredicates.toArray(new Predicate[0]));
        };
    }
}
