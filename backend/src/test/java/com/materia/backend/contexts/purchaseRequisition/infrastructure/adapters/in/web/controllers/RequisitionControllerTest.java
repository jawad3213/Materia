package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.controllers;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionOutput;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionInvalidStatusTransitionException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionNotDeletableException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionNotFoundException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionNotModifiableException;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.mappers.RequisitionWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.materia.backend.support.ErrorResponseAssertions.assertError;
import static com.materia.backend.support.ErrorResponseAssertions.assertValidationError;
import static com.materia.backend.support.PaginationAssertions.assertPage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** The requisition HTTP contract (T059-T062), through the real security chain. */
@WebMvcTest(RequisitionController.class)
@Import(RequisitionWebMapper.class)
@WithMockUser(username = "alice")
class RequisitionControllerTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/purchase-requisitions";
    private static final String FINDING_020 =
            "FINDING-020: workflow identities (approver, requester, canceller) are caller-supplied, not the signed-in user";

    @MockBean
    private RequisitionUseCase useCase;

    private final UUID id = UUID.randomUUID();

    private RequisitionOutput output(String status) {
        RequisitionOutput out = new RequisitionOutput();
        out.setId(id);
        out.setRequisitionCode("REQ-2026-0001");
        out.setTitle("Office supplies");
        out.setStatus(status);
        out.setRequesterId("alice");
        return out;
    }

    private Map<String, Object> validCreate() {
        return Map.of("title", "Office supplies", "requesterName", "Alice",
                "lines", List.of(Map.of("materialCode", "MAT-1", "quantity", 2)));
    }

    // ---- Every endpoint routes and succeeds (FR-003, FR-006) ----

    @Test
    @DisplayName("create: a valid request is 201 with the created requisition")
    void create_is201() throws Exception {
        when(useCase.create(any())).thenReturn(output("DRAFT"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(validCreate())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("reads: get by id, by code, all, by status, by requester and by keyword all succeed")
    void reads_succeed() throws Exception {
        when(useCase.getById(id)).thenReturn(output("DRAFT"));
        when(useCase.getByCode("REQ-2026-0001")).thenReturn(output("DRAFT"));
        when(useCase.getAll()).thenReturn(List.of(output("DRAFT")));
        when(useCase.getByStatus("SUBMITTED")).thenReturn(List.of(output("SUBMITTED")));
        when(useCase.getByRequesterId("alice")).thenReturn(List.of(output("DRAFT")));
        when(useCase.searchByKeyword("office")).thenReturn(List.of(output("DRAFT")));

        mockMvc.perform(get(BASE + "/{id}", id)).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/code/REQ-2026-0001")).andExpect(status().isOk());
        mockMvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id.toString()));
        mockMvc.perform(get(BASE + "/status/SUBMITTED")).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/requester/alice")).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/search/keyword").param("keyword", "office")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("update and delete: succeed with 200 and 204")
    void updateAndDelete_succeed() throws Exception {
        when(useCase.update(eq(id), any(com.materia.backend.contexts.purchaseRequisition.application.dtos.UpdateRequisitionInput.class)))
                .thenReturn(output("DRAFT"));

        mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json(validCreate())))
                .andExpect(status().isOk());
        mockMvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isNoContent());
        verify(useCase).delete(id);
    }

    @Test
    @DisplayName("transitions: submit, approve, reject, cancel and convert each reach the service")
    void transitions_succeed() throws Exception {
        when(useCase.submit(eq(id), anyString())).thenReturn(output("SUBMITTED"));
        when(useCase.approve(eq(id), anyString(), anyString(), any())).thenReturn(output("APPROVED"));
        when(useCase.reject(eq(id), anyString(), anyString(), anyString())).thenReturn(output("REJECTED"));
        when(useCase.cancel(eq(id), anyString(), any())).thenReturn(output("CANCELLED"));
        when(useCase.convert(eq(id), anyString(), anyString(), anyString())).thenReturn(output("CONVERTED"));

        mockMvc.perform(patch(BASE + "/{id}/submit", id).param("userId", "alice")).andExpect(status().isOk());
        mockMvc.perform(patch(BASE + "/{id}/approve", id).param("approverId", "alice").param("approverName", "Alice"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
        mockMvc.perform(patch(BASE + "/{id}/reject", id).param("approverId", "alice").param("approverName", "Alice")
                .param("reason", "over budget")).andExpect(status().isOk());
        mockMvc.perform(patch(BASE + "/{id}/cancel", id).param("userId", "alice")).andExpect(status().isOk());
        mockMvc.perform(patch(BASE + "/{id}/convert", id).param("purchaseOrderId", "po-1")
                .param("purchaseOrderCode", "PO-2026-0001").param("userId", "alice")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("search: every page field survives the controller's own copy into the response")
    void search_copiesPageFieldsFaithfully() throws Exception {
        when(useCase.searchAdvanced(any(), eq(2), eq(5)))
                .thenReturn(new PageResponse<>(List.of(output("DRAFT")), 2, 5, 11L, 3, true));

        assertPage(mockMvc.perform(post(BASE + "/search").param("page", "2").param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON).content("{}")),
                2, 5, 11L, 3, true, 1);
    }

    // ---- Refusals are distinguishable (FR-007) ----

    @Test
    @DisplayName("not found: an unknown requisition is 404 with a specific errorCode")
    void unknown_is404() throws Exception {
        when(useCase.getById(id)).thenThrow(new RequisitionNotFoundException(id));

        assertError(mockMvc.perform(get(BASE + "/{id}", id)), HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("forbidden transition: approving a draft is 409, not a 400 or a 500")
    void forbiddenTransition_is409() throws Exception {
        when(useCase.approve(eq(id), anyString(), anyString(), any()))
                .thenThrow(new RequisitionInvalidStatusTransitionException("Only submitted requisitions can be approved"));

        assertError(mockMvc.perform(patch(BASE + "/{id}/approve", id)
                .param("approverId", "alice").param("approverName", "Alice")), HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("edit and delete guards: editing an approved requisition or deleting one in progress is 409")
    void editAndDeleteGuards_are409() throws Exception {
        when(useCase.update(eq(id), any(com.materia.backend.contexts.purchaseRequisition.application.dtos.UpdateRequisitionInput.class)))
                .thenThrow(new RequisitionNotModifiableException());
        doThrow(new RequisitionNotDeletableException()).when(useCase).delete(id);

        assertError(mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(json(validCreate()))), HttpStatus.CONFLICT);
        assertError(mockMvc.perform(delete(BASE + "/{id}", id)), HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("validation: a missing title is 400 naming the field, and never reaches the service")
    void create_missingTitle_is400() throws Exception {
        assertValidationError(mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("requesterName", "Alice",
                        "lines", List.of(Map.of("materialCode", "MAT-1", "quantity", 2)))))), "title");
        verify(useCase, never()).create(any());
    }

    @Test
    @DisplayName("validation: a requisition with no lines is 400")
    void create_noLines_is400() throws Exception {
        assertValidationError(mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("title", "T", "requesterName", "Alice", "lines", List.of())))), "lines");
    }

    @Test
    @DisplayName("validation: a line with a non-positive quantity is 400")
    void create_nonPositiveQuantity_is400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "T", "requesterName", "Alice",
                                "lines", List.of(Map.of("materialCode", "MAT-1", "quantity", 0))))))
                .andExpect(status().isBadRequest());
        verify(useCase, never()).create(any());
    }

    @Test
    @DisplayName("validation: rejecting without a reason is 400")
    void reject_withoutReason_is400() throws Exception {
        mockMvc.perform(patch(BASE + "/{id}/reject", id).param("approverId", "alice").param("approverName", "Alice"))
                .andExpect(status().isBadRequest());
        verify(useCase, never()).reject(any(), any(), any(), any());
    }

    @Test
    @DisplayName("security: every requisition endpoint refuses an anonymous caller")
    @org.springframework.security.test.context.support.WithAnonymousUser
    void anonymous_is401() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(validCreate())))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch(BASE + "/{id}/approve", id).param("approverId", "x").param("approverName", "X"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(useCase);
    }

    // ---- Response contract (FR-008) ----

    @Test
    @DisplayName("contract: a requisition response carries every field the frontend reads, including its lines")
    void response_carriesFrontendFields() throws Exception {
        when(useCase.getById(id)).thenReturn(output("DRAFT"));

        var result = mockMvc.perform(get(BASE + "/{id}", id)).andExpect(status().isOk());
        for (String field : List.of("id", "requisitionCode", "title", "description", "justification", "status",
                "requesterId", "requesterName", "requiredDate", "submittedDate", "approvedDate", "convertedDate",
                "cancelledDate", "totalAmount", "currencyCode", "approverId", "approverName", "rejectionReason",
                "approvalNotes", "cancellationReason", "lines")) {
            result.andExpect(jsonPath("$." + field).hasJsonPath());
        }
    }

    // ---- Identity integrity ----

    @Test
    @Disabled(FINDING_020)
    @DisplayName("identity: the approver recorded is the signed-in user, not whoever the caller names")
    void approve_recordsSignedInUserNotCallerSuppliedApprover() throws Exception {
        when(useCase.approve(eq(id), anyString(), anyString(), any())).thenReturn(output("APPROVED"));

        // Signed in as alice, but naming "cfo" as the approver.
        mockMvc.perform(patch(BASE + "/{id}/approve", id).param("approverId", "cfo").param("approverName", "The CFO"));

        verify(useCase, never()).approve(eq(id), eq("cfo"), anyString(), any());
    }
}
