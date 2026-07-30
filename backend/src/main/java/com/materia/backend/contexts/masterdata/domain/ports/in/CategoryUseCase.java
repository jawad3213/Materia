package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.application.dtos.category.CreateCategoryInput;
import com.materia.backend.contexts.masterdata.application.dtos.category.CategoryOutput;
import com.materia.backend.contexts.masterdata.application.dtos.category.UpdateCategoryInput;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Category management
 */
public interface CategoryUseCase extends BaseUseCase<CreateCategoryInput, CategoryOutput, UUID> {

    CategoryOutput update(UUID id, UpdateCategoryInput request);

    List<CategoryOutput> getRootCategories();

    List<CategoryOutput> getSubCategories(UUID parentId);
}
