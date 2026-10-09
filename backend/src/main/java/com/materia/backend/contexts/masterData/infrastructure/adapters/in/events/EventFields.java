package com.materia.backend.contexts.masterData.infrastructure.adapters.in.events;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/** Reads optional fields of an event payload. */
final class EventFields {

    private EventFields() {
    }

    static String text(JsonNode payload, String field) {
        JsonNode node = payload.path(field);
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    static UUID uuid(JsonNode payload, String field) {
        String value = text(payload, field);
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }
}
