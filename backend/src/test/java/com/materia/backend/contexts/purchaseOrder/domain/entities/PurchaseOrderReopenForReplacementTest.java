package com.materia.backend.contexts.purchaseOrder.domain.entities;

import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;
import java.util.Set;

import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;

/** A replacement for returned goods reopens a received or closed order so the replacement can be received. */
class PurchaseOrderReopenForReplacementTest {

    private static final Set<OrderStatus> REOPENABLE =
            EnumSet.of(OrderStatus.PARTIALLY_RECEIVED, OrderStatus.COMPLETED);

    @ParameterizedTest(name = "{0}")
    @EnumSource(OrderStatus.class)
    @DisplayName("reopen: received or closed orders go back to partially received; any other status is refused unchanged")
    void reopen_byStatus(OrderStatus status) {
        PurchaseOrder order = anOrder().withLine(10, "5.00").inStatus(status).build();

        if (REOPENABLE.contains(status)) {
            order.reopenForReplacement("buyer-1");
            assertEquals(OrderStatus.PARTIALLY_RECEIVED, order.getStatus());
            assertEquals(DeliveryStatus.PARTIAL, order.getDeliveryStatus());
            assertNull(order.getReceivedDate());
            assertEquals("buyer-1", order.getUpdatedBy());
        } else {
            assertThrows(PurchaseOrderInvalidStatusTransitionException.class, () -> order.reopenForReplacement("buyer-1"));
            assertEquals(status, order.getStatus());
        }
    }
}
