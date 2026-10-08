package com.materia.backend.contexts.goodsReceipt.domain.ports.in;

import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Read access to goods receipts for the other contexts. */
public interface GoodsReceiptLookupUseCase {

    Optional<GoodsReceiptView> findReceipt(UUID goodsReceiptId);

    /** Every receipt of the order, whatever its status. */
    List<GoodsReceiptView> findReceiptsForOrder(String purchaseOrderId);
}
