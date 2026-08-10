package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.ports.out.ReturnToVendorRepository;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.entities.ReturnToVendorJpaEntity;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.mappers.ReturnToVendorPersistenceMapper;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.repositories.SpringDataReturnToVendorRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ReturnToVendorRepositoryAdapter implements ReturnToVendorRepository {

    private final SpringDataReturnToVendorRepository springDataRepository;
    private final ReturnToVendorPersistenceMapper mapper;

    public ReturnToVendorRepositoryAdapter(
            SpringDataReturnToVendorRepository springDataRepository,
            ReturnToVendorPersistenceMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ReturnToVendor> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ReturnToVendor> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReturnToVendor save(ReturnToVendor entity) {
        ReturnToVendorJpaEntity jpaEntity = mapper.toJpaEntity(entity);
        ReturnToVendorJpaEntity saved = springDataRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public List<ReturnToVendor> saveAll(List<ReturnToVendor> entities) {
        List<ReturnToVendorJpaEntity> jpaEntities = entities.stream()
                .map(mapper::toJpaEntity)
                .collect(Collectors.toList());
        return springDataRepository.saveAll(jpaEntities).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(ReturnToVendor entity) {
        springDataRepository.deleteById(entity.getId());
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepository.existsById(id);
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }

    @Override
    public List<ReturnToVendor> findAllById(List<UUID> ids) {
        return springDataRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReturnToVendor> findByReturnCode(String returnCode) {
        return springDataRepository.findByReturnCode(returnCode).map(mapper::toDomain);
    }

    @Override
    public boolean existsByReturnCode(String returnCode) {
        return springDataRepository.existsByReturnCode(returnCode);
    }

    @Override
    public List<ReturnToVendor> findByGoodsReceiptId(String goodsReceiptId) {
        return springDataRepository.findByGoodsReceiptId(goodsReceiptId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnToVendor> findByPurchaseOrderId(String purchaseOrderId) {
        return springDataRepository.findByPurchaseOrderId(purchaseOrderId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnToVendor> findBySupplierId(String supplierId) {
        return springDataRepository.findBySupplierId(supplierId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnToVendor> findByStatus(ReturnStatus status) {
        return springDataRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnToVendor> findByResolutionType(ResolutionType resolutionType) {
        return springDataRepository.findByResolutionType(resolutionType.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnToVendor> findByReturnDateBetween(LocalDate startDate, LocalDate endDate) {
        return springDataRepository.findByReturnDateBetween(startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnToVendor> findByResolutionDateBetween(LocalDate startDate, LocalDate endDate) {
        return springDataRepository.findByResolutionDateBetween(startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnToVendor> search(String keyword) {
        return springDataRepository.search(keyword).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
