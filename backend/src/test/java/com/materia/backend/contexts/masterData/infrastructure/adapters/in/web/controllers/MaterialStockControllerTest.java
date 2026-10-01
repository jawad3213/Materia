package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.controllers;

import com.materia.backend.common.application.exceptions.BusinessException;
import com.materia.backend.contexts.masterData.application.dtos.material.ManualReorderOutput;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterData.application.dtos.material.ReorderRecommendationOutput;
import com.materia.backend.contexts.masterData.domain.exceptions.InsufficientStockException;
import com.materia.backend.contexts.masterData.domain.exceptions.MaterialNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialUseCase;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.mappers.MaterialWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.ErrorResponseAssertions.assertError;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The 7 stock and reorder endpoints (T073-T075, US3). The 9 catalogue endpoints on the same
 * controller are covered under US5 in MaterialCatalogueControllerTest.
 */
@WebMvcTest(MaterialController.class)
@Import(MaterialWebMapper.class)
@WithMockUser
class MaterialStockControllerTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/masterdata/materials";
    private static final String FINDING_025 =
            "FINDING-025: the reorder recommendation sends currencyCode, but the frontend renders currency";

    @MockBean
    private MaterialUseCase useCase;

    private final UUID id = UUID.randomUUID();

    private MaterialOutput material(int stock) {
        MaterialOutput out = new MaterialOutput();
        out.setId(id);
        out.setCode("MAT-2026-0001");
        out.setName("Printer paper");
        out.setCurrentStock(stock);
        out.setStatus("ACTIVE");
        return out;
    }

    private ReorderRecommendationOutput recommendation() {
        return new ReorderRecommendationOutput(id, "MAT-2026-0001", "Printer paper", 8, 0, 8, 20, 5, 100,
                new BigDecimal("250.00"), "MAD", "Point de réapprovisionnement atteint", false, "REORDER_NEEDED");
    }

    // ---- Stock movements ----

    @Test
    @DisplayName("increase: returns the material at its new level")
    void increase_returnsNewLevel() throws Exception {
        when(useCase.increaseStock(id, 5)).thenReturn(material(15));

        mockMvc.perform(patch(BASE + "/{id}/stock/increase", id).param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStock").value(15));
    }

    @Test
    @DisplayName("decrease: taking more than is in stock is 400 with a specific errorCode, not a 500")
    void decrease_insufficient_is400() throws Exception {
        when(useCase.decreaseStock(id, 99)).thenThrow(new InsufficientStockException("MAT-2026-0001", 99, 3));

        assertError(mockMvc.perform(patch(BASE + "/{id}/stock/decrease", id).param("quantity", "99")),
                HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("stock: an obsolete material is 400 with MATERIAL_OBSOLETE")
    void obsolete_is400WithCode() throws Exception {
        when(useCase.increaseStock(id, 1)).thenThrow(new BusinessException("Cannot increase stock", "MATERIAL_OBSOLETE"));

        assertError(mockMvc.perform(patch(BASE + "/{id}/stock/increase", id).param("quantity", "1")),
                HttpStatus.BAD_REQUEST, "MATERIAL_OBSOLETE");
    }

    @Test
    @DisplayName("stock: a missing or non-numeric quantity is 400, and never reaches the service")
    void quantity_missingOrMalformed_is400() throws Exception {
        mockMvc.perform(patch(BASE + "/{id}/stock/increase", id)).andExpect(status().isBadRequest());
        mockMvc.perform(patch(BASE + "/{id}/stock/decrease", id).param("quantity", "ten"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("stock: an unknown material is 404")
    void unknown_is404() throws Exception {
        when(useCase.decreaseStock(id, 1)).thenThrow(new MaterialNotFoundException(id.toString()));

        assertError(mockMvc.perform(patch(BASE + "/{id}/stock/decrease", id).param("quantity", "1")),
                HttpStatus.NOT_FOUND);
    }

    // ---- Shortage lists ----

    @Test
    @DisplayName("shortage lists: reorder-needed, critical and out-of-stock each return their materials")
    void shortageLists_areServed() throws Exception {
        when(useCase.getMaterialsNeedingReorder()).thenReturn(List.of(material(15)));
        when(useCase.getCriticalMaterials()).thenReturn(List.of(material(3)));
        when(useCase.getOutOfStockMaterials()).thenReturn(List.of(material(0)));

        mockMvc.perform(get(BASE + "/stock/reorder-needed")).andExpect(jsonPath("$[0].currentStock").value(15));
        mockMvc.perform(get(BASE + "/stock/critical")).andExpect(jsonPath("$[0].currentStock").value(3));
        mockMvc.perform(get(BASE + "/stock/out-of-stock")).andExpect(jsonPath("$[0].currentStock").value(0));
        mockMvc.perform(get(BASE + "/stock/out")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("shortage lists: nothing short is an empty list, not an error")
    void shortageLists_empty() throws Exception {
        when(useCase.getCriticalMaterials()).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/stock/critical")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    // ---- Reordering ----

    @Test
    @DisplayName("recommendation: carries every field the reorder dialog reads")
    void recommendation_carriesFrontendFields() throws Exception {
        when(useCase.getReorderRecommendation(id)).thenReturn(recommendation());

        var result = mockMvc.perform(get(BASE + "/{id}/reorder-recommendation", id)).andExpect(status().isOk());
        for (String field : List.of("materialId", "materialCode", "materialName", "currentStock", "stockOnOrder",
                "virtualStock", "reorderPoint", "safetyStock", "recommendedQuantity", "estimatedCost", "reason",
                "isUrgent", "stockStatus")) {
            result.andExpect(jsonPath("$." + field).exists());
        }
    }

    @Test
    @Disabled(FINDING_025)
    @DisplayName("recommendation: sends the currency under the name the dialog renders, so the cost shows its currency")
    void recommendation_sendsCurrencyUnderFrontendName() throws Exception {
        when(useCase.getReorderRecommendation(id)).thenReturn(recommendation());

        mockMvc.perform(get(BASE + "/{id}/reorder-recommendation", id))
                .andExpect(jsonPath("$.currency").value("MAD"));
    }

    @Test
    @DisplayName("manual reorder: raises a requisition, with or without a request body")
    void manualReorder_withAndWithoutBody() throws Exception {
        when(useCase.triggerReorder(eq(id), any(), any()))
                .thenReturn(new ManualReorderOutput(id, "MAT-2026-0001", "req-1", 40, "created"));

        mockMvc.perform(post(BASE + "/{id}/reorder", id).contentType(MediaType.APPLICATION_JSON)
                        .content(json(java.util.Map.of("quantity", 40, "reason", "top up"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("created"));
        mockMvc.perform(post(BASE + "/{id}/reorder", id)).andExpect(status().isOk());

        verify(useCase).triggerReorder(id, 40, "top up");
        verify(useCase).triggerReorder(id, null, null);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("security: stock endpoints refuse an anonymous caller")
    void anonymous_is401() throws Exception {
        mockMvc.perform(patch(BASE + "/{id}/stock/increase", id).param("quantity", "1"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(BASE + "/stock/critical")).andExpect(status().isUnauthorized());
        verifyNoInteractions(useCase);
    }
}
