package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.UpdateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.mappers.GoodsReceiptMapper;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptBusinessException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptLineRequiredException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptNotFoundException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptNotModifiableException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptValidationException;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.mappers.GoodsReceiptWebMapper;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.materia.backend.support.ErrorResponseAssertions.assertError;
import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * [T052] The goods receipt HTTP contract for endpoints 21–34: success, invalid input (400), missing
 * record (404), not the owner (403) and rule violation (409, F-008), plus the fields the screens use.
 */
@WebMvcTest(GoodsReceiptController.class)
@Import(GoodsReceiptWebMapper.class)
@WithMockUser(username = "receiver-1", authorities = {"receipt:read", "receipt:write", "receipt:quality"})
class GoodsReceiptControllerTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/goods-receipts";
    private static final Map<String, Object> LINE =
            Map.of("materialCode", "MAT-1", "quantityReceived", 2, "quantityRejected", 0);

    @MockBean
    private GoodsReceiptUseCase useCase;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID id = UUID.randomUUID();

    private static GoodsReceiptOutput output(ReceiptStatus status) {
        PurchaseOrder order = anOrder().withLine(10, "5.00").inStatus(OrderStatus.READY_FOR_RECEIPT).build();
        return new GoodsReceiptMapper().toResponse(aReceipt(order).inStatus(status).build());
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String createBody() throws Exception {
        return toJson(Map.of("purchaseOrderId", UUID.randomUUID().toString(), "lines", List.of(LINE)));
    }

    @Test
    @DisplayName("create: a valid receipt is 201; the order not being receivable is 409; not assigned is 403")
    void create_outcomes() throws Exception {
        when(useCase.create(any())).thenReturn(output(ReceiptStatus.DRAFT))
                .thenThrow(new GoodsReceiptBusinessException("The purchase order is not ready for receipt", "GOODS_RECEIPT_RULE_VIOLATION"))
                .thenThrow(new AccessDeniedException("You are not assigned to receive this purchase order"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("DRAFT"));
        assertError(mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody())), HttpStatus.CONFLICT);
        assertError(mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody())), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("create: no lines, a malformed code, or rejected above received is 400 before the service")
    void create_invalidInput_is400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("purchaseOrderId", "po", "lines", List.of())))).andExpect(status().isBadRequest());
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("purchaseOrderId", "po", "receiptCode", "BAD", "lines", List.of(LINE)))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(toJson(Map.of("purchaseOrderId", "po",
                        "lines", List.of(Map.of("materialCode", "M", "quantityReceived", 1, "quantityRejected", 2))))))
                .andExpect(status().isBadRequest());
        verify(useCase, never()).create(any());
    }

    @Test
    @DisplayName("reads: by id, code, all, status, order, receiver and keyword return 200; unknown id or code is 404")
    void reads_outcomes() throws Exception {
        GoodsReceiptOutput out = output(ReceiptStatus.DRAFT);
        when(useCase.getById(id)).thenReturn(out);
        when(useCase.getByCode("GR-2026-0001")).thenReturn(out);
        when(useCase.getAll()).thenReturn(List.of(out));
        when(useCase.getByStatus(ReceiptStatus.DRAFT)).thenReturn(List.of(out));
        when(useCase.getByPurchaseOrderId("po-1")).thenReturn(List.of(out));
        when(useCase.getByReceiverId("receiver-1")).thenReturn(List.of(out));
        when(useCase.search("acme")).thenReturn(List.of(out));
        UUID missing = UUID.randomUUID();
        when(useCase.getById(missing)).thenThrow(new GoodsReceiptNotFoundException("Goods receipt not found: " + missing));
        when(useCase.getByCode("GR-2026-9999")).thenThrow(new GoodsReceiptNotFoundException("Goods receipt not found"));

        mockMvc.perform(get(BASE + "/{id}", id)).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/code/GR-2026-0001")).andExpect(status().isOk());
        mockMvc.perform(get(BASE)).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/status/DRAFT")).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/purchase-order/po-1")).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/receiver/receiver-1")).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/search/keyword").param("keyword", "acme")).andExpect(status().isOk());
        assertError(mockMvc.perform(get(BASE + "/{id}", missing)), HttpStatus.NOT_FOUND);
        assertError(mockMvc.perform(get(BASE + "/code/GR-2026-9999")), HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("reads: an unknown status code is 400")
    void reads_unknownStatus_is400() throws Exception {
        mockMvc.perform(get(BASE + "/status/LOST")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update: an open receipt is 200; moving it to another order is 400; a closed one is 409")
    void update_outcomes() throws Exception {
        when(useCase.update(eq(id), any(UpdateGoodsReceiptInput.class))).thenReturn(output(ReceiptStatus.DRAFT))
                .thenThrow(new GoodsReceiptValidationException("The purchase order cannot be changed after receipt creation"))
                .thenThrow(new GoodsReceiptNotModifiableException("This goods receipt can no longer be modified"));
        String body = toJson(Map.of("notes", "x"));

        mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        assertError(mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body)), HttpStatus.BAD_REQUEST);
        assertError(mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body)), HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("delete: a draft is 204; a closed receipt is 409; someone else's receipt is 403")
    void delete_outcomes() throws Exception {
        UUID closed = UUID.randomUUID();
        UUID foreign = UUID.randomUUID();
        doThrow(new GoodsReceiptNotModifiableException("no longer deletable")).when(useCase).delete(eq(closed), anyString());
        doThrow(new AccessDeniedException("not yours")).when(useCase).delete(eq(foreign), anyString());

        mockMvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isNoContent());
        assertError(mockMvc.perform(delete(BASE + "/{id}", closed)), HttpStatus.CONFLICT);
        assertError(mockMvc.perform(delete(BASE + "/{id}", foreign)), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("lines: adding a line is 200; removing the last line is 409; an invalid line is 400")
    void lines_outcomes() throws Exception {
        when(useCase.addLine(eq(id), any(), anyString())).thenReturn(output(ReceiptStatus.DRAFT));
        when(useCase.removeLine(eq(id), anyInt(), anyString()))
                .thenThrow(new GoodsReceiptLineRequiredException("A goods receipt must contain at least one line"));

        mockMvc.perform(patch(BASE + "/{id}/lines", id).contentType(MediaType.APPLICATION_JSON).content(toJson(Map.of("line", LINE))))
                .andExpect(status().isOk());
        assertError(mockMvc.perform(delete(BASE + "/{id}/lines/0", id).contentType(MediaType.APPLICATION_JSON).content("{}")),
                HttpStatus.CONFLICT);
        mockMvc.perform(patch(BASE + "/{id}/lines", id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("validate and cancel: success is 200; a closed receipt is 409; a blank cancel reason is 400")
    void completeAndCancel_outcomes() throws Exception {
        when(useCase.complete(eq(id), anyString())).thenReturn(output(ReceiptStatus.COMPLETED))
                .thenThrow(new GoodsReceiptBusinessException("closed", "GOODS_RECEIPT_RULE_VIOLATION"));
        when(useCase.cancel(eq(id), anyString(), anyString())).thenReturn(output(ReceiptStatus.CANCELLED));

        mockMvc.perform(patch(BASE + "/{id}/complete", id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        assertError(mockMvc.perform(patch(BASE + "/{id}/complete", id).contentType(MediaType.APPLICATION_JSON).content("{}")),
                HttpStatus.CONFLICT);
        mockMvc.perform(patch(BASE + "/{id}/cancel", id).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("reason", "Counted wrong")))).andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(patch(BASE + "/{id}/cancel", id).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("reason", " ")))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("fields: the receipt response carries every field the goods receipt screens consume (FR-014)")
    void response_carriesEveryScreenField() throws Exception {
        PurchaseOrder order = anOrder().withLine(10, "5.00").inStatus(OrderStatus.READY_FOR_RECEIPT).build();
        GoodsReceipt receipt = aReceipt(order).receiving(0, 10, 2, "Damaged").build();
        receipt.setNotes("Dock 2");
        receipt.setDiscrepancyNotes("Two cracked");
        receipt.getLines().get(0).setBatchNumber("LOT-9");
        receipt.getLines().get(0).setStorageLocation("A-01");
        receipt.getLines().get(0).setStockBefore(5);
        receipt.getLines().get(0).setStockAfter(13);
        when(useCase.getById(id)).thenReturn(new GoodsReceiptMapper().toResponse(receipt));

        String json = mockMvc.perform(get(BASE + "/{id}", id)).andReturn().getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(json, new TypeReference<>() { });
        @SuppressWarnings("unchecked")
        Map<String, Object> line = ((List<Map<String, Object>>) response.get("lines")).get(0);

        for (String field : Set.of("id", "receiptCode", "purchaseOrderId", "purchaseOrderCode", "status", "receiptDate",
                "receivedBy", "receivedByName", "supplierName", "notes", "discrepancyNotes", "hasDiscrepancy",
                "totalQuantityOrdered", "totalQuantityReceived", "totalQuantityAccepted", "totalQuantityRejected", "createdAt")) {
            assertTrue(response.get(field) != null, () -> "receipt response is missing '" + field + "'");
        }
        for (String field : Set.of("id", "lineNumber", "purchaseOrderLineId", "materialCode", "materialName", "unitOfMeasure",
                "quantityReceived", "quantityRejected", "qualityStatus", "rejectionReason", "batchNumber", "storageLocation",
                "stockBefore", "stockAfter")) {
            assertTrue(line.get(field) != null, () -> "line response is missing '" + field + "'");
        }
    }
}
