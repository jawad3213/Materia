package com.materia.backend.contexts.purchaseOrder;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.masterData.application.services.ReorderService;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseRequisition.application.services.RequisitionService;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionBusinessException;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionRepository;
import com.materia.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.RequisitionFixtures.aRequisition;
import static com.materia.backend.support.fixtures.UserFixtures.aPurchaser;
import static com.materia.backend.support.fixtures.UserFixtures.aReceiver;
import static org.junit.jupiter.api.Assertions.*;

/** The requisition → purchase order → goods receipt chain against the real database. */
class ProcurementChainIT extends AbstractIntegrationTest {

    @Autowired private RequisitionService requisitionService;
    @Autowired private RequisitionRepository requisitions;
    @Autowired private ReorderService reorder;
    @Autowired private PurchaseOrderUseCase orders;
    @Autowired private GoodsReceiptUseCase receipts;
    @Autowired private MaterialRepository materials;
    @Autowired private UserRepository users;
    @PersistenceContext private EntityManager em;

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private Material material() {
        return materials.save(aMaterial().stock(0).price("5.00").build());
    }

    private Material reload(Material m) {
        flushAndClear();
        return materials.findById(m.getId()).orElseThrow();
    }

    private static int onOrder(Material m) {
        return m.getStockOnOrder() != null ? m.getStockOnOrder() : 0;
    }

    /** An approved requisition for the given materials and quantities, requested by "requester-1". */
    private Requisition approvedRequisition(Object... materialAndQuantity) {
        var builder = aRequisition().requestedBy("requester-1").inStatus(RequisitionStatus.APPROVED);
        for (int i = 0; i < materialAndQuantity.length; i += 2) {
            builder.withLine(new RequisitionLine((Material) materialAndQuantity[i], (Integer) materialAndQuantity[i + 1],
                    LocalDate.now().plusDays(10)));
        }
        Requisition saved = requisitions.save(builder.build());
        flushAndClear();
        return requisitions.findById(saved.getId()).orElseThrow();
    }

    private static PurchaseOrderLineInput line(Material m, int quantity, UUID requisitionLineId) {
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setMaterialId(m.getId());
        line.setMaterialCode(m.getCode().getValue());
        line.setMaterialName(m.getName());
        line.setQuantity(quantity);
        line.setUnitPrice(Money.of("5.00", CurrencyCode.MAD));
        line.setRequisitionLineId(requisitionLineId);
        return line;
    }

    private static CreatePurchaseOrderInput order(UUID supplierId, UUID requisitionId, PurchaseOrderLineInput... lines) {
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setRequisitionId(requisitionId);
        input.setSupplierId(supplierId);
        input.setSupplierName("Acme Supplies");
        input.setOrderedBy("buyer-1");
        input.setCurrencyCode("MAD");
        input.setLines(new ArrayList<>(List.of(lines)));
        input.setUserId("buyer-1");
        return input;
    }

    /** Takes a draft order to ready-for-receipt with a fresh receiver; returns the receiver's id. */
    private String readyForReceipt(UUID orderId) {
        User receiver = users.save(aReceiver().build());
        orders.submit(orderId, "buyer-1");
        flushAndClear();
        orders.confirm(orderId, "admin-1");
        flushAndClear();
        orders.assignReceiver(orderId, "buyer-1", "Bob", receiver.getId().toString(), null);
        flushAndClear();
        return receiver.getId().toString();
    }

    private void receive(PurchaseOrderOutput order, String receiverId, int received, int rejected) {
        GoodsReceiptLineInput line = new GoodsReceiptLineInput();
        line.setPurchaseOrderLineId(order.getLines().get(0).getId().toString());
        line.setMaterialCode(order.getLines().get(0).getMaterialCode());
        line.setQuantityReceived(received);
        line.setQuantityRejected(rejected);
        line.setQualityStatus(rejected == 0 ? "ACCEPTED" : "PARTIAL");
        line.setRejectionReason(rejected == 0 ? null : "Damaged");
        CreateGoodsReceiptInput receipt = new CreateGoodsReceiptInput();
        receipt.setPurchaseOrderId(order.getId().toString());
        receipt.setReceivedBy(receiverId);
        receipt.setReceivedByName("Rita");
        receipt.setUserId(receiverId);
        receipt.setLines(new ArrayList<>(List.of(line)));
        receipts.complete(receipts.create(receipt).getId(), receiverId);
        flushAndClear();
    }

