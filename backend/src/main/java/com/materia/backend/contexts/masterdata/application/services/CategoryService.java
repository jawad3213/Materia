package com.materia.backend.contexts.masterdata.application.services;

import com.materia.backend.contexts.masterdata.application.dtos.category.CreateCategoryInput;
import com.materia.backend.contexts.masterdata.application.dtos.category.CategoryOutput;
import com.materia.backend.contexts.masterdata.application.dtos.category.UpdateCategoryInput;
import com.materia.backend.contexts.masterdata.application.mappers.CategoryMapper;
import com.materia.backend.common.application.exceptions.BusinessException;
import com.materia.backend.common.application.exceptions.ValidationException;
import com.materia.backend.contexts.masterdata.domain.entities.Category;
import com.materia.backend.contexts.masterdata.domain.exceptions.CategoryNotFoundException;
import com.materia.backend.contexts.masterdata.domain.ports.in.CategoryUseCase;
import com.materia.backend.contexts.masterdata.domain.ports.out.CategoryRepository;
import com.materia.backend.contexts.masterdata.domain.ports.out.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class CategoryService implements CategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository categoryRepository,
                           MaterialRepository materialRepository,
                           CategoryMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.materialRepository = materialRepository;
        this.mapper = mapper;
    }

    // ============================================================
    // CRUD OPERATIONS (BaseUseCase)
    // ============================================================

    @Override
    @Transactional
    public CategoryOutput create(CreateCategoryInput request) {
        Category category = mapper.toEntity(request);
        Category parent = resolveParentCategory(normalizeParentId(request.getParentId()), null);
        applyHierarchyMetadata(category, parent);
        Category saved = categoryRepository.save(category);
        return mapper.toResponse(saved);
    }

    @Override
    public CategoryOutput update(UUID id, CreateCategoryInput request) {
        UpdateCategoryInput updateRequest = new UpdateCategoryInput();
        updateRequest.setName(request.getName());
        updateRequest.setDescription(request.getDescription());
        updateRequest.setShortDescription(request.getShortDescription());
        updateRequest.setParentId(request.getParentId());
        updateRequest.setCategoryType(request.getCategoryType());
        updateRequest.setUpdatedBy(request.getCreatedBy());
        return update(id, updateRequest);
    }

    @Override
    @Transactional
    public CategoryOutput update(UUID id, UpdateCategoryInput request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));

        String previousName = existing.getName();
        String previousParentId = normalizeParentId(existing.getParentId());
        boolean parentProvided = request.getParentId() != null;
        String targetParentId = parentProvided ? normalizeParentId(request.getParentId()) : previousParentId;
        Category targetParent = resolveParentCategory(targetParentId, id);

        mapper.updateEntity(existing, request);
        applyHierarchyMetadata(existing, targetParent);
        Category updated = categoryRepository.save(existing);

        if (parentProvided && !Objects.equals(previousParentId, targetParentId)) {
            refreshDescendantHierarchy(updated);
        }

        if (!Objects.equals(previousName, updated.getName())) {
            syncMaterialCategoryNames(updated);
        }

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));

        if (categoryRepository.existsByParentId(id.toString())) {
            throw new BusinessException("Cannot delete category with sub-categories", "CATEGORY_HAS_CHILDREN");
        }
        if (materialRepository.existsByCategoryId(id.toString())) {
            throw new BusinessException("Cannot delete category with linked materials", "CATEGORY_HAS_MATERIALS");
        }

        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryOutput getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        return mapper.toResponse(category);
    }

    @Override
    public CategoryOutput getByCode(String code) {
        Category category = categoryRepository.findByCode(code)
                .orElseThrow(() -> new CategoryNotFoundException(code));
        return mapper.toResponse(category);
    }

    @Override
    public List<CategoryOutput> getAll() {
        return mapper.toResponseList(categoryRepository.findAll());
    }

    // ============================================================
    // CUSTOM OPERATIONS
    // ============================================================

    @Override
    public List<CategoryOutput> getRootCategories() {
        return mapper.toResponseList(categoryRepository.findRootCategories());
    }

    @Override
    public List<CategoryOutput> getSubCategories(UUID parentId) {
        return mapper.toResponseList(categoryRepository.findByParentId(parentId.toString()));
    }

    private void applyHierarchyMetadata(Category category, Category parent) {
        if (parent == null) {
            category.setParentId(null);
            category.setParentCode(null);
            category.setLevel(0);
            category.setPath(buildRootPath(category.getCode()));
            return;
        }

        category.setParentId(parent.getId().toString());
        category.setParentCode(parent.getCode());
        category.setLevel((parent.getLevel() != null ? parent.getLevel() : 0) + 1);
        category.setPath(buildChildPath(parent, category.getCode()));
    }

    private String buildRootPath(String code) {
        return "/" + code + "/";
    }

    private String buildChildPath(Category parent, String code) {
        String parentPath = StringUtils.hasText(parent.getPath())
                ? parent.getPath()
                : buildRootPath(parent.getCode());
        return parentPath + code + "/";
    }

    private Category resolveParentCategory(String parentId, UUID currentCategoryId) {
        if (parentId == null) {
            return null;
        }

        UUID parentUuid = parseUuid(parentId, "parentId");
        if (currentCategoryId != null && currentCategoryId.equals(parentUuid)) {
            throw new ValidationException("A category cannot be its own parent", Map.of("parentId", "Category cannot be its own parent"));
        }
        if (currentCategoryId != null && isDescendant(parentUuid.toString(), currentCategoryId.toString())) {
            throw new ValidationException("A category cannot be moved under one of its descendants",
                    Map.of("parentId", "Parent category cannot be a descendant of the category"));
        }

        return categoryRepository.findById(parentUuid)
                .orElseThrow(() -> new CategoryNotFoundException(parentId));
    }

    private boolean isDescendant(String candidateId, String ancestorId) {
        for (Category child : categoryRepository.findByParentId(ancestorId)) {
            String childId = child.getId().toString();
            if (childId.equals(candidateId) || isDescendant(candidateId, childId)) {
                return true;
            }
        }
        return false;
    }

    private void refreshDescendantHierarchy(Category parent) {
        for (Category child : categoryRepository.findByParentId(parent.getId().toString())) {
            applyHierarchyMetadata(child, parent);
            Category savedChild = categoryRepository.save(child);
            refreshDescendantHierarchy(savedChild);
        }
    }

    private void syncMaterialCategoryNames(Category category) {
        List<com.materia.backend.contexts.masterdata.domain.entities.Material> materials =
                materialRepository.findByCategoryId(category.getId().toString());

        if (materials.isEmpty()) {
            return;
        }

        for (com.materia.backend.contexts.masterdata.domain.entities.Material material : materials) {
            material.setCategoryName(category.getName());
        }
        materialRepository.saveAll(materials);
    }

    private Category getCategoryByStringId(String rawId, String fieldName) {
        UUID uuid = parseUuid(rawId, fieldName);
        return categoryRepository.findById(uuid)
                .orElseThrow(() -> new CategoryNotFoundException(rawId));
    }

    private UUID parseUuid(String rawId, String fieldName) {
        try {
            return UUID.fromString(rawId);
        } catch (RuntimeException ex) {
            throw new ValidationException("Invalid " + fieldName,
                    Map.of(fieldName, "Must be a valid UUID"));
        }
    }

    private String normalizeParentId(String parentId) {
        return StringUtils.hasText(parentId) ? parentId.trim() : null;
    }
}
