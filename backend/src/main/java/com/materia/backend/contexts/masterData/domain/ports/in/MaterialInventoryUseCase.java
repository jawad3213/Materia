package com.materia.backend.contexts.masterData.domain.ports.in;

import com.materia.backend.contexts.masterData.application.dtos.material.MaterialReference;
import com.materia.backend.contexts.masterData.application.dtos.material.StockMovement;

import java.util.Optional;
import java.util.UUID;

/**
 * Material lookups and stock movements for the other contexts (requisitions, orders, receipts, returns).
 *
 * <p>Methods taking an id and a code look the material up by id first and fall back to the code, unless stated
 * otherwise.
 */
public interface MaterialInventoryUseCase {

    Optional<MaterialReference> findMaterial(UUID materialId);

    Optional<MaterialReference> findMaterialByCode(String materialCode);

    /** Adds to the stock on order; false when the quantity is not positive or the material is unknown. */
    boolean addStockOnOrder(UUID materialId, String materialCode, int quantity, String userId);

    /** Releases up to {@code quantity} of the stock on order, never below zero; returns what was released. */
    int releaseStockOnOrder(UUID materialId, String materialCode, int quantity, String userId);

    /**
     * Goods received: the accepted quantity enters stock and everything received (accepted or rejected) leaves the
     * stock on order. The material is looked up by id when one is given, else by code, without fallback.
     *
     * @return the stock before and after, or empty when the material is unknown
     */
    Optional<StockMovement> receiveGoods(UUID materialId, String materialCode, int acceptedQuantity,
                                         int receivedQuantity, String reason, String userId);
}
