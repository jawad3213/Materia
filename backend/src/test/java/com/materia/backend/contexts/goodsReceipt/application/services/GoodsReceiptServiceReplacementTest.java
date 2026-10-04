package com.materia.backend.contexts.goodsReceipt.application.services;

import com.materia.backend.contexts.goodsReceipt.application.mappers.GoodsReceiptMapper;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptBusinessException;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptEventPublisher;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.ReplacedReturnQuantities;
import com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.DEFAULT_RECEIVER_ID;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Goods returned for replacement are expected again: a reopened order receives exactly the replacement. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoodsReceiptServiceReplacementTest {

    @Mock private GoodsReceiptRepository receipts;
    @Mock private GoodsReceiptEventPublisher receiptEvents;
    @Mock private GoodsReceiptCodeGeneratorService codeGenerator;
    @Mock private PurchaseOrderRepository orders;
    @Mock private MaterialRepository materials;
    @Mock private PurchaseOrderEventPublisher orderEvents;

    private GoodsReceiptService service;
    private PurchaseOrder order;
    private final List<GoodsReceipt> saved = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new GoodsReceiptService(receipts, new GoodsReceiptMapper(), receiptEvents, codeGenerator,
                orders, materials, orderEvents);
        when(codeGenerator.generateCode()).thenReturn(ReceiptCode.of("GR-2026-0010"));
        when(receipts.save(any(GoodsReceipt.class))).thenAnswer(inv -> {
            GoodsReceipt receipt = inv.getArgument(0);
            saved.add(receipt);
            when(receipts.findById(receipt.getId())).thenReturn(Optional.of(receipt));
            return receipt;
        });
        when(orders.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        Material material = aMaterial().stock(6).build();
        when(materials.findById(any())).thenReturn(Optional.of(material));
        when(materials.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));

        // 10 ordered, all received on an earlier receipt with 4 rejected; the order was reopened for a replacement.
        order = anOrder().withLine(10, "5.00").inStatus(OrderStatus.PARTIALLY_RECEIVED).build();
        GoodsReceipt earlier = aReceipt(order).receiving(0, 10, 4, "Scratched").build();
        earlier.complete(DEFAULT_RECEIVER_ID);
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));
        when(receipts.findByPurchaseOrderId(order.getId().toString())).thenReturn(List.of(earlier));
    }

    private String orderLine() {
        return order.getLines().get(0).getId().toString();
    }

    @Test
    @DisplayName("replacement: the replaced quantity is outstanding again and receiving it completes the order")
    void replacedQuantity_isReceivedAgain() {
        service.setReplacedReturnQuantities(purchaseOrderId -> Map.of(orderLine(), 4));

        service.receiveRemaining(order.getId().toString(), DEFAULT_RECEIVER_ID, "Rita");

        GoodsReceipt replacement = saved.get(saved.size() - 1);
        assertEquals(4, replacement.getLines().get(0).getQuantityReceived());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    @DisplayName("replacement: without a replacement pending, nothing is outstanding on a fully received line")
    void noReplacement_nothingOutstanding() {
        service.setReplacedReturnQuantities(null);

        assertThrows(GoodsReceiptBusinessException.class,
                () -> service.receiveRemaining(order.getId().toString(), DEFAULT_RECEIVER_ID, "Rita"));
        assertTrue(saved.isEmpty());
    }

    @Test
    @DisplayName("replacement: a replacement for another line does not reopen this one")
    void replacementOnAnotherLine_isIgnored() {
        service.setReplacedReturnQuantities(purchaseOrderId -> Map.of("another-line", 4));

        assertThrows(GoodsReceiptBusinessException.class,
                () -> service.receiveRemaining(order.getId().toString(), DEFAULT_RECEIVER_ID, "Rita"));
    }

    @Test
    @DisplayName("replacement: the default lookup reports no replacement")
    void defaultLookup_isEmpty() {
        assertTrue(ReplacedReturnQuantities.NONE.byPurchaseOrderLine("po-1").isEmpty());
    }
}
