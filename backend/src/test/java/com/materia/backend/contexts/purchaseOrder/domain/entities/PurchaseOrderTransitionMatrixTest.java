package com.materia.backend.contexts.purchaseOrder.domain.entities;

import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus.*;
import static com.materia.backend.support.TimeAssertions.assertWithin;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Every cell of the purchase order transition table in {@code specs/002-purchase-order-tests/data-model.md}
 * (US1, FR-002, SC-001): 9 statuses × 9 actions = 81 cells.
 *
 * <p>Permitted cells assert the resulting status. Forbidden cells assert refusal <em>and</em> that
 * the order is left unchanged, since a transition that throws after mutating would silently corrupt it.
 */
class PurchaseOrderTransitionMatrixTest {

    private static final String USER = "user-1";

    enum Action {
        SUBMIT(o -> o.submit(USER)),
        CONFIRM(o -> o.confirm(USER)),
        REJECT(o -> o.reject(USER, "Supplier cannot deliver")),
        ASSIGN(o -> o.assignReceiver(USER, "Bob Buyer", "receiver-2", "Rae Receiver")),
        TRACK(o -> o.updateDeliveryStatus(DeliveryStatus.IN_TRANSIT, USER)),
        RECEIVE_ALL(o -> o.recordReceipt(true, USER)),
        RECEIVE_PART(o -> o.recordReceipt(false, USER)),
        COMPLETE(o -> o.complete(USER)),
        CANCEL(o -> o.cancel(USER, "No longer needed"));

        final Consumer<PurchaseOrder> apply;

        Action(Consumer<PurchaseOrder> apply) {
            this.apply = apply;
        }
    }

    /** The permitted cells and where each leads. Every other cell must be refused. */
    private static final Map<OrderStatus, Map<Action, OrderStatus>> PERMITTED = Map.of(
            DRAFT, Map.of(Action.SUBMIT, SUBMITTED, Action.CANCEL, CANCELLED),
            SUBMITTED, Map.of(Action.CONFIRM, CONFIRMED, Action.REJECT, REJECTED, Action.CANCEL, CANCELLED),
            CONFIRMED, Map.of(Action.ASSIGN, READY_FOR_RECEIPT, Action.TRACK, CONFIRMED, Action.CANCEL, CANCELLED),
            READY_FOR_RECEIPT, Map.of(Action.TRACK, READY_FOR_RECEIPT, Action.RECEIVE_ALL, COMPLETED,
                    Action.RECEIVE_PART, PARTIALLY_RECEIVED, Action.CANCEL, CANCELLED),
            PARTIALLY_RECEIVED, Map.of(Action.TRACK, PARTIALLY_RECEIVED, Action.RECEIVE_ALL, COMPLETED,
                    Action.RECEIVE_PART, PARTIALLY_RECEIVED, Action.COMPLETE, COMPLETED),
            RECEIVED, Map.of(Action.COMPLETE, COMPLETED),
            COMPLETED, Map.of(),
            CANCELLED, Map.of(),
            REJECTED, Map.of()
    );

    static Stream<Arguments> permittedCells() {
        return PERMITTED.entrySet().stream().flatMap(from -> from.getValue().entrySet().stream()
                .map(cell -> Arguments.of(from.getKey(), cell.getKey(), cell.getValue())));
    }

    static Stream<Arguments> forbiddenCells() {
        return Arrays.stream(OrderStatus.values()).flatMap(from -> Arrays.stream(Action.values())
                .filter(action -> !PERMITTED.get(from).containsKey(action))
                .map(action -> Arguments.of(from, action)));
    }

    @Test
    @DisplayName("matrix: the table covers all 81 cells, one row per status (SC-001)")
    void matrix_coversEveryCell() {
        assertEquals(EnumSet.allOf(OrderStatus.class), PERMITTED.keySet());
        assertEquals(81, permittedCells().count() + forbiddenCells().count());
    }

