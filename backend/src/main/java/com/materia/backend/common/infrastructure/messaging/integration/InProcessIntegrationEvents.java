package com.materia.backend.common.infrastructure.messaging.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.common.domain.DomainEvent;
import com.materia.backend.common.infrastructure.messaging.outbox.OutboxRecorder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Delivers domain events to the {@link IntegrationEventHandler}s inside this application, after the publishing
 * transaction committed, in the very envelope {@code OutboxRelay} sends to Kafka. A context extracted as a service
 * turns this off ({@code app.events.in-process.enabled=false}) and receives the same envelopes from Kafka instead.
 *
 * <p>{@code app.events.in-process.mode}: {@code after-commit} (default, as across services) or {@code immediate}
 * (inside the publishing transaction; for tests that roll their transaction back).
 */
@Component
@ConditionalOnProperty(name = "app.events.in-process.enabled", havingValue = "true", matchIfMissing = true)
public class InProcessIntegrationEvents {

    private final IntegrationEventDispatcher dispatcher;
    private final ObjectMapper objectMapper;
    private final boolean immediate;

    public InProcessIntegrationEvents(IntegrationEventDispatcher dispatcher, ObjectMapper objectMapper,
                                      @Value("${app.events.in-process.mode:after-commit}") String mode) {
        this.dispatcher = dispatcher;
        this.objectMapper = objectMapper;
        this.immediate = "immediate".equalsIgnoreCase(mode);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void afterCommit(DomainEvent event) {
        if (!immediate) {
            dispatcher.dispatch(toIntegrationEvent(event, objectMapper));
        }
    }

    @EventListener
    public void inTransaction(DomainEvent event) {
        if (immediate) {
            dispatcher.dispatchInCurrentTransaction(toIntegrationEvent(event, objectMapper));
        }
    }

    /** The envelope of a domain event, with the payload serialized exactly as the outbox stores it. */
    public static IntegrationEvent toIntegrationEvent(DomainEvent event, ObjectMapper objectMapper) {
        return new IntegrationEvent(
                event.getEventId(),
                event.getEventType(),
                OutboxRecorder.sourceContextOf(event),
                event.getAggregateId() != null ? String.valueOf(event.getAggregateId()) : null,
                String.valueOf(event.getOccurredOn()),
                objectMapper.valueToTree(event));
    }
}
