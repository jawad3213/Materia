package com.materia.backend.common.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Domain Event - PURE POJO
 * Represents something significant that happened in the domain
 * NO framework dependencies (no Spring, no JPA, no Jackson annotations)
 *
 * Domain events are immutable and capture facts about the domain
 */
public abstract class DomainEvent {

    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final Object aggregateId;
    private final String eventType;

    /**
     * Constructor for new domain events
     * @param aggregateId The ID of the aggregate that generated the event
     */
    protected DomainEvent(Object aggregateId) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.eventType = determineEventType();
    }

    /**
     * Constructor for reconstituting events from persistence
     * @param eventId The UUID of the event
     * @param occurredOn When the event occurred
     * @param aggregateId The ID of the aggregate
     */
    protected DomainEvent(UUID eventId, LocalDateTime occurredOn, Object aggregateId) {
        this.eventId = eventId;
        this.occurredOn = occurredOn;
        this.aggregateId = aggregateId;
        this.eventType = determineEventType();
    }

    /**
     * Determine the event type from the class name
     */
    private String determineEventType() {
        String className = this.getClass().getSimpleName();
        // Remove "Event" suffix if present
        if (className.endsWith("Event")) {
            return className.substring(0, className.length() - 5);
        }
        return className;
    }

    // ============ GETTERS (Immutable - No Setters) ============

    /**
     * Get the unique event ID
     */
    public UUID getEventId() {
        return eventId;
    }

    /**
     * Get when the event occurred
     */
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    /**
     * Get the aggregate ID
     */
    public Object getAggregateId() {
        return aggregateId;
    }

    /**
     * Get the event type (e.g., "OrderCreated", "StockUpdated")
     * Can be overridden by subclasses
     */
    public String getEventType() {
        return eventType;
    }

    // ============ UTILITY METHODS ============

    /**
     * Check if this event occurred after another event
     */
    public boolean occurredAfter(DomainEvent other) {
        return this.occurredOn.isAfter(other.occurredOn);
    }

    /**
     * Check if this event occurred before another event
     */
    public boolean occurredBefore(DomainEvent other) {
        return this.occurredOn.isBefore(other.occurredOn);
    }

    /**
     * Get the time difference between this event and another
     */
    public long timeDifferenceInSeconds(DomainEvent other) {
        return java.time.Duration.between(this.occurredOn, other.occurredOn).getSeconds();
    }

    // ============ OVERRIDES ============

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEvent that = (DomainEvent) o;
        return eventId.equals(that.eventId);
    }

    @Override
    public int hashCode() {
        return eventId.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s{eventId=%s, aggregateId=%s, occurredOn=%s}",
                getEventType(), eventId, aggregateId, occurredOn);
    }
}