    @ParameterizedTest(name = "{0} --{1}--> {2}")
    @MethodSource("permittedCells")
    @DisplayName("matrix: a permitted transition succeeds and reaches the expected status (US1)")
    void permittedTransition_succeeds(OrderStatus from, Action action, OrderStatus to) {
        PurchaseOrder order = anOrder().withLine(10, "5.00").inStatus(from).build();

        action.apply.accept(order);

        assertEquals(to, order.getStatus());
    }

    @ParameterizedTest(name = "{0} --{1}--> refused")
    @MethodSource("forbiddenCells")
    @DisplayName("matrix: a forbidden transition is refused and leaves the order untouched (US1, FR-002)")
    void forbiddenTransition_isRefusedWithoutSideEffects(OrderStatus from, Action action) {
        PurchaseOrder order = anOrder().withLine(10, "5.00").inStatus(from).build();
        DeliveryStatus deliveryBefore = order.getDeliveryStatus();
        String notesBefore = order.getNotes();
        String assigneeBefore = order.getAssignedTo();

        assertThrows(PurchaseOrderInvalidStatusTransitionException.class, () -> action.apply.accept(order));

        assertEquals(from, order.getStatus());
        assertEquals(deliveryBefore, order.getDeliveryStatus());
        assertEquals(notesBefore, order.getNotes());
        assertEquals(assigneeBefore, order.getAssignedTo());
    }

    @ParameterizedTest(name = "no action from {0} yields a retired status")
    @EnumSource(OrderStatus.class)
    @DisplayName("matrix: no action from any status yields a status outside the 9 defined ones (IN_PROGRESS retired)")
    void noActionYieldsRetiredStatus(OrderStatus from) {
        Set<String> defined = Set.of("DRAFT", "SUBMITTED", "CONFIRMED", "READY_FOR_RECEIPT", "PARTIALLY_RECEIVED",
                "RECEIVED", "COMPLETED", "CANCELLED", "REJECTED");
        assertEquals(defined.size(), OrderStatus.values().length, "OrderStatus gained or lost a constant");
        for (Action action : PERMITTED.get(from).keySet()) {
            PurchaseOrder order = anOrder().inStatus(from).build();
            action.apply.accept(order);
            assertTrue(defined.contains(order.getStatus().name()), action + " from " + from + " gave " + order.getStatus());
        }
    }

    @ParameterizedTest(name = "{0}: DELIVERED and PARTIAL refused")
    @EnumSource(OrderStatus.class)
    @DisplayName("tracking: delivered and partial can never be set directly, only by a recorded receipt (US1-8)")
    void deliveredAndPartial_areRefusedFromEveryStatus(OrderStatus from) {
        for (DeliveryStatus forbidden : new DeliveryStatus[]{DeliveryStatus.DELIVERED, DeliveryStatus.PARTIAL}) {
            PurchaseOrder order = anOrder().inStatus(from).build();
            DeliveryStatus before = order.getDeliveryStatus();

            assertThrows(PurchaseOrderInvalidStatusTransitionException.class,
                    () -> order.updateDeliveryStatus(forbidden, USER));

            assertEquals(from, order.getStatus());
            assertEquals(before, order.getDeliveryStatus());
        }
    }

    @ParameterizedTest(name = "{0}: tracking to {1} keeps the order status")
    @MethodSource("trackingCells")
    @DisplayName("tracking: shipped, in transit, delayed and not shipped change only the delivery status (US1-7)")
    void tracking_changesOnlyDeliveryStatus(OrderStatus from, DeliveryStatus tracking) {
        PurchaseOrder order = anOrder().inStatus(from).build();

        order.updateDeliveryStatus(tracking, USER);

        assertEquals(from, order.getStatus());
        assertEquals(tracking, order.getDeliveryStatus());
    }

