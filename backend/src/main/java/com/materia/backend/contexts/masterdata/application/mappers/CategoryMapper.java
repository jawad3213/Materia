package com.materia.backend.contexts.masterdata.application.mappers;

import com.materia.backend.contexts.masterdata.application.dtos.category.requests.CreateCategoryRequest;
import com.materia.backend.contexts.masterdata.application.dtos.category.requests.UpdateCategoryRequest;
import com.materia.backend.contexts.masterdata.application.dtos.category.responses.CategoryResponseDto;
import com.materia.backend.contexts.masterdata.domain.entities.Category;
import com.materia.backend.contexts.masterdata.domain.enums.CategoryType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import com.materia.backend.common.application.BaseMapper;

/**
 * Mapper for Category: Request DTO -> Entity and Entity -> Response DTO
 */
@Component
public class CategoryMapper implements BaseMapper<Category, CreateCategoryRequest, UpdateCategoryRequest, CategoryResponseDto> {

    // ============================================================
    // REQUEST DTO -> ENTITY
    // ============================================================

    public Category toEntity(CreateCategoryRequest request) {
        if (request == null) return null;

        Category.Builder builder = Category.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .createdBy(request.getCreatedBy());

        if (request.getParentId() != null) builder.parentId(request.getParentId());
        if (request.getCategoryType() != null) builder.categoryType(CategoryType.valueOf(request.getCategoryType()));
        if (request.getColor() != null) builder.color(request.getColor());
        if (request.getIcon() != null) builder.icon(request.getIcon());

        return builder.build();
    }

    public void updateEntity(Category entity, UpdateCategoryRequest request) {
        if (entity == null || request == null) return;

        if (request.getName() != null) entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getShortDescription() != null) entity.setShortDescription(request.getShortDescription());
        if (request.getParentId() != null) entity.setParentId(request.getParentId());
        if (request.getCategoryType() != null) entity.setCategoryType(CategoryType.valueOf(request.getCategoryType()));
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getColor() != null) entity.setColor(request.getColor());
        if (request.getIcon() != null) entity.setIcon(request.getIcon());
    }

    // ============================================================
    // ENTITY -> RESPONSE DTO
    // ============================================================

    public CategoryResponseDto toResponse(Category entity) {
        if (entity == null) return null;

        CategoryResponseDto response = new CategoryResponseDto();
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
        response.setColor(entity.getColor());
        response.setIcon(entity.getIcon());
        response.setMaterialCount(entity.getMaterialCount());
        response.setSubCategoryCount(entity.getSubCategoryCount());
        response.setTotalItems(entity.getTotalItems());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public List<CategoryResponseDto> toResponseList(List<Category> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
