package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.mappers.PurchaseOrderMapper;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidLineException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidStatusTransitionException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderLineRequiredException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotFoundException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotModifiableException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import com.materia.backend.contexts.purchaseOrder.domain.valueObjects.OrderCode;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** [T022] Purchase order service orchestration for the lifecycle, with every port mocked (US1, US4-7, US4-8). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PurchaseOrderServiceLifecycleTest {

    @Mock private PurchaseOrderRepository repository;
    @Mock private PurchaseOrderCodeGeneratorService codeGenerator;
    @Mock private GoodsReceiptUseCase goodsReceiptUseCase;
    @Mock private RequisitionUseCase requisitionUseCase;
    @Mock private PurchaseOrderEventPublisher eventPublisher;
    @Mock private ReceiverDirectory receiverDirectory;

    private PurchaseOrderService service;

    @BeforeEach
    void setUp() {
        service = new PurchaseOrderService(repository, new PurchaseOrderMapper(), codeGenerator,
                goodsReceiptUseCase, requisitionUseCase, eventPublisher, receiverDirectory);
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(codeGenerator.generateCode()).thenReturn(OrderCode.of("PO-2026-0101"));
    }

    private PurchaseOrder stored(OrderStatus status) {
        PurchaseOrder order = anOrder().withLine(10, "5.00").inStatus(status).build();
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));
        return order;
    }

    private static PurchaseOrderLineInput lineInput(UUID id, int quantity, String price) {
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setId(id);
        line.setMaterialCode("MAT-" + quantity);
        line.setQuantity(quantity);
        line.setUnitPrice(Money.of(price, CurrencyCode.MAD));
        return line;
    }

    private static CreatePurchaseOrderInput createInput(PurchaseOrderLineInput... lines) {
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setSupplierId(UUID.randomUUID());
        input.setSupplierName("Acme Supplies");
        input.setOrderedBy("buyer-1");
        input.setCurrencyCode("MAD");
        input.setLines(new ArrayList<>(Arrays.asList(lines)));
        input.setUserId("buyer-1");
        return input;
    }

    // ---- create ----

    @Test
    @DisplayName("create: assigns the generated code, numbers lines 1..n, gives each line an id and totals the order")
    void create_normalisesLinesAndTotals() {
        PurchaseOrderOutput out = service.create(createInput(lineInput(null, 2, "10.00"), lineInput(null, 3, "5.00")));

        assertEquals("PO-2026-0101", out.getOrderCode());
        assertEquals(List.of(1, 2), out.getLines().stream().map(l -> l.getLineNumber()).toList());
        assertTrue(out.getLines().stream().allMatch(l -> l.getId() != null));
        assertEquals(OrderStatus.DRAFT.getCode(), out.getStatus());
        verify(repository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("create: an order without lines is refused and nothing is saved (edge case)")
    void create_withoutLines_isRefused() {
        assertThrows(RuntimeException.class, () -> service.create(createInput()));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create: a line whose currency differs from its unit price is refused")
    void create_lineCurrencyMismatch_isRefused() {
        PurchaseOrderLineInput line = lineInput(null, 1, "10.00");
        line.setCurrencyCode("EUR");

        assertThrows(RuntimeException.class, () -> service.create(createInput(line)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create: without a requisition the requisition module is never consulted")
    void create_withoutRequisition_leavesRequisitionsAlone() {
        service.create(createInput(lineInput(null, 1, "1.00")));

        verifyNoInteractions(requisitionUseCase);
    }

    // ---- update ----

    @Test
    @DisplayName("update: a draft keeps the ids of lines sent back and gives new lines fresh ids (edge case)")
    void update_keepsReturnedLineIds() {
        PurchaseOrder order = stored(OrderStatus.DRAFT);
        UUID keptId = order.getLines().get(0).getId();
        UpdatePurchaseOrderInput input = new UpdatePurchaseOrderInput();
        input.setLines(new ArrayList<>(List.of(lineInput(keptId, 4, "5.00"), lineInput(null, 1, "2.00"))));
        input.setUserId("buyer-1");

        PurchaseOrderOutput out = service.update(order.getId(), input);

        assertEquals(keptId, out.getLines().get(0).getId());
        assertNotNull(out.getLines().get(1).getId());
        assertNotEquals(keptId, out.getLines().get(1).getId());
        assertEquals(2, out.getLines().size());
    }

    @Test
    @DisplayName("update: an order past submission cannot be edited (US1-15)")
    void update_afterSubmission_isRefused() {
        PurchaseOrder order = stored(OrderStatus.CONFIRMED);

        assertThrows(PurchaseOrderNotModifiableException.class, () -> service.update(order.getId(), new UpdatePurchaseOrderInput()));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update: the create-shaped overload delegates to the same rules")
    void update_createShapedOverload_delegates() {
        PurchaseOrder order = stored(OrderStatus.SUBMITTED);
        CreatePurchaseOrderInput input = createInput(lineInput(null, 2, "3.00"));
        input.setNotes("Revised");

        PurchaseOrderOutput out = service.update(order.getId(), input);

        assertEquals("Revised", out.getNotes());
    }

    // ---- transitions ----

    @Test
    @DisplayName("transitions: submit, confirm, complete, cancel and tracking load the order, apply the rule and save")
    void transitions_loadApplyAndSave() {
        assertEquals("SUBMITTED", service.submit(stored(OrderStatus.DRAFT).getId(), "buyer-1").getStatus());
        assertEquals("CONFIRMED", service.confirm(stored(OrderStatus.SUBMITTED).getId(), "admin-1").getStatus());
        assertEquals("COMPLETED", service.complete(stored(OrderStatus.PARTIALLY_RECEIVED).getId(), "buyer-1").getStatus());
        assertEquals("CANCELLED", service.cancel(stored(OrderStatus.CONFIRMED).getId(), "buyer-1", "Not needed").getStatus());
        assertEquals("SHIPPED", service.updateDeliveryStatus(stored(OrderStatus.CONFIRMED).getId(), "SHIPPED", "buyer-1").getDeliveryStatus());
        verify(repository, times(5)).save(any());
    }

    @Test
    @DisplayName("transitions: a refused transition saves nothing")
    void refusedTransition_savesNothing() {
        PurchaseOrder order = stored(OrderStatus.COMPLETED);

        assertThrows(PurchaseOrderInvalidStatusTransitionException.class, () -> service.cancel(order.getId(), "buyer-1", "x"));
        assertThrows(PurchaseOrderInvalidStatusTransitionException.class,
                () -> service.updateDeliveryStatus(order.getId(), DeliveryStatus.SHIPPED.getCode(), "buyer-1"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("not found: every operation on an unknown order reports it as missing")
    void unknownOrder_isNotFound() {
        UUID missing = UUID.randomUUID();
        when(repository.findById(missing)).thenReturn(Optional.empty());

        assertThrows(PurchaseOrderNotFoundException.class, () -> service.getById(missing));
        assertThrows(PurchaseOrderNotFoundException.class, () -> service.submit(missing, "u"));
        assertThrows(PurchaseOrderNotFoundException.class, () -> service.confirm(missing, "u"));
        assertThrows(PurchaseOrderNotFoundException.class, () -> service.cancel(missing, "u", "r"));
        assertThrows(PurchaseOrderNotFoundException.class, () -> service.complete(missing, "u"));
        assertThrows(PurchaseOrderNotFoundException.class, () -> service.delete(missing, "u"));
        assertThrows(PurchaseOrderNotFoundException.class, () -> service.update(missing, new UpdatePurchaseOrderInput()));
    }

    // ---- assignment (US4-7, US4-8) ----

    @Test
    @DisplayName("assign: the assignee's name comes from their account, not from the request (US4-8)")
    void assign_usesDirectoryName() {
        PurchaseOrder order = stored(OrderStatus.CONFIRMED);
        when(receiverDirectory.findAssignableReceiver("receiver-7"))
                .thenReturn(Optional.of(new ReceiverDirectory.Receiver("receiver-7", "Real Name", "r7@materia.test")));

        PurchaseOrderOutput out = service.assignReceiver(order.getId(), "buyer-1", "Bob", "receiver-7", "Forged Name");

        assertEquals("receiver-7", out.getAssignedTo());
        assertEquals("Real Name", out.getAssignedToName());
        assertEquals("READY_FOR_RECEIPT", out.getStatus());
    }

    @Test
    @DisplayName("assign: a person who is not an active receiver cannot be assigned (US4-7)")
    void assign_unknownReceiver_isRefused() {
        PurchaseOrder order = stored(OrderStatus.CONFIRMED);
        when(receiverDirectory.findAssignableReceiver(any())).thenReturn(Optional.empty());

        assertThrows(PurchaseOrderValidationException.class,
                () -> service.assignReceiver(order.getId(), "buyer-1", "Bob", "purchaser-2", "Pat"));
        assertNull(order.getAssignedTo());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("assign: the receiver list is the directory's list")
    void assignableReceivers_comeFromDirectory() {
        List<ReceiverDirectory.Receiver> receivers = List.of(new ReceiverDirectory.Receiver("r1", "Rita", "r@m.test"));
        when(receiverDirectory.findAssignableReceivers()).thenReturn(receivers);

        assertEquals(receivers, service.getAssignableReceivers());
    }

    // ---- reads ----

    @Test
    @DisplayName("reads: lookups delegate to the matching repository query")
    void reads_delegate() {
        PurchaseOrder order = anOrder().build();
        UUID supplier = UUID.randomUUID();
        UUID requisition = UUID.randomUUID();
        when(repository.findByCode("PO-2026-0001")).thenReturn(Optional.of(order));
        when(repository.findAll()).thenReturn(List.of(order));
        when(repository.findByStatus(OrderStatus.DRAFT)).thenReturn(List.of(order));
        when(repository.findByDeliveryStatus(DeliveryStatus.SHIPPED)).thenReturn(List.of(order));
        when(repository.findBySupplierId(supplier)).thenReturn(List.of(order));
        when(repository.findByRequisitionId(requisition)).thenReturn(List.of(order));
        when(repository.search("acme")).thenReturn(List.of(order));

        assertEquals(order.getId(), service.getByCode("PO-2026-0001").getId());
        assertEquals(1, service.getAll().size());
        assertEquals(1, service.getByStatus("DRAFT").size());
        assertEquals(1, service.getByDeliveryStatus("SHIPPED").size());
        assertEquals(1, service.getBySupplierId(supplier).size());
        assertEquals(1, service.getByRequisitionId(requisition).size());
        assertEquals(1, service.searchByKeyword("acme").size());
    }

    @Test
    @DisplayName("reads: an unknown code is reported as missing; an unknown status code is invalid input")
    void reads_unknownCodeAndStatus() {
        when(repository.findByCode("PO-2026-9999")).thenReturn(Optional.empty());

        assertThrows(PurchaseOrderNotFoundException.class, () -> service.getByCode("PO-2026-9999"));
        assertThrows(PurchaseOrderValidationException.class, () -> service.getByStatus("IN_PROGRESS"));
    }

    @Test
    @DisplayName("delete: a draft is deleted; an order past submission cannot be (US1-15)")
    void delete_onlyModifiable() {
        PurchaseOrder draft = stored(OrderStatus.DRAFT);
        service.delete(draft.getId());
        verify(repository).deleteById(draft.getId());

        PurchaseOrder confirmed = stored(OrderStatus.CONFIRMED);
        assertThrows(PurchaseOrderNotModifiableException.class, () -> service.delete(confirmed.getId(), "admin-1"));
        verify(repository, never()).deleteById(confirmed.getId());
    }

    @Test
    @DisplayName("create: a null entry in the request's lines is ignored, not saved as an empty line")
    void create_nullLine_isIgnored() {
        CreatePurchaseOrderInput input = createInput(lineInput(null, 1, "1.00"));
        input.getLines().add(null);

        PurchaseOrderOutput out = service.create(input);

        assertEquals(1, out.getLines().size());
    }

    @Test
    @DisplayName("create: a line currency without a unit price is refused")
    void create_currencyWithoutPrice_isRefused() {
        PurchaseOrderLineInput line = lineInput(null, 1, "1.00");
        line.setUnitPrice(null);
        line.setCurrencyCode("MAD");

        RuntimeException e = assertThrows(RuntimeException.class, () -> service.create(createInput(line)));
        assertTrue(e instanceof PurchaseOrderValidationException || e instanceof PurchaseOrderInvalidLineException
                || e instanceof PurchaseOrderLineRequiredException, () -> "unexpected " + e);
    }
}
