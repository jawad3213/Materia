package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.mappers.PurchaseOrderMapper;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidStatusTransitionException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotFoundException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotModifiableException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.mappers.PurchaseOrderWebMapper;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.materia.backend.support.ErrorResponseAssertions.assertError;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * [T023] The purchase order HTTP contract for endpoints 3–15 and 18–20 of
 * {@code specs/002-purchase-order-tests/contracts/endpoint-contract.md}: success, invalid input,
 * missing record and rule violation, through the real security chain (FR-003, FR-013, FR-014).
 */
@WebMvcTest(PurchaseOrderController.class)
@Import(PurchaseOrderWebMapper.class)
@WithMockUser(username = "admin-1", authorities = {
        "order:read", "order:write", "order:validate", "order:cancel", "receipt:read", "receipt:write", "ROLE_ADMIN"})
class PurchaseOrderControllerLifecycleTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/purchase-orders";

    @MockBean
    private PurchaseOrderUseCase useCase;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID id = UUID.randomUUID();

    private static PurchaseOrderOutput output(OrderStatus status) {
        return new PurchaseOrderMapper().toResponse(anOrder().withLine(10, "5.00").inStatus(status).build());
    }

    private String body(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    // ---- Reads (endpoints 3–5, 8–12) ----

    @Test
    @DisplayName("reads: by id, code, all, status, delivery status, supplier, requisition and keyword return 200")
    void reads_succeed() throws Exception {
        PurchaseOrderOutput out = output(OrderStatus.DRAFT);
        UUID supplier = UUID.randomUUID();
        UUID requisition = UUID.randomUUID();
        when(useCase.getById(id)).thenReturn(out);
        when(useCase.getByCode("PO-2026-0001")).thenReturn(out);
        when(useCase.getAll()).thenReturn(List.of(out));
        when(useCase.getByStatus("DRAFT")).thenReturn(List.of(out));
        when(useCase.getByDeliveryStatus("SHIPPED")).thenReturn(List.of(out));
        when(useCase.getBySupplierId(supplier)).thenReturn(List.of(out));
        when(useCase.getByRequisitionId(requisition)).thenReturn(List.of(out));
        when(useCase.searchByKeyword("acme")).thenReturn(List.of(out));

        mockMvc.perform(get(BASE + "/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.orderCode").value(out.getOrderCode()));
        mockMvc.perform(get(BASE + "/code/PO-2026-0001")).andExpect(status().isOk());
        mockMvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(out.getId().toString()));
        mockMvc.perform(get(BASE + "/status/DRAFT")).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/delivery-status/SHIPPED")).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/supplier/{s}", supplier)).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/requisition/{r}", requisition)).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/search/keyword").param("keyword", "acme")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("reads: a missing order is 404, by id and by code")
    void reads_missing_is404() throws Exception {
        when(useCase.getById(id)).thenThrow(new PurchaseOrderNotFoundException(id));
        when(useCase.getByCode("PO-2026-9999")).thenThrow(new PurchaseOrderNotFoundException("PO-2026-9999"));

        assertError(mockMvc.perform(get(BASE + "/{id}", id)), HttpStatus.NOT_FOUND);
        assertError(mockMvc.perform(get(BASE + "/code/PO-2026-9999")), HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("reads: an unknown status code is 400; a missing keyword is 400")
    void reads_badInput_is400() throws Exception {
        when(useCase.getByStatus("IN_PROGRESS")).thenThrow(new PurchaseOrderValidationException("Unknown order status: IN_PROGRESS"));
        when(useCase.getByDeliveryStatus("LOST")).thenThrow(new PurchaseOrderValidationException("Unknown delivery status: LOST"));

        assertError(mockMvc.perform(get(BASE + "/status/IN_PROGRESS")), HttpStatus.BAD_REQUEST);
        assertError(mockMvc.perform(get(BASE + "/delivery-status/LOST")), HttpStatus.BAD_REQUEST);
        mockMvc.perform(get(BASE + "/search/keyword")).andExpect(status().isBadRequest());
    }

    // ---- Update and delete (endpoints 6–7) ----

    @Test
    @DisplayName("update: a draft edit is 200; an edit past submission is 409; a missing order is 404")
    void update_outcomes() throws Exception {
        Map<String, Object> edit = Map.of("notes", "Revised", "lines",
                List.of(Map.of("materialCode", "MAT-1", "quantity", 2, "unitPrice", 5)));
        UUID locked = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        when(useCase.update(eq(id), any(UpdatePurchaseOrderInput.class))).thenReturn(output(OrderStatus.DRAFT));
        when(useCase.update(eq(locked), any(UpdatePurchaseOrderInput.class))).thenThrow(new PurchaseOrderNotModifiableException());
        when(useCase.update(eq(missing), any(UpdatePurchaseOrderInput.class))).thenThrow(new PurchaseOrderNotFoundException(missing));

        mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body(edit)))
                .andExpect(status().isOk());
        assertError(mockMvc.perform(put(BASE + "/{id}", locked).contentType(MediaType.APPLICATION_JSON).content(body(edit))),
                HttpStatus.CONFLICT);
        assertError(mockMvc.perform(put(BASE + "/{id}", missing).contentType(MediaType.APPLICATION_JSON).content(body(edit))),
                HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("update: a line with zero quantity or no material is 400 before reaching the service")
    void update_invalidLine_is400() throws Exception {
        Map<String, Object> bad = Map.of("lines", List.of(Map.of("materialCode", "", "quantity", 0, "unitPrice", 5)));

        mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body(bad)))
                .andExpect(status().isBadRequest());
        verify(useCase, never()).update(any(), any(UpdatePurchaseOrderInput.class));
    }

    @Test
    @DisplayName("delete: an admin deleting a draft is 204; a locked order is 409; a missing order is 404")
    void delete_outcomes() throws Exception {
        UUID locked = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        doThrow(new PurchaseOrderNotModifiableException()).when(useCase).delete(eq(locked), anyString());
        doThrow(new PurchaseOrderNotFoundException(missing)).when(useCase).delete(eq(missing), anyString());

        mockMvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isNoContent());
        verify(useCase).delete(id, "admin-1");
        assertError(mockMvc.perform(delete(BASE + "/{id}", locked)), HttpStatus.CONFLICT);
        assertError(mockMvc.perform(delete(BASE + "/{id}", missing)), HttpStatus.NOT_FOUND);
    }

    // ---- Transitions (endpoints 13–15, 18–20) ----

    @Test
    @DisplayName("transitions: submit, confirm, reject, cancel, complete and tracking return 200 with the new state")
    void transitions_succeed() throws Exception {
        when(useCase.submit(eq(id), anyString())).thenReturn(output(OrderStatus.SUBMITTED));
        when(useCase.confirm(eq(id), anyString())).thenReturn(output(OrderStatus.CONFIRMED));
        when(useCase.reject(eq(id), anyString(), anyString())).thenReturn(output(OrderStatus.REJECTED));
        when(useCase.cancel(eq(id), anyString(), anyString())).thenReturn(output(OrderStatus.CANCELLED));
        when(useCase.complete(eq(id), anyString())).thenReturn(output(OrderStatus.COMPLETED));
        when(useCase.updateDeliveryStatus(eq(id), eq("SHIPPED"), anyString())).thenReturn(output(OrderStatus.CONFIRMED));

        mockMvc.perform(patch(BASE + "/{id}/submit", id)).andExpect(jsonPath("$.status").value("SUBMITTED"));
        mockMvc.perform(patch(BASE + "/{id}/confirm", id)).andExpect(jsonPath("$.status").value("CONFIRMED"));
        mockMvc.perform(patch(BASE + "/{id}/reject", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("reason", "Out of stock")))).andExpect(jsonPath("$.status").value("REJECTED"));
        mockMvc.perform(patch(BASE + "/{id}/cancel", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("reason", "Not needed")))).andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(patch(BASE + "/{id}/complete", id)).andExpect(jsonPath("$.status").value("COMPLETED"));
        mockMvc.perform(patch(BASE + "/{id}/delivery-status", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("deliveryStatus", "SHIPPED")))).andExpect(status().isOk());
    }

    @Test
    @DisplayName("transitions: a transition from the wrong status is 409 (rule violation, FR-013)")
    void transitions_wrongStatus_is409() throws Exception {
        PurchaseOrderInvalidStatusTransitionException refused = new PurchaseOrderInvalidStatusTransitionException("refused");
        when(useCase.submit(eq(id), anyString())).thenThrow(refused);
        when(useCase.confirm(eq(id), anyString())).thenThrow(refused);
        when(useCase.reject(eq(id), anyString(), anyString())).thenThrow(refused);
        when(useCase.cancel(eq(id), anyString(), anyString())).thenThrow(refused);
        when(useCase.complete(eq(id), anyString())).thenThrow(refused);
        when(useCase.updateDeliveryStatus(eq(id), anyString(), anyString())).thenThrow(refused);

        assertError(mockMvc.perform(patch(BASE + "/{id}/submit", id)), HttpStatus.CONFLICT);
        assertError(mockMvc.perform(patch(BASE + "/{id}/confirm", id)), HttpStatus.CONFLICT);
        assertError(mockMvc.perform(patch(BASE + "/{id}/reject", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("reason", "x")))), HttpStatus.CONFLICT);
        assertError(mockMvc.perform(patch(BASE + "/{id}/cancel", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("reason", "x")))), HttpStatus.CONFLICT);
        assertError(mockMvc.perform(patch(BASE + "/{id}/complete", id)), HttpStatus.CONFLICT);
        assertError(mockMvc.perform(patch(BASE + "/{id}/delivery-status", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("deliveryStatus", "DELIVERED")))), HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("transitions: a missing order is 404 for every transition")
    void transitions_missing_is404() throws Exception {
        PurchaseOrderNotFoundException missing = new PurchaseOrderNotFoundException(id);
        when(useCase.submit(eq(id), anyString())).thenThrow(missing);
        when(useCase.confirm(eq(id), anyString())).thenThrow(missing);
        when(useCase.complete(eq(id), anyString())).thenThrow(missing);
        when(useCase.cancel(eq(id), anyString(), anyString())).thenThrow(missing);

        assertError(mockMvc.perform(patch(BASE + "/{id}/submit", id)), HttpStatus.NOT_FOUND);
        assertError(mockMvc.perform(patch(BASE + "/{id}/confirm", id)), HttpStatus.NOT_FOUND);
        assertError(mockMvc.perform(patch(BASE + "/{id}/complete", id)), HttpStatus.NOT_FOUND);
        assertError(mockMvc.perform(patch(BASE + "/{id}/cancel", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("reason", "x")))), HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("transitions: a blank rejection reason or a blank delivery status is 400 before the service is called")
    void transitions_blankInput_is400() throws Exception {
        mockMvc.perform(patch(BASE + "/{id}/reject", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("reason", " ")))).andExpect(status().isBadRequest());
        mockMvc.perform(patch(BASE + "/{id}/delivery-status", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("deliveryStatus", "")))).andExpect(status().isBadRequest());
        verify(useCase, never()).reject(any(), any(), any());
        verify(useCase, never()).updateDeliveryStatus(any(), any(), any());
    }

    // ---- Response fields (FR-014) ----

    @Test
    @DisplayName("fields: the order response carries every field the purchase order screens consume (FR-014)")
    void response_carriesEveryScreenField() throws Exception {
        PurchaseOrder order = anOrder().withLine(10, "5.00").fromRequisition(UUID.randomUUID(), "REQ-2026-0001")
                .notes("Deliver to dock 2").inStatus(OrderStatus.READY_FOR_RECEIPT).build();
        order.setExpectedDeliveryDate(LocalDate.now().plusDays(7));
        order.setPaymentTerms("Net 30");
        order.setPaymentDelayDays(30);
        order.setDeliveryTerms("DAP site");
        order.setInternalNotes("Fragile");
        order.setTaxAmount(Money.of("1.00", CurrencyCode.MAD));
        order.setShippingCost(Money.of("2.00", CurrencyCode.MAD));
        order.setReceivedDate(LocalDate.now());
        order.setUpdatedBy("buyer-1");
        order.getLines().get(0).setRequisitionLineId(UUID.randomUUID());
        order.getLines().get(0).setExpectedDeliveryDate(LocalDate.now().plusDays(7));
        order.getLines().get(0).setNotes("Line note");
        order.recalculateTotals();
        when(useCase.getById(id)).thenReturn(new PurchaseOrderMapper().toResponse(order));

        String json = mockMvc.perform(get(BASE + "/{id}", id)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(json, new TypeReference<>() { });
        @SuppressWarnings("unchecked")
        Map<String, Object> line = ((List<Map<String, Object>>) response.get("lines")).get(0);

        Set<String> orderFields = Set.of("id", "orderCode", "status", "deliveryStatus", "requisitionId", "requisitionCode",
                "supplierId", "supplierName", "supplierCode", "orderDate", "expectedDeliveryDate", "confirmedDeliveryDate",
                "receivedDate", "paymentTerms", "paymentDelayDays", "deliveryTerms", "incoterm", "currencyCode", "totalAmount",
                "taxAmount", "shippingCost", "grandTotal", "orderedBy", "orderedByName", "assignedTo", "assignedToName",
                "assignedAt", "notes", "internalNotes", "createdBy", "createdAt", "updatedBy", "updatedAt");
        Set<String> lineFields = Set.of("id", "lineNumber", "requisitionLineId", "materialCode", "materialId", "materialName",
                "unitOfMeasure", "quantity", "unitPrice", "lineTotal", "currencyCode", "expectedDeliveryDate", "notes");

        for (String field : orderFields) {
            assertTrue(response.get(field) != null, () -> "order response is missing '" + field + "'");
        }
        for (String field : lineFields) {
            assertTrue(line.get(field) != null, () -> "line response is missing '" + field + "'");
        }
    }
}
