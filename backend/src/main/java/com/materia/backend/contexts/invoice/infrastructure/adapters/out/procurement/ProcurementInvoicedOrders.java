package com.materia.backend.contexts.invoice.infrastructure.adapters.out.procurement;

import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptView;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptLookupUseCase;
import com.materia.backend.contexts.invoice.domain.ports.out.InvoicedOrders;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderView;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderLookupUseCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Reads orders and receipts through the public use cases of the purchase order and goods receipt contexts. */
@Component
public class ProcurementInvoicedOrders implements InvoicedOrders {

    private final PurchaseOrderLookupUseCase orders;
    private final GoodsReceiptLookupUseCase receipts;

    public ProcurementInvoicedOrders(PurchaseOrderLookupUseCase orders, GoodsReceiptLookupUseCase receipts) {
        this.orders = orders;
        this.receipts = receipts;
    }

    @Override
    public Optional<OrderToInvoice> find(UUID purchaseOrderId) {
        return orders.findOrder(purchaseOrderId).map(ProcurementInvoicedOrders::toOrderToInvoice);
    }

    @Override
    public List<ReceivedLine> validatedReceiptLines(String purchaseOrderId) {
        return receipts.findReceiptsForOrder(purchaseOrderId).stream()
                .filter(GoodsReceiptView::validated)
                .flatMap(receipt -> receipt.lines().stream())
                .map(line -> new ReceivedLine(line.purchaseOrderLineId(), line.quantityReceived(),
                        line.quantityAccepted(), line.quantityRejected()))
                .toList();
    }

    private static OrderToInvoice toOrderToInvoice(PurchaseOrderView order) {
        return new OrderToInvoice(order.id(), order.orderCode(), order.goodsReceived(), order.supplierId(),
                order.supplierName(), order.currencyCode(),
                order.lines().stream()
                        .map(line -> new OrderedLine(line.id(), line.materialCode(), line.materialName(),
                                line.unitOfMeasure(), line.quantity(), line.unitPrice(), line.taxRate()))
                        .toList());
    }
}
