package com.materia.backend.contexts.purchaseOrder.domain.ports.out;

import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderConfirmedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderDeliveredEvent;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderSubmittedEvent;

/**
 * Outbound port for notifying other modules about purchase order changes.
 * An infrastructure adapter can implement this with Kafka, Spring events, or another broker.
 */
public interface PurchaseOrderEventPublisher {

    void publish(PurchaseOrderSubmittedEvent event);

    void publish(PurchaseOrderConfirmedEvent event);

    void publish(PurchaseOrderDeliveredEvent event);
}
