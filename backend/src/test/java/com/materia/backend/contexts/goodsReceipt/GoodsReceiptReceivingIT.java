package com.materia.backend.contexts.goodsReceipt;

import com.materia.backend.support.fixtures.ReferenceRows;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptBusinessException;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.UserFixtures.aReceiver;
import static org.junit.jupiter.api.Assertions.*;

/** [T038, T039] Receiving against the real database: receipt, order and stock checked together (US3, FR-005, FR-008). */
class GoodsReceiptReceivingIT extends AbstractIntegrationTest {

    @Autowired private PurchaseOrderUseCase orders;
    @Autowired private PurchaseOrderRepository orderRepository;
    @Autowired private GoodsReceiptUseCase receipts;
    @Autowired private GoodsReceiptRepository receiptRepository;
    @Autowired private MaterialRepository materials;
    @Autowired private UserRepository users;
    @Autowired private JdbcTemplate jdbc;
    @PersistenceContext private EntityManager em;

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    /** An order assigned to a fresh receiver, ready for receipt, one line per material. */
    private record Receivable(PurchaseOrderOutput order, String receiverId) {
        PurchaseOrderLineOutput line(int index) {
            return order.getLines().get(index);
        }
    }

    private Receivable receivableOrder(int[] quantities, Material... lineMaterials) {
        User receiver = users.save(aReceiver().build());
        List<PurchaseOrderLineInput> lines = new ArrayList<>();
        for (int i = 0; i < lineMaterials.length; i++) {
            PurchaseOrderLineInput line = new PurchaseOrderLineInput();
            line.setMaterialId(lineMaterials[i].getId());
            line.setMaterialCode(lineMaterials[i].getCode().getValue());
            line.setMaterialName(lineMaterials[i].getName());
            line.setQuantity(quantities[i]);
            line.setUnitPrice(Money.of("5.00", CurrencyCode.MAD));
            lines.add(line);
        }
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setSupplierId(ReferenceRows.newSupplier(jdbc));
        input.setSupplierName("Acme Supplies");
        input.setOrderedBy("buyer-1");
        input.setCurrencyCode("MAD");
        input.setLines(lines);
        input.setUserId("buyer-1");
        UUID id = orders.create(input).getId();
        orders.submit(id, "buyer-1");
        orders.confirm(id, "admin-1");
        PurchaseOrderOutput ready = orders.assignReceiver(id, "buyer-1", "Bob", receiver.getId().toString(), null);
        return new Receivable(ready, receiver.getId().toString());
    }

    private CreateGoodsReceiptInput receipt(Receivable r, String caller, int[][] lineQuantities) {
        List<GoodsReceiptLineInput> lines = new ArrayList<>();
        for (int i = 0; i < lineQuantities.length; i++) {
            GoodsReceiptLineInput line = new GoodsReceiptLineInput();
            line.setPurchaseOrderLineId(r.line(i).getId().toString());
            line.setMaterialCode(r.line(i).getMaterialCode());
            line.setQuantityReceived(lineQuantities[i][0]);
            line.setQuantityRejected(lineQuantities[i][1]);
            line.setQualityStatus(lineQuantities[i][1] == 0 ? "ACCEPTED" : "PARTIAL");
            line.setRejectionReason(lineQuantities[i][1] == 0 ? null : "Damaged in transit");
            lines.add(line);
        }
        CreateGoodsReceiptInput input = new CreateGoodsReceiptInput();
        input.setPurchaseOrderId(r.order().getId().toString());
        input.setReceivedBy(caller);
        input.setReceivedByName(caller);
        input.setUserId(caller);
        input.setLines(lines);
        return input;
    }

    private GoodsReceiptOutput receiveAndValidate(Receivable r, int[][] lineQuantities) {
        GoodsReceiptOutput created = receipts.create(receipt(r, r.receiverId(), lineQuantities));
        return receipts.complete(created.getId(), r.receiverId());
    }

    private int stockOf(Material m) {
        flushAndClear();
        return materials.findById(m.getId()).orElseThrow().getCurrentStock();
    }

