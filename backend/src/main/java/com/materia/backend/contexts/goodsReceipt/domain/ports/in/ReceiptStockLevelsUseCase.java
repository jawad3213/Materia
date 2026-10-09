package com.materia.backend.contexts.goodsReceipt.domain.ports.in;

import java.util.UUID;

/** Records on a receipt line the material stock before and after its goods entered stock (reported by master data). */
public interface ReceiptStockLevelsUseCase {

    /** Does nothing when the receipt or the line no longer exists. */
    void recordStockLevels(UUID goodsReceiptId, UUID goodsReceiptLineId, int stockBefore, int stockAfter);
}
