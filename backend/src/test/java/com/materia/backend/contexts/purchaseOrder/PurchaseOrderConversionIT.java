package com.materia.backend.contexts.purchaseOrder;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionBusinessException;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionRepository;
import com.materia.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.RequisitionFixtures.aRequisition;
import static org.junit.jupiter.api.Assertions.*;

/**
 * [T031, T032] Conversion and release against the real database, with every step committed.
 *
 * <p>The base class rolls each test back, which would hide a missing rollback inside the service.
 * These tests opt out ({@code NOT_SUPPORTED}) so the service's own transaction is the only one, then
 * read back afterwards to see what really persisted (FR-007, research R4). Committed rows are
 * removed in {@link #cleanUp()}.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PurchaseOrderConversionIT extends AbstractIntegrationTest {

    @Autowired private PurchaseOrderUseCase orders;
    @Autowired private PurchaseOrderRepository orderRepository;
    @Autowired private RequisitionRepository requisitions;
    @Autowired private JdbcTemplate jdbc;

    private final List<UUID> createdRequisitions = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (UUID requisitionId : createdRequisitions) {
            jdbc.update("delete from purchase_order_lines where purchase_order_id in "
                    + "(select id from purchase_orders where requisition_id = ?)", requisitionId);
            jdbc.update("delete from purchase_orders where requisition_id = ?", requisitionId);
            jdbc.update("delete from purchase_requisition_lines where requisition_id = ?", requisitionId);
            jdbc.update("delete from purchase_requisitions where id = ?", requisitionId);
        }
        createdRequisitions.clear();
    }

    private Requisition committedRequisition(RequisitionStatus status) {
        Requisition saved = requisitions.save(aRequisition().inStatus(status).withLine(3, "4.00").build());
        createdRequisitions.add(saved.getId());
        return saved;
    }

    private CreatePurchaseOrderInput fromRequisition(UUID requisitionId) {
        // An order from a requisition orders the requisition's own lines.
        var requested = requisitions.findById(requisitionId).orElseThrow().getLines().get(0);
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setRequisitionLineId(requested.getId());
        line.setMaterialCode(requested.getMaterialCode());
        line.setQuantity(requested.getQuantity());
        line.setUnitPrice(Money.of("4.00", CurrencyCode.MAD));
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setRequisitionId(requisitionId);
        input.setSupplierId(UUID.randomUUID());
        input.setSupplierName("Acme Supplies");
        input.setOrderedBy("buyer-1");
        input.setCurrencyCode("MAD");
        input.setLines(new ArrayList<>(List.of(line)));
        input.setUserId("buyer-1");
        return input;
    }

    private long ordersFor(UUID requisitionId) {
        return orderRepository.findByRequisitionId(requisitionId).size();
    }

    // ---- T031: conversion ----

    @Test
    @DisplayName("convert: an order from an approved requisition is committed and both records point at each other (US2-1)")
    void create_fromApproved_linksBothRecords() {
        Requisition requisition = committedRequisition(RequisitionStatus.APPROVED);

        PurchaseOrderOutput order = orders.create(fromRequisition(requisition.getId()));

        Requisition converted = requisitions.findById(requisition.getId()).orElseThrow();
        assertEquals(RequisitionStatus.CONVERTED, converted.getStatus());
        assertEquals(order.getId().toString(), converted.getPurchaseOrderId());
        assertEquals(order.getOrderCode(), converted.getPurchaseOrderCode());
        assertEquals(requisition.getRequisitionCode().getValue(), order.getRequisitionCode());
        assertEquals(1, ordersFor(requisition.getId()));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = RequisitionStatus.class, names = "APPROVED", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("convert: from a requisition that is not approved, creation is refused and no order is left behind (US2-2, FR-007)")
    void create_fromNonApproved_leavesNothingBehind(RequisitionStatus status) {
        Requisition requisition = committedRequisition(status);

        assertThrows(RequisitionBusinessException.class, () -> orders.create(fromRequisition(requisition.getId())));

        assertEquals(0, ordersFor(requisition.getId()), "an order was committed despite the refused conversion");
        assertEquals(status, requisitions.findById(requisition.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("convert: a second order from an already converted requisition is refused; still exactly one order (US2-4)")
    void create_twice_onlyOneOrder() {
        Requisition requisition = committedRequisition(RequisitionStatus.APPROVED);
        orders.create(fromRequisition(requisition.getId()));

        assertThrows(RequisitionBusinessException.class, () -> orders.create(fromRequisition(requisition.getId())));

        assertEquals(1, ordersFor(requisition.getId()));
    }

    // ---- T032: release ----

    private PurchaseOrderOutput convertedOrder(Requisition requisition) {
        return orders.create(fromRequisition(requisition.getId()));
    }

    private void assertReleased(Requisition requisition) {
        Requisition released = requisitions.findById(requisition.getId()).orElseThrow();
        assertEquals(RequisitionStatus.APPROVED, released.getStatus());
        assertNull(released.getPurchaseOrderId());
        assertNull(released.getPurchaseOrderCode());
    }

    @Test
    @DisplayName("release: deleting the order returns the requisition to approved, and it can be ordered again (US2-6)")
    void delete_releasesRequisition() {
        Requisition requisition = committedRequisition(RequisitionStatus.APPROVED);
        PurchaseOrderOutput order = convertedOrder(requisition);

        orders.delete(order.getId(), "admin-1");

        assertReleased(requisition);
        assertDoesNotThrow(() -> orders.create(fromRequisition(requisition.getId())));
    }

    @Test
    @DisplayName("release: cancelling the order returns the requisition to approved (US2-6)")
    void cancel_releasesRequisition() {
        Requisition requisition = committedRequisition(RequisitionStatus.APPROVED);
        PurchaseOrderOutput order = convertedOrder(requisition);

        orders.cancel(order.getId(), "buyer-1", "Supplier delay");

        assertReleased(requisition);
    }

    @Test
    @DisplayName("release: a supplier rejection returns the requisition to approved (US2-6)")
    void reject_releasesRequisition() {
        Requisition requisition = committedRequisition(RequisitionStatus.APPROVED);
        PurchaseOrderOutput order = convertedOrder(requisition);
        orders.submit(order.getId(), "buyer-1");

        orders.reject(order.getId(), "admin-1", "Cannot deliver");

        assertReleased(requisition);
        assertDoesNotThrow(() -> orders.create(fromRequisition(requisition.getId())));
    }
}
