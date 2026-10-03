package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.controllers;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.application.dtos.supplier.*;
import com.materia.backend.contexts.masterData.domain.exceptions.SupplierNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.in.SupplierUseCase;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.mappers.SupplierWebMapper;
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
 * Web MVC slice tests for SupplierController (T097, T100, T101, US5).
 */
@WebMvcTest(SupplierController.class)
@Import(SupplierWebMapper.class)
@WithMockUser(authorities = {"supplier:read", "supplier:write", "supplier:delete"})
class SupplierControllerTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/masterdata/suppliers";

    @MockBean private SupplierUseCase supplierUseCase;

    private final UUID id = UUID.randomUUID();

    private SupplierOutput sampleOutput() {
        SupplierOutput out = new SupplierOutput();
        out.setId(id);
        out.setCode("SUP-001");
        out.setName("Acme Supplies");
        out.setDescription("Reliable vendor");
        out.setContactPerson("John Supplier");
        out.setContactEmail("contact@acme.test");
        out.setContactPhone("+212611223344");
        out.setAddress("Industrial Park");
        out.setCity("Casablanca");
        out.setCountry("Morocco");
        out.setPostalCode("20000");
        out.setPaymentTerms(List.of("NET30"));
        out.setPaymentDelay(30);
        out.setCurrencyCode("MAD");
        out.setStatus("ACTIVE");
        return out;
    }

    private SupplierListOutput sampleListOutput() {
        SupplierListOutput out = new SupplierListOutput();
        out.setId(id);
        out.setCode("SUP-001");
        out.setName("Acme Supplies");
        out.setCity("Casablanca");
        out.setCountry("Morocco");
        out.setStatus("ACTIVE");
        out.setCurrencyCode("MAD");
        return out;
    }

    private Map<String, Object> validCreateRequest() {
        Map<String, Object> req = new java.util.LinkedHashMap<>();
        req.put("name", "Acme Supplies");
        req.put("country", "Morocco");
        req.put("city", "Casablanca");
        req.put("address", "123 Business St");
        req.put("contactPerson", "John Doe");
        req.put("contactPhone", "+212600000000");
        req.put("contactEmail", "contact@acme.test");
        req.put("paymentDelay", 30);
        req.put("paymentTerms", List.of("NET30"));
        req.put("currencyCode", "MAD");
        req.put("createdBy", "admin");
        return req;
    }

    @Test
    @DisplayName("create: returns 201 Created with supplier representation")
    void create_success() throws Exception {
        when(supplierUseCase.create(any(CreateSupplierInput.class))).thenReturn(sampleOutput());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("SUP-001"))
                .andExpect(jsonPath("$.name").value("Acme Supplies"));
    }

    @Test
    @DisplayName("getById: returns 200 OK or 404 Not Found")
    void getById_statusCodes() throws Exception {
        when(supplierUseCase.getById(id)).thenReturn(sampleOutput());
        mockMvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        UUID unknown = UUID.randomUUID();
        when(supplierUseCase.getById(unknown)).thenThrow(new SupplierNotFoundException(unknown.toString()));
        mockMvc.perform(get(BASE + "/{id}", unknown))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getByCode: returns 200 OK or 404 Not Found")
    void getByCode_statusCodes() throws Exception {
        when(supplierUseCase.getByCode("SUP-001")).thenReturn(sampleOutput());
        mockMvc.perform(get(BASE + "/code/SUP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUP-001"));

        when(supplierUseCase.getByCode("UNKNOWN")).thenThrow(new SupplierNotFoundException("UNKNOWN"));
        mockMvc.perform(get(BASE + "/code/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getAll: returns 200 OK with list")
    void getAll_returnsList() throws Exception {
        when(supplierUseCase.getAll()).thenReturn(List.of(sampleOutput()));
        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SUP-001"));
    }

    @Test
    @DisplayName("search: returns 200 OK with matching suppliers")
    void search_returnsList() throws Exception {
        when(supplierUseCase.searchSuppliers("Acme")).thenReturn(List.of(sampleOutput()));
        mockMvc.perform(get(BASE + "/search").param("keyword", "Acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Acme Supplies"));
    }

    @Test
    @DisplayName("update: returns 200 OK with updated representation")
    void update_success() throws Exception {
        when(supplierUseCase.update(eq(id), any(UpdateSupplierInput.class))).thenReturn(sampleOutput());

        mockMvc.perform(put(BASE + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("delete: returns 204 No Content")
    void delete_success() throws Exception {
        doNothing().when(supplierUseCase).delete(id);

        mockMvc.perform(delete(BASE + "/{id}", id))
                .andExpect(status().isNoContent());
        verify(supplierUseCase).delete(id);
    }

    @Test
    @DisplayName("paged endpoints: list and filter return PageResponse with items")
    void pagedEndpoints_returnPageResponse() throws Exception {
        PageResponse<SupplierListOutput> page = new PageResponse<>(List.of(sampleListOutput()), 0, 10, 1L, 1, true);
        when(supplierUseCase.getAllList(0, 10)).thenReturn(page);
        when(supplierUseCase.filterList(any(), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get(BASE + "/list").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(post(BASE + "/filter/list").param("page", "0").param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    @DisplayName("contract: supplier response carries every field frontend suppliers module consumes (FR-008)")
    void response_carriesFrontendFields() throws Exception {
        when(supplierUseCase.getById(id)).thenReturn(sampleOutput());

        var result = mockMvc.perform(get(BASE + "/{id}", id)).andExpect(status().isOk());
        for (String field : List.of("id", "code", "name", "description", "contactPerson",
                "contactEmail", "contactPhone", "address", "city", "country", "postalCode",
                "paymentDelay", "currencyCode", "status")) {
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
    @WithMockUser(authorities = {"supplier:read", "supplier:write"})
    @DisplayName("security: user lacking supplier:delete receives 403 Forbidden")
    void delete_withoutDeleteAuthority_is403() throws Exception {
        mockMvc.perform(delete(BASE + "/{id}", id))
                .andExpect(status().isForbidden());
    }
}
