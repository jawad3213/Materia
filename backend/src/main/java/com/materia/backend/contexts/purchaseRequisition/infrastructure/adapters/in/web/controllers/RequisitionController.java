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

    @PostMapping
    public ResponseEntity<RequisitionWebResponse> createRequisition(
            @Valid @RequestBody CreateRequisitionWebRequest webRequest) {
        CreateRequisitionInput request = webMapper.toAppCreateRequest(webRequest);
        RequisitionOutput response = requisitionUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequisitionWebResponse> getRequisitionById(@PathVariable UUID id) {
        RequisitionOutput response = requisitionUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<RequisitionWebResponse> getRequisitionByCode(@PathVariable String code) {
        RequisitionOutput response = requisitionUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping
    public ResponseEntity<List<RequisitionWebResponse>> getAllRequisitions() {
        List<RequisitionOutput> responses = requisitionUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequisitionWebResponse> updateRequisition(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRequisitionWebRequest webRequest) {
        UpdateRequisitionInput request = webMapper.toAppUpdateRequest(webRequest);
        RequisitionOutput response = requisitionUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequisition(@PathVariable UUID id) {
        requisitionUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RequisitionWebResponse>> getRequisitionsByStatus(@PathVariable String status) {
        List<RequisitionOutput> responses = requisitionUseCase.getByStatus(status);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<RequisitionWebResponse>> getRequisitionsByRequester(@PathVariable String requesterId) {
        List<RequisitionOutput> responses = requisitionUseCase.getByRequesterId(requesterId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/search/keyword")
    public ResponseEntity<List<RequisitionWebResponse>> searchByKeyword(@RequestParam String keyword) {
        List<RequisitionOutput> responses = requisitionUseCase.searchByKeyword(keyword);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

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

    @PatchMapping("/{id}/submit")
    public ResponseEntity<RequisitionWebResponse> submitRequisition(
            @PathVariable UUID id,
            @RequestParam String userId) {
        RequisitionOutput response = requisitionUseCase.submit(id, userId);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<RequisitionWebResponse> approveRequisition(
            @PathVariable UUID id,
            @RequestParam String approverId,
            @RequestParam String approverName,
            @RequestParam(required = false) String notes) {
        RequisitionOutput response = requisitionUseCase.approve(id, approverId, approverName, notes);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<RequisitionWebResponse> rejectRequisition(
            @PathVariable UUID id,
            @RequestParam String approverId,
            @RequestParam String approverName,
            @RequestParam String reason) {
        RequisitionOutput response = requisitionUseCase.reject(id, approverId, approverName, reason);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/convert")
    public ResponseEntity<RequisitionWebResponse> convertRequisition(
            @PathVariable UUID id,
            @RequestParam String purchaseOrderId,
            @RequestParam String purchaseOrderCode,
            @RequestParam String userId) {
        RequisitionOutput response = requisitionUseCase.convert(id, purchaseOrderId, purchaseOrderCode, userId);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RequisitionWebResponse> cancelRequisition(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "current-user") String userId,
            @RequestParam(required = false) String reason) {
        RequisitionOutput response = requisitionUseCase.cancel(id, userId, reason);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }
}
