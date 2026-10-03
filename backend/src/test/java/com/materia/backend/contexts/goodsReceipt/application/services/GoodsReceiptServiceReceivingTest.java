package com.materia.backend.contexts.goodsReceipt.application.services;

import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.application.mappers.GoodsReceiptMapper;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptBusinessException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptValidationException;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptEventPublisher;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.DEFAULT_RECEIVER_ID;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.DEFAULT_RECEIVER_NAME;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** [T035] Receiving rules enforced by the goods receipt service, with ports mocked (US3). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoodsReceiptServiceReceivingTest {

    private static final String RECEIVER = DEFAULT_RECEIVER_ID;

    @Mock private GoodsReceiptRepository receipts;
    @Mock private GoodsReceiptEventPublisher receiptEvents;
    @Mock private GoodsReceiptCodeGeneratorService codeGenerator;
    @Mock private PurchaseOrderRepository orders;
    @Mock private MaterialRepository materials;
    @Mock private PurchaseOrderEventPublisher orderEvents;

    private GoodsReceiptService service;
    private PurchaseOrder order;
    private Material material;

    @BeforeEach
    void setUp() {
        service = new GoodsReceiptService(receipts, new GoodsReceiptMapper(), receiptEvents, codeGenerator,
                orders, materials, orderEvents);
        when(receipts.save(any(GoodsReceipt.class))).thenAnswer(inv -> inv.getArgument(0));
        when(receipts.findByPurchaseOrderId(any())).thenReturn(List.of());
        when(codeGenerator.generateCode()).thenReturn(ReceiptCode.of("GR-2026-0001"));
        when(orders.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        order = receivable(OrderStatus.READY_FOR_RECEIPT);
        material = aMaterial().stock(20).build();
        when(materials.findById(any())).thenReturn(Optional.of(material));
        when(materials.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PurchaseOrder receivable(OrderStatus status) {
        PurchaseOrder po = anOrder().withLine(10, "5.00").inStatus(status).build();
        when(orders.findById(po.getId())).thenReturn(Optional.of(po));
        return po;
    }

    private CreateGoodsReceiptInput input(PurchaseOrder po, String caller, int received, int rejected, String reason) {
        PurchaseOrderLine orderLine = po.getLines().get(0);
        GoodsReceiptLineInput line = new GoodsReceiptLineInput();
        line.setPurchaseOrderLineId(orderLine.getId().toString());
        line.setMaterialCode(orderLine.getMaterialCode());
        line.setQuantityReceived(received);
        line.setQuantityRejected(rejected);
        line.setQualityStatus(rejected == 0 ? QualityStatus.ACCEPTED.getCode() : QualityStatus.PARTIAL.getCode());
        line.setRejectionReason(reason);
        CreateGoodsReceiptInput in = new CreateGoodsReceiptInput();
        in.setPurchaseOrderId(po.getId().toString());
        in.setReceivedBy(caller);
        in.setReceivedByName(caller);
        in.setUserId(caller);
        in.setLines(new ArrayList<>(List.of(line)));
        return in;
    }

    // ---- Creation preconditions ----

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = OrderStatus.class, names = {"READY_FOR_RECEIPT", "PARTIALLY_RECEIVED"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("create: an order that is not ready for receipt cannot be received; a rule violation (US3-7, F-008)")
    void create_orderNotReceivable_isRefused(OrderStatus status) {
        PurchaseOrder po = receivable(status);

        assertThrows(GoodsReceiptBusinessException.class, () -> service.create(input(po, RECEIVER, 5, 0, null)));
        verify(receipts, never()).save(any());
    }

    @Test
    @DisplayName("create: someone other than the assigned receiver cannot record a receipt (US3-6)")
    void create_byNonAssignee_isRefused() {
        assertThrows(AccessDeniedException.class, () -> service.create(input(order, "someone-else", 5, 0, null)));
        verify(receipts, never()).save(any());
    }

    @Test
    @DisplayName("create: a receipt whose receiver differs from the caller is refused")
    void create_receiverNotCaller_isRefused() {
        CreateGoodsReceiptInput in = input(order, RECEIVER, 5, 0, null);
        in.setReceivedBy("someone-else");

        assertThrows(AccessDeniedException.class, () -> service.create(in));
    }

    @Test
    @DisplayName("create: a line that references no line of the order is invalid input")
    void create_unknownOrderLine_isRefused() {
        CreateGoodsReceiptInput in = input(order, RECEIVER, 5, 0, null);
        in.getLines().get(0).setPurchaseOrderLineId(UUID.randomUUID().toString());

        assertThrows(GoodsReceiptValidationException.class, () -> service.create(in));
    }

    @Test
    @DisplayName("create: a line whose material differs from the order line is a rule violation (F-008)")
    void create_materialMismatch_isRefused() {
        CreateGoodsReceiptInput in = input(order, RECEIVER, 5, 0, null);
        in.getLines().get(0).setMaterialCode("MAT-OTHER");

        assertThrows(GoodsReceiptBusinessException.class, () -> service.create(in));
    }

    @Test
    @DisplayName("create: receiving more than ordered on one receipt is invalid input")
    void create_moreThanOrdered_isRefused() {
        assertThrows(RuntimeException.class, () -> service.create(input(order, RECEIVER, 11, 0, null)));
        verify(receipts, never()).save(any());
    }

    @Test
    @DisplayName("create: receiving beyond what remains across earlier completed receipts is refused (US3-5)")
    void create_beyondRemaining_isRefused() {
        GoodsReceipt earlier = aReceipt(order).receiving(0, 7, 0, null).inStatus(ReceiptStatus.PARTIAL).build();
        when(receipts.findByPurchaseOrderId(order.getId().toString())).thenReturn(List.of(earlier));

        assertThrows(GoodsReceiptBusinessException.class, () -> service.create(input(order, RECEIVER, 4, 0, null)));
        assertDoesNotThrow(() -> service.create(input(order, RECEIVER, 3, 0, null)));
    }

    @Test
    @DisplayName("create: draft and cancelled receipts do not count towards what has been received (US3-10)")
    void create_ignoresDraftAndCancelledReceipts() {
        GoodsReceipt draft = aReceipt(order).receiving(0, 9, 0, null).build();
        GoodsReceipt cancelled = aReceipt(order).receiving(0, 9, 0, null).inStatus(ReceiptStatus.CANCELLED).build();
        when(receipts.findByPurchaseOrderId(order.getId().toString())).thenReturn(List.of(draft, cancelled));

        assertDoesNotThrow(() -> service.create(input(order, RECEIVER, 10, 0, null)));
    }

    @Test
    @DisplayName("create: supplier, expected date and receiver name come from the order, not the request (US3-11)")
    void create_hydratesFromOrder() {
        CreateGoodsReceiptInput in = input(order, RECEIVER, 5, 0, null);
        in.setReceivedByName("Forged Name");
        in.setSupplierName("Forged Supplier");

        GoodsReceiptOutput out = service.create(in);

        assertEquals(DEFAULT_RECEIVER_NAME, out.getReceivedByName());
        assertEquals(order.getSupplierName(), out.getSupplierName());
        assertEquals(order.getOrderCode().getValue(), out.getPurchaseOrderCode());
        assertEquals(ReceiptStatus.DRAFT.getCode(), out.getStatus());
        verifyNoInteractions(materials);
    }

    // ---- Validation (complete) ----

    private GoodsReceipt storedDraft(GoodsReceipt receipt) {
        when(receipts.findById(receipt.getId())).thenReturn(Optional.of(receipt));
        return receipt;
    }

    @Test
    @DisplayName("validate: a full receipt adds the accepted quantity to stock and completes the order as delivered (US3-1)")
    void complete_full_updatesStockAndCompletesOrder() {
        GoodsReceipt receipt = storedDraft(aReceipt(order).build());

        service.complete(receipt.getId(), RECEIVER);

        assertEquals(30, material.getCurrentStock());
        assertEquals(20, receipt.getLines().get(0).getStockBefore());
        assertEquals(30, receipt.getLines().get(0).getStockAfter());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(DeliveryStatus.DELIVERED, order.getDeliveryStatus());
        verify(orders).save(order);
    }

    @Test
    @DisplayName("validate: a partial receipt leaves the order partially received (US3-2)")
    void complete_partial_marksOrderPartial() {
        GoodsReceipt receipt = storedDraft(aReceipt(order).receiving(0, 6, 0, null).build());

        service.complete(receipt.getId(), RECEIVER);

        assertEquals(26, material.getCurrentStock());
        assertEquals(OrderStatus.PARTIALLY_RECEIVED, order.getStatus());
        assertEquals(DeliveryStatus.PARTIAL, order.getDeliveryStatus());
    }

    @Test
    @DisplayName("validate: only the accepted quantity reaches stock when some is rejected (US3-4)")
    void complete_withRejection_addsOnlyAccepted() {
        GoodsReceipt receipt = storedDraft(aReceipt(order).receiving(0, 10, 3, "Damaged").build());

        service.complete(receipt.getId(), RECEIVER);

        assertEquals(27, material.getCurrentStock());
    }

    @Test
    @DisplayName("validate: a fully rejected line leaves stock untouched")
    void complete_fullyRejected_leavesStock() {
        GoodsReceipt receipt = storedDraft(aReceipt(order).receiving(0, 10, 10, "Wrong item").build());

        service.complete(receipt.getId(), RECEIVER);

        assertEquals(20, material.getCurrentStock());
        verify(materials, never()).save(any());
    }

    @Test
    @DisplayName("validate: the remainder after earlier receipts completes the order (US3-3)")
    void complete_remainder_completesOrder() {
        PurchaseOrder partial = receivable(OrderStatus.PARTIALLY_RECEIVED);
        GoodsReceipt earlier = aReceipt(partial).receiving(0, 6, 0, null).inStatus(ReceiptStatus.PARTIAL).build();
        when(receipts.findByPurchaseOrderId(partial.getId().toString())).thenReturn(List.of(earlier));
        GoodsReceipt remainder = storedDraft(aReceipt(partial).receiving(0, 4, 0, null).build());

        service.complete(remainder.getId(), RECEIVER);

        assertEquals(OrderStatus.COMPLETED, partial.getStatus());
    }

    @Test
    @DisplayName("validate: a material that no longer exists aborts validation before the order changes")
    void complete_missingMaterial_isRefused() {
        when(materials.findById(any())).thenReturn(Optional.empty());
        when(materials.findByCode(any())).thenReturn(Optional.empty());
        GoodsReceipt receipt = storedDraft(aReceipt(order).build());

        assertThrows(GoodsReceiptValidationException.class, () -> service.complete(receipt.getId(), RECEIVER));
        assertEquals(OrderStatus.READY_FOR_RECEIPT, order.getStatus());
        verify(orders, never()).save(any());
    }

    @Test
    @DisplayName("validate: someone other than the assigned receiver cannot validate a receipt (US3-6)")
    void complete_byNonAssignee_isRefused() {
        GoodsReceipt receipt = storedDraft(aReceipt(order).build());

        assertThrows(AccessDeniedException.class, () -> service.complete(receipt.getId(), "someone-else"));
        assertEquals(20, material.getCurrentStock());
    }
}
