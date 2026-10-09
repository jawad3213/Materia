package com.materia.backend.common.infrastructure.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.common.domain.DomainEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transactional outbox: records every domain event in {@code outbox_messages} in the transaction of the change
 * that raised it, so an event is stored if and only if the change commits. {@link OutboxRelay} publishes the
 * stored events to the message broker.
 *
 * <p>In-process listeners keep receiving the events as before; this only adds the durable copy other services
 * will consume once the contexts are split.
 */
@Component
public class OutboxRecorder {

    private static final Pattern CONTEXT_PACKAGE = Pattern.compile("^com\\.materia\\.backend\\.contexts\\.([^.]+)\\..*$");

    private final SpringDataOutboxRepository outbox;
    private final ObjectMapper objectMapper;

    public OutboxRecorder(SpringDataOutboxRepository outbox, ObjectMapper objectMapper) {
        this.outbox = outbox;
        this.objectMapper = objectMapper;
    }

    /** Runs just before the transaction commits; an event raised outside a transaction is recorded on its own. */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void record(DomainEvent event) {
        outbox.save(new OutboxMessageJpaEntity(
                event.getEventId(),
                event.getEventType(),
                sourceContextOf(event),
                event.getAggregateId() != null ? String.valueOf(event.getAggregateId()) : null,
                toJson(event),
                event.getOccurredOn()));
    }

    public static String sourceContextOf(DomainEvent event) {
        Matcher matcher = CONTEXT_PACKAGE.matcher(event.getClass().getName());
        return matcher.matches() ? matcher.group(1) : "common";
    }

    private String toJson(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            // Failing here rolls the change back: an event that cannot be stored must not be lost silently.
            throw new IllegalStateException("Cannot serialize " + event.getEventType() + " for the outbox", e);
        }
    }
}
