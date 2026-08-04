package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.CreatePurchaseOrderWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderAssignReceiverWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderCancelWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderCompleteWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderConfirmReceiptWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderConfirmWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderSubmitWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderWebResponse;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.UpdatePurchaseOrderDeliveryStatusWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.UpdatePurchaseOrderWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.mappers.PurchaseOrderWebMapper;
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
 * REST controller for purchase orders.
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

    @PostMapping
    public ResponseEntity<PurchaseOrderWebResponse> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderWebRequest webRequest) {
        CreatePurchaseOrderInput request = webMapper.toAppCreateRequest(webRequest);
        PurchaseOrderOutput response = purchaseOrderUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderWebResponse> getPurchaseOrderById(@PathVariable UUID id) {
        PurchaseOrderOutput response = purchaseOrderUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PurchaseOrderWebResponse> getPurchaseOrderByCode(@PathVariable String code) {
        PurchaseOrderOutput response = purchaseOrderUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderWebResponse>> getAllPurchaseOrders() {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderWebResponse> updatePurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePurchaseOrderWebRequest webRequest) {
        UpdatePurchaseOrderInput request = webMapper.toAppUpdateRequest(webRequest);
        PurchaseOrderOutput response = purchaseOrderUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable UUID id) {
        purchaseOrderUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrderWebResponse>> getPurchaseOrdersByStatus(@PathVariable String status) {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.getByStatus(status);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/delivery-status/{deliveryStatus}")
    public ResponseEntity<List<PurchaseOrderWebResponse>> getPurchaseOrdersByDeliveryStatus(
            @PathVariable String deliveryStatus) {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.getByDeliveryStatus(deliveryStatus);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrderWebResponse>> getPurchaseOrdersBySupplierId(
            @PathVariable UUID supplierId) {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.getBySupplierId(supplierId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/requisition/{requisitionId}")
    public ResponseEntity<List<PurchaseOrderWebResponse>> getPurchaseOrdersByRequisitionId(
            @PathVariable UUID requisitionId) {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.getByRequisitionId(requisitionId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/search/keyword")
    public ResponseEntity<List<PurchaseOrderWebResponse>> searchPurchaseOrdersByKeyword(
            @RequestParam String keyword) {
        List<PurchaseOrderOutput> responses = purchaseOrderUseCase.searchByKeyword(keyword);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PatchMapping("/{id}/submit")
    public ResponseEntity<PurchaseOrderWebResponse> submitPurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderSubmitWebRequest webRequest) {
        PurchaseOrderOutput response = purchaseOrderUseCase.submit(id, webRequest.getUserId());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<PurchaseOrderWebResponse> confirmPurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderConfirmWebRequest webRequest) {
        PurchaseOrderOutput response = purchaseOrderUseCase.confirm(id, webRequest.getUserId());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/assign-receiver")
    public ResponseEntity<PurchaseOrderWebResponse> assignReceiver(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderAssignReceiverWebRequest webRequest) {
        PurchaseOrderOutput response = purchaseOrderUseCase.assignReceiver(
                id,
                webRequest.getUserId(),
                webRequest.getUserName(),
                webRequest.getAssignedUserId(),
                webRequest.getAssignedUserName()
        );
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/confirm-receipt")
    public ResponseEntity<PurchaseOrderWebResponse> confirmReceipt(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderConfirmReceiptWebRequest webRequest) {
        PurchaseOrderOutput response = purchaseOrderUseCase.confirmReceipt(
                id,
                webRequest.getReceiverId(),
                webRequest.getReceiverName()
        );
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderWebResponse> cancelPurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderCancelWebRequest webRequest) {
        PurchaseOrderOutput response = purchaseOrderUseCase.cancel(id, webRequest.getUserId(), webRequest.getReason());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<PurchaseOrderWebResponse> completePurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderCompleteWebRequest webRequest) {
        PurchaseOrderOutput response = purchaseOrderUseCase.complete(id, webRequest.getUserId());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/delivery-status")
    public ResponseEntity<PurchaseOrderWebResponse> updateDeliveryStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePurchaseOrderDeliveryStatusWebRequest webRequest) {
        PurchaseOrderOutput response = purchaseOrderUseCase.updateDeliveryStatus(
                id,
                webRequest.getDeliveryStatus(),
                webRequest.getUserId()
        );
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }
}
