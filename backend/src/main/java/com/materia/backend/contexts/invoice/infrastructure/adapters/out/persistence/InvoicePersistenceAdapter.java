package com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.invoice.domain.entities.Invoice;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.ports.out.InvoiceRepository;
import com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.entities.InvoiceJpaEntity;
import com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.mappers.InvoicePersistenceMapper;
import com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.repositories.InvoiceSpringDataRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class InvoicePersistenceAdapter implements InvoiceRepository {

    private final InvoiceSpringDataRepository invoiceSpringDataRepository;
    private final InvoicePersistenceMapper mapper;

    public InvoicePersistenceAdapter(InvoiceSpringDataRepository invoiceSpringDataRepository, InvoicePersistenceMapper mapper) {
        this.invoiceSpringDataRepository = invoiceSpringDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Invoice save(Invoice domainEntity) {
        InvoiceJpaEntity jpaEntity = mapper.toJpaEntity(domainEntity);
        InvoiceJpaEntity savedEntity = invoiceSpringDataRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return invoiceSpringDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceSpringDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findAllById(List<UUID> ids) {
        return invoiceSpringDataRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> saveAll(List<Invoice> entities) {
        List<InvoiceJpaEntity> jpaEntities = entities.stream()
                .map(mapper::toJpaEntity)
                .collect(Collectors.toList());
        return invoiceSpringDataRepository.saveAll(jpaEntities).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        invoiceSpringDataRepository.deleteById(id);
    }

    @Override
    public void delete(Invoice entity) {
        invoiceSpringDataRepository.delete(mapper.toJpaEntity(entity));
    }

    @Override
    public boolean existsById(UUID id) {
        return invoiceSpringDataRepository.existsById(id);
    }

    @Override
    public long count() {
        return invoiceSpringDataRepository.count();
    }

    @Override
    public Optional<Invoice> findByInvoiceCode(String invoiceCode) {
        return invoiceSpringDataRepository.findByInvoiceCode(invoiceCode).map(mapper::toDomain);
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return invoiceSpringDataRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findBySupplierId(String supplierId) {
        return invoiceSpringDataRepository.findBySupplierId(supplierId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findByPurchaseOrderId(String purchaseOrderId) {
        return invoiceSpringDataRepository.findByPurchaseOrderId(purchaseOrderId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findByDueDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return invoiceSpringDataRepository.findByDueDateBetween(startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findByInvoiceDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return invoiceSpringDataRepository.findByInvoiceDateBetween(startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findByInvoiceType(com.materia.backend.contexts.invoice.domain.enums.InvoiceType type) {
        return invoiceSpringDataRepository.findByInvoiceType(type).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Invoice> findByInvoiceCode(com.materia.backend.contexts.invoice.domain.valueObjects.InvoiceCode invoiceCode) {
        if (invoiceCode == null) return Optional.empty();
        return invoiceSpringDataRepository.findByInvoiceCode(invoiceCode.getValue()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByInvoiceCode(String invoiceCode) {
        return invoiceSpringDataRepository.existsByInvoiceCode(invoiceCode);
    }

    @Override
    public List<Invoice> search(String keyword) {
        return invoiceSpringDataRepository.searchByKeyword(keyword).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