    // ---- 1. Reorder requisitions have a requester ----

    @Test
    @DisplayName("reorder: automatic and one-click requisitions are saved; the requester is the system or the user who clicked")
    void reorderRequisitions_areSavedWithARequester() {
        Material auto = material();
        Material manual = material();
        User buyer = users.save(aPurchaser().firstName("Bea").lastName("Buyer").build());

        String autoCode = requisitionService.createRequisitionFromReorder(auto, 10, "Below safety stock", true);
        reorder.triggerManualReorder(manual.getId(), 7, "Customer rush", buyer.getId().toString());
        flushAndClear();

        Requisition automatic = requisitions.findByCode(autoCode).orElseThrow();
        assertEquals(RequisitionService.SYSTEM_REQUESTER_ID, automatic.getRequesterId());
        assertEquals(RequisitionService.SYSTEM_REQUESTER_NAME, automatic.getRequesterName());
        Requisition oneClick = requisitions.findByRequesterId(buyer.getId().toString()).get(0);
        assertEquals("Bea Buyer", oneClick.getRequesterName(), "named from the user's account");
        assertEquals(7, oneClick.getLines().get(0).getQuantity());
        assertEquals(0, onOrder(reload(manual)), "a requisition is not stock on order");
        assertTrue(requisitionService.hasOpenRequisitionFor(auto.getId(), null), "the automatic reorder now sees it in progress");
    }

    // ---- 2. Stock on order follows purchase orders ----

    @Test
    @DisplayName("on order: an order adds its quantity, a receipt removes what arrived, closing short releases the rest")
    void onOrder_followsTheOrder() {
        Material m = material();
        PurchaseOrderOutput order = orders.create(order(UUID.randomUUID(), null, line(m, 10, null)));
        assertEquals(10, onOrder(reload(m)));

        UpdatePurchaseOrderInput edit = new UpdatePurchaseOrderInput();
        edit.setLines(new ArrayList<>(List.of(line(m, 12, null))));
        edit.setUserId("buyer-1");
        orders.update(order.getId(), edit);
        assertEquals(12, onOrder(reload(m)), "editing a draft replaces its quantities");

        String receiver = readyForReceipt(order.getId());
        receive(orders.getById(order.getId()), receiver, 7, 2);
        Material afterReceipt = reload(m);
        assertEquals(5, onOrder(afterReceipt), "7 received (5 accepted, 2 rejected) out of 12");
        assertEquals(5, afterReceipt.getCurrentStock(), "only accepted goods enter stock");

        orders.complete(order.getId(), "buyer-1");
        assertEquals(0, onOrder(reload(m)), "closing short releases what will never arrive");
    }

    @Test
    @DisplayName("on order: cancelling, rejecting or deleting an order releases its quantity")
    void onOrder_releasedWhenWithdrawn() {
        Material m = material();
        PurchaseOrderOutput cancelled = orders.create(order(UUID.randomUUID(), null, line(m, 4, null)));
        PurchaseOrderOutput rejected = orders.create(order(UUID.randomUUID(), null, line(m, 3, null)));
        PurchaseOrderOutput deleted = orders.create(order(UUID.randomUUID(), null, line(m, 2, null)));
        assertEquals(9, onOrder(reload(m)));

        // Each action is its own request (and transaction) in the application.
        orders.submit(cancelled.getId(), "buyer-1");
        flushAndClear();
        orders.confirm(cancelled.getId(), "admin-1");
        flushAndClear();
        orders.cancel(cancelled.getId(), "buyer-1", "Supplier out of stock");
        flushAndClear();
        orders.submit(rejected.getId(), "buyer-1");
        flushAndClear();
        orders.reject(rejected.getId(), "admin-1", "Price changed");
        flushAndClear();
        orders.delete(deleted.getId(), "buyer-1");

        assertEquals(0, onOrder(reload(m)));
    }

