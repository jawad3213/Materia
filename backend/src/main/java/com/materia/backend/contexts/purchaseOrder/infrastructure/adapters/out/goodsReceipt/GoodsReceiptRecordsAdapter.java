package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.goodsReceipt;

import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiptRecords;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Reads and records goods receipts through the public use case of the goods receipt context. */
@Component
public class GoodsReceiptRecordsAdapter implements ReceiptRecords {

    private final GoodsReceiptUseCase goodsReceipts;

    public GoodsReceiptRecordsAdapter(GoodsReceiptUseCase goodsReceipts) {
        this.goodsReceipts = goodsReceipts;
    }

    @Override
    public Map<String, Integer> receivedQuantitiesByOrderLine(String purchaseOrderId) {
        Map<String, Integer> received = new HashMap<>();
        for (GoodsReceiptOutput receipt : goodsReceipts.getByPurchaseOrderId(purchaseOrderId)) {
            if (!"COMPLETED".equals(receipt.getStatus()) && !"PARTIAL".equals(receipt.getStatus())) continue;
            for (var line : receipt.getLines()) {
                if (line.getPurchaseOrderLineId() == null) continue;
                received.merge(line.getPurchaseOrderLineId(),
                        line.getQuantityReceived() != null ? line.getQuantityReceived() : 0, Integer::sum);
            }
        }
        return received;
    }

    @Override
    public void receiveRemaining(String purchaseOrderId, String receiverId, String receiverName) {
        goodsReceipts.receiveRemaining(purchaseOrderId, receiverId, receiverName);
    }
}
