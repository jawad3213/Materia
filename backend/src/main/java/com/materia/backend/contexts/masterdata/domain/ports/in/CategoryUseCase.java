package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.application.dtos.category.requests.CreateCategoryRequest;
import com.materia.backend.contexts.masterdata.application.dtos.category.responses.CategoryResponseDto;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Category management
 */
public interface CategoryUseCase extends BaseUseCase<CreateCategoryRequest, CategoryResponseDto, UUID> {

    List<CategoryResponseDto> getRootCategories();

    List<CategoryResponseDto> getSubCategories(UUID parentId);
}
