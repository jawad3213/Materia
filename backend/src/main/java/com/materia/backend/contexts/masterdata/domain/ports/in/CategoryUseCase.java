package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.domain.entities.Category;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Category management
 * Hexagonal Architecture - Defines the use cases available to the outside
 */
public interface CategoryUseCase extends BaseUseCase<Category, UUID> {

    List<Category> getRootCategories();

    List<Category> getSubCategories(UUID parentId);
}
