package com.materia.backend.contexts.masterData.domain.ports.in;

import com.materia.backend.contexts.masterData.application.dtos.material.CreateMaterialInput;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterData.application.dtos.material.UpdateMaterialInput;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Material management
 */
public interface MaterialUseCase extends BaseUseCase<CreateMaterialInput, MaterialOutput, UUID> {

    MaterialOutput update(UUID id, UpdateMaterialInput request);

    List<MaterialOutput> getMaterialsByCategory(UUID categoryId);

    List<MaterialOutput> getMaterialsBySupplier(UUID supplierId);

    MaterialOutput increaseStock(UUID id, int quantity);

    MaterialOutput decreaseStock(UUID id, int quantity);

    List<MaterialOutput> getMaterialsBelowMinimumStock();

    List<MaterialOutput> getAvailableStockMaterials();

    List<MaterialOutput> getOutOfStockMaterials();

    List<MaterialOutput> getMaterialsByStatus(String status);

    List<MaterialOutput> getMaterialsByMaterialType(String materialType);

    com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.application.dtos.material.MaterialListOutput> getAllList(int page, int size);

    com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.application.dtos.material.MaterialListOutput> searchAdvancedList(
            com.materia.backend.contexts.masterData.application.dtos.material.MaterialSearchCriteria criteria, 
            int page, 
            int size);

    com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.application.dtos.material.MaterialListOutput> filterList(
            com.materia.backend.contexts.masterData.application.dtos.material.MaterialFilterCriteria criteria, 
            int page, 
            int size);
}
