package com.materia.backend.contexts.purchaseRequisition.domain.entities;

import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionInvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus.*;
import static com.materia.backend.support.fixtures.RequisitionFixtures.aRequisition;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Every cell of the requisition transition matrix in {@code data-model.md} (FR-005, SC-003),
 * exercised against the entity methods that actually enforce it.
 *
 * <p>The earlier {@code RequisitionStatusLifecycleTest} checks the status enum's predicates.
 * This class checks the behaviour itself. Permitted cells assert the resulting status;
 * forbidden cells assert refusal <em>and</em> that the requisition is left completely
 * unchanged, since a transition that throws after mutating would be a silent corruption.
 */
class RequisitionTransitionMatrixTest {

    enum Action {
        SUBMIT(r -> r.submit("user-1")),
        APPROVE(r -> r.approve("approver-1", "Approver", "ok")),
        REJECT(r -> r.reject("approver-1", "Approver", "over budget")),
        CANCEL(r -> r.cancel("user-1", "no longer needed")),
        CONVERT(r -> r.convert("po-1", "PO-2026-0001", "user-1"));

        final Consumer<Requisition> apply;

        Action(Consumer<Requisition> apply) {
            this.apply = apply;
        }
    }

    /** The permitted cells and where each leads. Every other cell must be refused. */
    private static final Map<RequisitionStatus, Map<Action, RequisitionStatus>> PERMITTED = Map.of(
            DRAFT, Map.of(Action.SUBMIT, SUBMITTED),
            SUBMITTED, Map.of(Action.APPROVE, APPROVED, Action.REJECT, REJECTED, Action.CANCEL, CANCELLED),
            APPROVED, Map.of(Action.CANCEL, CANCELLED, Action.CONVERT, CONVERTED),
            REJECTED, Map.of(),
            CANCELLED, Map.of(),
            CONVERTED, Map.of()
    );

    static Stream<Arguments> permittedCells() {
        return PERMITTED.entrySet().stream().flatMap(from -> from.getValue().entrySet().stream()
                .map(cell -> Arguments.of(from.getKey(), cell.getKey(), cell.getValue())));
    }

    static Stream<Arguments> forbiddenCells() {
        return Arrays.stream(RequisitionStatus.values()).flatMap(from -> Arrays.stream(Action.values())
                .filter(action -> !PERMITTED.get(from).containsKey(action))
                .map(action -> Arguments.of(from, action)));
    }

    @ParameterizedTest(name = "{0} --{1}--> {2}")
    @MethodSource("permittedCells")
    @DisplayName("matrix: a permitted transition succeeds and reaches the expected status")
    void permittedTransition_succeeds(RequisitionStatus from, Action action, RequisitionStatus to) {
        Requisition requisition = aRequisition().inStatus(from).build();

        action.apply.accept(requisition);

        assertEquals(to, requisition.getStatus());
    }

    @ParameterizedTest(name = "{0} --{1}--> refused")
    @MethodSource("forbiddenCells")
    @DisplayName("matrix: a forbidden transition is refused and leaves the requisition untouched")
    void forbiddenTransition_isRefusedWithoutSideEffects(RequisitionStatus from, Action action) {
        Requisition requisition = aRequisition().inStatus(from).build();
        Snapshot before = Snapshot.of(requisition);

        assertThrows(RequisitionInvalidStatusTransitionException.class, () -> action.apply.accept(requisition));

        assertEquals(before, Snapshot.of(requisition), "a refused transition must not change anything");
    }

    @org.junit.jupiter.api.Test
    @DisplayName("matrix: it is complete, covering all 30 cells with 6 permitted")
    void matrixIsComplete() {
        assertEquals(6, permittedCells().count());
        assertEquals(24, forbiddenCells().count());
    }

    /** The fields any transition could touch, compared as a whole. */
    record Snapshot(RequisitionStatus status, Object submitted, Object approved, Object converted,
                    Object cancelled, String approverId, String rejectionReason, String cancellationReason,
                    String purchaseOrderId) {
        static Snapshot of(Requisition r) {
            return new Snapshot(r.getStatus(), r.getSubmittedDate(), r.getApprovedDate(), r.getConvertedDate(),
                    r.getCancelledDate(), r.getApproverId(), r.getRejectionReason(), r.getCancellationReason(),
                    r.getPurchaseOrderId());
        }
    }
}
