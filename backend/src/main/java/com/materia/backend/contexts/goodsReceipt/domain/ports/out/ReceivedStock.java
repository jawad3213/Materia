package com.materia.backend.contexts.goodsReceipt.domain.ports.out;

import java.util.UUID;

/**
 * The material stock that receipts move. Moving it is asynchronous: the receipt announces what each line received,
 * and the stock levels come back later (see the goods receipt's stock-levels event handler).
 */
public interface ReceivedStock {

    /** Whether the material exists; by id when one is given, else by code. A read-only check made before committing. */
    boolean knows(UUID materialId, String materialCode);

    /**
     * Announces a validated receipt line: the accepted quantity enters stock, and everything received (accepted or
     * rejected) leaves the stock on order. Call it once the receipt is saved.
     */
    void received(UUID goodsReceiptId, UUID goodsReceiptLineId, UUID materialId, String materialCode,
                  int acceptedQuantity, int receivedQuantity, String reason, String userId);
}