    static Stream<Arguments> trackingCells() {
        return Stream.of(CONFIRMED, READY_FOR_RECEIPT, PARTIALLY_RECEIVED).flatMap(status -> Stream.of(
                        DeliveryStatus.NOT_SHIPPED, DeliveryStatus.SHIPPED, DeliveryStatus.IN_TRANSIT, DeliveryStatus.DELAYED)
                .map(tracking -> Arguments.of(status, tracking)));
    }

    // ---- Side effects of permitted transitions ----

    @Test
    @DisplayName("confirm: records today's date as the confirmed delivery date (US1-2)")
    void confirm_recordsConfirmationDate() {
        PurchaseOrder order = anOrder().inStatus(SUBMITTED).build();
        LocalDateTime before = LocalDateTime.now();

        order.confirm(USER);

        assertWithin(order.getConfirmedDeliveryDate(), before, LocalDateTime.now());
    }

    @Test
    @DisplayName("reject: keeps the supplier's reason on the order (US1-3)")
    void reject_keepsReasonInNotes() {
        PurchaseOrder order = anOrder().inStatus(SUBMITTED).build();

        order.reject(USER, "Out of stock until May");

        assertTrue(order.getNotes().contains("Out of stock until May"));
    }

    @Test
    @DisplayName("assign: records who was assigned, by whom, and when (US1-5)")
    void assign_recordsAssignment() {
        PurchaseOrder order = anOrder().inStatus(CONFIRMED).build();
        LocalDateTime before = LocalDateTime.now();

        order.assignReceiver("buyer-9", "Bea Buyer", "receiver-9", "Rex Receiver");

        assertEquals("receiver-9", order.getAssignedTo());
        assertEquals("Rex Receiver", order.getAssignedToName());
        assertEquals("buyer-9", order.getAssignedBy());
        assertEquals("Bea Buyer", order.getAssignedByName());
        assertWithin(order.getAssignedAt(), before, LocalDateTime.now());
    }

    @Test
    @DisplayName("receipt: a full receipt completes the order as delivered and records the received date (US3-1)")
    void fullReceipt_completesAsDelivered() {
        PurchaseOrder order = anOrder().inStatus(READY_FOR_RECEIPT).build();
        LocalDateTime before = LocalDateTime.now();

        order.recordReceipt(true, USER);

        assertEquals(COMPLETED, order.getStatus());
        assertEquals(DeliveryStatus.DELIVERED, order.getDeliveryStatus());
        assertWithin(order.getReceivedDate(), before, LocalDateTime.now());
    }

    @Test
    @DisplayName("receipt: a partial receipt marks the order partially received and partially delivered (US3-2)")
    void partialReceipt_marksPartial() {
        PurchaseOrder order = anOrder().inStatus(READY_FOR_RECEIPT).build();

        order.recordReceipt(false, USER);

        assertEquals(PARTIALLY_RECEIVED, order.getStatus());
        assertEquals(DeliveryStatus.PARTIAL, order.getDeliveryStatus());
        assertNull(order.getReceivedDate());
    }

    @Test
    @DisplayName("close short: completing a partially received order records the received date (US1-10)")
    void closeShort_recordsReceivedDate() {
        PurchaseOrder order = anOrder().inStatus(PARTIALLY_RECEIVED).build();
        LocalDateTime before = LocalDateTime.now();

        order.complete(USER);

        assertEquals(COMPLETED, order.getStatus());
        assertWithin(order.getReceivedDate(), before, LocalDateTime.now());
    }

    @ParameterizedTest(name = "{0} modifiable = {1}")
    @MethodSource("modifiability")
    @DisplayName("editing: only draft and submitted orders can be edited or deleted (US1-14, US1-15)")
    void onlyDraftAndSubmittedAreModifiable(OrderStatus status, boolean modifiable) {
        assertEquals(modifiable, anOrder().inStatus(status).build().isModifiable());
    }

    static Stream<Arguments> modifiability() {
        return Arrays.stream(OrderStatus.values())
                .map(s -> Arguments.of(s, s == DRAFT || s == SUBMITTED));
    }
}
