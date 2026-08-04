package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.entities.GoodsReceiptJpaEntity;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.mappers.GoodsReceiptPersistenceMapper;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.repositories.SpringDataGoodsReceiptRepository;
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

@Component
public class GoodsReceiptPersistenceAdapter implements GoodsReceiptRepository {

    private final SpringDataGoodsReceiptRepository jpaRepository;
    private final GoodsReceiptPersistenceMapper mapper;

    public GoodsReceiptPersistenceAdapter(SpringDataGoodsReceiptRepository jpaRepository,
                                         GoodsReceiptPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<GoodsReceipt> findById(UUID uuid) {
        return jpaRepository.findById(uuid).map(mapper::toDomainEntity);
    }

    @Override
    public List<GoodsReceipt> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public GoodsReceipt save(GoodsReceipt entity) {
        GoodsReceiptJpaEntity jpaEntity = mapper.toJpaEntity(entity);
        return mapper.toDomainEntity(jpaRepository.save(jpaEntity));
    }

    @Override
    public List<GoodsReceipt> saveAll(List<GoodsReceipt> entities) {
        List<GoodsReceiptJpaEntity> jpaEntities = entities.stream()
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
    public void delete(GoodsReceipt entity) {
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
    public List<GoodsReceipt> findAllById(List<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<GoodsReceipt> findByReceiptCode(String receiptCode) {
        return jpaRepository.findByReceiptCode(receiptCode).map(mapper::toDomainEntity);
    }

    @Override
    public boolean existsByReceiptCode(String receiptCode) {
        return jpaRepository.existsByReceiptCode(receiptCode);
    }

    @Override
    public List<GoodsReceipt> findByPurchaseOrderId(String purchaseOrderId) {
        return jpaRepository.findByPurchaseOrderId(purchaseOrderId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GoodsReceipt> findByStatus(ReceiptStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GoodsReceipt> findByReceivedBy(String receiverId) {
        return jpaRepository.findByReceivedBy(receiverId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GoodsReceipt> findBySupplierId(String supplierId) {
        return jpaRepository.findBySupplierId(supplierId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GoodsReceipt> findByReceiptDateBetween(LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByReceiptDateBetween(startDate, endDate).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GoodsReceipt> search(String keyword) {
        return jpaRepository.findAll(buildSearchSpecification(keyword)).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    private Specification<GoodsReceiptJpaEntity> buildSearchSpecification(String keywordString) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keywordString)) {
                return cb.conjunction();
            }

            String[] keywords = keywordString.split("[,\\s]+");
            List<Predicate> keywordPredicates = new ArrayList<>();

            for (String keyword : keywords) {
                if (!StringUtils.hasText(keyword)) {
                    continue;
                }

                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate receiptCodeMatch = cb.like(cb.lower(root.get("receiptCode")), searchPattern);
                Predicate purchaseOrderIdMatch = cb.like(cb.lower(cb.coalesce(root.get("purchaseOrderId"), "")), searchPattern);
                Predicate purchaseOrderCodeMatch = cb.like(cb.lower(cb.coalesce(root.get("purchaseOrderCode"), "")), searchPattern);
                Predicate supplierNameMatch = cb.like(cb.lower(cb.coalesce(root.get("supplierName"), "")), searchPattern);
                Predicate receivedByNameMatch = cb.like(cb.lower(cb.coalesce(root.get("receivedByName"), "")), searchPattern);
                Predicate notesMatch = cb.like(cb.lower(cb.coalesce(root.get("notes"), "")), searchPattern);
                Predicate discrepancyNotesMatch = cb.like(cb.lower(cb.coalesce(root.get("discrepancyNotes"), "")), searchPattern);

                keywordPredicates.add(cb.or(
                        receiptCodeMatch,
                        purchaseOrderIdMatch,
                        purchaseOrderCodeMatch,
                        supplierNameMatch,
                        receivedByNameMatch,
                        notesMatch,
                        discrepancyNotesMatch
                ));
            }

            return cb.and(keywordPredicates.toArray(new Predicate[0]));
        };
    }
}
