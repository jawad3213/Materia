package com.materia.backend.common.infrastructure.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Pending outbox messages are published in order, once acknowledged, and a failure stops the batch. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OutboxRelayTest {

    @Mock private SpringDataOutboxRepository outbox;
    @Mock private KafkaTemplate<String, Object> kafka;
    @Mock private PlatformTransactionManager transactionManager;

    private OutboxRelay relay;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        relay = new OutboxRelay(outbox, kafka, new TransactionTemplate(transactionManager), new ObjectMapper(),
                1000, 10, 1000);
    }

    private static OutboxMessageJpaEntity message(String context, String aggregateId) {
        return new OutboxMessageJpaEntity(UUID.randomUUID(), "SomethingHappened", context, aggregateId,
                "{\"code\":\"X-1\"}", LocalDateTime.now());
    }

    @Test
    @DisplayName("relay: each message goes to its context topic, keyed by aggregate, and is then marked published")
    @SuppressWarnings("unchecked")
    void publishesToContextTopic() {
        OutboxMessageJpaEntity first = message("purchaseOrder", "po-1");
        OutboxMessageJpaEntity second = message("invoice", "inv-1");
        when(outbox.findPending(any())).thenReturn(List.of(first, second));
        when(kafka.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        assertEquals(2, relay.relayBatch());

        ArgumentCaptor<Object> envelope = ArgumentCaptor.forClass(Object.class);
        verify(kafka).send(eq("materia.purchaseOrder.events"), eq("po-1"), envelope.capture());
        verify(kafka).send(eq("materia.invoice.events"), eq("inv-1"), any());
        Map<String, Object> sent = (Map<String, Object>) envelope.getValue();
        assertEquals(first.getEventId().toString(), sent.get("eventId"));
        assertEquals(Map.of("code", "X-1"), sent.get("payload"));
        assertNotNull(first.getPublishedAt());
        assertNotNull(second.getPublishedAt());
    }

    @Test
    @DisplayName("relay: a message the broker refuses stays pending, records the error and stops the batch")
    void failureStopsTheBatch() {
        OutboxMessageJpaEntity first = message("purchaseOrder", "po-1");
        OutboxMessageJpaEntity second = message("purchaseOrder", "po-1");
        when(outbox.findPending(any())).thenReturn(List.of(first, second));
        when(kafka.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker down")));

        assertEquals(0, relay.relayBatch());

        assertNull(first.getPublishedAt());
        assertEquals(1, first.getAttempts());
        assertTrue(first.getLastError().contains("broker down"));
        verify(kafka, times(1)).send(anyString(), anyString(), any());
        assertEquals(0, second.getAttempts());
    }
}
