package com.materia.backend.contexts.purchaseOrder.domain.ports.out;

import com.materia.backend.common.domain.BaseRepository;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for purchase order persistence.
 */
public interface PurchaseOrderRepository extends BaseRepository<PurchaseOrder> {

    Optional<PurchaseOrder> findByCode(String code);

    boolean existsByCode(String code);

    List<PurchaseOrder> findByStatus(OrderStatus status);

    List<PurchaseOrder> findByDeliveryStatus(DeliveryStatus status);

    List<PurchaseOrder> findBySupplierId(UUID supplierId);

    List<PurchaseOrder> findByRequisitionId(UUID requisitionId);

    List<PurchaseOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);

    List<PurchaseOrder> findByExpectedDeliveryDateBetween(LocalDate startDate, LocalDate endDate);

    List<PurchaseOrder> search(String keyword);
}
