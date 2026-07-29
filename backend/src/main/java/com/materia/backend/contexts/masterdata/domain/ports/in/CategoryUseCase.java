package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.domain.entities.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Input Port (Use Case) for Category management
 * Hexagonal Architecture - Defines the use cases available to the outside
 */
public interface CategoryUseCase {

    Category createCategory(Category category);

    Category updateCategory(UUID id, Category categoryDetails);

    void deleteCategory(UUID id);

    Optional<Category> getCategory(UUID id);

    Optional<Category> getCategoryByCode(String code);

    List<Category> getAllCategories();

    List<Category> getRootCategories();

    List<Category> getSubCategories(String parentId);
}
