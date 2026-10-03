package com.materia.backend.contexts.purchaseOrder.domain.enums;

import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.EnumSet;
import java.util.Set;

import static com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus.*;
import static org.junit.jupiter.api.Assertions.*;

/** [T020] Status code parsing and the lifecycle predicates (data-model.md). */
class OrderStatusAndDeliveryStatusTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(OrderStatus.class)
    @DisplayName("order status: every constant round-trips through its code and carries display metadata")
    void orderStatus_roundTrips(OrderStatus status) {
        assertEquals(status, OrderStatus.fromCode(status.getCode()));
        assertTrue(OrderStatus.isValidCode(status.getCode()));
        assertFalse(status.getLabel().isBlank());
        assertFalse(status.getDescription().isBlank());
        assertTrue(status.getColor().startsWith("#"));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"IN_PROGRESS", "draft", "UNKNOWN"})
    @DisplayName("order status: blank, unknown, lower-case and retired codes are refused")
    void orderStatus_unknownCodes_areRefused(String code) {
        assertThrows(PurchaseOrderValidationException.class, () -> OrderStatus.fromCode(code));
        assertFalse(OrderStatus.isValidCode(code));
    }

    @Test
    @DisplayName("order status: the code list names every constant")
    void orderStatus_codesListEveryConstant() {
        assertEquals(OrderStatus.values().length, OrderStatus.getCodes().size());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(OrderStatus.class)
    @DisplayName("order status: active, modifiable and cancellable match the data model")
    void orderStatus_predicates(OrderStatus status) {
        Set<OrderStatus> active = EnumSet.of(DRAFT, SUBMITTED, CONFIRMED, READY_FOR_RECEIPT, PARTIALLY_RECEIVED);
        Set<OrderStatus> modifiable = EnumSet.of(DRAFT, SUBMITTED);
        // Once goods have arrived the order can only be closed short (US1-13).
        Set<OrderStatus> cancellable = EnumSet.of(DRAFT, SUBMITTED, CONFIRMED, READY_FOR_RECEIPT);

        assertEquals(active.contains(status), status.isActive(), "isActive");
        assertEquals(modifiable.contains(status), status.isModifiable(), "isModifiable");
        assertEquals(cancellable.contains(status), status.isCancellable(), "isCancellable");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(DeliveryStatus.class)
    @DisplayName("delivery status: every constant round-trips through its code and carries a label")
    void deliveryStatus_roundTrips(DeliveryStatus status) {
        assertEquals(status, DeliveryStatus.fromCode(status.getCode()));
        assertFalse(status.getLabel().isBlank());
        assertFalse(status.getDescription().isBlank());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"LOST", "delivered"})
    @DisplayName("delivery status: blank and unknown codes are refused")
    void deliveryStatus_unknownCodes_areRefused(String code) {
        assertThrows(PurchaseOrderValidationException.class, () -> DeliveryStatus.fromCode(code));
    }
}
