package com.materia.backend.common.infrastructure.messaging.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/** A domain event recorded with the change that raised it, waiting to be relayed to the message broker. */
@Entity
@Table(name = "outbox_messages")
public class OutboxMessageJpaEntity {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    /** The bounded context that raised the event; the relay publishes to that context's topic. */
    @Column(name = "source_context", nullable = false, length = 60)
    private String sourceContext;

    @Column(name = "aggregate_id", length = 100)
    private String aggregateId;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    protected OutboxMessageJpaEntity() {
    }

    public OutboxMessageJpaEntity(UUID eventId, String eventType, String sourceContext, String aggregateId,
                                  String payload, LocalDateTime occurredAt) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.eventType = eventType;
        this.sourceContext = sourceContext;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.createdAt = LocalDateTime.now();
    }

    public void markPublished() {
        this.publishedAt = LocalDateTime.now();
        this.lastError = null;
    }

    public void markFailed(String error) {
        this.attempts++;
        this.lastError = error != null && error.length() > 1000 ? error.substring(0, 1000) : error;
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getSourceContext() { return sourceContext; }
    public String getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public int getAttempts() { return attempts; }
    public String getLastError() { return lastError; }
}
