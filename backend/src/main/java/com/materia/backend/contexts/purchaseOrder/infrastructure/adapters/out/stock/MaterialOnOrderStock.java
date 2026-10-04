package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.stock;

import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.OnOrderStock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Keeps the material's stock on order in step with purchase orders. */
@Component
public class MaterialOnOrderStock implements OnOrderStock {

    private static final Logger log = LoggerFactory.getLogger(MaterialOnOrderStock.class);

    private final MaterialRepository materialRepository;

    public MaterialOnOrderStock(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public void add(UUID materialId, String materialCode, int quantity) {
        if (quantity <= 0) return;
        find(materialId, materialCode).ifPresentOrElse(material -> {
            material.addStockOnOrder(quantity);
            materialRepository.save(material);
        }, () -> log.warn("Stock on order not recorded: material {} / {} not found", materialId, materialCode));
    }

    @Override
    public void release(UUID materialId, String materialCode, int quantity) {
        if (quantity <= 0) return;
        find(materialId, materialCode).ifPresent(material -> {
            int onOrder = material.getStockOnOrder() != null ? material.getStockOnOrder() : 0;
            int released = Math.min(onOrder, quantity);
            if (released > 0) {
                material.reduceStockOnOrder(released);
                materialRepository.save(material);
            }
        });
    }

    private Optional<Material> find(UUID materialId, String materialCode) {
        if (materialId != null) {
            Optional<Material> byId = materialRepository.findById(materialId);
            if (byId.isPresent()) return byId;
        }
        return materialCode != null && !materialCode.isBlank() ? materialRepository.findByCode(materialCode) : Optional.empty();
    }
}
