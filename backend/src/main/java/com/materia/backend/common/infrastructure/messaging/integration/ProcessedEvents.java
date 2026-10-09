package com.materia.backend.common.infrastructure.messaging.integration;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** The consumers' inbox: which events each consumer already applied. */
@Component
public class ProcessedEvents {

    private final JdbcTemplate jdbc;

    public ProcessedEvents(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Records that {@code consumer} applies this event now; false when it already did. Call it in the transaction of
     * the effect, so the record and the effect commit (or roll back) together.
     */
    public boolean markProcessed(String consumer, UUID eventId, String eventType) {
        return jdbc.update("insert into processed_events (consumer, event_id, event_type) values (?, ?, ?) "
                + "on conflict (consumer, event_id) do nothing", consumer, eventId, eventType) == 1;
    }
}
