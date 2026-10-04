package com.materia.backend.contexts.returnToVendor.application.services;

import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.returnToVendor.application.dtos.CreateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorOutput;
import com.materia.backend.contexts.returnToVendor.application.dtos.UpdateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.mappers.ReturnToVendorMapper;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.events.ReturnToVendorCancelledEvent;
import com.materia.backend.contexts.returnToVendor.domain.events.ReturnToVendorCreatedEvent;
import com.materia.backend.contexts.returnToVendor.domain.events.ReturnToVendorResolvedEvent;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorBusinessException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidLineException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidQuantityException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorLineRequiredException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorNotFoundException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorNotModifiableException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorValidationException;
import com.materia.backend.contexts.returnToVendor.domain.ports.out.ReturnToVendorRepository;
import com.materia.backend.contexts.returnToVendor.domain.valueObjects.ReturnCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Returns to vendor tied to the chain: a return sends back only what its goods receipt rejected, a replacement
 * reopens the purchase order and puts the quantity back on order, and a credit note is valued at order prices.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReturnToVendorServiceTest {

    private static final String USER = "buyer-1";

    @Mock private ReturnToVendorRepository returns;
    @Mock private ReturnToVendorCodeGeneratorService codeGenerator;
    @Mock private ApplicationEventPublisher events;
    @Mock private GoodsReceiptRepository receipts;
    @Mock private PurchaseOrderRepository orders;
    @Mock private MaterialRepository materials;

    private ReturnToVendorService service;
    private PurchaseOrder order;
    private GoodsReceipt receipt;
    private Material material;
    private final List<ReturnToVendor> stored = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new ReturnToVendorService(returns, new ReturnToVendorMapper(), codeGenerator, events, receipts, orders, materials);

        // 10 bolts at 5.00 received, 4 rejected as scratched; 4 nuts at 2.00 all accepted.
        order = anOrder().withLine(10, "5.00").withLine(4, "2.00").inStatus(OrderStatus.COMPLETED).build();
        receipt = aReceipt(order).receiving(0, 10, 4, "Scratched").receiving(1, 4, 0, null).build();
        receipt.complete("receiver-1");

        material = aMaterial().stock(6).build();
        when(codeGenerator.generateCode()).thenReturn(ReturnCode.of("RTN-2026-0001"));
        when(receipts.findById(receipt.getId())).thenReturn(Optional.of(receipt));
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));
        when(orders.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(materials.findById(any())).thenReturn(Optional.of(material));
        when(materials.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));
        when(returns.findByGoodsReceiptId(receipt.getId().toString())).thenAnswer(inv -> List.copyOf(stored));
        when(returns.save(any(ReturnToVendor.class))).thenAnswer(inv -> {
            ReturnToVendor saved = inv.getArgument(0);
            stored.removeIf(r -> r.getId().equals(saved.getId()));
            stored.add(saved);
            when(returns.findById(saved.getId())).thenReturn(Optional.of(saved));
            return saved;
        });
    }

    private String receiptLine(int index) {
        return receipt.getLines().get(index).getId().toString();
    }

    private static ReturnToVendorLineInput line(String receiptLineId, Integer quantity) {
        return new ReturnToVendorLineInput(receiptLineId, quantity, null);
    }

    private CreateReturnToVendorInput request(ReturnToVendorLineInput... lines) {
        CreateReturnToVendorInput input = new CreateReturnToVendorInput();
        input.setGoodsReceiptId(receipt.getId().toString());
        input.setReturnReason("Scratched surfaces");
        input.setLines(List.of(lines));
        input.setUserId(USER);
        return input;
    }

    private ReturnToVendorOutput pendingReturnOf(int quantity) {
        ReturnToVendorOutput created = service.create(request(line(receiptLine(0), quantity)));
        return service.submit(created.getId(), USER);
    }

    // ---- Creating a return from a goods receipt ----

    @Test
    @DisplayName("create: supplier, order, material, price and rejection reason come from the goods receipt")
    void create_isHydratedFromTheReceipt() {
        ReturnToVendorOutput out = service.create(request(line(receiptLine(0), 3)));

        assertEquals("RTN-2026-0001", out.getReturnCode());
        assertEquals(ReturnStatus.DRAFT.name(), out.getStatus());
        assertEquals(receipt.getReceiptCode().getValue(), out.getGoodsReceiptCode());
        assertEquals(order.getId().toString(), out.getPurchaseOrderId());
        assertEquals(order.getSupplierName(), out.getSupplierName());
        assertEquals(order.getCurrencyCode(), out.getCurrencyCode());
        var line = out.getLines().get(0);
        assertEquals(order.getLines().get(0).getId().toString(), line.getPurchaseOrderLineId());
        assertEquals(4, line.getRejectedQuantity());
        assertEquals(3, line.getQuantityToReturn());
        assertEquals(0, new BigDecimal("5.00").compareTo(line.getUnitPrice()));
        assertEquals(0, new BigDecimal("15.00").compareTo(out.getTotalValue()));
        assertEquals("Scratched", line.getRejectionReason());
        verify(events).publishEvent(any(ReturnToVendorCreatedEvent.class));
    }

    @Test
    @DisplayName("create: a reason given on the line replaces the one recorded at receipt")
    void create_lineReasonOverridesReceiptReason() {
        ReturnToVendorLineInput input = new ReturnToVendorLineInput(receiptLine(0), 1, "Wrong thread");

        ReturnToVendorOutput out = service.create(request(input));

        assertEquals("Wrong thread", out.getLines().get(0).getRejectionReason());
    }

    @Test
    @DisplayName("create: only a completed goods receipt can be returned; an unknown receipt is refused")
    void create_requiresACompletedReceipt() {
        GoodsReceipt draft = aReceipt(order).receiving(0, 10, 4, "Scratched").build();
        when(receipts.findById(draft.getId())).thenReturn(Optional.of(draft));
        CreateReturnToVendorInput onDraft = request(line(draft.getLines().get(0).getId().toString(), 1));
        onDraft.setGoodsReceiptId(draft.getId().toString());
        assertThrows(ReturnToVendorBusinessException.class, () -> service.create(onDraft));

        CreateReturnToVendorInput unknown = request(line(receiptLine(0), 1));
        unknown.setGoodsReceiptId("not-a-uuid");
        assertThrows(ReturnToVendorValidationException.class, () -> service.create(unknown));
        assertTrue(stored.isEmpty());
    }

    @Test
    @DisplayName("create: a line from another receipt, a repeated line, or a line with nothing rejected is refused")
    void create_refusesLinesThatCannotBeReturned() {
        assertThrows(ReturnToVendorInvalidLineException.class,
                () -> service.create(request(line(UUID.randomUUID().toString(), 1))));
        assertThrows(ReturnToVendorInvalidLineException.class,
                () -> service.create(request(line(receiptLine(0), 1), line(receiptLine(0), 1))));
        assertThrows(ReturnToVendorInvalidLineException.class,
                () -> service.create(request(line(receiptLine(1), 1))));
        assertThrows(ReturnToVendorLineRequiredException.class, () -> service.create(request()));
        assertTrue(stored.isEmpty());
    }

    @Test
    @DisplayName("create: the quantity must be positive and within what was rejected")
    void create_quantityMustBeWithinTheRejection() {
        assertThrows(ReturnToVendorInvalidQuantityException.class, () -> service.create(request(line(receiptLine(0), 0))));
        assertThrows(ReturnToVendorInvalidQuantityException.class, () -> service.create(request(line(receiptLine(0), null))));
        ReturnToVendorInvalidQuantityException tooMany = assertThrows(ReturnToVendorInvalidQuantityException.class,
                () -> service.create(request(line(receiptLine(0), 5))));
        assertTrue(tooMany.getMessage().contains("Only 4"), tooMany.getMessage());
    }

    @Test
    @DisplayName("create: other open returns of the receipt hold their quantity; a cancelled one releases it")
    void create_otherReturnsHoldTheirQuantity() {
        ReturnToVendorOutput first = service.create(request(line(receiptLine(0), 3)));

        ReturnToVendorInvalidQuantityException refused = assertThrows(ReturnToVendorInvalidQuantityException.class,
                () -> service.create(request(line(receiptLine(0), 2))));
        assertTrue(refused.getMessage().contains("Only 1"), refused.getMessage());

        service.cancel(first.getId(), USER, "Prepared twice");
        assertDoesNotThrow(() -> service.create(request(line(receiptLine(0), 4))));
    }

    @Test
    @DisplayName("create: an authenticated user is required")
    void create_requiresAUser() {
        CreateReturnToVendorInput input = request(line(receiptLine(0), 1));
        input.setUserId(" ");
        assertThrows(AccessDeniedException.class, () -> service.create(input));
    }

    // ---- Editing and deleting drafts ----

    @Test
    @DisplayName("update: a draft can raise its own quantity up to the rejection, since it does not count against itself")
    void update_draftExcludesItself() {
        ReturnToVendorOutput draft = service.create(request(line(receiptLine(0), 3)));
        UpdateReturnToVendorInput change = new UpdateReturnToVendorInput();
        change.setLines(List.of(line(receiptLine(0), 4)));
        change.setNotes("Pick-up on Monday");
        change.setUserId(USER);

        ReturnToVendorOutput updated = service.update(draft.getId(), change);

        assertEquals(4, updated.getLines().get(0).getQuantityToReturn());
        assertEquals("Pick-up on Monday", updated.getNotes());
        assertEquals("Scratched surfaces", updated.getReturnReason());
    }

    @Test
    @DisplayName("update and delete: only a draft can be changed or deleted; a blank reason is refused")
    void update_onlyDrafts() {
        ReturnToVendorOutput draft = service.create(request(line(receiptLine(0), 3)));
        UpdateReturnToVendorInput blankReason = new UpdateReturnToVendorInput();
        blankReason.setReturnReason(" ");
        blankReason.setUserId(USER);
        assertThrows(ReturnToVendorValidationException.class, () -> service.update(draft.getId(), blankReason));

        service.submit(draft.getId(), USER);
        UpdateReturnToVendorInput change = new UpdateReturnToVendorInput();
        change.setUserId(USER);
        assertThrows(ReturnToVendorNotModifiableException.class, () -> service.update(draft.getId(), change));
        assertThrows(ReturnToVendorNotModifiableException.class, () -> service.delete(draft.getId(), USER));
        verify(returns, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete: a draft is deleted; an unknown return is not found")
    void delete_draft() {
        ReturnToVendorOutput draft = service.create(request(line(receiptLine(0), 3)));

        service.delete(draft.getId(), USER);

        verify(returns).deleteById(draft.getId());
        assertThrows(ReturnToVendorNotFoundException.class, () -> service.getById(UUID.randomUUID()));
    }

    // ---- Shipping and resolution ----

    @Test
    @DisplayName("submit: the goods are with the supplier, and no stock moves (rejected goods never entered stock)")
    void submit_shipsWithoutStockMovement() {
        ReturnToVendorOutput pending = pendingReturnOf(3);

        assertEquals(ReturnStatus.PENDING.name(), pending.getStatus());
        assertEquals(3, pending.getLines().get(0).getQuantityAlreadyReturned());
        assertEquals(0, pending.getLines().get(0).getRemainingQuantity());
        verify(materials, never()).save(any());
        assertEquals(6, material.getCurrentStock());
    }

    @Test
    @DisplayName("replacement: the order is reopened for receipt and the quantity goes back on order")
    void replacement_reopensTheOrder() {
        int onOrderBefore = material.getStockOnOrder() != null ? material.getStockOnOrder() : 0;
        ReturnToVendorOutput pending = pendingReturnOf(3);

        ReturnToVendorOutput resolved = service.resolve(pending.getId(), USER, ResolutionType.REPLACEMENT, "RMA-77", "Sending new bolts");

        assertEquals(ReturnStatus.RESOLVED.name(), resolved.getStatus());
        assertEquals("RMA-77", resolved.getReplacementPurchaseOrderReference());
        assertEquals("Sending new bolts", resolved.getSupplierResponse());
        assertTrue(resolved.getLines().get(0).isReplaced());
        assertEquals(OrderStatus.PARTIALLY_RECEIVED, order.getStatus());
        verify(orders).save(order);
        assertEquals(onOrderBefore + 3, material.getStockOnOrder());
        verify(events).publishEvent(any(ReturnToVendorResolvedEvent.class));
    }

    @Test
    @DisplayName("replacement: an order that was cancelled or no longer exists cannot take a replacement")
    void replacement_needsAReopenableOrder() {
        ReturnToVendorOutput pending = pendingReturnOf(3);
        PurchaseOrder cancelled = anOrder().withLine(10, "5.00").inStatus(OrderStatus.CANCELLED).build();
        when(orders.findById(order.getId())).thenReturn(Optional.of(cancelled));
        assertThrows(ReturnToVendorBusinessException.class,
                () -> service.resolve(pending.getId(), USER, ResolutionType.REPLACEMENT, "RMA-1", null));

        when(orders.findById(order.getId())).thenReturn(Optional.empty());
        assertThrows(ReturnToVendorBusinessException.class,
                () -> service.resolve(pending.getId(), USER, ResolutionType.REPLACEMENT, "RMA-1", null));
        verify(orders, never()).save(any());
    }

    @Test
    @DisplayName("credit note: the amount is the returned goods at order prices, and the order stays closed")
    void creditNote_isValuedAtOrderPrices() {
        ReturnToVendorOutput pending = pendingReturnOf(3);

        ReturnToVendorOutput resolved = service.resolve(pending.getId(), USER, ResolutionType.CREDIT_NOTE, "AV-2026-12", null);

        assertEquals("AV-2026-12", resolved.getCreditNoteReference());
        assertEquals(0, new BigDecimal("15.00").compareTo(new BigDecimal(resolved.getCreditNoteAmount())));
        assertTrue(resolved.getLines().get(0).isCreditNote());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orders, never()).save(any());
        verify(materials, never()).save(any());
    }

    @Test
    @DisplayName("cancel: a pending return is cancelled with its reason and an event")
    void cancel_pendingReturn() {
        ReturnToVendorOutput pending = pendingReturnOf(2);

        ReturnToVendorOutput cancelled = service.cancel(pending.getId(), USER, "Supplier refused the pick-up");

        assertEquals(ReturnStatus.CANCELLED.name(), cancelled.getStatus());
        assertTrue(cancelled.getNotes().contains("Supplier refused the pick-up"));
        verify(events).publishEvent(any(ReturnToVendorCancelledEvent.class));
    }

    // ---- Queries ----

    @Test
    @DisplayName("queries: lists by receipt, order, supplier, status and keyword; a blank keyword lists everything")
    void queries() {
        service.create(request(line(receiptLine(0), 1)));
        when(returns.findAll()).thenAnswer(inv -> List.copyOf(stored));
        when(returns.findByPurchaseOrderId(order.getId().toString())).thenAnswer(inv -> List.copyOf(stored));
        when(returns.findBySupplierId(any())).thenAnswer(inv -> List.copyOf(stored));
        when(returns.findByStatus(ReturnStatus.DRAFT)).thenAnswer(inv -> List.copyOf(stored));
        when(returns.search("RTN")).thenAnswer(inv -> List.copyOf(stored));
        when(returns.findByReturnCode("RTN-2026-0001")).thenAnswer(inv -> Optional.of(stored.get(0)));

        assertEquals(1, service.getAll().size());
        assertEquals(1, service.search(" ").size());
        assertEquals(1, service.search("RTN").size());
        assertEquals(1, service.getByGoodsReceiptId(receipt.getId().toString()).size());
        assertEquals(1, service.getByPurchaseOrderId(order.getId().toString()).size());
        assertEquals(1, service.getBySupplierId(order.getSupplierId().toString()).size());
        assertEquals(1, service.getByStatus(ReturnStatus.DRAFT).size());
        assertEquals("RTN-2026-0001", service.getByCode("RTN-2026-0001").getReturnCode());
        assertThrows(ReturnToVendorNotFoundException.class, () -> service.getByCode("RTN-2026-0999"));
    }

    @Test
    @DisplayName("fixture: the receipt under test is completed, so its rejected goods are returnable")
    void fixture_receiptIsReturnable() {
        assertTrue(receipt.getStatus() == ReceiptStatus.COMPLETED || receipt.getStatus() == ReceiptStatus.PARTIAL);
    }
}
