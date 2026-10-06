package com.materia.backend.contexts.goodsReceipt;

import com.materia.backend.support.fixtures.ReferenceRows;
import org.springframework.jdbc.core.JdbcTemplate;
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
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static com.materia.backend.support.fixtures.UserFixtures.aReceiver;
import static org.junit.jupiter.api.Assertions.*;

/** [T053, T054] Receipt lookups against the real schema (US6-10, FR-006b), and the F-006 observation. */
class GoodsReceiptLookupIT extends AbstractIntegrationTest {

    @Autowired private JdbcTemplate jdbc;

    @Autowired private GoodsReceiptRepository receipts;
    @Autowired private GoodsReceiptUseCase receiptService;
    @Autowired private PurchaseOrderUseCase orders;
    @Autowired private PurchaseOrderRepository orderRepository;
    @Autowired private UserRepository users;
    @PersistenceContext private EntityManager em;

    private static Set<UUID> ids(List<GoodsReceipt> found) {
        return found.stream().map(GoodsReceipt::getId).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("lookups: by code, status, order, receiver and keyword each return exactly the matching receipts (US6-10)")
    void lookups_returnExactlyTheMatches() {
        PurchaseOrder orderA = orderRepository.save(anOrder().withLine(10, "5.00").assignedTo("rcv-a", "Ana").inStatus(OrderStatus.READY_FOR_RECEIPT).build());
        PurchaseOrder orderB = orderRepository.save(anOrder().withLine(10, "5.00").assignedTo("rcv-b", "Ben").inStatus(OrderStatus.READY_FOR_RECEIPT).build());
        GoodsReceipt aDraft = receipts.save(aReceipt(orderA).receiving(0, 2, 0, null).build());
        GoodsReceipt aDone = receipts.save(aReceipt(orderA).receiving(0, 3, 0, null).inStatus(ReceiptStatus.PARTIAL).build());
        GoodsReceipt bCancelled = receipts.save(aReceipt(orderB).inStatus(ReceiptStatus.CANCELLED).build());
        em.flush();
        em.clear();

        assertEquals(aDraft.getId(), receipts.findByReceiptCode(aDraft.getReceiptCode().getValue()).orElseThrow().getId());
        assertEquals(Set.of(aDraft.getId(), aDone.getId()), ids(receipts.findByPurchaseOrderId(orderA.getId().toString())));
        assertEquals(Set.of(bCancelled.getId()), ids(receipts.findByReceivedBy("rcv-b")));
        assertTrue(ids(receipts.findByStatus(ReceiptStatus.PARTIAL)).contains(aDone.getId()));
        assertFalse(ids(receipts.findByStatus(ReceiptStatus.PARTIAL)).contains(aDraft.getId()));
        assertTrue(ids(receipts.findByStatus(ReceiptStatus.CANCELLED)).contains(bCancelled.getId()));
        assertEquals(Set.of(aDraft.getId(), aDone.getId()), ids(receipts.search(orderA.getOrderCode().getValue())));
        assertTrue(receipts.existsByReceiptCode(aDone.getReceiptCode().getValue()));
        assertFalse(receipts.existsByReceiptCode("GR-2099-9999"));
    }

    @Test
    @DisplayName("persistence: a receipt round-trips with its lines, quantities and quality outcome")
    void receipt_roundTrips() {
        PurchaseOrder order = orderRepository.save(anOrder().withLine(10, "5.00").withLine(4, "2.00").inStatus(OrderStatus.READY_FOR_RECEIPT).build());
        GoodsReceipt saved = receipts.save(aReceipt(order).receiving(0, 9, 1, "Dented").build());
        em.flush();
        em.clear();

        GoodsReceipt loaded = receipts.findById(saved.getId()).orElseThrow();

        assertEquals(2, loaded.getLines().size());
        assertEquals(9, loaded.getLines().get(0).getQuantityReceived());
        assertEquals("Dented", loaded.getLines().get(0).getRejectionReason());
        assertEquals(13, loaded.getTotalQuantityReceived());
        assertTrue(loaded.isHasDiscrepancy());
    }

    // ---- T054: F-006 observation ----

    @Test
    @DisplayName("F-006 observation: after its order is cancelled, a draft receipt cannot be validated but its receiver can delete it")
    void cancelledOrder_draftReceipt_observedOutcome() {
        User receiver = users.save(aReceiver().build());
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setMaterialCode("MAT-F6");
        line.setQuantity(5);
        line.setUnitPrice(Money.of("1.00", CurrencyCode.MAD));
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setSupplierId(ReferenceRows.newSupplier(jdbc));
        input.setSupplierName("Acme");
        input.setOrderedBy("buyer-1");
        input.setCurrencyCode("MAD");
        input.setLines(new ArrayList<>(List.of(line)));
        input.setUserId("buyer-1");
        UUID orderId = orders.create(input).getId();
        orders.submit(orderId, "buyer-1");
        orders.confirm(orderId, "admin-1");
        PurchaseOrderOutput ready = orders.assignReceiver(orderId, "buyer-1", "Bob", receiver.getId().toString(), null);

        GoodsReceiptLineInput receiptLine = new GoodsReceiptLineInput();
        receiptLine.setPurchaseOrderLineId(ready.getLines().get(0).getId().toString());
        receiptLine.setMaterialCode("MAT-F6");
        receiptLine.setQuantityReceived(5);
        receiptLine.setQuantityRejected(0);
        receiptLine.setQualityStatus("ACCEPTED");
        CreateGoodsReceiptInput draftInput = new CreateGoodsReceiptInput();
        draftInput.setPurchaseOrderId(orderId.toString());
        draftInput.setReceivedBy(receiver.getId().toString());
        draftInput.setReceivedByName("Rita");
        draftInput.setUserId(receiver.getId().toString());
        draftInput.setLines(new ArrayList<>(List.of(receiptLine)));
        GoodsReceiptOutput draft = receiptService.create(draftInput);

        orders.cancel(orderId, "buyer-1", "Supplier went bankrupt");

        // Observed (F-006): the stranded draft can no longer be validated...
        assertThrows(GoodsReceiptBusinessException.class,
                () -> receiptService.complete(draft.getId(), receiver.getId().toString()));
        // ...but its receiver can still delete it, so it does not have to linger.
        assertDoesNotThrow(() -> receiptService.delete(draft.getId(), receiver.getId().toString()));
    }
}
