package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence.entities.PurchaseOrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for purchase orders.
 */
@Repository
public interface SpringDataPurchaseOrderRepository extends JpaRepository<PurchaseOrderJpaEntity, UUID>, JpaSpecificationExecutor<PurchaseOrderJpaEntity> {

    Optional<PurchaseOrderJpaEntity> findByOrderCode(String orderCode);

    boolean existsByOrderCode(String orderCode);

    List<PurchaseOrderJpaEntity> findByStatus(OrderStatus status);

    List<PurchaseOrderJpaEntity> findByDeliveryStatus(DeliveryStatus deliveryStatus);

    List<PurchaseOrderJpaEntity> findBySupplierId(UUID supplierId);

    List<PurchaseOrderJpaEntity> findByRequisitionId(UUID requisitionId);

    List<PurchaseOrderJpaEntity> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);

    List<PurchaseOrderJpaEntity> findByExpectedDeliveryDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT po
            FROM PurchaseOrderJpaEntity po
            WHERE LOWER(po.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(po.requisitionCode, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(po.supplierName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(po.orderedByName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(po.notes, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<PurchaseOrderJpaEntity> search(@Param("keyword") String keyword);
}
