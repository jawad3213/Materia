package com.materia.backend.contexts.invoice.domain.ports.out;

import com.materia.backend.common.domain.valueObjects.Money;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The purchase orders invoices are matched against, and what their receipts accepted. */
public interface InvoicedOrders {

    Optional<OrderToInvoice> find(UUID purchaseOrderId);

    /** The lines of the order's validated (completed or partial) goods receipts. */
    List<ReceivedLine> validatedReceiptLines(String purchaseOrderId);

    /** @param goodsReceived whether goods were received on the order (partially received, received or completed) */
    record OrderToInvoice(UUID id, String orderCode, boolean goodsReceived, UUID supplierId, String supplierName,
                          String currencyCode, List<OrderedLine> lines) {
    }

    /** @param taxRate the order line's VAT rate in percent; null when it has none */
    record OrderedLine(UUID id, String materialCode, String materialName, String unitOfMeasure, Integer quantity,
                       Money unitPrice, java.math.BigDecimal taxRate) {
    }

    record ReceivedLine(String purchaseOrderLineId, Integer quantityReceived, Integer quantityAccepted,
                        Integer quantityRejected) {
    }
}
