package com.materia.backend.contexts.goodsReceipt;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineOutput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.UserFixtures.aReceiver;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Procurement and master data talk only through events, delivered after commit as between two services: ordering
 * puts stock on order, receiving moves it into stock, and the receipt line records the levels master data reports.
 * Every call commits; the rows are removed afterwards.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(properties = "app.events.in-process.mode=after-commit")
class ReceiptStockEventsIT extends AbstractIntegrationTest {

    @Autowired private PurchaseOrderUseCase orders;
    @Autowired private GoodsReceiptUseCase receipts;
    @Autowired private MaterialRepository materials;
    @Autowired private UserRepository users;
    @Autowired private JdbcTemplate jdbc;

    private Material material;
    private User receiver;
    private UUID orderId;
    private UUID receiptId;

    @AfterEach
    void cleanUp() {
        if (receiptId != null) {
            jdbc.update("delete from goods_receipt_lines where goods_receipt_id = ?", receiptId);
            jdbc.update("delete from goods_receipts where id = ?", receiptId);
        }
        if (orderId != null) {
            jdbc.update("delete from purchase_order_lines where purchase_order_id = ?", orderId);
            jdbc.update("delete from purchase_orders where id = ?", orderId);
        }
        if (material != null) {
            jdbc.update("delete from material_stock_movements where material_id = ?", material.getId());
            jdbc.update("delete from materials where id = ?", material.getId());
            jdbc.update("delete from outbox_messages where aggregate_id = ?", material.getId().toString());
        }
        if (receiver != null) {
            jdbc.update("delete from users where id = ?", receiver.getId());
            jdbc.update("delete from outbox_messages where aggregate_id = ?", receiver.getId().toString());
        }
        jdbc.update("delete from processed_events where processed_at > now() - interval '10 minutes' "
                + "and consumer in ('masterData.stock-on-order', 'masterData.received-goods-stock', "
                + "'goodsReceipt.receipt-stock-levels')");
        if (orderId != null) jdbc.update("delete from outbox_messages where aggregate_id = ?", orderId.toString());
        if (receiptId != null) jdbc.update("delete from outbox_messages where aggregate_id = ?", receiptId.toString());
    }

    private Material reloaded() {
        return materials.findById(material.getId()).orElseThrow();
    }

    private PurchaseOrderOutput readyOrder(int quantity) {
        receiver = users.save(aReceiver().build());
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setMaterialId(material.getId());
        line.setMaterialCode(material.getCode().getValue());
        line.setMaterialName(material.getName());
        line.setQuantity(quantity);
        line.setUnitPrice(Money.of("5.00", CurrencyCode.MAD));
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setSupplierId(UUID.randomUUID());
        input.setSupplierName("Acme Supplies");
        input.setOrderedBy("buyer-1");
        input.setCurrencyCode("MAD");
        input.setLines(new ArrayList<>(List.of(line)));
        input.setUserId("buyer-1");
        orderId = orders.create(input).getId();
        orders.submit(orderId, "buyer-1");
        orders.confirm(orderId, "admin-1");
        return orders.assignReceiver(orderId, "buyer-1", "Bob", receiver.getId().toString(), null);
    }

    private GoodsReceiptOutput receive(PurchaseOrderOutput order, int received, int rejected) {
        GoodsReceiptLineInput line = new GoodsReceiptLineInput();
        line.setPurchaseOrderLineId(order.getLines().get(0).getId().toString());
        line.setMaterialCode(order.getLines().get(0).getMaterialCode());
        line.setQuantityReceived(received);
        line.setQuantityRejected(rejected);
        line.setQualityStatus(rejected == 0 ? "ACCEPTED" : "PARTIAL");
        line.setRejectionReason(rejected == 0 ? null : "Damaged in transit");
        CreateGoodsReceiptInput input = new CreateGoodsReceiptInput();
        input.setPurchaseOrderId(order.getId().toString());
        input.setReceivedBy(receiver.getId().toString());
        input.setReceivedByName("Bob");
        input.setUserId(receiver.getId().toString());
        input.setLines(new ArrayList<>(List.of(line)));
        GoodsReceiptOutput created = receipts.create(input);
        receiptId = created.getId();
        return receipts.complete(created.getId(), receiver.getId().toString());
    }

    @Test
    @DisplayName("events: an order puts stock on order, and its receipt moves the accepted goods into stock")
    void orderAndReceipt_moveStockThroughEvents() {
        material = materials.save(aMaterial().stock(20).build());

        PurchaseOrderOutput order = readyOrder(10);
        assertEquals(10, reloaded().getStockOnOrder(), "applied from StockOnOrderChanged after the order committed");

        receive(order, 10, 2);

        Material after = reloaded();
        assertEquals(28, after.getCurrentStock(), "8 accepted units entered stock");
        assertEquals(0, after.getStockOnOrder(), "all 10 received units left the stock on order");

        GoodsReceiptLineOutput line = receipts.getById(receiptId).getLines().get(0);
        assertEquals(20, line.getStockBefore(), "levels reported back by master data");
        assertEquals(28, line.getStockAfter());
    }
}
