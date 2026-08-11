package com.materia.backend.contexts.payement.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.payement.domain.entities.Payment;
import com.materia.backend.contexts.payement.domain.enums.PaymentStatus;
import com.materia.backend.contexts.payement.domain.ports.out.PaymentPort;
import com.materia.backend.contexts.payement.infrastructure.adapters.out.persistence.entities.PaymentJpaEntity;
import com.materia.backend.contexts.payement.infrastructure.adapters.out.persistence.mappers.PaymentPersistenceMapper;
import com.materia.backend.contexts.payement.infrastructure.adapters.out.persistence.repositories.PaymentSpringDataRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PaymentPersistenceAdapter implements PaymentPort {

    private final PaymentSpringDataRepository repository;
    private final PaymentPersistenceMapper mapper;

    public PaymentPersistenceAdapter(PaymentSpringDataRepository repository, PaymentPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment domainEntity) {
        PaymentJpaEntity jpaEntity = mapper.toJpaEntity(domainEntity);
        PaymentJpaEntity savedEntity = repository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Payment> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findAllById(List<UUID> ids) {
        return repository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> saveAll(List<Payment> entities) {
        List<PaymentJpaEntity> jpaEntities = entities.stream()
                .map(mapper::toJpaEntity)
                .collect(Collectors.toList());
        return repository.saveAll(jpaEntities).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public void delete(Payment entity) {
        repository.delete(mapper.toJpaEntity(entity));
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public Optional<Payment> findByCode(String code) {
        return repository.findByPaymentCode(code).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByPaymentCode(code);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return repository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findBySupplierId(String supplierId) {
        return repository.findBySupplierId(supplierId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> searchByKeyword(String keyword) {
        return repository.searchByKeyword(keyword).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
