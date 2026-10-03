package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.controllers;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.contexts.masterData.application.dtos.material.*;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.enums.UnitOfMeasure;
import com.materia.backend.contexts.masterData.domain.exceptions.DuplicateMaterialCodeException;
import com.materia.backend.contexts.masterData.domain.exceptions.MaterialNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialUseCase;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.mappers.MaterialWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web MVC slice tests for MaterialController catalogue operations (T098-T101, US5).
 */
@WebMvcTest(MaterialController.class)
@Import(MaterialWebMapper.class)
@WithMockUser(authorities = {"material:read", "material:write", "material:delete"})
class MaterialCatalogueControllerTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/masterdata/materials";

    @MockBean private MaterialUseCase materialUseCase;

    private final UUID id = UUID.randomUUID();

    private MaterialOutput sampleOutput() {
        MaterialOutput out = new MaterialOutput();
        out.setId(id);
        out.setCode("MAT-2026-0001");
        out.setName("Steel Bolt");
        out.setDescription("High grade steel");
        out.setShortDescription("Bolt");
        out.setMaterialType("RAW_MATERIAL");
        out.setUnitOfMeasure("PCE");
        out.setCategoryId(UUID.randomUUID().toString());
        out.setCategoryName("Fasteners");
        out.setSupplierId(UUID.randomUUID().toString());
        out.setSupplierName("Acme Fasteners");
        out.setStatus("ACTIVE");
        out.setCurrentStock(100);
        out.setAvailableStock(100);
        out.setMinimumStock(10);
        out.setMaximumStock(1000);
        out.setReorderPoint(20);
        out.setSafetyStock(5);
        out.setStandardPrice("10.00");
        out.setCurrencyCode("MAD");
        return out;
    }

    private MaterialListOutput sampleListOutput() {
        MaterialListOutput out = new MaterialListOutput();
        out.setId(id);
        out.setCode("MAT-2026-0001");
        out.setName("Steel Bolt");
        out.setCategoryName("Fasteners");
        out.setStatus("ACTIVE");
        out.setCurrentStock(100);
        out.setUnitOfMeasure("PCE");
        return out;
    }

    private Map<String, Object> validCreateRequest() {
        Map<String, Object> req = new java.util.LinkedHashMap<>();
        req.put("name", "Steel Bolt");
        req.put("materialType", "RAW_MATERIAL");
        req.put("unitOfMeasure", "PCE");
        req.put("categoryId", UUID.randomUUID().toString());
        req.put("supplierId", UUID.randomUUID().toString());
        req.put("currentStock", 100);
        req.put("minimumStock", 10);
        req.put("maximumStock", 1000);
        req.put("safetyStock", 20);
        req.put("standardPrice", "15.50");
        req.put("standardPriceCurrency", "MAD");
        req.put("createdBy", "admin");
        return req;
    }

    @Test
    @DisplayName("create: returns 201 Created with material representation")
    void create_success() throws Exception {
        when(materialUseCase.create(any(CreateMaterialInput.class))).thenReturn(sampleOutput());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("MAT-2026-0001"))
                .andExpect(jsonPath("$.name").value("Steel Bolt"));
    }

    @Test
    @DisplayName("create: duplicate code throws DuplicateMaterialCodeException and returns 409 Conflict (FINDING-005, T099)")
    void create_duplicateCode_returns409() throws Exception {
        when(materialUseCase.create(any(CreateMaterialInput.class)))
                .thenThrow(new DuplicateMaterialCodeException("MAT-2026-0001"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCreateRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("getById: returns 200 OK or 404 Not Found")
    void getById_statusCodes() throws Exception {
        when(materialUseCase.getById(id)).thenReturn(sampleOutput());
        mockMvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        UUID unknown = UUID.randomUUID();
        when(materialUseCase.getById(unknown)).thenThrow(new MaterialNotFoundException(unknown.toString()));
        mockMvc.perform(get(BASE + "/{id}", unknown))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getByCode: returns 200 OK or 404 Not Found")
    void getByCode_statusCodes() throws Exception {
        when(materialUseCase.getByCode("MAT-2026-0001")).thenReturn(sampleOutput());
        mockMvc.perform(get(BASE + "/code/MAT-2026-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MAT-2026-0001"));

        when(materialUseCase.getByCode("UNKNOWN")).thenThrow(new MaterialNotFoundException("UNKNOWN"));
        mockMvc.perform(get(BASE + "/code/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getAll: returns 200 OK with list")
    void getAll_returnsList() throws Exception {
        when(materialUseCase.getAll()).thenReturn(List.of(sampleOutput()));
        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("MAT-2026-0001"));
    }

    @Test
    @DisplayName("update: returns 200 OK with updated representation")
    void update_success() throws Exception {
        when(materialUseCase.update(eq(id), any(UpdateMaterialInput.class))).thenReturn(sampleOutput());

        mockMvc.perform(put(BASE + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("delete: returns 204 No Content")
    void delete_success() throws Exception {
        doNothing().when(materialUseCase).delete(id);

        mockMvc.perform(delete(BASE + "/{id}", id))
                .andExpect(status().isNoContent());
        verify(materialUseCase).delete(id);
    }

    @Test
    @DisplayName("paged endpoints: list, search and filter return PageResponse (T100)")
    void pagedEndpoints_returnPageResponse() throws Exception {
        PageResponse<MaterialListOutput> page = new PageResponse<>(List.of(sampleListOutput()), 0, 10, 1L, 1, true);
        when(materialUseCase.getAllList(0, 10)).thenReturn(page);
        when(materialUseCase.searchAdvancedList(any(), eq(0), eq(10))).thenReturn(page);
        when(materialUseCase.filterList(any(), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get(BASE + "/list").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));

        mockMvc.perform(post(BASE + "/search/list").param("page", "0").param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));

        mockMvc.perform(post(BASE + "/filter/list").param("page", "0").param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    @DisplayName("contract: material response carries every field frontend materials module consumes (FR-008, T101)")
    void response_carriesFrontendFields() throws Exception {
        when(materialUseCase.getById(id)).thenReturn(sampleOutput());

        var result = mockMvc.perform(get(BASE + "/{id}", id)).andExpect(status().isOk());
        for (String field : List.of("id", "code", "name", "description", "materialType",
                "unitOfMeasure", "status", "currentStock", "availableStock",
                "minimumStock", "maximumStock", "reorderPoint", "safetyStock", "standardPrice")) {
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
    @WithMockUser(authorities = {"material:read", "material:write"})
    @DisplayName("security: user lacking material:delete receives 403 Forbidden")
    void delete_withoutDeleteAuthority_is403() throws Exception {
        mockMvc.perform(delete(BASE + "/{id}", id))
                .andExpect(status().isForbidden());
    }
}
