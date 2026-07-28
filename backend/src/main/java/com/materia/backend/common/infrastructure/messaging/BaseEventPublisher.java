package com.materia.backend.common.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.materia.backend.common.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Base Event Publisher for publishing domain events to Kafka
 * Implements event publishing with retry and error handling
 */
@Component
public class BaseEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BaseEventPublisher.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public BaseEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Publish a domain event to the appropriate topic
     */
    public void publish(DomainEvent event) {
        try {
            String topic = determineTopic(event);
            String key = getEventKey(event);
            String message = objectMapper.writeValueAsString(event);

            log.info("Publishing event: {} (ID: {}) to topic: {} with key: {}",
                    event.getEventType(), event.getEventId(), topic, key);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Event published successfully: {} to partition {} with offset {}",
                            event.getEventType(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish event: {}", event.getEventType(), ex);
                    // Could send to dead letter queue or retry
                    sendToDeadLetter(event, ex);
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Error serializing event: {}", event.getEventType(), e);
        } catch (Exception e) {
            log.error("Error publishing event: {}", event.getEventType(), e);
            sendToDeadLetter(event, e);
        }
    }

    /**
     * Determine the Kafka topic based on event type
     * Business logic: routes events to appropriate topics
     */
    private String determineTopic(DomainEvent event) {
        String eventName = event.getEventType();

        if (eventName.contains("Auth") || eventName.contains("User") || eventName.contains("Login")) {
            return "auth-events";
        } else if (eventName.contains("Order")) {
            return "order-events";
        } else if (eventName.contains("Stock") || eventName.contains("Inventory")) {
            return "stock-events";
        } else if (eventName.contains("Catalog") || eventName.contains("Product")) {
            return "catalog-events";
        } else if (eventName.contains("Invoice")) {
            return "invoice-events";
        } else if (eventName.contains("Notification")) {
            return "notification-events";
        } else if (eventName.contains("Receipt")) {
            return "receipt-events";
        } else if (eventName.contains("Requisition")) {
            return "requisition-events";
        } else if (eventName.contains("Analytics")) {
            return "analytics-events";
        }

        return "default-events";
    }

    /**
     * Publish with custom topic
     */
    public void publishToTopic(String topic, DomainEvent event) {
        try {
            String key = getEventKey(event);
            String message = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(topic, key, message);
            log.info("Published event {} (ID: {}) to custom topic: {}", event.getEventType(), event.getEventId(), topic);
        } catch (JsonProcessingException e) {
            log.error("Error serializing event {} for custom topic: {}", event.getEventType(), topic, e);
        } catch (Exception e) {
            log.error("Error publishing {} to custom topic: {}", event.getEventType(), topic, e);
        }
    }

    /**
     * Send to dead letter queue when processing fails
     */
    private void sendToDeadLetter(DomainEvent event, Throwable error) {
        try {
            String key = getEventKey(event);
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("dead-letter-events", key, message);
            log.warn("Event sent to dead letter queue: {}", event.getEventType());
        } catch (JsonProcessingException e) {
            log.error("Error sending event {} to dead letter queue", event.getEventType(), e);
        }
    }

    /**
     * Publish multiple events
     */
    public void publishAll(java.util.List<DomainEvent> events) {
        events.forEach(this::publish);
    }
    
    /**
     * Helper to safely extract the Kafka key from the event.
     * Prefers AggregateId for partition ordering, falls back to EventId.
     */
    private String getEventKey(DomainEvent event) {
        return event.getAggregateId() != null 
                ? event.getAggregateId().toString() 
                : event.getEventId().toString();
    }
}