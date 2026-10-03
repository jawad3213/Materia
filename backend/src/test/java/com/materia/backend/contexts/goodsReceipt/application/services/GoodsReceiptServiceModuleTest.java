package com.materia.backend.contexts.goodsReceipt.application.services;

import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.UpdateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.mappers.GoodsReceiptMapper;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptBusinessException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptLineRequiredException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptNotFoundException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptNotModifiableException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptValidationException;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptEventPublisher;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.DEFAULT_RECEIVER_ID;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** [T051] Goods receipt operations beyond completing an order: drafts, edits, lines, deletion, lookups (US6). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoodsReceiptServiceModuleTest {

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
        when(codeGenerator.generateCode()).thenReturn(
                com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode.of("GR-2026-0100"));
        order = anOrder().withLine(10, "5.00").withLine(4, "2.00").inStatus(OrderStatus.READY_FOR_RECEIPT).build();
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));
    }

    private GoodsReceipt stored(ReceiptStatus status) {
        GoodsReceipt receipt = aReceipt(order).inStatus(status).build();
        when(receipts.findById(receipt.getId())).thenReturn(Optional.of(receipt));
        return receipt;
    }

    private GoodsReceiptLineInput lineFor(int orderLineIndex, int received) {
        var ol = order.getLines().get(orderLineIndex);
        GoodsReceiptLineInput line = new GoodsReceiptLineInput();
        line.setPurchaseOrderLineId(ol.getId().toString());
        line.setMaterialCode(ol.getMaterialCode());
        line.setQuantityReceived(received);
        line.setQuantityRejected(0);
        line.setQualityStatus("ACCEPTED");
        return line;
    }

    // ---- Editing ----

    @Test
    @DisplayName("update: a receipt cannot be moved to a different order (US6-8)")
    void update_changedOrder_isRefused() {
        GoodsReceipt receipt = stored(ReceiptStatus.DRAFT);
        UpdateGoodsReceiptInput input = new UpdateGoodsReceiptInput();
        input.setPurchaseOrderId(UUID.randomUUID().toString());
        input.setUserId(OWNER);

        assertThrows(GoodsReceiptValidationException.class, () -> service.update(receipt.getId(), input));
        verify(receipts, never()).save(any());
    }

    @Test
    @DisplayName("update: a code already used by another receipt is refused as a conflict (US6-11)")
    void update_duplicateCode_isRefused() {
        GoodsReceipt receipt = stored(ReceiptStatus.DRAFT);
        when(receipts.existsByReceiptCode("GR-2026-0777")).thenReturn(true);
        UpdateGoodsReceiptInput input = new UpdateGoodsReceiptInput();
        input.setReceiptCode("GR-2026-0777");
        input.setUserId(OWNER);

        assertThrows(GoodsReceiptBusinessException.class, () -> service.update(receipt.getId(), input));
    }

    @Test
    @DisplayName("update: a closed receipt cannot be edited (US6-5)")
    void update_closedReceipt_isRefused() {
        GoodsReceipt receipt = stored(ReceiptStatus.COMPLETED);
        UpdateGoodsReceiptInput input = new UpdateGoodsReceiptInput();
        input.setUserId(OWNER);

        assertThrows(GoodsReceiptNotModifiableException.class, () -> service.update(receipt.getId(), input));
    }

    @Test
    @DisplayName("update: an open receipt's notes and lines are saved and the totals recalculated")
    void update_openReceipt_isSaved() {
        GoodsReceipt receipt = stored(ReceiptStatus.DRAFT);
        UpdateGoodsReceiptInput input = new UpdateGoodsReceiptInput();
        input.setNotes("Pallet 3 of 4");
        input.setReceivedBy(OWNER);
        input.setLines(new java.util.ArrayList<>(List.of(lineFor(0, 5))));
        input.setUserId(OWNER);

        var out = service.update(receipt.getId(), input);

        assertEquals("Pallet 3 of 4", out.getNotes());
        assertEquals(5, out.getTotalQuantityReceived());
    }

    // ---- Lines ----

    @Test
    @DisplayName("lines: adding a line re-checks it against the order, like creation (US6-2)")
    void addLine_isCheckedAgainstOrder() {
        GoodsReceipt receipt = aReceipt(order).skipping(1).build();
        receipt.removeLine(1);
        when(receipts.findById(receipt.getId())).thenReturn(Optional.of(receipt));

        var out = service.addLine(receipt.getId(), lineFor(1, 4), OWNER);
        assertEquals(2, out.getLines().size());

        GoodsReceiptLineInput mismatched = lineFor(1, 1);
        mismatched.setMaterialCode("MAT-OTHER");
        assertThrows(GoodsReceiptBusinessException.class, () -> service.addLine(receipt.getId(), mismatched, OWNER));
    }

    @Test
    @DisplayName("lines: removing a line from a two-line receipt keeps the other (US6-3)")
    void removeLine_keepsOthers() {
        GoodsReceipt receipt = stored(ReceiptStatus.DRAFT);

        var out = service.removeLine(receipt.getId(), 0, OWNER);

        assertEquals(1, out.getLines().size());
    }

    @Test
    @DisplayName("lines: the last remaining line cannot be removed (US6-4)")
    void removeLine_lastLine_isRefused() {
        GoodsReceipt receipt = stored(ReceiptStatus.DRAFT);
        service.removeLine(receipt.getId(), 0, OWNER);

        assertThrows(GoodsReceiptLineRequiredException.class, () -> service.removeLine(receipt.getId(), 0, OWNER));
    }

    // ---- Ownership (US6-7) ----

    @Test
    @DisplayName("ownership: only the order's assigned receiver may change, validate, cancel or delete a receipt (US6-7)")
    void nonOwner_isRefusedEverything() {
        GoodsReceipt receipt = stored(ReceiptStatus.DRAFT);
        String stranger = "someone-else";

        assertThrows(AccessDeniedException.class, () -> service.addLine(receipt.getId(), lineFor(0, 1), stranger));
        assertThrows(AccessDeniedException.class, () -> service.removeLine(receipt.getId(), 0, stranger));
        assertThrows(AccessDeniedException.class, () -> service.complete(receipt.getId(), stranger));
        assertThrows(AccessDeniedException.class, () -> service.cancel(receipt.getId(), stranger, "x"));
        assertThrows(AccessDeniedException.class, () -> service.delete(receipt.getId(), stranger));
        assertEquals(ReceiptStatus.DRAFT, receipt.getStatus());
        verify(receipts, never()).deleteById(any());
    }

    // ---- Cancel and delete ----

    @Test
    @DisplayName("cancel: the owner can cancel an open receipt (US6, US3-10)")
    void cancel_byOwner() {
        GoodsReceipt receipt = stored(ReceiptStatus.DRAFT);

        assertEquals(ReceiptStatus.CANCELLED.getCode(), service.cancel(receipt.getId(), OWNER, "Counted wrong").getStatus());
    }

    @Test
    @DisplayName("delete: the owner can delete a draft; nothing touches stock (US6-6)")
    void delete_draftByOwner() {
        GoodsReceipt receipt = stored(ReceiptStatus.DRAFT);

        service.delete(receipt.getId(), OWNER);

        verify(receipts).deleteById(receipt.getId());
        verifyNoInteractions(materials);
    }

    @Test
    @DisplayName("delete: a closed receipt cannot be deleted, with or without an actor (US6-5)")
    void delete_closedReceipt_isRefused() {
        GoodsReceipt receipt = stored(ReceiptStatus.COMPLETED);

        assertThrows(GoodsReceiptNotModifiableException.class, () -> service.delete(receipt.getId(), OWNER));
        assertThrows(GoodsReceiptNotModifiableException.class, () -> service.delete(receipt.getId()));
        verify(receipts, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete: the actor-less overload deletes an open receipt")
    void delete_withoutActor_deletesOpenReceipt() {
        GoodsReceipt receipt = stored(ReceiptStatus.DRAFT);

        service.delete(receipt.getId());

        verify(receipts).deleteById(receipt.getId());
    }

    // ---- Lookups (US6-10) ----

    @Test
    @DisplayName("lookups: by id, code, all, status, order, receiver and keyword delegate to the matching query")
    void lookups_delegate() {
        GoodsReceipt receipt = stored(ReceiptStatus.DRAFT);
        when(receipts.findByReceiptCode("GR-2026-0001")).thenReturn(Optional.of(receipt));
        when(receipts.findAll()).thenReturn(List.of(receipt));
        when(receipts.findByStatus(ReceiptStatus.DRAFT)).thenReturn(List.of(receipt));
        when(receipts.findByPurchaseOrderId(order.getId().toString())).thenReturn(List.of(receipt));
        when(receipts.findByReceivedBy(OWNER)).thenReturn(List.of(receipt));
        when(receipts.search("acme")).thenReturn(List.of(receipt));

        assertEquals(receipt.getId(), service.getById(receipt.getId()).getId());
        assertEquals(receipt.getId(), service.getByCode("GR-2026-0001").getId());
        assertEquals(1, service.getAll().size());
        assertEquals(1, service.getByStatus(ReceiptStatus.DRAFT).size());
        assertEquals(1, service.getByPurchaseOrderId(order.getId().toString()).size());
        assertEquals(1, service.getByReceiverId(OWNER).size());
        assertEquals(1, service.search("acme").size());
    }

    @Test
    @DisplayName("lookups: an unknown receipt id or code is reported as missing")
    void lookups_unknown_isNotFound() {
        UUID missing = UUID.randomUUID();
        when(receipts.findById(missing)).thenReturn(Optional.empty());
        when(receipts.findByReceiptCode("GR-2026-9999")).thenReturn(Optional.empty());

        assertThrows(GoodsReceiptNotFoundException.class, () -> service.getById(missing));
        assertThrows(GoodsReceiptNotFoundException.class, () -> service.getByCode("GR-2026-9999"));
        assertThrows(GoodsReceiptNotFoundException.class, () -> service.complete(missing, OWNER));
        assertThrows(GoodsReceiptNotFoundException.class, () -> service.cancel(missing, OWNER, "x"));
    }

    @Test
    @DisplayName("create: a supplied code that is already in use is refused as a conflict (US6-11)")
    void create_duplicateCode_isRefused() {
        when(receipts.existsByReceiptCode("GR-2026-0555")).thenReturn(true);
        var input = new com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput();
        input.setReceiptCode("GR-2026-0555");
        input.setPurchaseOrderId(order.getId().toString());
        input.setReceivedBy(OWNER);
        input.setReceivedByName("Rita");
        input.setUserId(OWNER);
        input.setLines(new java.util.ArrayList<>(List.of(lineFor(0, 1))));

        assertThrows(GoodsReceiptBusinessException.class, () -> service.create(input));
    }

    @Test
    @DisplayName("create: an order id that is not a UUID, or that does not exist, is invalid input")
    void create_badOrderId_isRefused() {
        var input = new com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput();
        input.setPurchaseOrderId("not-a-uuid");
        input.setReceivedBy(OWNER);
        input.setReceivedByName("Rita");
        input.setUserId(OWNER);
        input.setLines(new java.util.ArrayList<>(List.of(lineFor(0, 1))));

        assertThrows(GoodsReceiptValidationException.class, () -> service.create(input));

        input.setPurchaseOrderId(UUID.randomUUID().toString());
        assertThrows(GoodsReceiptValidationException.class, () -> service.create(input));
    }

    @Test
    @DisplayName("create: a request with no authenticated user is refused")
    void create_withoutUser_isRefused() {
        var input = new com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput();
        input.setPurchaseOrderId(order.getId().toString());
        input.setReceivedBy(OWNER);
        input.setReceivedByName("Rita");
        input.setLines(new java.util.ArrayList<>(List.of(lineFor(0, 1))));

        assertThrows(AccessDeniedException.class, () -> service.create(input));
    }

    @Test
    @DisplayName("validate: a fully rejected line yields a rejection signal alongside the completion")
    void complete_withRejection_publishesRejectedEvent() {
        GoodsReceipt receipt = aReceipt(order).receiving(0, 10, 10, "Wrong item").build();
        when(receipts.findById(receipt.getId())).thenReturn(Optional.of(receipt));
        when(materials.findById(any())).thenReturn(Optional.of(
                com.materia.backend.support.fixtures.MaterialFixtures.aMaterial().stock(0).build()));
        when(materials.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orders.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.complete(receipt.getId(), OWNER);

        verify(receiptEvents).publish(any(com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptRejectedEvent.class));
        verify(receiptEvents).publish(any(com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptPartialEvent.class));
    }
}
