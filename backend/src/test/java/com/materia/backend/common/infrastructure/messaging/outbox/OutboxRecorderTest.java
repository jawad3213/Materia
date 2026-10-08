package com.materia.backend.common.infrastructure.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptCompletedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderDeliveredEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

/** Domain events are stored with their context and a JSON payload. */
@ExtendWith(MockitoExtension.class)
class OutboxRecorderTest {

    @Mock private SpringDataOutboxRepository outbox;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private OutboxMessageJpaEntity recorded(Object event) {
        new OutboxRecorder(outbox, objectMapper).record((com.materia.backend.common.domain.DomainEvent) event);
        ArgumentCaptor<OutboxMessageJpaEntity> captor = ArgumentCaptor.forClass(OutboxMessageJpaEntity.class);
        verify(outbox).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("record: the event is stored under the context that raised it, with its payload")
    void recordsEventWithContextAndPayload() {
        UUID orderId = UUID.randomUUID();
        PurchaseOrderDeliveredEvent event = new PurchaseOrderDeliveredEvent(orderId, "PO-2026-0001", DeliveryStatus.DELIVERED, "user-1");

        OutboxMessageJpaEntity message = recorded(event);

        assertEquals(event.getEventId(), message.getEventId());
        assertEquals("PurchaseOrderDelivered", message.getEventType());
        assertEquals("purchaseOrder", message.getSourceContext());
        assertEquals(orderId.toString(), message.getAggregateId());
        assertTrue(message.getPayload().contains("\"orderCode\":\"PO-2026-0001\""), message.getPayload());
        assertTrue(message.getPayload().contains("DELIVERED"), message.getPayload());
        assertNull(message.getPublishedAt());
        assertEquals(0, message.getAttempts());
    }

    @Test
    @DisplayName("record: events of other contexts go to their own context")
    void recordsGoodsReceiptEvents() {
        OutboxMessageJpaEntity message = recorded(new GoodsReceiptCompletedEvent(UUID.randomUUID(), "GR-1", "po-1", 5, 4, "user-1"));

        assertEquals("goodsReceipt", message.getSourceContext());
        assertTrue(message.getPayload().contains("\"totalQuantityAccepted\":4"), message.getPayload());
    }
}
