package com.materia.backend.support.fixtures;

import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** [T014] The builders reach every status through real transitions, so later tests can rely on them. */
class FixturesReachabilityTest {

    @ParameterizedTest(name = "order fixture reaches {0}")
    @EnumSource(OrderStatus.class)
    @DisplayName("fixtures: every purchase order status is reachable through the builder")
    void everyOrderStatusIsReachable(OrderStatus status) {
        PurchaseOrder order = anOrder().withLine(10, "5.00").inStatus(status).build();

        assertEquals(status, order.getStatus());
    }

    @ParameterizedTest(name = "receipt fixture reaches {0}")
    @EnumSource(ReceiptStatus.class)
    @DisplayName("fixtures: every goods receipt status is reachable through the builder")
    void everyReceiptStatusIsReachable(ReceiptStatus status) {
        PurchaseOrder order = anOrder().withLine(10, "5.00").inStatus(OrderStatus.READY_FOR_RECEIPT).build();

        GoodsReceipt receipt = aReceipt(order).inStatus(status).build();

        assertEquals(status, receipt.getStatus());
        assertNotNull(receipt.getReceiptCode());
    }
}
