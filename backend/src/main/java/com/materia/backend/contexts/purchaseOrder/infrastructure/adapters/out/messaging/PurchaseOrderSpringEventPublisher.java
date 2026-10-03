package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.messaging;

import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderConfirmedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderDeliveredEvent;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderSubmittedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring In-Process Event adapter for the purchase order event publishing port.
 * Active when app.messaging.type=spring (or when not explicitly set).
 */
@Component
@ConditionalOnProperty(name = "app.messaging.type", havingValue = "spring", matchIfMissing = true)
public class PurchaseOrderSpringEventPublisher implements PurchaseOrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PurchaseOrderSpringEventPublisher.class);
    private final ApplicationEventPublisher eventPublisher;

    public PurchaseOrderSpringEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(PurchaseOrderSubmittedEvent event) {
        log.info("📢 [Spring Event Published] PurchaseOrderSubmittedEvent for ID: {}", event.getPurchaseOrderId());
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publish(PurchaseOrderConfirmedEvent event) {
        log.info("📢 [Spring Event Published] PurchaseOrderConfirmedEvent for ID: {}", event.getPurchaseOrderId());
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publish(PurchaseOrderDeliveredEvent event) {
        log.info("📢 [Spring Event Published] PurchaseOrderDeliveredEvent for ID: {}", event.getPurchaseOrderId());
        eventPublisher.publishEvent(event);
    }
}
