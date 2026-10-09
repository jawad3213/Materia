package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.requisition.RequisitionSourceAdapter;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.goodsReceipt.GoodsReceiptRecordsAdapter;
import com.materia.backend.common.domain.fx.FxDocument;
import com.materia.backend.common.domain.fx.ReportingFx;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.purchaseOrder.application.mappers.PurchaseOrderMapper;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Confirming an order values it in the reporting currency; submitting or a refused confirmation does not. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PurchaseOrderFxValuationTest {

    @Mock private PurchaseOrderRepository repository;
    @Mock private PurchaseOrderCodeGeneratorService codeGenerator;
    @Mock private GoodsReceiptUseCase goodsReceiptUseCase;
    @Mock private RequisitionUseCase requisitionUseCase;
    @Mock private PurchaseOrderEventPublisher events;
    @Mock private ReceiverDirectory receiverDirectory;
    @Mock private ReportingFx reportingFx;

    private PurchaseOrderService service;

    @BeforeEach
    void setUp() {
        service = new PurchaseOrderService(repository, new PurchaseOrderMapper(), codeGenerator,
                new GoodsReceiptRecordsAdapter(goodsReceiptUseCase), new RequisitionSourceAdapter(requisitionUseCase),
                events, receiverDirectory);
        service.setReportingFx(reportingFx);
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PurchaseOrder stored(OrderStatus status) {
        PurchaseOrder order = anOrder().inStatus(status).build();
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));
        return order;
    }

    @Test
    @DisplayName("rule: a confirmed order is valued at the rate of its order date")
    void confirm_valuesTheOrder() {
        PurchaseOrder order = stored(OrderStatus.SUBMITTED);

        service.confirm(order.getId(), "admin-1");

        verify(reportingFx).valueDocument(FxDocument.PURCHASE_ORDER, order.getId());
    }

    @Test
    @DisplayName("rule: submitting, or a refused confirmation, values nothing")
    void notCommitted_notValued() {
        PurchaseOrder draft = stored(OrderStatus.DRAFT);
        PurchaseOrder completed = stored(OrderStatus.COMPLETED);

        service.submit(draft.getId(), "buyer-1");
        assertThrows(RuntimeException.class, () -> service.confirm(completed.getId(), "admin-1"));

        verifyNoInteractions(reportingFx);
    }
}
