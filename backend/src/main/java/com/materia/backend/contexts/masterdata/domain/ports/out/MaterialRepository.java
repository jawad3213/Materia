package com.materia.backend.contexts.masterData.domain.ports.out;

import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;

import com.materia.backend.common.domain.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Output Port for material persistence
 * Hexagonal Architecture - The domain defines the contract,
 * the infrastructure implements it
 */
public interface MaterialRepository extends BaseRepository<Material> {

    /**
     * Finds a material by its code
     */
    Optional<Material> findByCode(String code);


    /**
     * Finds materials by category
     */
    List<Material> findByCategoryId(String categoryId);

    /**
     * Finds materials by supplier
     */
    List<Material> findBySupplierId(String supplierId);

    /**
     * Finds materials by status
     */
    List<Material> findByStatus(MaterialStatus status);

    /**
     * Finds materials by material type
     */
    List<Material> findByMaterialType(com.materia.backend.contexts.masterData.domain.enums.MaterialType materialType);

    /**
     * Checks if a material code already exists
     */
    boolean existsByCode(String code);

    /**
     * Checks if at least one material is linked to a category
     */
    boolean existsByCategoryId(String categoryId);

    /**
     * Checks if at least one material is linked to a supplier
     */
    boolean existsBySupplierId(String supplierId);

    /**
     * Searches by keyword (name, description, keywords)
     */
    List<Material> search(String keyword);

    /**
     * Finds materials whose stock is below the minimum threshold
     */
    List<Material> findBelowMinimumStock();

    /**
     * Finds materials whose stock is below the reorder point
     */
    List<Material> findBelowReorderPoint();

    /**
     * Finds materials with available stock > 0
     */
    List<Material> findAvailableStock();

    /**
     * Finds materials with available stock <= 0 (out of stock)
     */
    List<Material> findOutOfStock();

    /**
     * Advanced paginated search with multiple filters
     */
    com.materia.backend.common.application.PageResponse<Material> searchAdvanced(
            com.materia.backend.contexts.masterData.domain.valueObjects.MaterialSearchFilter filter, 
            int page, 
            int size);
}
