package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.masterData.application.dtos.category.CategoryOutput;
import com.materia.backend.contexts.masterData.application.dtos.category.CreateCategoryInput;
import com.materia.backend.contexts.masterData.application.dtos.category.UpdateCategoryInput;
import com.materia.backend.contexts.masterData.domain.enums.MaterialCategoryType;
import com.materia.backend.contexts.masterData.domain.exceptions.CategoryNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.in.CategoryUseCase;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.mappers.CategoryWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web MVC slice tests for CategoryController (T096, T101, US5).
 */
@WebMvcTest(CategoryController.class)
@Import(CategoryWebMapper.class)
@WithMockUser(authorities = {"category:read", "category:write", "category:delete"})
class CategoryControllerTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/masterdata/categories";

    @MockBean private CategoryUseCase categoryUseCase;

    private final UUID id = UUID.randomUUID();

    private CategoryOutput sampleOutput() {
        CategoryOutput out = new CategoryOutput();
        out.setId(id);
        out.setCode("CAT-001");
        out.setName("Electronics");
        out.setDescription("Components");
        out.setCategoryType("ELECTRONIC_CAT");
        out.setStatus("ACTIVE");
        out.setLevel(0);
        out.setPath("/CAT-001");
        out.setMaterialCount(5);
        out.setSubCategoryCount(1);
        out.setTotalItems(6);
        out.setCreatedAt(LocalDateTime.now());
        return out;
    }

    private Map<String, Object> validCreateRequest() {
        return Map.of(
                "name", "Electronics",
                "description", "Components",
                "categoryType", "ELECTRONIC_CAT",
                "status", "ACTIVE",
                "createdBy", "admin"
        );
    }

    @Test
    @DisplayName("create: valid category returns 201 Created with representation")
    void create_success() throws Exception {
        when(categoryUseCase.create(any(CreateCategoryInput.class))).thenReturn(sampleOutput());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("CAT-001"))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @DisplayName("getById: returns 200 OK or 404 when not found")
    void getById_statusCodes() throws Exception {
        when(categoryUseCase.getById(id)).thenReturn(sampleOutput());
        mockMvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        UUID unknown = UUID.randomUUID();
        when(categoryUseCase.getById(unknown)).thenThrow(new CategoryNotFoundException(unknown.toString()));
        mockMvc.perform(get(BASE + "/{id}", unknown))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getByCode: returns 200 OK or 404 when not found")
    void getByCode_statusCodes() throws Exception {
        when(categoryUseCase.getByCode("CAT-001")).thenReturn(sampleOutput());
        mockMvc.perform(get(BASE + "/code/CAT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CAT-001"));

        when(categoryUseCase.getByCode("UNKNOWN")).thenThrow(new CategoryNotFoundException("UNKNOWN"));
        mockMvc.perform(get(BASE + "/code/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getAll: returns 200 OK with list of categories")
    void getAll_returnsList() throws Exception {
        when(categoryUseCase.getAll()).thenReturn(List.of(sampleOutput()));
        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CAT-001"));
    }

    @Test
    @DisplayName("update: returns 200 OK with updated representation")
    void update_success() throws Exception {
        when(categoryUseCase.update(eq(id), any(UpdateCategoryInput.class))).thenReturn(sampleOutput());

        mockMvc.perform(put(BASE + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("delete: returns 204 No Content")
    void delete_success() throws Exception {
        doNothing().when(categoryUseCase).delete(id);

        mockMvc.perform(delete(BASE + "/{id}", id))
                .andExpect(status().isNoContent());
        verify(categoryUseCase).delete(id);
    }

    @Test
    @DisplayName("roots and subcategories: return 200 OK with lists")
    void hierarchyEndpoints_returnList() throws Exception {
        when(categoryUseCase.getRootCategories()).thenReturn(List.of(sampleOutput()));
        when(categoryUseCase.getSubCategories(id)).thenReturn(List.of(sampleOutput()));

        mockMvc.perform(get(BASE + "/roots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()));

        mockMvc.perform(get(BASE + "/{parentId}/subcategories", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()));
    }

    @Test
    @DisplayName("contract: category response carries every field frontend categories module consumes (FR-008)")
    void response_carriesFrontendFields() throws Exception {
        when(categoryUseCase.getById(id)).thenReturn(sampleOutput());

        var result = mockMvc.perform(get(BASE + "/{id}", id)).andExpect(status().isOk());
        for (String field : List.of("id", "code", "name", "description", "status",
                "level", "path", "materialCount", "subCategoryCount", "totalItems")) {
            result.andExpect(jsonPath("$." + field).hasJsonPath());
        }
    }

    @Test
    @WithAnonymousUser
    @DisplayName("security: anonymous request returns 401 Unauthorized")
    void anonymous_is401() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"category:read", "category:write"})
    @DisplayName("security: user lacking category:delete receives 403 Forbidden")
    void delete_withoutDeleteAuthority_is403() throws Exception {
        mockMvc.perform(delete(BASE + "/{id}", id))
                .andExpect(status().isForbidden());
    }
}
