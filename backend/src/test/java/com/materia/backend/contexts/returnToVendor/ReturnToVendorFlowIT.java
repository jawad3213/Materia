package com.materia.backend.contexts.returnToVendor;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptBusinessException;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.returnToVendor.application.dtos.CreateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorOutput;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidQuantityException;
import com.materia.backend.contexts.returnToVendor.domain.ports.in.ReturnToVendorUseCase;
import com.materia.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.UserFixtures.aReceiver;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The return step of the chain against the real database: goods rejected at receipt are sent back, then
 * either replaced (the order is reopened and the replacement received on it) or credited.
 */
class ReturnToVendorFlowIT extends AbstractIntegrationTest {

    @Autowired private PurchaseOrderUseCase orders;
    @Autowired private GoodsReceiptUseCase receipts;
    @Autowired private ReturnToVendorUseCase returns;
    @Autowired private MaterialRepository materials;
    @Autowired private UserRepository users;
    @PersistenceContext private EntityManager em;

    private Material material;
    private String receiverId;
    private UUID orderId;
    private String orderLineId;

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    /** An order of 10 at 5.00 MAD, received in full with 4 rejected. Returns the completed receipt. */
    private GoodsReceiptOutput receivedWithRejects() {
        material = materials.save(aMaterial().stock(0).build());
        User receiver = users.save(aReceiver().build());
        receiverId = receiver.getId().toString();

        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setMaterialId(material.getId());
        line.setMaterialCode(material.getCode().getValue());
        line.setMaterialName(material.getName());
        line.setQuantity(10);
        line.setUnitPrice(Money.of("5.00", CurrencyCode.MAD));
        CreatePurchaseOrderInput order = new CreatePurchaseOrderInput();
        order.setSupplierId(UUID.randomUUID());
        order.setSupplierName("Acme Supplies");
        order.setOrderedBy("buyer-1");
        order.setCurrencyCode("MAD");
        order.setLines(new ArrayList<>(List.of(line)));
        order.setUserId("buyer-1");
        orderId = orders.create(order).getId();
        orders.submit(orderId, "buyer-1");
        orders.confirm(orderId, "admin-1");
        PurchaseOrderOutput ready = orders.assignReceiver(orderId, "buyer-1", "Bob", receiverId, null);
        orderLineId = ready.getLines().get(0).getId().toString();

        GoodsReceiptOutput receipt = receive(10, 4, "Scratched");
        flushAndClear();
        return receipt;
    }

    private GoodsReceiptOutput receive(int received, int rejected, String reason) {
        GoodsReceiptLineInput receiptLine = new GoodsReceiptLineInput();
        receiptLine.setPurchaseOrderLineId(orderLineId);
        receiptLine.setMaterialCode(material.getCode().getValue());
        receiptLine.setQuantityReceived(received);
        receiptLine.setQuantityRejected(rejected);
        receiptLine.setRejectionReason(reason);
        receiptLine.setQualityStatus(rejected == 0 ? "ACCEPTED" : "PARTIAL");
        CreateGoodsReceiptInput receipt = new CreateGoodsReceiptInput();
        receipt.setPurchaseOrderId(orderId.toString());
        receipt.setReceivedBy(receiverId);
        receipt.setReceivedByName("Rita");
        receipt.setUserId(receiverId);
        receipt.setLines(new ArrayList<>(List.of(receiptLine)));
        return receipts.complete(receipts.create(receipt).getId(), receiverId);
    }

    private ReturnToVendorOutput returnOf(GoodsReceiptOutput receipt, int quantity) {
        CreateReturnToVendorInput input = new CreateReturnToVendorInput();
        input.setGoodsReceiptId(receipt.getId().toString());
        input.setReturnReason("Scratched surfaces");
        input.setLines(List.of(new ReturnToVendorLineInput(receipt.getLines().get(0).getId().toString(), quantity, null)));
        input.setUserId("buyer-1");
        ReturnToVendorOutput created = returns.create(input);
        flushAndClear();
        return created;
    }

    private Material reloadedMaterial() {
        return materials.findById(material.getId()).orElseThrow();
    }

    @Test
    @DisplayName("replacement: the returned goods are expected again, received on the same order, and enter stock")
    void replacement_isReceivedOnTheReopenedOrder() {
        GoodsReceiptOutput receipt = receivedWithRejects();
        assertEquals("COMPLETED", orders.getById(orderId).getStatus());
        assertEquals(6, reloadedMaterial().getCurrentStock());
        int onOrderAfterReceipt = reloadedMaterial().getStockOnOrder();

        ReturnToVendorOutput draft = returnOf(receipt, 4);
        assertEquals(0, new BigDecimal("20.00").compareTo(draft.getTotalValue()));
        returns.submit(draft.getId(), "buyer-1");
        flushAndClear();
        assertEquals(6, reloadedMaterial().getCurrentStock(), "rejected goods never entered stock");

        ReturnToVendorOutput resolved = returns.resolve(draft.getId(), "buyer-1", ResolutionType.REPLACEMENT, "RMA-77", null);
        flushAndClear();
        assertEquals("RESOLVED", resolved.getStatus());
        assertEquals("PARTIALLY_RECEIVED", orders.getById(orderId).getStatus());
        assertEquals(onOrderAfterReceipt + 4, reloadedMaterial().getStockOnOrder());

        GoodsReceiptOutput replacement = receive(4, 0, null);
        flushAndClear();
        assertEquals(4, replacement.getLines().get(0).getQuantityReceived());
        assertEquals("COMPLETED", orders.getById(orderId).getStatus());
        assertEquals(10, reloadedMaterial().getCurrentStock());
        assertEquals(onOrderAfterReceipt, reloadedMaterial().getStockOnOrder());

        assertThrows(Exception.class, () -> receive(1, 0, null), "nothing more is expected on the order");
    }

    @Test
    @DisplayName("quantities: returns cannot exceed the rejection together, a cancelled return frees its quantity, and a credit note is valued at order price")
    void quantitiesAndCreditNote() {
        GoodsReceiptOutput receipt = receivedWithRejects();

        ReturnToVendorOutput first = returnOf(receipt, 3);
        assertThrows(ReturnToVendorInvalidQuantityException.class, () -> returnOf(receipt, 2));
        ReturnToVendorOutput second = returnOf(receipt, 1);

        returns.cancel(first.getId(), "buyer-1", "Prepared by mistake");
        flushAndClear();
        ReturnToVendorOutput third = returnOf(receipt, 3);

        returns.submit(second.getId(), "buyer-1");
        flushAndClear();
        ReturnToVendorOutput credited = returns.resolve(second.getId(), "buyer-1", ResolutionType.CREDIT_NOTE, "AV-2026-3", "Credited");
        flushAndClear();

        assertEquals(0, new BigDecimal("5.00").compareTo(new BigDecimal(credited.getCreditNoteAmount())));
        assertEquals("COMPLETED", orders.getById(orderId).getStatus(), "a credit note does not reopen the order");
        assertEquals(3, returns.getById(third.getId()).getLines().get(0).getQuantityToReturn());
        assertEquals(3, returns.getByGoodsReceiptId(receipt.getId().toString()).size());
        assertThrows(GoodsReceiptBusinessException.class, () -> receive(1, 0, null));
    }
}
