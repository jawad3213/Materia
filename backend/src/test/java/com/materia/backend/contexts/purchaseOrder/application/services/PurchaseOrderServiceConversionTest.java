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
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import com.materia.backend.contexts.purchaseOrder.domain.valueObjects.OrderCode;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionOutput;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionInvalidStatusTransitionException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionNotFoundException;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** [T028, T029] Requisition conversion on create and release on withdrawal (US2, FR-004, FR-007 ordering). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PurchaseOrderServiceConversionTest {

    @Mock private PurchaseOrderRepository repository;
    @Mock private PurchaseOrderCodeGeneratorService codeGenerator;
    @Mock private GoodsReceiptUseCase goodsReceiptUseCase;
    @Mock private RequisitionUseCase requisitionUseCase;
    @Mock private PurchaseOrderEventPublisher eventPublisher;
    @Mock private ReceiverDirectory receiverDirectory;

    private PurchaseOrderService service;
    private final UUID requisitionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new PurchaseOrderService(repository, new PurchaseOrderMapper(), codeGenerator,
                goodsReceiptUseCase, requisitionUseCase, eventPublisher, receiverDirectory);
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(codeGenerator.generateCode()).thenReturn(OrderCode.of("PO-2026-0200"));
    }

    private static RequisitionOutput requisition(String code, String status, String purchaseOrderId) {
        RequisitionOutput out = new RequisitionOutput();
        out.setRequisitionCode(code);
        out.setStatus(status);
        out.setPurchaseOrderId(purchaseOrderId);
        return out;
    }

    private CreatePurchaseOrderInput createFromRequisition(String clientCode) {
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setMaterialCode("MAT-1");
        line.setQuantity(3);
        line.setUnitPrice(Money.of("4.00", CurrencyCode.MAD));
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setRequisitionId(requisitionId);
        input.setRequisitionCode(clientCode);
        input.setSupplierId(UUID.randomUUID());
        input.setSupplierName("Acme Supplies");
        input.setOrderedBy("buyer-1");
        input.setCurrencyCode("MAD");
        input.setLines(new ArrayList<>(List.of(line)));
        input.setUserId("buyer-1");
        return input;
    }

    // ---- Conversion on create ----

    @Test
    @DisplayName("convert: the order is saved first, then the requisition is converted with the saved id and code (US2-1)")
    void create_savesThenConverts() {
        when(requisitionUseCase.getById(requisitionId)).thenReturn(requisition("REQ-2026-0007", "APPROVED", null));

        PurchaseOrderOutput out = service.create(createFromRequisition("REQ-2026-0007"));

        InOrder order = inOrder(requisitionUseCase, repository);
        order.verify(requisitionUseCase).getById(requisitionId);
        order.verify(repository).save(any(PurchaseOrder.class));
        order.verify(requisitionUseCase).convert(requisitionId, out.getId().toString(), "PO-2026-0200", "buyer-1");
    }

    @Test
    @DisplayName("convert: the requisition code is copied from the requisition, not trusted from the client (US2-1)")
    void create_copiesRequisitionCodeFromSource() {
        when(requisitionUseCase.getById(requisitionId)).thenReturn(requisition("REQ-2026-0007", "APPROVED", null));

        PurchaseOrderOutput out = service.create(createFromRequisition("REQ-FORGED"));

        assertEquals("REQ-2026-0007", out.getRequisitionCode());
    }

    @Test
    @DisplayName("convert: a requisition that is not approved makes creation fail; the failure propagates (US2-2, FR-007)")
    void create_conversionRefused_propagates() {
        when(requisitionUseCase.getById(requisitionId)).thenReturn(requisition("REQ-2026-0007", "SUBMITTED", null));
        doThrow(new RequisitionInvalidStatusTransitionException("Only approved requisitions can be converted"))
                .when(requisitionUseCase).convert(any(), anyString(), anyString(), anyString());

        assertThrows(RequisitionInvalidStatusTransitionException.class,
                () -> service.create(createFromRequisition("REQ-2026-0007")));
    }

    @Test
    @DisplayName("convert: an unknown requisition makes creation fail before anything is saved (US2-3)")
    void create_unknownRequisition_savesNothing() {
        when(requisitionUseCase.getById(requisitionId)).thenThrow(new RequisitionNotFoundException(requisitionId));

        assertThrows(RequisitionNotFoundException.class, () -> service.create(createFromRequisition("REQ-2026-0007")));
        verify(repository, never()).save(any());
        verify(requisitionUseCase, never()).convert(any(), any(), any(), any());
    }

    // ---- The originating requisition cannot change ----

    @Test
    @DisplayName("edit: pointing an order at a different requisition is refused (US2-5)")
    void update_changedRequisition_isRefused() {
        PurchaseOrder order = anOrder().fromRequisition(requisitionId, "REQ-2026-0007").build();
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));
        UpdatePurchaseOrderInput edit = new UpdatePurchaseOrderInput();
        edit.setRequisitionId(UUID.randomUUID());

        assertThrows(PurchaseOrderValidationException.class, () -> service.update(order.getId(), edit));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("edit: sending the same or no requisition keeps the original link and code")
    void update_sameOrNoRequisition_keepsLink() {
        PurchaseOrder order = anOrder().fromRequisition(requisitionId, "REQ-2026-0007").build();
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));
        UpdatePurchaseOrderInput same = new UpdatePurchaseOrderInput();
        same.setRequisitionId(requisitionId);
        same.setRequisitionCode("REQ-FORGED");

        PurchaseOrderOutput out = service.update(order.getId(), same);
        assertEquals(requisitionId, out.getRequisitionId());
        assertEquals("REQ-2026-0007", out.getRequisitionCode());

        PurchaseOrderOutput none = service.update(order.getId(), new UpdatePurchaseOrderInput());
        assertEquals(requisitionId, none.getRequisitionId());
    }

    // ---- Release on withdrawal (T029) ----

    private PurchaseOrder storedFromRequisition(OrderStatus status) {
        PurchaseOrder order = anOrder().fromRequisition(requisitionId, "REQ-2026-0007").inStatus(status).build();
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));
        return order;
    }

    @Test
    @DisplayName("release: deleting, cancelling or rejecting releases a requisition converted to this order (US2-6)")
    void withdrawal_releasesLinkedRequisition() {
        PurchaseOrder deleted = storedFromRequisition(OrderStatus.DRAFT);
        PurchaseOrder cancelled = storedFromRequisition(OrderStatus.CONFIRMED);
        PurchaseOrder rejected = storedFromRequisition(OrderStatus.SUBMITTED);
        when(requisitionUseCase.getById(requisitionId))
                .thenReturn(requisition("REQ-2026-0007", "CONVERTED", deleted.getId().toString()))
                .thenReturn(requisition("REQ-2026-0007", "CONVERTED", cancelled.getId().toString()))
                .thenReturn(requisition("REQ-2026-0007", "CONVERTED", rejected.getId().toString()));

        service.delete(deleted.getId(), "admin-1");
        service.cancel(cancelled.getId(), "buyer-1", "Not needed");
        service.reject(rejected.getId(), "admin-1", "Supplier declined");

        verify(requisitionUseCase).revertConversion(requisitionId, deleted.getId().toString(), "admin-1");
        verify(requisitionUseCase).revertConversion(requisitionId, cancelled.getId().toString(), "buyer-1");
        verify(requisitionUseCase).revertConversion(requisitionId, rejected.getId().toString(), "admin-1");
    }

    @Test
    @DisplayName("release: a requisition never converted, or linked to another order, is left untouched (US2-7)")
    void withdrawal_unlinkedRequisition_isUntouched() {
        PurchaseOrder notConverted = storedFromRequisition(OrderStatus.CONFIRMED);
        PurchaseOrder otherLink = storedFromRequisition(OrderStatus.CONFIRMED);
        when(requisitionUseCase.getById(requisitionId))
                .thenReturn(requisition("REQ-2026-0007", "APPROVED", null))
                .thenReturn(requisition("REQ-2026-0007", "CONVERTED", UUID.randomUUID().toString()));

        assertEquals("CANCELLED", service.cancel(notConverted.getId(), "buyer-1", "x").getStatus());
        assertEquals("CANCELLED", service.cancel(otherLink.getId(), "buyer-1", "x").getStatus());

        verify(requisitionUseCase, never()).revertConversion(any(), any(), any());
    }

    @Test
    @DisplayName("release: an order created without a requisition is withdrawn with no requisition involved (US2-8)")
    void withdrawal_withoutRequisition_neverConsultsRequisitions() {
        PurchaseOrder order = anOrder().inStatus(OrderStatus.CONFIRMED).build();
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));

        service.cancel(order.getId(), "buyer-1", "x");

        verifyNoInteractions(requisitionUseCase);
    }

    @Test
    @DisplayName("release: a refused withdrawal releases nothing")
    void refusedWithdrawal_releasesNothing() {
        PurchaseOrder order = storedFromRequisition(OrderStatus.COMPLETED);

        assertThrows(RuntimeException.class, () -> service.cancel(order.getId(), "buyer-1", "x"));

        verify(requisitionUseCase, never()).revertConversion(any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("release: the single-argument delete also releases the requisition (records no actor, F-007)")
    void delete_withoutActor_stillReleases() {
        PurchaseOrder order = storedFromRequisition(OrderStatus.DRAFT);
        when(requisitionUseCase.getById(requisitionId))
                .thenReturn(requisition("REQ-2026-0007", "CONVERTED", order.getId().toString()));

        service.delete(order.getId());

        verify(requisitionUseCase).revertConversion(eq(requisitionId), eq(order.getId().toString()), isNull());
    }
}
