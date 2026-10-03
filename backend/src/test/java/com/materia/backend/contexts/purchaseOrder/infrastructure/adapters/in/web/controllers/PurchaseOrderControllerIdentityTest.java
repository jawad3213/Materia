package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.mappers.PurchaseOrderWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * [T042] A user id in the request body never stands in for the signed-in user (FR-012, US4-6).
 * Each request forges an identity; the use case must receive the principal instead.
 */
@WebMvcTest(PurchaseOrderController.class)
@Import(PurchaseOrderWebMapper.class)
@WithMockUser(username = "real-user", authorities = {
        "order:read", "order:write", "order:validate", "order:cancel", "receipt:write", "ROLE_ADMIN"})
class PurchaseOrderControllerIdentityTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/purchase-orders";
    private static final String PRINCIPAL = "real-user";
    private static final String FORGED = "forged-user";

    @MockBean(answer = Answers.RETURNS_MOCKS)
    private PurchaseOrderUseCase useCase;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID id = UUID.randomUUID();

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    @DisplayName("identity: create ignores a forged createdBy and records the signed-in user")
    void create_usesPrincipal() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("supplierId", UUID.randomUUID());
        body.put("supplierName", "Acme");
        body.put("currencyCode", "MAD");
        body.put("orderedBy", "buyer-1");
        body.put("createdBy", FORGED);
        body.put("lines", List.of(Map.of("materialCode", "MAT-1", "quantity", 1, "unitPrice", 1)));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(toJson(body)));

        ArgumentCaptor<CreatePurchaseOrderInput> input = ArgumentCaptor.forClass(CreatePurchaseOrderInput.class);
        verify(useCase).create(input.capture());
        assertEquals(PRINCIPAL, input.getValue().getUserId());
    }

    @Test
    @DisplayName("identity: update ignores a forged updatedBy")
    void update_usesPrincipal() throws Exception {
        mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("notes", "x", "updatedBy", FORGED))));

        ArgumentCaptor<UpdatePurchaseOrderInput> input = ArgumentCaptor.forClass(UpdatePurchaseOrderInput.class);
        verify(useCase).update(eq(id), input.capture());
        assertEquals(PRINCIPAL, input.getValue().getUserId());
    }

    @Test
    @DisplayName("identity: submit, confirm, cancel and complete ignore a forged userId")
    void transitions_usePrincipal() throws Exception {
        String forged = toJson(Map.of("userId", FORGED, "reason", "x"));

        mockMvc.perform(patch(BASE + "/{id}/submit", id).contentType(MediaType.APPLICATION_JSON).content(forged));
        mockMvc.perform(patch(BASE + "/{id}/confirm", id).contentType(MediaType.APPLICATION_JSON).content(forged));
        mockMvc.perform(patch(BASE + "/{id}/cancel", id).contentType(MediaType.APPLICATION_JSON).content(forged));
        mockMvc.perform(patch(BASE + "/{id}/complete", id).contentType(MediaType.APPLICATION_JSON).content(forged));
        mockMvc.perform(patch(BASE + "/{id}/reject", id).contentType(MediaType.APPLICATION_JSON).content(forged));

        verify(useCase).submit(id, PRINCIPAL);
        verify(useCase).confirm(id, PRINCIPAL);
        verify(useCase).cancel(eq(id), eq(PRINCIPAL), anyString());
        verify(useCase).complete(id, PRINCIPAL);
        verify(useCase).reject(eq(id), eq(PRINCIPAL), anyString());
    }

    @Test
    @DisplayName("identity: delivery tracking ignores a forged userId")
    void deliveryStatus_usesPrincipal() throws Exception {
        mockMvc.perform(patch(BASE + "/{id}/delivery-status", id).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("deliveryStatus", "SHIPPED", "userId", FORGED))));

        verify(useCase).updateDeliveryStatus(id, "SHIPPED", PRINCIPAL);
    }

    @Test
    @DisplayName("identity: assignment records the signed-in user as the assigner, whatever the body says (US4-8)")
    void assign_usesPrincipalAsAssigner() throws Exception {
        mockMvc.perform(patch(BASE + "/{id}/assign-receiver", id).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("userId", FORGED, "assignedUserId", "receiver-1", "assignedUserName", "Forged Name"))));

        verify(useCase).assignReceiver(eq(id), eq(PRINCIPAL), anyString(), eq("receiver-1"), any());
    }

    @Test
    @DisplayName("identity: confirming receipt treats the signed-in user as the receiver, ignoring a forged receiverId (US4-6)")
    void confirmReceipt_usesPrincipalAsReceiver() throws Exception {
        mockMvc.perform(patch(BASE + "/{id}/confirm-receipt", id).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("receiverId", FORGED, "receiverName", "Rita"))));

        verify(useCase).confirmReceipt(eq(id), eq(PRINCIPAL), anyString());
    }

    @Test
    @DisplayName("identity: delete records the signed-in user")
    void delete_usesPrincipal() throws Exception {
        mockMvc.perform(delete(BASE + "/{id}", id));

        verify(useCase).delete(id, PRINCIPAL);
    }
}
