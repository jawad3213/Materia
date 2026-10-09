package com.materia.backend.common.infrastructure.messaging.outbox;

import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderDeliveredEvent;
import com.materia.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The outbox stores an event exactly when its transaction commits. These tests commit for real (the usual
 * test transaction would roll back before the recorder runs), so they clean up after themselves.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OutboxIT extends AbstractIntegrationTest {

    @Autowired private ApplicationEventPublisher events;
    @Autowired private TransactionTemplate transactions;
    @Autowired private SpringDataOutboxRepository outbox;

    @AfterEach
    void cleanUp() {
        outbox.deleteAll();
    }

    private static PurchaseOrderDeliveredEvent delivered() {
        return new PurchaseOrderDeliveredEvent(UUID.randomUUID(), "PO-2026-0042", DeliveryStatus.DELIVERED, "user-1");
    }

    @Test
    @DisplayName("outbox: an event raised in a committed transaction is stored, pending, with its JSON payload")
    void committedEventIsStored() {
        PurchaseOrderDeliveredEvent event = delivered();

        transactions.executeWithoutResult(status -> events.publishEvent(event));

        // Other tests commit events too: look for this one rather than counting the table.
        List<OutboxMessageJpaEntity> stored = outbox.findAll().stream()
                .filter(m -> event.getEventId().equals(m.getEventId())).toList();
        assertEquals(1, stored.size());
        OutboxMessageJpaEntity message = stored.get(0);
        assertEquals(event.getEventId(), message.getEventId());
        assertEquals("purchaseOrder", message.getSourceContext());
        assertNull(message.getPublishedAt());
        assertTrue(message.getPayload().contains("PO-2026-0042"));
    }

    @Test
    @DisplayName("outbox: an event raised in a rolled-back transaction is not stored")
    void rolledBackEventIsNotStored() {
        PurchaseOrderDeliveredEvent event = delivered();
        transactions.executeWithoutResult(status -> {
            events.publishEvent(event);
            status.setRollbackOnly();
        });

        assertTrue(outbox.findAll().stream().noneMatch(m -> event.getEventId().equals(m.getEventId())));
    }
}
