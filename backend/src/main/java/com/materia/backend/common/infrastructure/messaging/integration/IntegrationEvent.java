package com.materia.backend.common.infrastructure.messaging.integration;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * An event as another service receives it: the envelope {@code OutboxRelay} sends to Kafka. Handlers read the
 * payload as JSON, never as the publisher's Java class, so they keep working once the publisher is a separate service.
 *
 * @param sourceContext the publishing context, e.g. {@code goodsReceipt}
 * @param eventType     the event's simple name without the "Event" suffix, e.g. {@code GoodsReceiptCompleted}
 */
public record IntegrationEvent(UUID eventId, String eventType, String sourceContext, String aggregateId,
                               String occurredAt, JsonNode payload) {

    public boolean is(String context, String type) {
        return context.equals(sourceContext) && type.equals(eventType);
    }
}
