package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.application.dtos.material.CreateMaterialInput;
import com.materia.backend.contexts.masterdata.application.dtos.material.MaterialOutput;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Material management
 */
public interface MaterialUseCase extends BaseUseCase<CreateMaterialInput, MaterialOutput, UUID> {

    List<MaterialOutput> getMaterialsByCategory(UUID categoryId);

    List<MaterialOutput> getMaterialsBySupplier(UUID supplierId);

    MaterialOutput increaseStock(UUID id, int quantity);

    MaterialOutput decreaseStock(UUID id, int quantity);

    List<MaterialOutput> getMaterialsBelowMinimumStock();

    List<MaterialOutput> getAvailableStockMaterials();

    List<MaterialOutput> getOutOfStockMaterials();
}
