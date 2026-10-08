package com.materia.backend.contexts.goodsReceipt.domain.ports.out;

import java.util.Optional;
import java.util.UUID;

/** The material stock that receipts move. */
public interface ReceivedStock {

    /**
     * Accepted goods enter stock; everything received (accepted or rejected) leaves the stock on order. The
     * material is found by id when one is given, else by code.
     *
     * @return the stock before and after, or empty when the material is unknown
     */
    Optional<StockLevels> receive(UUID materialId, String materialCode, int acceptedQuantity, int receivedQuantity,
                                  String reason, String userId);

    /** Releases up to {@code quantity} of the stock on order, never below zero. */
    void releaseOnOrder(UUID materialId, String materialCode, int quantity, String userId);

    record StockLevels(int before, int after) {
    }
}
