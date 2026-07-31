package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.masterdata.domain.entities.Supplier;
import com.materia.backend.contexts.masterdata.domain.ports.out.SupplierRepository;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities.SupplierJpaEntity;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.mappers.SupplierPersistenceMapper;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.repositories.SpringDataSupplierRepository;
import org.springframework.stereotype.Component;

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
    public List<String> findCodesByPrefix(String prefix) {
        return jpaRepository.findCodesByPrefix(prefix);
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
    public List<Supplier> findByCity(String city) {
        return jpaRepository.findByCity(city).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) { return jpaRepository.existsByCode(code); }

    @Override
    public List<Supplier> search(String keyword) {
        return jpaRepository.search(keyword).stream()
                .map(mapper::toDomainEntity).collect(Collectors.toList());
    }
}
