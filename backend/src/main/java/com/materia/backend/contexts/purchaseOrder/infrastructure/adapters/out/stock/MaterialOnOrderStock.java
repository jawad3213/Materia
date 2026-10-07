package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.stock;

import com.materia.backend.contexts.masterData.domain.ports.in.MaterialInventoryUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.OnOrderStock;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Records stock on order through the public use case of the master data context. */
@Component
public class MaterialOnOrderStock implements OnOrderStock {

    private final MaterialInventoryUseCase materials;

    public MaterialOnOrderStock(MaterialInventoryUseCase materials) {
        this.materials = materials;
    }

    @Override
    public void add(UUID materialId, String materialCode, int quantity) {
        materials.addStockOnOrder(materialId, materialCode, quantity, null);
    }

    @Override
    public void release(UUID materialId, String materialCode, int quantity) {
        materials.releaseStockOnOrder(materialId, materialCode, quantity, null);
    }
}
