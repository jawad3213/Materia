package com.materia.backend.contexts.goodsReceipt.application.services;

import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.application.mappers.GoodsReceiptMapper;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptBusinessException;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptEventPublisher;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
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
import java.util.Optional;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.DEFAULT_RECEIVER_ID;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** [T036] "Receive everything outstanding" builds and validates a real receipt for exactly what remains (US3-8, US3-9). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoodsReceiptServiceReceiveRemainingTest {

    @Mock private GoodsReceiptRepository receipts;
    @Mock private GoodsReceiptEventPublisher receiptEvents;
    @Mock private GoodsReceiptCodeGeneratorService codeGenerator;
    @Mock private PurchaseOrderRepository orders;
    @Mock private MaterialRepository materials;
    @Mock private PurchaseOrderEventPublisher orderEvents;

    private GoodsReceiptService service;
    private final List<GoodsReceipt> saved = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new GoodsReceiptService(receipts, new GoodsReceiptMapper(), receiptEvents, codeGenerator,
                orders, materials, orderEvents);
        when(codeGenerator.generateCode()).thenReturn(ReceiptCode.of("GR-2026-0009"));
        when(receipts.save(any(GoodsReceipt.class))).thenAnswer(inv -> {
            GoodsReceipt receipt = inv.getArgument(0);
            saved.add(receipt);
            when(receipts.findById(receipt.getId())).thenReturn(Optional.of(receipt));
            return receipt;
        });
        when(orders.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        Material material = aMaterial().stock(0).build();
        when(materials.findById(any())).thenReturn(Optional.of(material));
        when(materials.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PurchaseOrder stored(PurchaseOrder po, List<GoodsReceipt> earlier) {
        when(orders.findById(po.getId())).thenReturn(Optional.of(po));
        when(receipts.findByPurchaseOrderId(po.getId().toString())).thenReturn(earlier);
        return po;
    }

    @Test
    @DisplayName("receive all: one accepted line per order line, for the full outstanding quantity, validated (US3-8)")
    void receiveAll_buildsOutstandingLinesAndValidates() {
        PurchaseOrder po = stored(anOrder().withLine(10, "5.00").withLine(4, "2.00")
                .inStatus(OrderStatus.READY_FOR_RECEIPT).build(), List.of());

        GoodsReceiptOutput out = service.receiveRemaining(po.getId().toString(), DEFAULT_RECEIVER_ID, "Rita");

        GoodsReceipt receipt = saved.get(saved.size() - 1);
        assertEquals(2, receipt.getLines().size());
        assertEquals(List.of(10, 4), receipt.getLines().stream().map(l -> l.getQuantityReceived()).toList());
        assertTrue(receipt.getLines().stream().allMatch(l -> l.getQuantityRejected() == 0));
        assertEquals(ReceiptStatus.COMPLETED.getCode(), out.getStatus());
        assertEquals(OrderStatus.COMPLETED, po.getStatus());
    }

    @Test
    @DisplayName("receive all: lines already fully received are skipped; partly received lines get only the remainder")
    void receiveAll_skipsFullyReceivedLines() {
        PurchaseOrder po = anOrder().withLine(10, "5.00").withLine(4, "2.00").inStatus(OrderStatus.PARTIALLY_RECEIVED).build();
        GoodsReceipt earlier = aReceipt(po).receiving(0, 6, 0, null).receiving(1, 4, 0, null)
                .inStatus(ReceiptStatus.PARTIAL).build();
        stored(po, List.of(earlier));

        service.receiveRemaining(po.getId().toString(), DEFAULT_RECEIVER_ID, "Rita");

        GoodsReceipt receipt = saved.get(saved.size() - 1);
        assertEquals(1, receipt.getLines().size());
        assertEquals(4, receipt.getLines().get(0).getQuantityReceived());
        assertEquals(po.getLines().get(0).getId().toString(), receipt.getLines().get(0).getPurchaseOrderLineId());
        assertEquals(OrderStatus.COMPLETED, po.getStatus());
    }

    @Test
    @DisplayName("F-005: a receipt for exactly what remains expects only the remainder and validates as completed")
    void remainderReceipt_isCompleted() {
        PurchaseOrder po = anOrder().withLine(10, "5.00").inStatus(OrderStatus.PARTIALLY_RECEIVED).build();
        GoodsReceipt earlier = aReceipt(po).receiving(0, 6, 0, null).inStatus(ReceiptStatus.PARTIAL).build();
        stored(po, List.of(earlier));

        GoodsReceiptOutput out = service.receiveRemaining(po.getId().toString(), DEFAULT_RECEIVER_ID, "Rita");

        GoodsReceipt receipt = saved.get(saved.size() - 1);
        assertEquals(4, receipt.getLines().get(0).getQuantityOrdered());
        assertEquals(0, receipt.getLines().get(0).getQuantityPending());
        assertFalse(receipt.hasDiscrepancy());
        assertEquals(ReceiptStatus.COMPLETED.getCode(), out.getStatus());
        assertEquals(OrderStatus.COMPLETED, po.getStatus());
    }

    @Test
    @DisplayName("receive all: nothing left to receive is refused as a rule violation, and nothing is saved (US3-9)")
    void receiveAll_nothingRemaining_isRefused() {
        PurchaseOrder po = anOrder().withLine(10, "5.00").inStatus(OrderStatus.PARTIALLY_RECEIVED).build();
        GoodsReceipt earlier = aReceipt(po).inStatus(ReceiptStatus.COMPLETED).build();
        stored(po, List.of(earlier));

        assertThrows(GoodsReceiptBusinessException.class,
                () -> service.receiveRemaining(po.getId().toString(), DEFAULT_RECEIVER_ID, "Rita"));
        assertTrue(saved.isEmpty());
    }
}
