package com.materia.backend.common.infrastructure.messaging.outbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Publishes the events recorded by {@link OutboxRecorder} to Kafka, oldest first, on the topic of the context that
 * raised them ({@code materia.<context>.events}), keyed by aggregate id so one aggregate's events stay in order.
 *
 * <p>Delivery is at least once: a message is marked published only after the broker acknowledged it, so consumers
 * must ignore an {@code eventId} they already processed. Off by default; enable with
 * {@code app.outbox.relay.enabled=true} together with {@code app.messaging.type=kafka}.
 *
 * <p>It runs on its own thread rather than through {@code @Scheduled}, which this application does not enable.
 */
@Component
@ConditionalOnProperty(name = "app.outbox.relay.enabled", havingValue = "true")
public class OutboxRelay implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final TypeReference<Map<String, Object>> JSON_OBJECT = new TypeReference<>() { };

    private final SpringDataOutboxRepository outbox;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TransactionTemplate transactions;
    private final ObjectMapper objectMapper;
    private final long intervalMs;
    private final int batchSize;
    private final long sendTimeoutMs;
    private ScheduledExecutorService executor;

    public OutboxRelay(SpringDataOutboxRepository outbox,
                       KafkaTemplate<String, Object> kafkaTemplate,
                       TransactionTemplate transactions,
                       ObjectMapper objectMapper,
                       @Value("${app.outbox.relay.interval-ms:2000}") long intervalMs,
                       @Value("${app.outbox.relay.batch-size:100}") int batchSize,
                       @Value("${app.outbox.relay.send-timeout-ms:10000}") long sendTimeoutMs) {
        this.outbox = outbox;
        this.kafkaTemplate = kafkaTemplate;
        this.transactions = transactions;
        this.objectMapper = objectMapper;
        this.intervalMs = intervalMs;
        this.batchSize = batchSize;
        this.sendTimeoutMs = sendTimeoutMs;
    }

    /** Relays one batch; returns how many messages were published. Stops at the first failure to keep the order. */
    public int relayBatch() {
        List<OutboxMessageJpaEntity> pending = transactions.execute(status -> outbox.findPending(PageRequest.of(0, batchSize)));
        int published = 0;
        for (OutboxMessageJpaEntity message : pending != null ? pending : List.<OutboxMessageJpaEntity>of()) {
            try {
                kafkaTemplate.send(topicOf(message), message.getAggregateId(), envelope(message))
                        .get(sendTimeoutMs, TimeUnit.MILLISECONDS);
                message.markPublished();
                transactions.executeWithoutResult(status -> outbox.save(message));
                published++;
            } catch (Exception e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                log.warn("Outbox relay could not publish {} {}: {}", message.getEventType(), message.getEventId(), e.toString());
                message.markFailed(e.toString());
                transactions.executeWithoutResult(status -> outbox.save(message));
                break;
            }
        }
        return published;
    }

    static String topicOf(OutboxMessageJpaEntity message) {
        return "materia." + message.getSourceContext() + ".events";
    }

    private Map<String, Object> envelope(OutboxMessageJpaEntity message) throws Exception {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", message.getEventId().toString());
        envelope.put("eventType", message.getEventType());
        envelope.put("sourceContext", message.getSourceContext());
        envelope.put("aggregateId", message.getAggregateId());
        envelope.put("occurredAt", message.getOccurredAt().toString());
        envelope.put("payload", objectMapper.readValue(message.getPayload(), JSON_OBJECT));
        return envelope;
    }

    @Override
    public synchronized void start() {
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "outbox-relay");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(() -> {
            try {
                relayBatch();
            } catch (RuntimeException e) {
                log.error("Outbox relay batch failed", e);
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        log.info("Outbox relay started (every {} ms, batches of {})", intervalMs, batchSize);
    }

    @Override
    public synchronized void stop() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return executor != null;
    }
}
