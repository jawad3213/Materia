package com.materia.backend.contexts.masterdata.application.services;

import com.materia.backend.contexts.masterdata.application.dtos.category.requests.CreateCategoryRequest;
import com.materia.backend.contexts.masterdata.application.dtos.category.responses.CategoryResponseDto;
import com.materia.backend.contexts.masterdata.application.mappers.CategoryMapper;
import com.materia.backend.contexts.masterdata.domain.entities.Category;
import com.materia.backend.contexts.masterdata.domain.exceptions.CategoryNotFoundException;
import com.materia.backend.contexts.masterdata.domain.ports.in.CategoryUseCase;
import com.materia.backend.contexts.masterdata.domain.ports.out.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService implements CategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    // ============================================================
    // CRUD OPERATIONS (BaseUseCase)
    // ============================================================

    @Override
    public CategoryResponseDto create(CreateCategoryRequest request) {
        Category category = mapper.toEntity(request);
        Category saved = categoryRepository.save(category);
        return mapper.toResponse(saved);
    }

    @Override
    public CategoryResponseDto update(UUID id, CreateCategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getShortDescription() != null) existing.setShortDescription(request.getShortDescription());

        Category updated = categoryRepository.save(existing);
        return mapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponseDto getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        return mapper.toResponse(category);
    }

    @Override
    public CategoryResponseDto getByCode(String code) {
        Category category = categoryRepository.findByCode(code)
                .orElseThrow(() -> new CategoryNotFoundException(code));
        return mapper.toResponse(category);
    }

    @Override
    public List<CategoryResponseDto> getAll() {
        return mapper.toResponseList(categoryRepository.findAll());
    }

    // ============================================================
    // CUSTOM OPERATIONS
    // ============================================================

    @Override
    public List<CategoryResponseDto> getRootCategories() {
        return mapper.toResponseList(categoryRepository.findRootCategories());
    }

    @Override
    public List<CategoryResponseDto> getSubCategories(UUID parentId) {
        return mapper.toResponseList(categoryRepository.findByParentId(parentId.toString()));
    }
}