    private PurchaseOrder reloadOrder(Receivable r) {
        flushAndClear();
        return orderRepository.findById(r.order().getId()).orElseThrow();
    }

    @Test
    @DisplayName("receive: a full receipt completes the order as delivered and adds every accepted unit to stock (US3-1)")
    void fullReceipt_completesOrderAndStock() {
        Material m = materials.save(aMaterial().stock(20).build());
        Receivable r = receivableOrder(new int[]{10}, m);

        GoodsReceiptOutput validated = receiveAndValidate(r, new int[][]{{10, 0}});

        assertEquals(ReceiptStatus.COMPLETED.getCode(), validated.getStatus());
        PurchaseOrder order = reloadOrder(r);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(DeliveryStatus.DELIVERED, order.getDeliveryStatus());
        assertNotNull(order.getReceivedDate());
        assertEquals(30, stockOf(m));
    }

    @Test
    @DisplayName("receive: a partial receipt leaves the remainder outstanding; the remainder later completes the order (US3-2, US3-3)")
    void partialThenRemainder_completesOrder() {
        Material m = materials.save(aMaterial().stock(0).build());
        Receivable r = receivableOrder(new int[]{10}, m);

        receiveAndValidate(r, new int[][]{{6, 0}});
        assertEquals(OrderStatus.PARTIALLY_RECEIVED, reloadOrder(r).getStatus());
        assertEquals(6, stockOf(m));

        receiveAndValidate(r, new int[][]{{4, 0}});
        assertEquals(OrderStatus.COMPLETED, reloadOrder(r).getStatus());
        assertEquals(10, stockOf(m));
    }

    @Test
    @DisplayName("receive: rejected units never reach stock; only the accepted quantity is added (US3-4)")
    void rejectedUnits_doNotReachStock() {
        Material m = materials.save(aMaterial().stock(5).build());
        Receivable r = receivableOrder(new int[]{10}, m);

        receiveAndValidate(r, new int[][]{{10, 3}});

        assertEquals(12, stockOf(m));
    }

    @Test
    @DisplayName("receive: receiving beyond what remains across receipts is refused and stock is unchanged (US3-5)")
    void overReceipt_isRefused() {
        Material m = materials.save(aMaterial().stock(0).build());
        Receivable r = receivableOrder(new int[]{10}, m);
        receiveAndValidate(r, new int[][]{{8, 0}});

        assertThrows(GoodsReceiptBusinessException.class,
                () -> receipts.create(receipt(r, r.receiverId(), new int[][]{{3, 0}})));
        assertEquals(8, stockOf(m));
    }

    @Test
    @DisplayName("receive: someone other than the assigned receiver cannot record a receipt (US3-6)")
    void nonAssignee_isRefused() {
        Material m = materials.save(aMaterial().stock(0).build());
        Receivable r = receivableOrder(new int[]{10}, m);

        assertThrows(AccessDeniedException.class,
                () -> receipts.create(receipt(r, "someone-else", new int[][]{{10, 0}})));
    }

    @Test
    @DisplayName("receive: a cancelled draft does not count towards received quantities and leaves stock untouched (US3-10)")
    void cancelledDraft_isNotCounted() {
        Material m = materials.save(aMaterial().stock(0).build());
        Receivable r = receivableOrder(new int[]{10}, m);
        GoodsReceiptOutput draft = receipts.create(receipt(r, r.receiverId(), new int[][]{{9, 0}}));
        receipts.cancel(draft.getId(), r.receiverId(), "Counted wrong");

        GoodsReceiptOutput full = receiveAndValidate(r, new int[][]{{10, 0}});

        assertEquals(ReceiptStatus.COMPLETED.getCode(), full.getStatus());
        assertEquals(10, stockOf(m));
    }

