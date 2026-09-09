package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.messaging;

import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptCompletedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptCreatedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptPartialEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptRejectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * In-process domain event listener for Goods Receipt events.
 * Active when app.messaging.type=spring (or when not explicitly set).
 */
@Component
@ConditionalOnProperty(name = "app.messaging.type", havingValue = "spring", matchIfMissing = true)
public class GoodsReceiptEventListener {

    private static final Logger log = LoggerFactory.getLogger(GoodsReceiptEventListener.class);

    @EventListener
    public void handleCreated(GoodsReceiptCreatedEvent event) {
        log.info("📥 [Spring Event Received] GoodsReceiptCreatedEvent: receiptCode={}, id={}",
                event.getReceiptCode(), event.getGoodsReceiptId());
    }

    @EventListener
    public void handleCompleted(GoodsReceiptCompletedEvent event) {
        log.info("📥 [Spring Event Received] GoodsReceiptCompletedEvent: receiptCode={}, id={}",
                event.getReceiptCode(), event.getGoodsReceiptId());
    }

    @EventListener
    public void handlePartial(GoodsReceiptPartialEvent event) {
        log.info("📥 [Spring Event Received] GoodsReceiptPartialEvent: receiptCode={}, id={}",
                event.getReceiptCode(), event.getGoodsReceiptId());
    }

    @EventListener
    public void handleRejected(GoodsReceiptRejectedEvent event) {
        log.info("📥 [Spring Event Received] GoodsReceiptRejectedEvent: receiptCode={}, id={}",
                event.getReceiptCode(), event.getGoodsReceiptId());
    }
}
