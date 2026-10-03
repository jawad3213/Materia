package com.materia.backend.contexts.purchaseOrder;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionRepository;
import com.materia.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.materia.backend.support.fixtures.RequisitionFixtures.aRequisition;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * [T033] Two simultaneous conversions of one approved requisition produce exactly one order (FR-009, research R5).
 *
 * <p>The requisition's optimistic-lock version makes the second commit fail and roll back its order.
 * The assertion is on the final state, not on which thread wins, so the outcome is deterministic.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PurchaseOrderConcurrentConversionIT extends AbstractIntegrationTest {

    private static final int ATTEMPTS = 2;

    @Autowired private PurchaseOrderUseCase orders;
    @Autowired private PurchaseOrderRepository orderRepository;
    @Autowired private RequisitionRepository requisitions;
    @Autowired private JdbcTemplate jdbc;

    private UUID requisitionId;

    @AfterEach
    void cleanUp() {
        if (requisitionId == null) {
            return;
        }
        jdbc.update("delete from purchase_order_lines where purchase_order_id in "
                + "(select id from purchase_orders where requisition_id = ?)", requisitionId);
        jdbc.update("delete from purchase_orders where requisition_id = ?", requisitionId);
        jdbc.update("delete from purchase_requisition_lines where requisition_id = ?", requisitionId);
        jdbc.update("delete from purchase_requisitions where id = ?", requisitionId);
    }

    private CreatePurchaseOrderInput fromRequisition(UUID id) {
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setMaterialCode("MAT-1");
        line.setQuantity(1);
        line.setUnitPrice(Money.of("4.00", CurrencyCode.MAD));
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setRequisitionId(id);
        input.setSupplierId(UUID.randomUUID());
        input.setSupplierName("Acme Supplies");
        input.setOrderedBy("buyer-1");
        input.setCurrencyCode("MAD");
        input.setLines(new ArrayList<>(List.of(line)));
        input.setUserId("buyer-1");
        return input;
    }

    @Test
    @DisplayName("concurrency: two simultaneous orders from one approved requisition leave exactly one order (FR-009)")
    void simultaneousConversions_leaveExactlyOneOrder() throws Exception {
        Requisition requisition = requisitions.save(aRequisition().inStatus(RequisitionStatus.APPROVED).build());
        requisitionId = requisition.getId();

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(ATTEMPTS);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < ATTEMPTS; i++) {
            results.add(pool.submit(() -> {
                start.await();
                try {
                    orders.create(fromRequisition(requisitionId));
                    return true;
                } catch (RuntimeException refused) {
                    return false;
                }
            }));
        }
        start.countDown();
        pool.shutdown();
        pool.awaitTermination(60, TimeUnit.SECONDS);

        int succeeded = 0;
        for (Future<Boolean> result : results) {
            succeeded += result.get() ? 1 : 0;
        }
        assertEquals(1, orderRepository.findByRequisitionId(requisitionId).size(), "orders committed for the requisition");
        assertEquals(1, succeeded, "successful conversions");
        assertEquals(RequisitionStatus.CONVERTED, requisitions.findById(requisitionId).orElseThrow().getStatus());
    }
}
