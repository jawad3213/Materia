package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.purchaseOrder;

import com.materia.backend.contexts.goodsReceipt.domain.ports.out.OrderedGoods;
import com.materia.backend.contexts.purchaseOrder.application.dtos.OrderForReceiptOutput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderReceivingUseCase;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Reads and updates purchase orders through the public use case of the purchase order context. */
@Component
public class PurchaseOrderGoodsAdapter implements OrderedGoods {

    private final PurchaseOrderReceivingUseCase orders;

    public PurchaseOrderGoodsAdapter(PurchaseOrderReceivingUseCase orders) {
        this.orders = orders;
    }

    @Override
    public Optional<OrderToReceive> find(UUID purchaseOrderId) {
        return orders.findForReceipt(purchaseOrderId).map(PurchaseOrderGoodsAdapter::toOrderToReceive);
    }

    @Override
    public void recordDelivery(UUID purchaseOrderId, boolean fullyReceived, String userId) {
        orders.recordReceipt(purchaseOrderId, fullyReceived, userId);
    }

    private static OrderToReceive toOrderToReceive(OrderForReceiptOutput order) {
        return new OrderToReceive(order.id(), order.orderCode(), order.receivable(), order.assignedTo(),
                order.assignedToName(), order.supplierId(), order.supplierName(), order.expectedDeliveryDate(),
                order.requisitionId(),
                order.lines().stream()
                        .map(line -> new OrderedLine(line.id(), line.lineNumber(), line.materialId(), line.materialCode(),
                                line.materialName(), line.unitOfMeasure(), line.unitPrice(), line.quantity(),
                                line.requisitionLineId()))
                        .toList());
    }
}
