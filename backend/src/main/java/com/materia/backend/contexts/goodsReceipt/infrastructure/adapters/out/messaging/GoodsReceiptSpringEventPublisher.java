package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.messaging;

import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptCompletedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptCreatedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptPartialEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptRejectedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring In-Process Event adapter for the goods receipt event publishing port.
 * Active when app.messaging.type=spring (or when not explicitly set).
 */
@Component
@ConditionalOnProperty(name = "app.messaging.type", havingValue = "spring", matchIfMissing = true)
public class GoodsReceiptSpringEventPublisher implements GoodsReceiptEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(GoodsReceiptSpringEventPublisher.class);
    private final ApplicationEventPublisher eventPublisher;

    public GoodsReceiptSpringEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(GoodsReceiptCreatedEvent event) {
        log.info("📢 [Spring Event Published] GoodsReceiptCreatedEvent for ID: {}", event.getGoodsReceiptId());
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publish(GoodsReceiptCompletedEvent event) {
        log.info("📢 [Spring Event Published] GoodsReceiptCompletedEvent for ID: {}", event.getGoodsReceiptId());
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publish(GoodsReceiptPartialEvent event) {
        log.info("📢 [Spring Event Published] GoodsReceiptPartialEvent for ID: {}", event.getGoodsReceiptId());
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publish(GoodsReceiptRejectedEvent event) {
        log.info("📢 [Spring Event Published] GoodsReceiptRejectedEvent for ID: {}", event.getGoodsReceiptId());
        eventPublisher.publishEvent(event);
    }
}
