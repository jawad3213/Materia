package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.CreatePurchaseOrderWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderAssignReceiverWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderCancelWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderCompleteWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderConfirmReceiptWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderConfirmWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderRejectWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderSubmitWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderWebResponse;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.UpdatePurchaseOrderDeliveryStatusWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.UpdatePurchaseOrderWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.mappers.PurchaseOrderWebMapper;
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
 * REST controller for purchase orders with method-level security and principal resolution.
 */
@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderUseCase purchaseOrderUseCase;
    private final PurchaseOrderWebMapper webMapper;

    public PurchaseOrderController(PurchaseOrderUseCase purchaseOrderUseCase,
                                   PurchaseOrderWebMapper webMapper) {
        this.purchaseOrderUseCase = purchaseOrderUseCase;
        this.webMapper = webMapper;
    }

    @PreAuthorize("hasAuthority('order:write')")
    @PostMapping
    public ResponseEntity<PurchaseOrderWebResponse> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderWebRequest webRequest,
            Authentication authentication) {
        String principal = resolvePrincipal(authentication, webRequest.getCreatedBy(), "system");
        webRequest.setCreatedBy(principal);
        if (webRequest.getOrderedBy() == null || webRequest.getOrderedBy().isBlank()) {
            webRequest.setOrderedBy(principal);
        }
        CreatePurchaseOrderInput request = webMapper.toAppCreateRequest(webRequest);
        request.setUserId(principal);
        PurchaseOrderOutput response = purchaseOrderUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('order:write')")
    @GetMapping("/assignable-receivers")
    public ResponseEntity<List<ReceiverDirectory.Receiver>> getAssignableReceivers() {
        return ResponseEntity.ok(purchaseOrderUseCase.getAssignableReceivers());
    }

    @PreAuthorize("hasAuthority('order:read')")
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderWebResponse> getPurchaseOrderById(@PathVariable UUID id) {
        PurchaseOrderOutput response = purchaseOrderUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('order:read')")
    @GetMapping("/code/{code}")
    public ResponseEntity<PurchaseOrderWebResponse> getPurchaseOrderByCode(@PathVariable String code) {
        PurchaseOrderOutput response = purchaseOrderUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('order:read')")
    @GetMapping
    public ResponseEntity<List<PurchaseOrderWebResponse>> getAllPurchaseOrders() {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('order:write')")
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderWebResponse> updatePurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePurchaseOrderWebRequest webRequest,
            Authentication authentication) {
        String principal = resolvePrincipal(authentication, webRequest.getUpdatedBy(), "unknown");
        webRequest.setUpdatedBy(principal);
        UpdatePurchaseOrderInput request = webMapper.toAppUpdateRequest(webRequest);
        request.setUserId(principal);
        PurchaseOrderOutput response = purchaseOrderUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('order:write') and hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable UUID id, Authentication authentication) {
        purchaseOrderUseCase.delete(id, resolvePrincipal(authentication, null, "unknown"));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('order:read')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrderWebResponse>> getPurchaseOrdersByStatus(@PathVariable String status) {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.getByStatus(status);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('order:read')")
    @GetMapping("/delivery-status/{deliveryStatus}")
    public ResponseEntity<List<PurchaseOrderWebResponse>> getPurchaseOrdersByDeliveryStatus(
            @PathVariable String deliveryStatus) {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.getByDeliveryStatus(deliveryStatus);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('order:read')")
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrderWebResponse>> getPurchaseOrdersBySupplierId(
            @PathVariable UUID supplierId) {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.getBySupplierId(supplierId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('order:read')")
    @GetMapping("/requisition/{requisitionId}")
    public ResponseEntity<List<PurchaseOrderWebResponse>> getPurchaseOrdersByRequisitionId(
            @PathVariable UUID requisitionId) {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.getByRequisitionId(requisitionId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('order:read')")
    @GetMapping("/search/keyword")
    public ResponseEntity<List<PurchaseOrderWebResponse>> searchPurchaseOrdersByKeyword(
            @RequestParam String keyword) {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.searchByKeyword(keyword);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('order:write')")
    @PatchMapping("/{id}/submit")
    public ResponseEntity<PurchaseOrderWebResponse> submitPurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) PurchaseOrderSubmitWebRequest webRequest,
            Authentication authentication) {
        String principal = resolvePrincipal(authentication, webRequest != null ? webRequest.getUserId() : null, "unknown");
        PurchaseOrderOutput response = purchaseOrderUseCase.submit(id, principal);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('order:validate')")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<PurchaseOrderWebResponse> confirmPurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) PurchaseOrderConfirmWebRequest webRequest,
            Authentication authentication) {
        String principal = resolvePrincipal(authentication, webRequest != null ? webRequest.getUserId() : null, "unknown");
        PurchaseOrderOutput response = purchaseOrderUseCase.confirm(id, principal);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('order:validate')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<PurchaseOrderWebResponse> rejectPurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderRejectWebRequest webRequest,
            Authentication authentication) {
        String principal = resolvePrincipal(authentication, null, "unknown");
        PurchaseOrderOutput response = purchaseOrderUseCase.reject(id, principal, webRequest.getReason());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('order:write')")
    @PatchMapping("/{id}/assign-receiver")
    public ResponseEntity<PurchaseOrderWebResponse> assignReceiver(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderAssignReceiverWebRequest webRequest,
            Authentication authentication) {
        String principal = resolvePrincipal(authentication, webRequest.getUserId(), "unknown");
        String userName = (webRequest.getUserName() != null && !webRequest.getUserName().isBlank())
                ? webRequest.getUserName()
                : principal;
        PurchaseOrderOutput response = purchaseOrderUseCase.assignReceiver(
                id,
                principal,
                userName,
                webRequest.getAssignedUserId(),
                webRequest.getAssignedUserName()
        );
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('order:write') or hasAuthority('receipt:write')")
    @PatchMapping("/{id}/confirm-receipt")
    public ResponseEntity<PurchaseOrderWebResponse> confirmReceipt(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) PurchaseOrderConfirmReceiptWebRequest webRequest,
            Authentication authentication) {
        // The receiver is always the caller; a body-supplied id must not stand in for the assigned receiver.
        String principal = resolvePrincipal(authentication, null, null);
        String receiverId = principal;
        String receiverName = (webRequest != null && webRequest.getReceiverName() != null && !webRequest.getReceiverName().isBlank())
                ? webRequest.getReceiverName()
                : principal;
        PurchaseOrderOutput response = purchaseOrderUseCase.confirmReceipt(
                id,
                receiverId,
                receiverName
        );
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('order:cancel')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderWebResponse> cancelPurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) PurchaseOrderCancelWebRequest webRequest,
            Authentication authentication) {
        String principal = resolvePrincipal(authentication, webRequest != null ? webRequest.getUserId() : null, "unknown");
        String reason = (webRequest != null && webRequest.getReason() != null && !webRequest.getReason().isBlank())
                ? webRequest.getReason()
                : "No reason specified";
        PurchaseOrderOutput response = purchaseOrderUseCase.cancel(id, principal, reason);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('order:write')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<PurchaseOrderWebResponse> completePurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) PurchaseOrderCompleteWebRequest webRequest,
            Authentication authentication) {
        String principal = resolvePrincipal(authentication, webRequest != null ? webRequest.getUserId() : null, "unknown");
        PurchaseOrderOutput response = purchaseOrderUseCase.complete(id, principal);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('order:write')")
    @PatchMapping("/{id}/delivery-status")
    public ResponseEntity<PurchaseOrderWebResponse> updateDeliveryStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePurchaseOrderDeliveryStatusWebRequest webRequest,
            Authentication authentication) {
        String principal = resolvePrincipal(authentication, webRequest.getUserId(), "unknown");
        PurchaseOrderOutput response = purchaseOrderUseCase.updateDeliveryStatus(
                id,
                webRequest.getDeliveryStatus(),
                principal
        );
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    private String resolvePrincipal(Authentication authentication, String fallback, String defaultName) {
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return defaultName;
    }
}
