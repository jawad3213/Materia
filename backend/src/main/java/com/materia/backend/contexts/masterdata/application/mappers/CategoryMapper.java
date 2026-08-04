package com.materia.backend.contexts.masterData.application.mappers;

import com.materia.backend.contexts.masterData.application.dtos.category.CreateCategoryInput;
import com.materia.backend.contexts.masterData.application.dtos.category.CategoryOutput;
import com.materia.backend.contexts.masterData.application.dtos.category.UpdateCategoryInput;
import com.materia.backend.contexts.masterData.domain.entities.Category;
import com.materia.backend.contexts.masterData.domain.enums.MaterialCategoryType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import com.materia.backend.common.application.BaseMapper;

/**
 * Mapper for Category: Request DTO -> Entity and Entity -> Response DTO
 */
@Component
public class CategoryMapper implements BaseMapper<Category, CreateCategoryInput, UpdateCategoryInput, CategoryOutput> {

    // ============================================================
    // REQUEST DTO -> ENTITY
    // ============================================================

    public Category toEntity(CreateCategoryInput request) {
        if (request == null) return null;

        Category.Builder builder = Category.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .createdBy(request.getCreatedBy());

        if (request.getParentId() != null) builder.parentId(request.getParentId());
        if (request.getCategoryType() != null) builder.categoryType(MaterialCategoryType.fromValue(request.getCategoryType()));

        return builder.build();
    }

    public void updateEntity(Category entity, UpdateCategoryInput request) {
        if (entity == null || request == null) return;

        if (request.getName() != null) entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getShortDescription() != null) entity.setShortDescription(request.getShortDescription());
        if (request.getParentId() != null) entity.setParentId(request.getParentId());
        if (request.getCategoryType() != null) entity.setCategoryType(MaterialCategoryType.fromValue(request.getCategoryType()));
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getUpdatedBy() != null) entity.setUpdatedBy(request.getUpdatedBy());
    }

    // ============================================================
    // ENTITY -> RESPONSE DTO
    // ============================================================

    public CategoryOutput toResponse(Category entity) {
        if (entity == null) return null;

        CategoryOutput response = new CategoryOutput();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setShortDescription(entity.getShortDescription());
        response.setParentId(entity.getParentId());
        response.setParentCode(entity.getParentCode());
        response.setLevel(entity.getLevel());
        response.setPath(entity.getPath());
        response.setChildrenIds(entity.getChildrenIds());
        response.setCategoryType(entity.getCategoryType() != null ? entity.getCategoryType().name() : null);
        response.setStatus(entity.getStatus());
        response.setMaterialCount(entity.getMaterialCount());
        response.setSubCategoryCount(entity.getSubCategoryCount());
        response.setTotalItems(entity.getTotalItems());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public List<CategoryOutput> toResponseList(List<Category> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
