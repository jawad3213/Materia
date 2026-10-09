package com.materia.backend.common.infrastructure.messaging.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.common.domain.DomainEvent;
import com.materia.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The delivery guarantees handlers rely on: after commit only, at most once per consumer, and again after a failure.
 * Each publication commits for real ({@code NOT_SUPPORTED}); the rows it leaves are removed afterwards.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@org.springframework.test.context.TestPropertySource(properties = "app.events.in-process.mode=after-commit")
class IntegrationEventsIT extends AbstractIntegrationTest {

    /** A domain event of the shared kernel, so its source context is "common". */
    static class ProbeEvent extends DomainEvent {
        private final String note;

        ProbeEvent(String note) {
            super(UUID.randomUUID());
            this.note = note;
        }

        public String getNote() {
            return note;
        }
    }

    static class ProbeHandler implements IntegrationEventHandler {
        final List<IntegrationEvent> received = new ArrayList<>();
        final AtomicBoolean failNext = new AtomicBoolean();

        @Override
        public String consumerName() {
            return "test.probe";
        }

        @Override
        public boolean handles(IntegrationEvent event) {
            return event.is("common", "Probe");
        }

        @Override
        public void handle(IntegrationEvent event) {
            if (failNext.getAndSet(false)) {
                throw new IllegalStateException("simulated failure");
            }
            received.add(event);
        }
    }

    @TestConfiguration
    static class ProbeConfig {
        @Bean
        ProbeHandler probeHandler() {
            return new ProbeHandler();
        }
    }

    @Autowired private ApplicationEventPublisher events;
    @Autowired private org.springframework.transaction.PlatformTransactionManager transactionManager;
    @Autowired private IntegrationEventDispatcher dispatcher;
    @Autowired private ProbeHandler handler;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper objectMapper;

    private final List<UUID> published = new ArrayList<>();

    @BeforeEach
    void reset() {
        handler.received.clear();
        handler.failNext.set(false);
    }

    @AfterEach
    void cleanUp() {
        for (UUID id : published) {
            jdbc.update("delete from processed_events where event_id = ?", id);
            jdbc.update("delete from outbox_messages where event_id = ?", id);
        }
    }

    private ProbeEvent publishInTransaction(String note, boolean rollBack) {
        ProbeEvent event = new ProbeEvent(note);
        published.add(event.getEventId());
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            events.publishEvent(event);
            if (rollBack) status.setRollbackOnly();
        });
        return event;
    }

    @Test
    @DisplayName("events: a committed change's event reaches the handler once, with its JSON payload")
    void committedEvent_isDeliveredOnceAsJson() {
        ProbeEvent event = publishInTransaction("hello", false);

        assertEquals(1, handler.received.size());
        IntegrationEvent received = handler.received.get(0);
        assertEquals(event.getEventId(), received.eventId());
        assertEquals("hello", received.payload().path("note").asText());
    }

    @Test
    @DisplayName("events: delivering the same event again (Kafka redelivery) does not apply it twice")
    void redelivery_isIgnored() {
        ProbeEvent event = publishInTransaction("once", false);

        List<String> failed = dispatcher.dispatch(InProcessIntegrationEvents.toIntegrationEvent(event, objectMapper));

        assertTrue(failed.isEmpty());
        assertEquals(1, handler.received.size(), "the second delivery is skipped");
    }

    @Test
    @DisplayName("events: a rolled-back change's event never reaches the handler")
    void rolledBackEvent_isNotDelivered() {
        publishInTransaction("never", true);

        assertTrue(handler.received.isEmpty());
    }

    @Test
    @DisplayName("events: after a handler failure nothing is recorded, so the next delivery applies the event")
    void failedHandler_isRetried() {
        handler.failNext.set(true);
        ProbeEvent event = publishInTransaction("retry", false);
        assertTrue(handler.received.isEmpty(), "the failed attempt applied nothing");

        List<String> failed = dispatcher.dispatch(InProcessIntegrationEvents.toIntegrationEvent(event, objectMapper));

        assertTrue(failed.isEmpty());
        assertEquals(1, handler.received.size());
    }
}
