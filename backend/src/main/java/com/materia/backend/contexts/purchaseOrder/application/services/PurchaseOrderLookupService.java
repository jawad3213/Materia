package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderView;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderLookupUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PurchaseOrderLookupService implements PurchaseOrderLookupUseCase {

    private static final Set<OrderStatus> RECEIVABLE =
            EnumSet.of(OrderStatus.READY_FOR_RECEIPT, OrderStatus.PARTIALLY_RECEIVED);

    private static final Set<OrderStatus> GOODS_RECEIVED =
            EnumSet.of(OrderStatus.PARTIALLY_RECEIVED, OrderStatus.COMPLETED);

    private final PurchaseOrderRepository purchaseOrderRepository;

    public PurchaseOrderLookupService(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Override
    public Optional<PurchaseOrderView> findOrder(UUID purchaseOrderId) {
        return purchaseOrderRepository.findById(purchaseOrderId).map(PurchaseOrderLookupService::toView);
    }

    private static PurchaseOrderView toView(PurchaseOrder order) {
        return new PurchaseOrderView(
                order.getId(),
                order.getOrderCode() != null ? order.getOrderCode().getValue() : null,
                order.getStatus() != null ? order.getStatus().name() : null,
                RECEIVABLE.contains(order.getStatus()),
                GOODS_RECEIVED.contains(order.getStatus()),
                order.getSupplierId(),
                order.getSupplierCode(),
                order.getSupplierName(),
                order.getCurrencyCode(),
                order.getAssignedTo(),
                order.getAssignedToName(),
                order.getExpectedDeliveryDate(),
                order.getRequisitionId(),
                order.getLines().stream()
                        .map(line -> new PurchaseOrderView.Line(line.getId(), line.getLineNumber(),
                                line.getMaterialId(), line.getMaterialCode(), line.getMaterialName(),
                                line.getUnitOfMeasure(), line.getUnitPrice(), line.getQuantity(),
                                line.getRequisitionLineId(), line.getTaxRate()))
                        .toList());
    }
}
