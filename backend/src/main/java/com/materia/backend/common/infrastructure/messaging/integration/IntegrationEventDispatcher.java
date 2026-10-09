package com.materia.backend.common.infrastructure.messaging.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Hands an event to every handler that wants it, each in its own new transaction that first records the event in
 * {@link ProcessedEvents}: a repeated delivery is skipped, and a failed handler leaves no record, so a redelivery
 * applies it again.
 */
@Component
public class IntegrationEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(IntegrationEventDispatcher.class);

    private final List<IntegrationEventHandler> handlers;
    private final ProcessedEvents processedEvents;
    private final TransactionTemplate newTransaction;

    public IntegrationEventDispatcher(List<IntegrationEventHandler> handlers, ProcessedEvents processedEvents,
                                      PlatformTransactionManager transactionManager) {
        this.handlers = handlers;
        this.processedEvents = processedEvents;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** @return the handlers that failed (their effect rolled back and the event stays unprocessed for them) */
    public List<String> dispatch(IntegrationEvent event) {
        return handlers.stream()
                .filter(handler -> handler.handles(event))
                .filter(handler -> !deliver(handler, event))
                .map(IntegrationEventHandler::consumerName)
                .toList();
    }

    /**
     * Delivers inside the caller's transaction, letting a handler failure propagate. Only for the {@code immediate}
     * in-process mode, which tests use so that a rolled-back test transaction still sees the handlers' effects.
     */
    public void dispatchInCurrentTransaction(IntegrationEvent event) {
        for (IntegrationEventHandler handler : handlers) {
            if (handler.handles(event)
                    && processedEvents.markProcessed(handler.consumerName(), event.eventId(), event.eventType())) {
                handler.handle(event);
            }
        }
    }

    private boolean deliver(IntegrationEventHandler handler, IntegrationEvent event) {
        try {
            newTransaction.executeWithoutResult(status -> {
                if (processedEvents.markProcessed(handler.consumerName(), event.eventId(), event.eventType())) {
                    handler.handle(event);
                } else {
                    log.debug("{} already applied event {} ({}): skipped", handler.consumerName(), event.eventId(),
                            event.eventType());
                }
            });
            return true;
        } catch (RuntimeException e) {
            log.error("{} failed on event {} ({} from {})", handler.consumerName(), event.eventId(), event.eventType(),
                    event.sourceContext(), e);
            return false;
        }
    }
}
