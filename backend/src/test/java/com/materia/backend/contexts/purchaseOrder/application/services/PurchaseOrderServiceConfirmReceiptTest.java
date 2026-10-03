package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.purchaseOrder.application.mappers.PurchaseOrderMapper;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotFoundException;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/** [T037] Confirming receipt on an order delegates to a real goods receipt; it never flips the status itself (US3-8). */
@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceConfirmReceiptTest {

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
    }

    @Test
    @DisplayName("confirm receipt: hands off to the goods receipt module once and returns the reloaded order")
    void confirmReceipt_delegatesToGoodsReceipt() {
        PurchaseOrder ready = anOrder().inStatus(OrderStatus.READY_FOR_RECEIPT).build();
        PurchaseOrder afterReceipt = anOrder().inStatus(OrderStatus.COMPLETED).build();
        when(repository.findById(ready.getId())).thenReturn(Optional.of(ready), Optional.of(afterReceipt));

        var out = service.confirmReceipt(ready.getId(), "receiver-1", "Rita");

        verify(goodsReceiptUseCase).receiveRemaining(ready.getId().toString(), "receiver-1", "Rita");
        assertEquals("COMPLETED", out.getStatus());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("confirm receipt: an unknown order is not found and no receipt is attempted")
    void confirmReceipt_unknownOrder_isNotFound() {
        UUID missing = UUID.randomUUID();
        when(repository.findById(missing)).thenReturn(Optional.empty());

        assertThrows(PurchaseOrderNotFoundException.class, () -> service.confirmReceipt(missing, "receiver-1", "Rita"));
        verifyNoInteractions(goodsReceiptUseCase);
    }
}