    @Test
    @DisplayName("receive all: confirming receipt on the order records a validated receipt for exactly what was outstanding (US3-8)")
    void confirmReceipt_recordsRealReceipt() {
        Material a = materials.save(aMaterial().stock(0).build());
        Material b = materials.save(aMaterial().stock(1).build());
        Receivable r = receivableOrder(new int[]{10, 4}, a, b);
        receiveAndValidate(r, new int[][]{{7, 0}, {0, 0}});

        PurchaseOrderOutput after = orders.confirmReceipt(r.order().getId(), r.receiverId(), "Rita");

        assertEquals("COMPLETED", after.getStatus());
        flushAndClear();
        List<GoodsReceipt> all = receiptRepository.findByPurchaseOrderId(r.order().getId().toString());
        GoodsReceipt last = all.stream().filter(g -> g.getStatus() != ReceiptStatus.DRAFT)
                .max((x, y) -> x.getCreatedAt().compareTo(y.getCreatedAt())).orElseThrow();
        assertEquals(List.of(3, 4), last.getLines().stream().map(l -> l.getQuantityReceived()).toList());
        assertEquals(10, stockOf(a));
        assertEquals(5, stockOf(b));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("reads: receipts can be read outside a caller's transaction, as a web request does (F-014)")
    void reads_workWithoutCallerTransaction() {
        Material m = materials.save(aMaterial().stock(0).build());
        Receivable r = receivableOrder(new int[]{10}, m);
        GoodsReceiptOutput draft = receipts.create(receipt(r, r.receiverId(), new int[][]{{4, 0}}));
        try {
            assertEquals(1, receipts.getById(draft.getId()).getLines().size());
            assertFalse(receipts.getAll().isEmpty());
            assertEquals(1, receipts.getByPurchaseOrderId(r.order().getId().toString()).size());
            assertEquals(draft.getId(), receipts.getByCode(draft.getReceiptCode()).getId());
        } finally {
            jdbc.update("delete from goods_receipt_lines where goods_receipt_id = ?", draft.getId());
            jdbc.update("delete from goods_receipts where id = ?", draft.getId());
            jdbc.update("delete from purchase_order_lines where purchase_order_id = ?", r.order().getId());
            jdbc.update("delete from purchase_orders where id = ?", r.order().getId());
            jdbc.update("delete from materials where id = ?", m.getId());
            jdbc.update("delete from users where id = ?::uuid", r.receiverId());
        }
    }

    // ---- T039: indivisibility of validation (FR-008, research R4) ----

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("indivisible: a validation that fails part-way leaves receipt, order and stock exactly as they were (FR-008)")
    void failedValidation_changesNothing() {
        Material real = materials.save(aMaterial().stock(20).build());
        Material ghost = aMaterial().stock(0).build(); // never saved: its lookup fails during validation
        Receivable r = receivableOrder(new int[]{10, 5}, real, ghost);
        GoodsReceiptOutput draft = receipts.create(receipt(r, r.receiverId(), new int[][]{{10, 0}, {5, 0}}));
        try {
            // The first line's stock is increased before the second line's material lookup fails.
            assertThrows(RuntimeException.class, () -> receipts.complete(draft.getId(), r.receiverId()));

            assertEquals(ReceiptStatus.DRAFT.getCode(), receipts.getById(draft.getId()).getStatus());
            PurchaseOrder order = orderRepository.findById(r.order().getId()).orElseThrow();
            assertEquals(OrderStatus.READY_FOR_RECEIPT, order.getStatus());
            assertEquals(DeliveryStatus.NOT_SHIPPED, order.getDeliveryStatus());
            assertEquals(20, materials.findById(real.getId()).orElseThrow().getCurrentStock(),
                    "the first line's stock increase was not rolled back");
        } finally {
            jdbc.update("delete from goods_receipt_lines where goods_receipt_id = ?", draft.getId());
            jdbc.update("delete from goods_receipts where id = ?", draft.getId());
            jdbc.update("delete from purchase_order_lines where purchase_order_id = ?", r.order().getId());
            jdbc.update("delete from purchase_orders where id = ?", r.order().getId());
            jdbc.update("delete from material_stock_movements where material_id = ?", real.getId());
            jdbc.update("delete from materials where id = ?", real.getId());
            jdbc.update("delete from users where id = ?::uuid", r.receiverId());
        }
    }
}
