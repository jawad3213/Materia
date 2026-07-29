package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.masterdata.application.dtos.category.requests.CreateCategoryRequest;
import com.materia.backend.contexts.masterdata.application.dtos.category.responses.CategoryResponseDto;
import com.materia.backend.contexts.masterdata.domain.ports.in.CategoryUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/masterdata/categories")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    public CategoryController(CategoryUseCase categoryUseCase) {
        this.categoryUseCase = categoryUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody CreateCategoryRequest request) {
        CategoryResponseDto response = categoryUseCase.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategory(@PathVariable UUID id) {
        CategoryResponseDto response = categoryUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CategoryResponseDto> getCategoryByCode(@PathVariable String code) {
        CategoryResponseDto response = categoryUseCase.getByCode(code);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> responses = categoryUseCase.getAll();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable UUID id,
            @RequestBody CreateCategoryRequest request) {
        CategoryResponseDto response = categoryUseCase.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Custom Endpoints ----

    @GetMapping("/roots")
    public ResponseEntity<List<CategoryResponseDto>> getRootCategories() {
        List<CategoryResponseDto> responses = categoryUseCase.getRootCategories();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryResponseDto>> getSubCategories(@PathVariable UUID parentId) {
        List<CategoryResponseDto> responses = categoryUseCase.getSubCategories(parentId);
        return ResponseEntity.ok(responses);
    }
}
