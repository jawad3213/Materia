package com.materia.backend.contexts.goodsReceipt.application.services;

import com.materia.backend.contexts.goodsReceipt.application.mappers.GoodsReceiptMapper;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptEventPublisher;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderDeliveredEvent;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.DEFAULT_RECEIVER_ID;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** [T061] Validating a receipt emits one delivery signal carrying the order's resulting delivery status (US5-3). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoodsReceiptServiceDeliveredEventTest {

    @Mock private GoodsReceiptRepository receipts;
    @Mock private GoodsReceiptEventPublisher receiptEvents;
    @Mock private GoodsReceiptCodeGeneratorService codeGenerator;
    @Mock private PurchaseOrderRepository orders;
    @Mock private MaterialRepository materials;
    @Mock private PurchaseOrderEventPublisher orderEvents;

    private GoodsReceiptService service;
    private PurchaseOrder order;

    @BeforeEach
    void setUp() {
        service = new GoodsReceiptService(receipts, new GoodsReceiptMapper(), receiptEvents, codeGenerator,
                orders, materials, orderEvents);
        when(receipts.save(any(GoodsReceipt.class))).thenAnswer(inv -> inv.getArgument(0));
        when(receipts.findByPurchaseOrderId(any())).thenReturn(List.of());
        when(orders.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(materials.findById(any())).thenReturn(Optional.of(aMaterial().stock(0).build()));
        when(materials.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));
        order = anOrder().withLine(10, "5.00").inStatus(OrderStatus.READY_FOR_RECEIPT).build();
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));
    }

    private PurchaseOrderDeliveredEvent validate(GoodsReceipt receipt) {
        when(receipts.findById(receipt.getId())).thenReturn(Optional.of(receipt));
        service.complete(receipt.getId(), DEFAULT_RECEIVER_ID);
        ArgumentCaptor<PurchaseOrderDeliveredEvent> event = ArgumentCaptor.forClass(PurchaseOrderDeliveredEvent.class);
        verify(orderEvents, times(1)).publish(event.capture());
        return event.getValue();
    }

    @Test
    @DisplayName("delivery signal: a full receipt signals DELIVERED for the order, by the validating user")
    void fullReceipt_signalsDelivered() {
        PurchaseOrderDeliveredEvent event = validate(aReceipt(order).build());

        assertEquals(order.getId(), event.getPurchaseOrderId());
        assertEquals(order.getOrderCode().getValue(), event.getOrderCode());
        assertEquals(DeliveryStatus.DELIVERED, event.getDeliveryStatus());
        assertEquals(DEFAULT_RECEIVER_ID, event.getUpdatedBy());
    }

    @Test
    @DisplayName("delivery signal: a partial receipt signals PARTIAL")
    void partialReceipt_signalsPartial() {
        assertEquals(DeliveryStatus.PARTIAL, validate(aReceipt(order).receiving(0, 4, 0, null).build()).getDeliveryStatus());
    }

    @Test
    @DisplayName("delivery signal: a refused validation emits no signal on either publisher (US5-4)")
    void refusedValidation_signalsNothing() {
        GoodsReceipt receipt = aReceipt(order).build();
        when(receipts.findById(receipt.getId())).thenReturn(Optional.of(receipt));

        assertThrows(RuntimeException.class, () -> service.complete(receipt.getId(), "someone-else"));

        verifyNoInteractions(orderEvents);
        verifyNoInteractions(receiptEvents);
    }
}
