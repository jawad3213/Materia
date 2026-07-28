package com.materia.backend.common.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.materia.backend.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Base Event Consumer for consuming domain events from Kafka
 * Provides a registry for event handlers
 */
@Component
public class BaseEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BaseEventConsumer.class);
    private final ObjectMapper objectMapper;
    private final Map<Class<? extends DomainEvent>, Consumer<DomainEvent>> handlers = new ConcurrentHashMap<>();

    public BaseEventConsumer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Register a handler for a specific event type
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void registerHandler(Class<T> eventClass, Consumer<T> handler) {
        handlers.put(eventClass, event -> handler.accept((T) event));
        log.info("Registered handler for event: {}", eventClass.getSimpleName());
    }

    /**
     * Unregister a handler
     */
    public void unregisterHandler(Class<? extends DomainEvent> eventClass) {
        handlers.remove(eventClass);
        log.info("Unregistered handler for event: {}", eventClass.getSimpleName());
    }

    /**
     * Check if a handler exists for an event
     */
    public boolean hasHandler(Class<? extends DomainEvent> eventClass) {
        return handlers.containsKey(eventClass);
    }

    /**
     * Kafka listener for all domain events across all bounded contexts
     */
    @KafkaListener(
            topics = {
                "auth-events", "order-events", "stock-events", 
                "catalog-events", "invoice-events", "notification-events", 
                "receipt-events", "requisition-events", "analytics-events"
            },
            groupId = "${spring.kafka.consumer.group-id:default-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDomainEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        consumeEvent(message, topic, offset, partition);
    }

    /**
     * Kafka listener for dead letter events
     */
    @KafkaListener(
            topics = "dead-letter-events",
            groupId = "${spring.kafka.consumer.group-id:default-group}-dlq",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDeadLetterEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.warn("Received dead letter message from topic: {}", topic);
        // Handle dead letter messages - log, alert, manual intervention
        // Could store in database for manual review
        try {
            DomainEvent event = objectMapper.readValue(message, DomainEvent.class);
            log.error("Dead letter event: {}", event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Error processing dead letter message", e);
        }
    }

    /**
     * Generic event consumption logic
     */
    private void consumeEvent(String message, String topic, long offset, int partition) {
        try {
            log.debug("Received message from topic: {}, partition: {}, offset: {}",
                    topic, partition, offset);

            // Deserialize event - need to determine the concrete class
            // This is simplified - in production you'd use a type resolver
            DomainEvent event = objectMapper.readValue(message, DomainEvent.class);

            // Find and invoke handler
            Class<?> eventClass = event.getClass();
            Consumer<DomainEvent> handler = handlers.get(eventClass);

            if (handler != null) {
                handler.accept(event);
                log.info("Event processed: {} from topic: {}",
                        eventClass.getSimpleName(), topic);
            } else {
                log.warn("No handler registered for event: {} from topic: {}",
                        eventClass.getSimpleName(), topic);
                // Could store unhandled events for later processing
            }

        } catch (Exception e) {
            log.error("Error processing event from topic: {}, offset: {}", topic, offset, e);
            // Send to dead letter queue or handle accordingly
        }
    }

    /**
     * Get registered handlers count
     */
    public int getHandlerCount() {
        return handlers.size();
    }

    /**
     * Clear all handlers
     */
    public void clearHandlers() {
        handlers.clear();
        log.info("All handlers cleared");
    }
}