    // ---- 3. Orders match their requisition ----

    @Test
    @DisplayName("requisition match: more than requested, a missing line, a foreign line, another currency or another supplier is refused")
    void ordersMustMatchTheirRequisition() {
        Material a = material();
        Material b = material();
        Requisition requisition = approvedRequisition(a, 10, b, 5);
        UUID lineA = requisition.getLines().get(0).getId();
        UUID lineB = requisition.getLines().get(1).getId();
        UUID supplier = UUID.randomUUID();

        assertThrows(PurchaseOrderRequisitionMismatchException.class,
                () -> orders.create(order(supplier, requisition.getId(), line(a, 11, lineA), line(b, 5, lineB))), "too many");
        assertThrows(PurchaseOrderRequisitionMismatchException.class,
                () -> orders.create(order(supplier, requisition.getId(), line(a, 10, lineA))), "line B forgotten");
        assertThrows(PurchaseOrderRequisitionMismatchException.class,
                () -> orders.create(order(supplier, requisition.getId(), line(a, 10, lineA), line(b, 5, null))), "line not from the requisition");
        assertThrows(PurchaseOrderRequisitionMismatchException.class,
                () -> orders.create(order(supplier, requisition.getId(), line(a, 10, lineA), line(a, 5, lineB))), "wrong material");
        CreatePurchaseOrderInput euro = order(supplier, requisition.getId(), line(a, 10, lineA), line(b, 5, lineB));
        euro.setCurrencyCode("EUR");
        euro.getLines().forEach(l -> l.setUnitPrice(Money.of("5.00", CurrencyCode.EUR)));
        assertThrows(PurchaseOrderRequisitionMismatchException.class, () -> orders.create(euro), "other currency");
        flushAndClear();
        assertEquals(RequisitionStatus.APPROVED, requisitions.findById(requisition.getId()).orElseThrow().getStatus(),
                "refused orders leave the requisition untouched");

        PurchaseOrderOutput ok = orders.create(order(supplier, requisition.getId(), line(a, 8, lineA), line(b, 5, lineB)));
        assertNotNull(ok.getId(), "ordering less than requested is allowed");
    }

    @Test
    @DisplayName("requisition match: a requisition naming several suppliers must be split; one naming a supplier is ordered from it")
    void requisitionSuppliers() {
        Material a = material();
        Material b = material();
        Requisition requisition = approvedRequisition(a, 1, b, 1);
        UUID supplierA = UUID.randomUUID();
        requisition.getLines().get(0).setSupplierId(supplierA);
        requisition.getLines().get(1).setSupplierId(UUID.randomUUID());
        requisitions.save(requisition);
        flushAndClear();
        Requisition reloaded = requisitions.findById(requisition.getId()).orElseThrow();
        UUID lineA = reloaded.getLines().get(0).getId();
        UUID lineB = reloaded.getLines().get(1).getId();

        assertThrows(PurchaseOrderRequisitionMismatchException.class,
                () -> orders.create(order(supplierA, requisition.getId(), line(a, 1, lineA), line(b, 1, lineB))));

        Requisition single = approvedRequisition(a, 1);
        single.getLines().get(0).setSupplierId(supplierA);
        requisitions.save(single);
        flushAndClear();
        UUID singleLine = requisitions.findById(single.getId()).orElseThrow().getLines().get(0).getId();
        assertThrows(PurchaseOrderRequisitionMismatchException.class,
                () -> orders.create(order(UUID.randomUUID(), single.getId(), line(a, 1, singleLine))), "another supplier");
        assertNotNull(orders.create(order(supplierA, single.getId(), line(a, 1, singleLine))).getId());
    }

    // ---- 4. Receipts reach the requisition ----

