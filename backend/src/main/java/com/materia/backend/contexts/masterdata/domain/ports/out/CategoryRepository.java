package com.materia.backend.contexts.masterdata.domain.ports.out;

import com.materia.backend.contexts.masterdata.domain.entities.Category;
import com.materia.backend.contexts.masterdata.domain.enums.MaterialCategoryType;

import com.materia.backend.common.domain.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Output Port for category persistence
 * Hexagonal Architecture - The domain defines the contract,
 * the infrastructure implements it
 */
public interface CategoryRepository extends BaseRepository<Category> {

    /**
     * Finds a category by its code
     */
    Optional<Category> findByCode(String code);

    /**
     * Finds root categories (without parent)
     */
    List<Category> findRootCategories();

    /**
     * Finds sub-categories of a parent
     */
    List<Category> findByParentId(String parentId);

    /**
     * Finds categories by type
     */
    List<Category> findByCategoryType(MaterialCategoryType categoryType);

    /**
     * Finds active categories
     */
    List<Category> findByStatus(String status);

    /**
     * Checks if a category code already exists
     */
    boolean existsByCode(String code);

    /**
     * Checks if a category has children
     */
    boolean existsByParentId(String parentId);

    /**
     * Searches by keyword (name, description)
     */
    List<Category> search(String keyword);
}
