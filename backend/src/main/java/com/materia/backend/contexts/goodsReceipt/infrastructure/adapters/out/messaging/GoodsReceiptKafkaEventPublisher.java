package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.messaging;

import com.materia.backend.common.infrastructure.messaging.BaseEventPublisher;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptCompletedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptCreatedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptPartialEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptRejectedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Kafka adapter for the goods receipt event publishing port. Active when app.messaging.type=kafka. */
@Component
@ConditionalOnProperty(name = "app.messaging.type", havingValue = "kafka")
public class GoodsReceiptKafkaEventPublisher implements GoodsReceiptEventPublisher {

    private final BaseEventPublisher eventPublisher;

    public GoodsReceiptKafkaEventPublisher(BaseEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(GoodsReceiptCreatedEvent event) {
        eventPublisher.publish(event);
    }

    @Override
    public void publish(GoodsReceiptCompletedEvent event) {
        eventPublisher.publish(event);
    }

    @Override
    public void publish(GoodsReceiptPartialEvent event) {
        eventPublisher.publish(event);
    }

    @Override
    public void publish(GoodsReceiptRejectedEvent event) {
        eventPublisher.publish(event);
    }
}
