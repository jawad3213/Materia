package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.material;

import com.materia.backend.contexts.masterData.application.dtos.material.MaterialReference;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialInventoryUseCase;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.MaterialCatalog;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.MaterialSnapshot;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Reads materials through the public use case of the master data context. */
@Component
public class MasterDataMaterialCatalog implements MaterialCatalog {

    private final MaterialInventoryUseCase materials;

    public MasterDataMaterialCatalog(MaterialInventoryUseCase materials) {
        this.materials = materials;
    }

    @Override
    public Optional<MaterialSnapshot> findById(UUID materialId) {
        return materials.findMaterial(materialId).map(MasterDataMaterialCatalog::toSnapshot);
    }

    @Override
    public Optional<MaterialSnapshot> findByCode(String materialCode) {
        return materials.findMaterialByCode(materialCode).map(MasterDataMaterialCatalog::toSnapshot);
    }

    public static MaterialSnapshot toSnapshot(MaterialReference material) {
        return new MaterialSnapshot(material.id(), material.code(), material.name(), material.description(),
                material.unitOfMeasureCode(), material.standardPrice(), material.orderable());
    }
}
