package com.materia.backend.contexts.goodsReceipt.application.services;

import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.UpdateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.mappers.GoodsReceiptMapper;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptBusinessException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptValidationException;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptEventPublisher;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.DEFAULT_RECEIVER_ID;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** [T084] Goods receipt guard paths: codes, unchanged orders, missing callers and receivers (US6). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoodsReceiptServiceGuardsTest {

    private static final String OWNER = DEFAULT_RECEIVER_ID;

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
        when(codeGenerator.generateCode()).thenReturn(ReceiptCode.of("GR-2026-0200"));
        order = anOrder().withLine(10, "5.00").inStatus(OrderStatus.READY_FOR_RECEIPT).build();
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));
    }

    private GoodsReceipt stored() {
        GoodsReceipt receipt = aReceipt(order).inStatus(ReceiptStatus.DRAFT).build();
        when(receipts.findById(receipt.getId())).thenReturn(Optional.of(receipt));
        return receipt;
    }

    private CreateGoodsReceiptInput createInput(String code) {
        PurchaseOrderLine orderLine = order.getLines().get(0);
        GoodsReceiptLineInput line = new GoodsReceiptLineInput();
        line.setPurchaseOrderLineId(orderLine.getId().toString());
        line.setMaterialCode(orderLine.getMaterialCode());
        line.setQuantityReceived(3);
        line.setQuantityRejected(0);
        line.setQualityStatus("ACCEPTED");
        CreateGoodsReceiptInput in = new CreateGoodsReceiptInput();
        in.setReceiptCode(code);
        in.setPurchaseOrderId(order.getId().toString());
        in.setReceivedBy(OWNER);
        in.setReceivedByName("Receiver One");
        in.setUserId(OWNER);
        in.setLines(new ArrayList<>(List.of(line)));
        return in;
    }

    @Test
    @DisplayName("create: a free code given by the caller is used as is, trimmed")
    void create_withFreeCode_usesIt() {
        assertEquals("GR-2026-0999", service.create(createInput(" GR-2026-0999 ")).getReceiptCode());
        verify(codeGenerator, never()).generateCode();
    }

    @Test
    @DisplayName("create: a code already used is refused as a conflict; a blank code is generated")
    void create_codeRules() {
        when(receipts.existsByReceiptCode("GR-2026-0998")).thenReturn(true);
        assertThrows(GoodsReceiptBusinessException.class, () -> service.create(createInput("GR-2026-0998")));

        assertEquals("GR-2026-0200", service.create(createInput(" ")).getReceiptCode());
    }

    @Test
    @DisplayName("update: resending the same order and the receipt's own code is accepted; a blank code is ignored")
    void update_sameOrderAndCode_areAccepted() {
        GoodsReceipt receipt = stored();
        UpdateGoodsReceiptInput same = new UpdateGoodsReceiptInput();
        same.setPurchaseOrderId(receipt.getPurchaseOrderId());
        same.setReceiptCode(receipt.getReceiptCode().getValue());
        same.setUserId(OWNER);
        assertDoesNotThrow(() -> service.update(receipt.getId(), same));
        verify(receipts, never()).existsByReceiptCode(any());

        UpdateGoodsReceiptInput blank = new UpdateGoodsReceiptInput();
        blank.setReceiptCode(" ");
        blank.setUserId(OWNER);
        assertDoesNotThrow(() -> service.update(receipt.getId(), blank));
    }

    @Test
    @DisplayName("update: a new code that is free is accepted")
    void update_newFreeCode_isAccepted() {
        GoodsReceipt receipt = stored();
        UpdateGoodsReceiptInput input = new UpdateGoodsReceiptInput();
        input.setReceiptCode("GR-2026-0555");
        input.setUserId(OWNER);

        assertDoesNotThrow(() -> service.update(receipt.getId(), input));
        verify(receipts).existsByReceiptCode("GR-2026-0555");
    }

    @Test
    @DisplayName("complete: without a signed-in user, or a receipt with no receiver, completion is refused")
    void complete_missingCallerOrReceiver_isRefused() {
        GoodsReceipt receipt = stored();
        assertThrows(AccessDeniedException.class, () -> service.complete(receipt.getId(), " "));
        assertThrows(AccessDeniedException.class, () -> service.complete(receipt.getId(), null));

        receipt.setReceivedBy(null);
        assertThrows(AccessDeniedException.class, () -> service.complete(receipt.getId(), OWNER));
        verify(receipts, never()).save(any());
    }

    @Test
    @DisplayName("complete: an order with no assigned receiver cannot be received against")
    void complete_unassignedOrder_isRefused() {
        GoodsReceipt receipt = stored();
        order.setAssignedTo(null);

        assertThrows(AccessDeniedException.class, () -> service.complete(receipt.getId(), OWNER));
    }

    @Test
    @DisplayName("delete and line edits: a blank or missing caller is refused")
    void ownerOnlyOperations_blankCaller_isRefused() {
        GoodsReceipt receipt = stored();

        assertThrows(AccessDeniedException.class, () -> service.delete(receipt.getId(), " "));
        assertThrows(AccessDeniedException.class, () -> service.delete(receipt.getId(), null));
        assertThrows(AccessDeniedException.class, () -> service.removeLine(receipt.getId(), 0, ""));
        verify(receipts, never()).deleteById(any());
    }

    @Test
    @DisplayName("lookups: a receipt pointing at a malformed order id is reported as invalid")
    void malformedOrderId_isInvalid() {
        GoodsReceipt receipt = stored();
        receipt.setPurchaseOrderId("not-a-uuid");

        assertThrows(GoodsReceiptValidationException.class, () -> service.delete(receipt.getId(), OWNER));
    }
}
