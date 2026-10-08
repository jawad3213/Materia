package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.stock;

import com.materia.backend.contexts.goodsReceipt.domain.ports.out.ReceivedStock;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialInventoryUseCase;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Moves material stock through the public use case of the master data context. */
@Component
public class MaterialReceivedStock implements ReceivedStock {

    private final MaterialInventoryUseCase materials;

    public MaterialReceivedStock(MaterialInventoryUseCase materials) {
        this.materials = materials;
    }

    @Override
    public Optional<StockLevels> receive(UUID materialId, String materialCode, int acceptedQuantity,
                                         int receivedQuantity, String reason, String userId) {
        return materials.receiveGoods(materialId, materialCode, acceptedQuantity, receivedQuantity, reason, userId)
                .map(movement -> new StockLevels(movement.stockBefore(), movement.stockAfter()));
    }

    @Override
    public void releaseOnOrder(UUID materialId, String materialCode, int quantity, String userId) {
        materials.releaseStockOnOrder(materialId, materialCode, quantity, userId);
    }
}
