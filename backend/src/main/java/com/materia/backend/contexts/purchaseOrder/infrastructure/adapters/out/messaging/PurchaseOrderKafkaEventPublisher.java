package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.messaging;

import com.materia.backend.common.infrastructure.messaging.BaseEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderConfirmedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderDeliveredEvent;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderSubmittedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Kafka adapter for the purchase order event publishing port. Active when app.messaging.type=kafka. */
@Component
@ConditionalOnProperty(name = "app.messaging.type", havingValue = "kafka")
public class PurchaseOrderKafkaEventPublisher implements PurchaseOrderEventPublisher {

    private final BaseEventPublisher eventPublisher;

    public PurchaseOrderKafkaEventPublisher(BaseEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(PurchaseOrderSubmittedEvent event) {
        eventPublisher.publish(event);
    }

    @Override
    public void publish(PurchaseOrderConfirmedEvent event) {
        eventPublisher.publish(event);
    }

    @Override
    public void publish(PurchaseOrderDeliveredEvent event) {
        eventPublisher.publish(event);
    }
}
