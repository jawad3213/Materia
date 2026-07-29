package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.masterdata.application.dtos.category.CreateCategoryInput;
import com.materia.backend.contexts.masterdata.application.dtos.category.CategoryOutput;
import com.materia.backend.contexts.masterdata.domain.ports.in.CategoryUseCase;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.category.CreateCategoryWebRequest;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.category.CategoryWebResponse;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.mappers.CategoryWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/masterdata/categories")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;
    private final CategoryWebMapper webMapper;

    public CategoryController(CategoryUseCase categoryUseCase, CategoryWebMapper webMapper) {
        this.categoryUseCase = categoryUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping
    public ResponseEntity<CategoryWebResponse> createCategory(@Valid @RequestBody CreateCategoryWebRequest webRequest) {
        CreateCategoryInput request = webMapper.toAppCreateRequest(webRequest);
        CategoryOutput response = categoryUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryWebResponse> getCategory(@PathVariable UUID id) {
        CategoryOutput response = categoryUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CategoryWebResponse> getCategoryByCode(@PathVariable String code) {
        CategoryOutput response = categoryUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping
    public ResponseEntity<List<CategoryWebResponse>> getAllCategories() {
        List<CategoryOutput> responses = categoryUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryWebResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCategoryWebRequest webRequest) {
        CreateCategoryInput request = webMapper.toAppCreateRequest(webRequest);
        CategoryOutput response = categoryUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Custom Endpoints ----

    @GetMapping("/roots")
    public ResponseEntity<List<CategoryWebResponse>> getRootCategories() {
        List<CategoryOutput> responses = categoryUseCase.getRootCategories();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryWebResponse>> getSubCategories(@PathVariable UUID parentId) {
        List<CategoryOutput> responses = categoryUseCase.getSubCategories(parentId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }
}
