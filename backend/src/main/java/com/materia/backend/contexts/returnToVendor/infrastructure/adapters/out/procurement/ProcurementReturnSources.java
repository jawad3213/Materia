package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.procurement;

import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptView;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptLookupUseCase;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialInventoryUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderLookupUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderReceivingUseCase;
import com.materia.backend.contexts.returnToVendor.domain.ports.out.ReturnSources;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Reaches receipts, orders and material stock through the public use cases of the goods receipt, purchase order
 * and master data contexts.
 */
@Component
public class ProcurementReturnSources implements ReturnSources {

    private final GoodsReceiptLookupUseCase receipts;
    private final PurchaseOrderLookupUseCase orders;
    private final PurchaseOrderReceivingUseCase receiving;
    private final MaterialInventoryUseCase materials;

    public ProcurementReturnSources(GoodsReceiptLookupUseCase receipts,
                                    PurchaseOrderLookupUseCase orders,
                                    PurchaseOrderReceivingUseCase receiving,
                                    MaterialInventoryUseCase materials) {
        this.receipts = receipts;
        this.orders = orders;
        this.receiving = receiving;
        this.materials = materials;
    }

    @Override
    public Optional<ReceiptToReturn> findReceipt(UUID goodsReceiptId) {
        return receipts.findReceipt(goodsReceiptId).map(ProcurementReturnSources::toReceiptToReturn);
    }

    @Override
    public Optional<OrderToReplace> findOrder(UUID purchaseOrderId) {
        return orders.findOrder(purchaseOrderId).map(order -> new OrderToReplace(order.id(), order.status(),
                order.goodsReceived(), order.supplierCode(), order.currencyCode()));
    }

    @Override
    public void reopenOrderForReplacement(UUID purchaseOrderId, String userId) {
        receiving.reopenForReplacement(purchaseOrderId, userId);
    }

    @Override
    public void putBackOnOrder(UUID materialId, String materialCode, int quantity, String userId) {
        materials.addStockOnOrder(materialId, materialCode, quantity, userId);
    }

    private static ReceiptToReturn toReceiptToReturn(GoodsReceiptView receipt) {
        return new ReceiptToReturn(receipt.id(), receipt.receiptCode(), receipt.validated(), receipt.purchaseOrderId(),
                receipt.purchaseOrderCode(), receipt.supplierId(), receipt.supplierName(),
                receipt.lines().stream()
                        .map(line -> new ReceivedLine(line.id(), line.purchaseOrderLineId(), line.materialId(),
                                line.materialCode(), line.materialName(), line.unitOfMeasure(), line.unitPrice(),
                                line.quantityRejected(), line.rejectionReason(), line.qualityNotes()))
                        .toList());
    }
}
