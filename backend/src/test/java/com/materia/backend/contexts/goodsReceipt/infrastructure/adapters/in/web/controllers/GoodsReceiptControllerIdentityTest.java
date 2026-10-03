package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.UpdateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.mappers.GoodsReceiptWebMapper;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** [T043] Goods receipt writes always act as the signed-in user, whatever identity the body carries (FR-012). */
@WebMvcTest(GoodsReceiptController.class)
@Import(GoodsReceiptWebMapper.class)
@WithMockUser(username = "real-receiver", authorities = {"receipt:read", "receipt:write", "receipt:quality"})
class GoodsReceiptControllerIdentityTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/goods-receipts";
    private static final String PRINCIPAL = "real-receiver";
    private static final String FORGED = "forged-user";
    private static final Map<String, Object> LINE =
            Map.of("materialCode", "MAT-1", "quantityReceived", 1, "quantityRejected", 0);

    @MockBean(answer = Answers.RETURNS_MOCKS)
    private GoodsReceiptUseCase useCase;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID id = UUID.randomUUID();

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    @DisplayName("identity: create ignores a forged receiver and records the signed-in user as receiver")
    void create_usesPrincipal() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(toJson(Map.of(
                "purchaseOrderId", UUID.randomUUID().toString(), "receivedBy", FORGED, "receivedByName", "Forged",
                "lines", List.of(LINE)))));

        ArgumentCaptor<CreateGoodsReceiptInput> input = ArgumentCaptor.forClass(CreateGoodsReceiptInput.class);
        verify(useCase).create(input.capture());
        assertEquals(PRINCIPAL, input.getValue().getUserId());
        assertEquals(PRINCIPAL, input.getValue().getReceivedBy());
    }

    @Test
    @DisplayName("identity: update ignores a forged receiver")
    void update_usesPrincipal() throws Exception {
        mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("receivedBy", FORGED, "notes", "x"))));

        ArgumentCaptor<UpdateGoodsReceiptInput> input = ArgumentCaptor.forClass(UpdateGoodsReceiptInput.class);
        verify(useCase).update(eq(id), input.capture());
        assertEquals(PRINCIPAL, input.getValue().getUserId());
        assertEquals(PRINCIPAL, input.getValue().getReceivedBy());
    }

    @Test
    @DisplayName("identity: delete, line changes, validation and cancellation act as the signed-in user")
    void otherWrites_usePrincipal() throws Exception {
        mockMvc.perform(delete(BASE + "/{id}", id));
        mockMvc.perform(patch(BASE + "/{id}/lines", id).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("line", LINE, "userId", FORGED))));
        mockMvc.perform(delete(BASE + "/{id}/lines/0", id).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("userId", FORGED))));
        mockMvc.perform(patch(BASE + "/{id}/complete", id).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("userId", FORGED))));
        mockMvc.perform(patch(BASE + "/{id}/cancel", id).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("userId", FORGED, "reason", "x"))));

        verify(useCase).delete(id, PRINCIPAL);
        verify(useCase).addLine(eq(id), any(), eq(PRINCIPAL));
        verify(useCase).removeLine(id, 0, PRINCIPAL);
        verify(useCase).complete(id, PRINCIPAL);
        verify(useCase).cancel(eq(id), eq(PRINCIPAL), anyString());
    }
}
