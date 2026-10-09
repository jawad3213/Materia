package com.materia.backend.support.fixtures;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.common.domain.DomainEvent;
import com.materia.backend.common.infrastructure.messaging.integration.IntegrationEvent;
import com.materia.backend.common.infrastructure.messaging.integration.IntegrationEventHandler;
import com.materia.backend.common.infrastructure.messaging.integration.InProcessIntegrationEvents;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * For unit tests without Spring: hands each published domain event at once to the registered integration event
 * handlers, in the JSON envelope they receive in production. Events a handler publishes in turn go round the same way.
 */
public final class InlineEvents implements ApplicationEventPublisher {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final List<IntegrationEventHandler> handlers = new ArrayList<>();
    private final List<DomainEvent> published = new ArrayList<>();

    public InlineEvents add(IntegrationEventHandler handler) {
        handlers.add(handler);
        return this;
    }

    public List<DomainEvent> published() {
        return published;
    }

    @Override
    public void publishEvent(Object event) {
        if (!(event instanceof DomainEvent domainEvent)) return;
        published.add(domainEvent);
        IntegrationEvent integrationEvent = InProcessIntegrationEvents.toIntegrationEvent(domainEvent, objectMapper);
        for (IntegrationEventHandler handler : List.copyOf(handlers)) {
            if (handler.handles(integrationEvent)) handler.handle(integrationEvent);
        }
    }
}
