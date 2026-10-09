package com.materia.backend.common.infrastructure.messaging.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Consumes the envelopes {@code OutboxRelay} publishes on {@code materia.<context>.events} and hands them to the
 * {@link IntegrationEventHandler}s. Messages are read as plain JSON text: no Java type named by a message is ever
 * instantiated. A handler failure is retried a few times; {@link ProcessedEvents} makes the retries safe.
 */
@Configuration
@ConditionalOnProperty(name = "app.messaging.type", havingValue = "kafka")
public class KafkaIntegrationEventConsumer {

    private final IntegrationEventDispatcher dispatcher;
    private final ObjectMapper objectMapper;

    public KafkaIntegrationEventConsumer(IntegrationEventDispatcher dispatcher, ObjectMapper objectMapper) {
        this.dispatcher = dispatcher;
        this.objectMapper = objectMapper;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> integrationEventListenerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${app.events.consumer-group:materia}") String groupId,
            @Value("${spring.kafka.listener.auto-startup:true}") boolean autoStartup) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
                new StringDeserializer(), new StringDeserializer()));
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(2_000L, 4)));
        factory.setAutoStartup(autoStartup);
        return factory;
    }

    @KafkaListener(topicPattern = "materia\\..*\\.events", containerFactory = "integrationEventListenerFactory")
    public void consume(String message) throws Exception {
        IntegrationEvent event = parse(message, objectMapper);
        List<String> failed = dispatcher.dispatch(event);
        if (!failed.isEmpty()) {
            // Redelivered by the error handler; the handlers that already succeeded skip it then.
            throw new IllegalStateException("Event " + event.eventId() + " failed in " + failed);
        }
    }

    /** Reads an envelope written by OutboxRelay. */
    static IntegrationEvent parse(String message, ObjectMapper objectMapper) throws Exception {
        JsonNode envelope = objectMapper.readTree(message);
        JsonNode aggregateId = envelope.path("aggregateId");
        return new IntegrationEvent(
                UUID.fromString(envelope.path("eventId").asText()),
                envelope.path("eventType").asText(),
                envelope.path("sourceContext").asText(),
                aggregateId.isMissingNode() || aggregateId.isNull() ? null : aggregateId.asText(),
                envelope.path("occurredAt").asText(null),
                envelope.path("payload"));
    }
}
