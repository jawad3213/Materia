package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.contexts.purchaseOrder.application.dtos.OrderForReceiptOutput;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderDeliveredEvent;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotFoundException;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderReceivingUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PurchaseOrderReceivingService implements PurchaseOrderReceivingUseCase {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderEventPublisher eventPublisher;

    public PurchaseOrderReceivingService(PurchaseOrderRepository purchaseOrderRepository,
                                         PurchaseOrderEventPublisher eventPublisher) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderForReceiptOutput> findForReceipt(UUID purchaseOrderId) {
        return purchaseOrderRepository.findById(purchaseOrderId).map(PurchaseOrderReceivingService::toOutput);
    }

    @Override
    public void recordReceipt(UUID purchaseOrderId, boolean fullyReceived, String userId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(purchaseOrderId));
        purchaseOrder.recordReceipt(fullyReceived, userId);
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        eventPublisher.publish(new PurchaseOrderDeliveredEvent(
                saved.getId(), saved.getOrderCode().getValue(), saved.getDeliveryStatus(), userId));
    }

    private static OrderForReceiptOutput toOutput(PurchaseOrder order) {
        boolean receivable = order.getStatus() == OrderStatus.READY_FOR_RECEIPT
                || order.getStatus() == OrderStatus.PARTIALLY_RECEIVED;
        return new OrderForReceiptOutput(
                order.getId(),
                order.getOrderCode() != null ? order.getOrderCode().getValue() : null,
                receivable,
                order.getAssignedTo(),
                order.getAssignedToName(),
                order.getSupplierId(),
                order.getSupplierName(),
                order.getExpectedDeliveryDate(),
                order.getRequisitionId(),
                order.getLines().stream()
                        .map(line -> new OrderForReceiptOutput.Line(line.getId(), line.getLineNumber(),
                                line.getMaterialId(), line.getMaterialCode(), line.getMaterialName(),
                                line.getUnitOfMeasure(), line.getUnitPrice(), line.getQuantity(),
                                line.getRequisitionLineId()))
                        .toList());
    }
}
