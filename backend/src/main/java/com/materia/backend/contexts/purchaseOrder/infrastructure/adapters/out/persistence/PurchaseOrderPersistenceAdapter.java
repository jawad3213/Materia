package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence.entities.PurchaseOrderJpaEntity;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence.mappers.PurchaseOrderPersistenceMapper;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence.repositories.SpringDataPurchaseOrderRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence adapter for purchase orders.
 */
@Component
public class PurchaseOrderPersistenceAdapter implements PurchaseOrderRepository {

    private final SpringDataPurchaseOrderRepository jpaRepository;
    private final PurchaseOrderPersistenceMapper mapper;

    public PurchaseOrderPersistenceAdapter(SpringDataPurchaseOrderRepository jpaRepository,
                                           PurchaseOrderPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<PurchaseOrder> findById(UUID uuid) {
        return jpaRepository.findById(uuid).map(mapper::toDomainEntity);
    }

    @Override
    public List<PurchaseOrder> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PurchaseOrder save(PurchaseOrder entity) {
        PurchaseOrderJpaEntity jpa = mapper.toJpaEntity(entity);
        if (entity.getId() != null) {
            jpaRepository.findById(entity.getId()).ifPresent(existing -> carryLineVersions(existing, jpa));
        }
        return mapper.toDomainEntity(jpaRepository.save(jpa));
    }

    /**
     * Domain lines carry no version, so a line rebuilt from the domain would be merged with a null
     * version and rejected as stale. Lines that already exist keep their stored version; new lines
     * stay unversioned and are inserted (spec 002 research F-013).
     */
    private static void carryLineVersions(PurchaseOrderJpaEntity existing, PurchaseOrderJpaEntity updated) {
        java.util.Map<UUID, Long> versions = new java.util.HashMap<>();
        existing.getLines().forEach(line -> versions.put(line.getId(), line.getVersion()));
        updated.getLines().forEach(line -> {
            if (versions.containsKey(line.getId())) {
                line.setVersion(versions.get(line.getId()));
            }
        });
    }

    @Override
    public List<PurchaseOrder> saveAll(List<PurchaseOrder> entities) {
        List<PurchaseOrderJpaEntity> jpaEntities = entities.stream()
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
    public void delete(PurchaseOrder entity) {
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
    public List<PurchaseOrder> findAllById(List<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PurchaseOrder> findByCode(String code) {
        return jpaRepository.findByOrderCode(code).map(mapper::toDomainEntity);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByOrderCode(code);
    }

    @Override
    public List<PurchaseOrder> findByStatus(OrderStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrder> findByDeliveryStatus(DeliveryStatus status) {
        return jpaRepository.findByDeliveryStatus(status).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrder> findBySupplierId(UUID supplierId) {
        return jpaRepository.findBySupplierId(supplierId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrder> findByRequisitionId(UUID requisitionId) {
        return jpaRepository.findByRequisitionId(requisitionId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByOrderDateBetween(startDate, endDate).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrder> findByExpectedDeliveryDateBetween(LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByExpectedDeliveryDateBetween(startDate, endDate).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrder> search(String keyword) {
        return jpaRepository.findAll(buildSearchSpecification(keyword)).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    private Specification<PurchaseOrderJpaEntity> buildSearchSpecification(String keywordString) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keywordString)) {
                return cb.conjunction();
            }

            String[] keywords = keywordString.split("[,\\s]+");
            List<Predicate> keywordPredicates = new ArrayList<>();

            for (String kw : keywords) {
                if (!StringUtils.hasText(kw)) {
                    continue;
                }

                String searchPattern = "%" + kw.trim().toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("orderCode")), searchPattern);
                Predicate requisitionCodeMatch = cb.like(cb.lower(cb.coalesce(root.get("requisitionCode"), "")), searchPattern);
                Predicate supplierNameMatch = cb.like(cb.lower(cb.coalesce(root.get("supplierName"), "")), searchPattern);
                Predicate orderedByNameMatch = cb.like(cb.lower(cb.coalesce(root.get("orderedByName"), "")), searchPattern);
                Predicate notesMatch = cb.like(cb.lower(cb.coalesce(root.get("notes"), "")), searchPattern);

                keywordPredicates.add(cb.or(codeMatch, requisitionCodeMatch, supplierNameMatch, orderedByNameMatch, notesMatch));
            }

            return cb.and(keywordPredicates.toArray(new Predicate[0]));
        };
    }
}
