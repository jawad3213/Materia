package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.purchaseOrder.application.mappers.PurchaseOrderMapper;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderConfirmedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderSubmittedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** [T060] One signal per triggering action, with the right content; none on refusal or other actions (US5, FR-015). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PurchaseOrderServiceEventsTest {

    @Mock private PurchaseOrderRepository repository;
    @Mock private PurchaseOrderCodeGeneratorService codeGenerator;
    @Mock private GoodsReceiptUseCase goodsReceiptUseCase;
    @Mock private RequisitionUseCase requisitionUseCase;
    @Mock private PurchaseOrderEventPublisher events;
    @Mock private ReceiverDirectory receiverDirectory;

    private PurchaseOrderService service;

    @BeforeEach
    void setUp() {
        service = new PurchaseOrderService(repository, new PurchaseOrderMapper(), codeGenerator,
                goodsReceiptUseCase, requisitionUseCase, events, receiverDirectory);
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PurchaseOrder stored(OrderStatus status) {
        PurchaseOrder order = anOrder().inStatus(status).build();
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));
        return order;
    }

    @Test
    @DisplayName("submit: exactly one submission signal naming the order, its supplier and the submitter (US5-1)")
    void submit_publishesSubmitted() {
        PurchaseOrder order = stored(OrderStatus.DRAFT);

        service.submit(order.getId(), "buyer-1");

        ArgumentCaptor<PurchaseOrderSubmittedEvent> event = ArgumentCaptor.forClass(PurchaseOrderSubmittedEvent.class);
        verify(events, times(1)).publish(event.capture());
        assertEquals(order.getId(), event.getValue().getPurchaseOrderId());
        assertEquals(order.getOrderCode().getValue(), event.getValue().getOrderCode());
        assertEquals(order.getSupplierId(), event.getValue().getSupplierId());
        assertEquals(order.getSupplierName(), event.getValue().getSupplierName());
        assertEquals("buyer-1", event.getValue().getSubmittedBy());
    }

    @Test
    @DisplayName("confirm: exactly one confirmation signal naming the order, its supplier and the confirmer (US5-2)")
    void confirm_publishesConfirmed() {
        PurchaseOrder order = stored(OrderStatus.SUBMITTED);

        service.confirm(order.getId(), "admin-1");

        ArgumentCaptor<PurchaseOrderConfirmedEvent> event = ArgumentCaptor.forClass(PurchaseOrderConfirmedEvent.class);
        verify(events, times(1)).publish(event.capture());
        assertEquals(order.getId(), event.getValue().getPurchaseOrderId());
        assertEquals(order.getSupplierName(), event.getValue().getSupplierName());
        assertEquals("admin-1", event.getValue().getConfirmedBy());
    }

    @Test
    @DisplayName("refusal: a refused submission or confirmation emits nothing (US5-4)")
    void refusedTransitions_publishNothing() {
        PurchaseOrder completed = stored(OrderStatus.COMPLETED);

        assertThrows(RuntimeException.class, () -> service.submit(completed.getId(), "buyer-1"));
        assertThrows(RuntimeException.class, () -> service.confirm(completed.getId(), "admin-1"));

        verifyNoInteractions(events);
    }

    @Test
    @DisplayName("other actions: rejecting, cancelling, completing and tracking emit no order signal")
    void otherActions_publishNothing() {
        service.reject(stored(OrderStatus.SUBMITTED).getId(), "admin-1", "No stock");
        service.cancel(stored(OrderStatus.CONFIRMED).getId(), "buyer-1", "x");
        service.complete(stored(OrderStatus.PARTIALLY_RECEIVED).getId(), "buyer-1");
        service.updateDeliveryStatus(stored(OrderStatus.CONFIRMED).getId(), "SHIPPED", "buyer-1");

        verifyNoInteractions(events);
    }
}
