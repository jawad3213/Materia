package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.domain.entities.Material;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Input Port (Use Case) for Material management
 * Hexagonal Architecture - Defines the use cases available to the outside
 */
public interface MaterialUseCase {

    Material createMaterial(Material material);

    Material updateMaterial(UUID id, Material materialDetails);

    void deleteMaterial(UUID id);

    Optional<Material> getMaterial(UUID id);

    Optional<Material> getMaterialByCode(String code);

    List<Material> getAllMaterials();

    List<Material> getMaterialsByCategory(String categoryId);

    List<Material> getMaterialsBySupplier(String supplierId);

    Material increaseStock(UUID id, int quantity);

    Material decreaseStock(UUID id, int quantity);

    List<Material> getMaterialsBelowMinimumStock();
}
