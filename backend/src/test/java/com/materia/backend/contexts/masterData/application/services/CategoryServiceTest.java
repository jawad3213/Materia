package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.common.application.exceptions.BusinessException;
import com.materia.backend.common.application.exceptions.ValidationException;
import com.materia.backend.contexts.masterData.application.dtos.category.CategoryOutput;
import com.materia.backend.contexts.masterData.application.dtos.category.CreateCategoryInput;
import com.materia.backend.contexts.masterData.application.dtos.category.UpdateCategoryInput;
import com.materia.backend.contexts.masterData.application.mappers.CategoryMapper;
import com.materia.backend.contexts.masterData.domain.entities.Category;
import com.materia.backend.contexts.masterData.domain.enums.MaterialCategoryType;
import com.materia.backend.contexts.masterData.domain.exceptions.CategoryNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.out.CategoryRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Service tests for CategoryService (T092, US5).
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private CategoryCodeGeneratorService codeGenerator;

    private CategoryMapper mapper;
    private CategoryService categoryService;

    private final UUID categoryId = UUID.randomUUID();
    private Category category;

    @BeforeEach
    void setUp() {
        mapper = new CategoryMapper();
        categoryService = new CategoryService(categoryRepository, materialRepository, mapper, codeGenerator);

        category = Category.builder()
                .id(categoryId)
                .code("CAT-001")
                .name("Electronics")
                .description("Components")
                .categoryType(MaterialCategoryType.ELECTRONIC_CAT)
                .build();
    }

    @Test
    @DisplayName("create: generates code, sets hierarchy and persists category")
    void create_success() {
        CreateCategoryInput input = new CreateCategoryInput();
        input.setName("Hardware");
        input.setDescription("Nuts and bolts");
        input.setCategoryType("COMPONENT_CAT");

        when(codeGenerator.generateCode()).thenReturn("CAT-002");
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryOutput output = categoryService.create(input);

        assertNotNull(output);
        assertEquals("Hardware", output.getName());
        assertEquals("CAT-002", output.getCode());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("create: referencing an unknown parent throws CategoryNotFoundException")
    void create_unknownParent_throws() {
        CreateCategoryInput input = new CreateCategoryInput();
        input.setName("Microcontrollers");
        input.setParentId(UUID.randomUUID().toString());

        when(codeGenerator.generateCode()).thenReturn("CAT-003");
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.create(input));
    }

    @Test
    @DisplayName("update: circular hierarchy where category is its own parent throws ValidationException")
    void update_ownParent_throwsValidationException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        UpdateCategoryInput input = new UpdateCategoryInput();
        input.setName("Updated name");
        input.setParentId(categoryId.toString());

        assertThrows(ValidationException.class, () -> categoryService.update(categoryId, input));
    }

    @Test
    @DisplayName("delete: category with children cannot be deleted (CATEGORY_HAS_CHILDREN)")
    void delete_withChildren_refused() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentId(categoryId.toString())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.delete(categoryId));
        assertEquals("CATEGORY_HAS_CHILDREN", ex.getErrorCode());
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete: category with linked materials cannot be deleted (CATEGORY_HAS_MATERIALS)")
    void delete_withMaterials_refused() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentId(categoryId.toString())).thenReturn(false);
        when(materialRepository.existsByCategoryId(categoryId.toString())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.delete(categoryId));
        assertEquals("CATEGORY_HAS_MATERIALS", ex.getErrorCode());
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete: category without children or materials is deleted")
    void delete_empty_succeeds() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentId(categoryId.toString())).thenReturn(false);
        when(materialRepository.existsByCategoryId(categoryId.toString())).thenReturn(false);

        categoryService.delete(categoryId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    @DisplayName("lookup: getById returns category or throws CategoryNotFoundException")
    void getById_successAndNotFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        assertNotNull(categoryService.getById(categoryId));

        UUID unknown = UUID.randomUUID();
        when(categoryRepository.findById(unknown)).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> categoryService.getById(unknown));
    }

    @Test
    @DisplayName("hierarchy queries: getRootCategories returns roots only")
    void getRootCategories_returnsRoots() {
        when(categoryRepository.findRootCategories()).thenReturn(List.of(category));
        List<CategoryOutput> roots = categoryService.getRootCategories();
        assertEquals(1, roots.size());
    }
}
