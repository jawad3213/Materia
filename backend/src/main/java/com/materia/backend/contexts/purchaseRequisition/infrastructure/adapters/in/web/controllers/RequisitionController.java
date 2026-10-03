package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.controllers;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.CreateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionOutput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionSearchCriteria;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.UpdateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.dtos.request.CreateRequisitionWebRequest;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.dtos.request.RequisitionSearchWebRequest;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.dtos.request.UpdateRequisitionWebRequest;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.dtos.response.RequisitionWebResponse;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.mappers.RequisitionWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for purchase requisitions.
 */
@RestController
@RequestMapping("/api/v1/purchase-requisitions")
public class RequisitionController {

    private final RequisitionUseCase requisitionUseCase;
    private final RequisitionWebMapper webMapper;

    public RequisitionController(RequisitionUseCase requisitionUseCase, RequisitionWebMapper webMapper) {
        this.requisitionUseCase = requisitionUseCase;
        this.webMapper = webMapper;
    }

    @PreAuthorize("hasAuthority('requisition:write')")
    @PostMapping
    public ResponseEntity<RequisitionWebResponse> createRequisition(
            @Valid @RequestBody CreateRequisitionWebRequest webRequest) {
        CreateRequisitionInput request = webMapper.toAppCreateRequest(webRequest);
        RequisitionOutput response = requisitionUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('requisition:read')")
    @GetMapping("/{id}")
    public ResponseEntity<RequisitionWebResponse> getRequisitionById(@PathVariable UUID id) {
        RequisitionOutput response = requisitionUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('requisition:read')")
    @GetMapping("/code/{code}")
    public ResponseEntity<RequisitionWebResponse> getRequisitionByCode(@PathVariable String code) {
        RequisitionOutput response = requisitionUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('requisition:read')")
    @GetMapping
    public ResponseEntity<List<RequisitionWebResponse>> getAllRequisitions() {
        List<RequisitionOutput> responses = requisitionUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('requisition:write')")
    @PutMapping("/{id}")
    public ResponseEntity<RequisitionWebResponse> updateRequisition(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRequisitionWebRequest webRequest) {
        UpdateRequisitionInput request = webMapper.toAppUpdateRequest(webRequest);
        RequisitionOutput response = requisitionUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('requisition:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequisition(@PathVariable UUID id) {
        requisitionUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('requisition:read')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RequisitionWebResponse>> getRequisitionsByStatus(@PathVariable String status) {
        List<RequisitionOutput> responses = requisitionUseCase.getByStatus(status);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('requisition:read')")
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<RequisitionWebResponse>> getRequisitionsByRequester(@PathVariable String requesterId) {
        List<RequisitionOutput> responses = requisitionUseCase.getByRequesterId(requesterId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('requisition:read')")
    @GetMapping("/search/keyword")
    public ResponseEntity<List<RequisitionWebResponse>> searchByKeyword(@RequestParam String keyword) {
        List<RequisitionOutput> responses = requisitionUseCase.searchByKeyword(keyword);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('requisition:read')")
    @PostMapping("/search")
    public ResponseEntity<PageResponse<RequisitionWebResponse>> searchAdvanced(
            @RequestBody RequisitionSearchWebRequest webRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        RequisitionSearchCriteria criteria = webMapper.toAppSearchCriteria(webRequest);
        PageResponse<RequisitionOutput> appPage = requisitionUseCase.searchAdvanced(criteria, page, size);
        PageResponse<RequisitionWebResponse> webPage = new PageResponse<>(
                webMapper.toWebResponseList(appPage.getContent()),
                appPage.getPageNumber(),
                appPage.getPageSize(),
                appPage.getTotalElements(),
                appPage.getTotalPages(),
                appPage.isLast()
        );
        return ResponseEntity.ok(webPage);
    }

    @PreAuthorize("hasAuthority('requisition:write')")
    @PatchMapping("/{id}/submit")
    public ResponseEntity<RequisitionWebResponse> submitRequisition(
            @PathVariable UUID id,
            Authentication authentication) {
        // FINDING-020 fix: actor identity resolved from the authenticated JWT principal, not from a request parameter.
        String principal = authentication != null ? authentication.getName() : "unknown";
        RequisitionOutput response = requisitionUseCase.submit(id, principal);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('requisition:validate')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<RequisitionWebResponse> approveRequisition(
            @PathVariable UUID id,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        // FINDING-020 fix: approverId and approverName are derived from the authenticated principal only.
        String principal = authentication != null ? authentication.getName() : "unknown";
        RequisitionOutput response = requisitionUseCase.approve(id, principal, principal, notes);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('requisition:validate')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<RequisitionWebResponse> rejectRequisition(
            @PathVariable UUID id,
            @RequestParam String reason,
            Authentication authentication) {
        // FINDING-020 fix: approverId and approverName are derived from the authenticated principal only.
        String principal = authentication != null ? authentication.getName() : "unknown";
        RequisitionOutput response = requisitionUseCase.reject(id, principal, principal, reason);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('requisition:write')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RequisitionWebResponse> cancelRequisition(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        // FINDING-020 fix: cancelling user resolved from the authenticated principal.
        // The default "current-user" string is replaced by the actual principal name.
        String principal = authentication != null ? authentication.getName() : "unknown";
        RequisitionOutput response = requisitionUseCase.cancel(id, principal, reason);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }
}
