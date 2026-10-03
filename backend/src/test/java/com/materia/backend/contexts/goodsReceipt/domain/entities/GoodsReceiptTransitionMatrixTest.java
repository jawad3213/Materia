package com.materia.backend.contexts.goodsReceipt.domain.entities;

import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptBusinessException;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus.*;
import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;

/**
 * [T047] Every cell of the goods receipt transition table in data-model.md: 5 statuses × 4 actions,
 * plus modifiability (US6-5). {@code IN_PROGRESS} is built directly: nothing produces it (F-004).
 */
class GoodsReceiptTransitionMatrixTest {

    enum Action {
        ADD_LINE(r -> r.addLine(GoodsReceiptLine.builder().materialCode("MAT-EXTRA").quantityOrdered(1)
                .quantityReceived(1).quantityRejected(0).qualityStatus(QualityStatus.ACCEPTED).build())),
        REMOVE_LINE(r -> r.removeLine(1)),
        COMPLETE(r -> r.complete("receiver-1")),
        CANCEL(r -> r.cancel("receiver-1", "Recorded in error"));

        final Consumer<GoodsReceipt> apply;

        Action(Consumer<GoodsReceipt> apply) {
            this.apply = apply;
        }
    }

    /** Permitted cells and the status each leads to. Every other cell must be refused. */
    private static final Map<ReceiptStatus, Map<Action, ReceiptStatus>> PERMITTED = Map.of(
            DRAFT, Map.of(Action.ADD_LINE, DRAFT, Action.REMOVE_LINE, DRAFT, Action.COMPLETE, COMPLETED, Action.CANCEL, CANCELLED),
            IN_PROGRESS, Map.of(Action.ADD_LINE, IN_PROGRESS, Action.REMOVE_LINE, IN_PROGRESS,
                    Action.COMPLETE, COMPLETED, Action.CANCEL, CANCELLED),
            COMPLETED, Map.of(),
            PARTIAL, Map.of(),
            CANCELLED, Map.of()
    );

    private static GoodsReceipt receiptIn(ReceiptStatus status) {
        PurchaseOrder order = anOrder().withLine(10, "5.00").withLine(4, "2.00").inStatus(OrderStatus.READY_FOR_RECEIPT).build();
        return aReceipt(order).inStatus(status).build();
    }

    static Stream<Arguments> permittedCells() {
        return PERMITTED.entrySet().stream().flatMap(from -> from.getValue().entrySet().stream()
                .map(cell -> Arguments.of(from.getKey(), cell.getKey(), cell.getValue())));
    }

    static Stream<Arguments> forbiddenCells() {
        return Arrays.stream(ReceiptStatus.values()).flatMap(from -> Arrays.stream(Action.values())
                .filter(action -> !PERMITTED.get(from).containsKey(action))
                .map(action -> Arguments.of(from, action)));
    }

    @Test
    @DisplayName("matrix: the table covers all 20 cells, one row per receipt status")
    void matrix_coversEveryCell() {
        assertEquals(EnumSet.allOf(ReceiptStatus.class), PERMITTED.keySet());
        assertEquals(20, permittedCells().count() + forbiddenCells().count());
    }

    @ParameterizedTest(name = "{0} --{1}--> {2}")
    @MethodSource("permittedCells")
    @DisplayName("matrix: an open receipt accepts the action and reaches the expected status (US6-2, US6-3)")
    void permitted_succeeds(ReceiptStatus from, Action action, ReceiptStatus to) {
        GoodsReceipt receipt = receiptIn(from);

        action.apply.accept(receipt);

        assertEquals(to, receipt.getStatus());
    }

    @ParameterizedTest(name = "{0} --{1}--> refused")
    @MethodSource("forbiddenCells")
    @DisplayName("matrix: a closed receipt refuses every change and is left untouched (US6-5)")
    void forbidden_isRefusedWithoutSideEffects(ReceiptStatus from, Action action) {
        GoodsReceipt receipt = receiptIn(from);
        int linesBefore = receipt.getLines().size();
        Integer receivedBefore = receipt.getTotalQuantityReceived();

        assertThrows(GoodsReceiptBusinessException.class, () -> action.apply.accept(receipt));

        assertEquals(from, receipt.getStatus());
        assertEquals(linesBefore, receipt.getLines().size());
        assertEquals(receivedBefore, receipt.getTotalQuantityReceived());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(ReceiptStatus.class)
    @DisplayName("matrix: only draft and in-progress receipts are modifiable, cancellable or open")
    void openStatuses(ReceiptStatus status) {
        boolean open = status == DRAFT || status == IN_PROGRESS;

        assertEquals(open, status.isModifiable());
        assertEquals(open, status.isCancellable());
        assertEquals(status == COMPLETED || status == PARTIAL, receiptIn(status).isComplete());
    }

    @Test
    @DisplayName("lines: adding or removing a line on an open receipt recalculates the totals")
    void lineChanges_recalculateTotals() {
        GoodsReceipt receipt = receiptIn(DRAFT);

        Action.ADD_LINE.apply.accept(receipt);
        assertEquals(15, receipt.getTotalQuantityReceived());

        receipt.removeLine(0);
        assertEquals(5, receipt.getTotalQuantityReceived());
    }

    @Test
    @DisplayName("cancel: the reason is kept in the receipt notes")
    void cancel_keepsReason() {
        GoodsReceipt receipt = receiptIn(DRAFT);

        receipt.cancel("receiver-1", "Wrong truck");

        assertTrue(receipt.getNotes().contains("Wrong truck"));
    }
}
