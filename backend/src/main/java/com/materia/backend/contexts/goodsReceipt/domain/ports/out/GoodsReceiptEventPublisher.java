package com.materia.backend.contexts.goodsReceipt.domain.ports.out;

import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptCompletedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptCreatedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptPartialEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptRejectedEvent;

/**
 * Outbound port for notifying other modules about goods receipt changes.
 * An infrastructure adapter can implement this with Kafka, Spring events, or another broker.
 */
public interface GoodsReceiptEventPublisher {

    void publish(GoodsReceiptCreatedEvent event);

    void publish(GoodsReceiptCompletedEvent event);

    void publish(GoodsReceiptPartialEvent event);

    void publish(GoodsReceiptRejectedEvent event);
}
