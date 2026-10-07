package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.application.dtos.material.MaterialReference;
import com.materia.backend.contexts.masterData.application.dtos.material.StockMovement;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialInventoryUseCase;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class MaterialInventoryService implements MaterialInventoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(MaterialInventoryService.class);

    private final MaterialRepository materialRepository;

    public MaterialInventoryService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaterialReference> findMaterial(UUID materialId) {
        return materialId != null ? materialRepository.findById(materialId).map(MaterialInventoryService::toReference)
                : Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaterialReference> findMaterialByCode(String materialCode) {
        return materialCode != null ? materialRepository.findByCode(materialCode).map(MaterialInventoryService::toReference)
                : Optional.empty();
    }

    @Override
    public boolean addStockOnOrder(UUID materialId, String materialCode, int quantity, String userId) {
        if (quantity <= 0) return false;
        Optional<Material> found = find(materialId, materialCode);
        if (found.isEmpty()) {
            log.warn("Stock on order not recorded: material {} / {} not found", materialId, materialCode);
            return false;
        }
        Material material = found.get();
        material.addStockOnOrder(quantity);
        if (userId != null) material.setUpdatedBy(userId);
        materialRepository.save(material);
        return true;
    }

    @Override
    public int releaseStockOnOrder(UUID materialId, String materialCode, int quantity, String userId) {
        if (quantity <= 0) return 0;
        return find(materialId, materialCode).map(material -> {
            int released = releaseOnOrder(material, quantity);
            if (released > 0) {
                if (userId != null) material.setUpdatedBy(userId);
                materialRepository.save(material);
            }
            return released;
        }).orElse(0);
    }

    @Override
    public Optional<StockMovement> receiveGoods(UUID materialId, String materialCode, int acceptedQuantity,
                                                int receivedQuantity, String reason, String userId) {
        Optional<Material> found = materialId != null ? materialRepository.findById(materialId)
                : materialCode != null ? materialRepository.findByCode(materialCode) : Optional.empty();
        return found.map(material -> {
            int stockBefore = material.getCurrentStock() != null ? material.getCurrentStock() : 0;
            material.increaseStock(acceptedQuantity, reason);
            releaseOnOrder(material, receivedQuantity);
            if (userId != null) material.setUpdatedBy(userId);
            Material saved = materialRepository.save(material);
            return new StockMovement(stockBefore, saved.getCurrentStock() != null ? saved.getCurrentStock() : 0);
        });
    }

    /** Reduces the stock on order by up to {@code quantity}; returns what was released. */
    private static int releaseOnOrder(Material material, int quantity) {
        int onOrder = material.getStockOnOrder() != null ? material.getStockOnOrder() : 0;
        int released = Math.min(onOrder, quantity);
        if (released <= 0) return 0;
        material.reduceStockOnOrder(released);
        return released;
    }

    private Optional<Material> find(UUID materialId, String materialCode) {
        if (materialId != null) {
            Optional<Material> byId = materialRepository.findById(materialId);
            if (byId != null && byId.isPresent()) return byId;
        }
        if (materialCode == null || materialCode.isBlank()) return Optional.empty();
        Optional<Material> byCode = materialRepository.findByCode(materialCode);
        return byCode != null ? byCode : Optional.empty();
    }

    public static MaterialReference toReference(Material material) {
        return new MaterialReference(
                material.getId(),
                material.getCode() != null ? material.getCode().getValue() : null,
                material.getName(),
                material.getDescription(),
                material.getUnitOfMeasure() != null ? material.getUnitOfMeasure().getCode() : null,
                material.getStandardPrice(),
                material.isOrderable());
    }
}
