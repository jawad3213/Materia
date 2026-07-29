package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.domain.entities.Material;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port d'entrÃ©e (Use Case) pour la gestion des MatÃ©riaux
 * Architecture Hexagonale - DÃ©finit les cas d'utilisation disponibles pour l'extÃ©rieur
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