    @Test
    @DisplayName("receipts: what arrives on the order is recorded on the requisition line it was requested on")
    void receipts_updateTheRequisition() {
        Material m = material();
        Requisition requisition = approvedRequisition(m, 10);
        UUID requested = requisition.getLines().get(0).getId();
        PurchaseOrderOutput order = orders.create(order(UUID.randomUUID(), requisition.getId(), line(m, 10, requested)));
        String receiver = readyForReceipt(order.getId());

        receive(orders.getById(order.getId()), receiver, 7, 1);
        RequisitionLine afterFirst = requisitions.findById(requisition.getId()).orElseThrow().getLines().get(0);
        assertEquals(6, afterFirst.getQuantityReceived(), "accepted goods");
        assertEquals(1, afterFirst.getQuantityRejected());
        assertFalse(afterFirst.isFullyReceived());

        receive(orders.getById(order.getId()), receiver, 3, 0);
        Requisition afterSecond = requisitions.findById(requisition.getId()).orElseThrow();
        assertEquals(9, afterSecond.getLines().get(0).getQuantityReceived());
    }

    @Test
    @DisplayName("persistence: changing, adding and removing lines on a saved requisition is stored (it used to be silently lost)")
    void requisitionLineChanges_arePersisted() {
        Material a = material();
        Material b = material();
        Material c = material();
        Requisition requisition = approvedRequisition(a, 2, b, 3);
        UUID keptLineId = requisition.getLines().get(0).getId();

        requisition.getLines().get(0).updateQuantity(9);
        requisition.removeLine(1);
        requisition.addLine(new RequisitionLine(c, 4, LocalDate.now().plusDays(5)));
        requisitions.save(requisition);
        flushAndClear();

        Requisition reloaded = requisitions.findById(requisition.getId()).orElseThrow();
        assertEquals(2, reloaded.getLines().size());
        RequisitionLine kept = reloaded.getLines().stream().filter(l -> l.getId().equals(keptLineId)).findFirst().orElseThrow();
        assertEquals(9, kept.getQuantity(), "the existing line keeps its id and gets the new quantity");
        assertTrue(reloaded.getLines().stream().anyMatch(l -> c.getCode().getValue().equals(l.getMaterialCode())), "new line stored");
        assertTrue(reloaded.getLines().stream().noneMatch(l -> b.getCode().getValue().equals(l.getMaterialCode())), "removed line gone");
    }

    // ---- 5. No self-approval ----

    @Test
    @DisplayName("approval: the requester cannot approve their own requisition; someone else can")
    void selfApproval_isRefused() {
        Requisition requisition = requisitions.save(aRequisition().requestedBy("requester-9").inStatus(RequisitionStatus.SUBMITTED).build());
        flushAndClear();

        assertThrows(RequisitionBusinessException.class,
                () -> requisitionService.approve(requisition.getId(), "requester-9", "Me", "fine"));
        flushAndClear();
        assertEquals(RequisitionStatus.SUBMITTED, requisitions.findById(requisition.getId()).orElseThrow().getStatus());

        assertEquals("APPROVED", requisitionService.approve(requisition.getId(), "manager-1", "Max", "ok").getStatus());
    }

    @Test
    @DisplayName("approval: whoever created a requisition on someone else's behalf cannot approve it either")
    void creatorApproval_isRefused() {
        Requisition onBehalf = aRequisition().requestedBy("employee-4").inStatus(RequisitionStatus.SUBMITTED).build();
        onBehalf.setCreatedBy("buyer-3");
        Requisition saved = requisitions.save(onBehalf);
        flushAndClear();

        assertThrows(RequisitionBusinessException.class,
                () -> requisitionService.approve(saved.getId(), "buyer-3", "buyer-3", "fine"));
        flushAndClear();
        User manager = users.save(aPurchaser().firstName("Mona").lastName("Manager").build());
        var approved = requisitionService.approve(saved.getId(), manager.getId().toString(), manager.getId().toString(), "ok");
        assertEquals("APPROVED", approved.getStatus());
        assertEquals("Mona Manager", approved.getApproverName(), "named from the approver's account, not their id");
    }
}
