package com.materia.backend.common.infrastructure.messaging.integration;

/**
 * Reacts to another context's events, the way it will once that context is a separate service.
 *
 * <p>Each delivery runs in its own transaction, after the publisher's transaction committed. A handler sees an event
 * at most once (by {@link #consumerName()} and event id), so it does not need to be idempotent itself; it must not
 * rely on the publisher's database or classes, only on the event's JSON payload.
 */
public interface IntegrationEventHandler {

    /** Stable name under which the events this handler applied are recorded; never change it once deployed. */
    String consumerName();

    boolean handles(IntegrationEvent event);

    void handle(IntegrationEvent event);
}
