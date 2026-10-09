package com.materia.backend.common.infrastructure.messaging.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KafkaIntegrationEventConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("kafka: reads the envelope OutboxRelay writes, keeping the payload as JSON")
    void parse_readsTheRelayEnvelope() throws Exception {
        UUID id = UUID.randomUUID();
        String message = "{\"eventId\":\"" + id + "\",\"eventType\":\"GoodsReceiptCompleted\","
                + "\"sourceContext\":\"goodsReceipt\",\"aggregateId\":\"agg-1\",\"occurredAt\":\"2026-10-09T10:00\","
                + "\"payload\":{\"receiptCode\":\"GR-0001\",\"lines\":[{\"quantity\":3}]}}";

        IntegrationEvent event = KafkaIntegrationEventConsumer.parse(message, objectMapper);

        assertEquals(id, event.eventId());
        assertTrue(event.is("goodsReceipt", "GoodsReceiptCompleted"));
        assertEquals("agg-1", event.aggregateId());
        assertEquals(3, event.payload().path("lines").get(0).path("quantity").asInt());
    }

    @Test
    @DisplayName("kafka: an envelope without aggregate id is read with none")
    void parse_withoutAggregateId() throws Exception {
        String message = "{\"eventId\":\"" + UUID.randomUUID() + "\",\"eventType\":\"X\",\"sourceContext\":\"y\","
                + "\"aggregateId\":null,\"payload\":{}}";

        assertNull(KafkaIntegrationEventConsumer.parse(message, objectMapper).aggregateId());
    }
}
