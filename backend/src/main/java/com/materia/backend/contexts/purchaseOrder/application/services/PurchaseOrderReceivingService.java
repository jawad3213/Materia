package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderDeliveredEvent;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotFoundException;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderReceivingUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void recordReceipt(UUID purchaseOrderId, boolean fullyReceived, String userId) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(purchaseOrderId);
        purchaseOrder.recordReceipt(fullyReceived, userId);
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        eventPublisher.publish(new PurchaseOrderDeliveredEvent(
                saved.getId(), saved.getOrderCode().getValue(), saved.getDeliveryStatus(), userId));
    }

    @Override
    public void reopenForReplacement(UUID purchaseOrderId, String userId) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(purchaseOrderId);
        purchaseOrder.reopenForReplacement(userId);
        purchaseOrderRepository.save(purchaseOrder);
    }

    private PurchaseOrder getPurchaseOrder(UUID purchaseOrderId) {
        return purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(purchaseOrderId));
    }
}
