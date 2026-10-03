package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.mappers.PurchaseOrderMapper;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.mappers.PurchaseOrderWebMapper;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionInvalidStatusTransitionException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionNotFoundException;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.materia.backend.support.ErrorResponseAssertions.assertError;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** [T030] HTTP outcomes for creating an order from a requisition (endpoint 1) and for requisition changes on edit (US2). */
@WebMvcTest(PurchaseOrderController.class)
@Import(PurchaseOrderWebMapper.class)
@WithMockUser(username = "buyer-1", authorities = {"order:read", "order:write", "order:cancel", "receipt:read"})
class PurchaseOrderControllerConversionTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/purchase-orders";

    @MockBean
    private PurchaseOrderUseCase useCase;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID requisitionId = UUID.randomUUID();

    private String createBody() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("requisitionId", requisitionId);
        body.put("supplierId", UUID.randomUUID());
        body.put("supplierName", "Acme Supplies");
        body.put("currencyCode", "MAD");
        body.put("orderedBy", "buyer-1");
        body.put("createdBy", "buyer-1");
        body.put("lines", List.of(Map.of("materialCode", "MAT-1", "quantity", 2, "unitPrice", 5)));
        return objectMapper.writeValueAsString(body);
    }

    @Test
    @DisplayName("create: an order from an approved requisition is 201 (US2-1)")
    void create_fromApprovedRequisition_is201() throws Exception {
        when(useCase.create(any(CreatePurchaseOrderInput.class)))
                .thenReturn(new PurchaseOrderMapper().toResponse(anOrder().fromRequisition(requisitionId, "REQ-2026-0001").build()));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requisitionCode").value("REQ-2026-0001"));
    }

    @Test
    @DisplayName("create: an unknown requisition is 404 (US2-3)")
    void create_unknownRequisition_is404() throws Exception {
        when(useCase.create(any(CreatePurchaseOrderInput.class))).thenThrow(new RequisitionNotFoundException(requisitionId));

        assertError(mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody())),
                HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("create: a requisition that is not approved is 409 (US2-2, FR-013)")
    void create_unapprovedRequisition_is409() throws Exception {
        when(useCase.create(any(CreatePurchaseOrderInput.class)))
                .thenThrow(new RequisitionInvalidStatusTransitionException("Only approved requisitions can be converted"));

        assertError(mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody())),
                HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("create: a request with no lines is 400 before reaching the service")
    void create_withoutLines_is400() throws Exception {
        Map<String, Object> body = objectMapper.readValue(createBody(), Map.class);
        body.put("lines", List.of());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
        verify(useCase, never()).create(any());
    }

    @Test
    @DisplayName("edit: changing the originating requisition is 400 (US2-5)")
    void update_changedRequisition_is400() throws Exception {
        UUID id = UUID.randomUUID();
        when(useCase.update(eq(id), any(UpdatePurchaseOrderInput.class)))
                .thenThrow(new PurchaseOrderValidationException("The originating requisition cannot be changed"));

        assertError(mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("requisitionId", UUID.randomUUID())))), HttpStatus.BAD_REQUEST);
    }
}
