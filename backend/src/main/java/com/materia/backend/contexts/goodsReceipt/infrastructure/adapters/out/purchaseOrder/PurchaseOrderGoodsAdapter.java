package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.purchaseOrder;

import com.materia.backend.contexts.goodsReceipt.domain.ports.out.OrderedGoods;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderView;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderLookupUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderReceivingUseCase;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Reads and updates purchase orders through the public use cases of the purchase order context. */
@Component
public class PurchaseOrderGoodsAdapter implements OrderedGoods {

    private final PurchaseOrderLookupUseCase orders;
    private final PurchaseOrderReceivingUseCase receiving;

    public PurchaseOrderGoodsAdapter(PurchaseOrderLookupUseCase orders, PurchaseOrderReceivingUseCase receiving) {
        this.orders = orders;
        this.receiving = receiving;
    }

    @Override
    public Optional<OrderToReceive> find(UUID purchaseOrderId) {
        return orders.findOrder(purchaseOrderId).map(PurchaseOrderGoodsAdapter::toOrderToReceive);
    }

    @Override
    public void recordDelivery(UUID purchaseOrderId, boolean fullyReceived, String userId) {
        receiving.recordReceipt(purchaseOrderId, fullyReceived, userId);
    }

    private static OrderToReceive toOrderToReceive(PurchaseOrderView order) {
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
