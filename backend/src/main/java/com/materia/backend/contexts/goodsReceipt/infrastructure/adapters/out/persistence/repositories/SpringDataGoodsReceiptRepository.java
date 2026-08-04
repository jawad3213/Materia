package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.entities.GoodsReceiptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataGoodsReceiptRepository extends JpaRepository<GoodsReceiptJpaEntity, UUID>, JpaSpecificationExecutor<GoodsReceiptJpaEntity> {

    Optional<GoodsReceiptJpaEntity> findByReceiptCode(String receiptCode);

    boolean existsByReceiptCode(String receiptCode);

    List<GoodsReceiptJpaEntity> findByPurchaseOrderId(String purchaseOrderId);

    List<GoodsReceiptJpaEntity> findByStatus(ReceiptStatus status);

    List<GoodsReceiptJpaEntity> findByReceivedBy(String receivedBy);

    List<GoodsReceiptJpaEntity> findBySupplierId(String supplierId);

    List<GoodsReceiptJpaEntity> findByReceiptDateBetween(LocalDate startDate, LocalDate endDate);
}
