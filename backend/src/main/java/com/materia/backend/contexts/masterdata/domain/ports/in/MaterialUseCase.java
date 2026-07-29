package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.domain.entities.Material;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Material management
 * Hexagonal Architecture - Defines the use cases available to the outside
 */
public interface MaterialUseCase extends BaseUseCase<Material, UUID> {

    List<Material> getMaterialsByCategory(UUID categoryId);

    List<Material> getMaterialsBySupplier(UUID supplierId);

    Material increaseStock(UUID id, int quantity);

    Material decreaseStock(UUID id, int quantity);

    List<Material> getMaterialsBelowMinimumStock();
}
