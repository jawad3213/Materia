package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.returnToVendor.application.dtos.CreateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorOutput;
import com.materia.backend.contexts.returnToVendor.application.dtos.UpdateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.ports.in.ReturnToVendorUseCase;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.CreateReturnToVendorWebRequest;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.ReturnToVendorLineWebRequest;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.ReturnToVendorWebResponse;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.UpdateReturnToVendorWebRequest;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.mappers.ReturnToVendorWebMapper;
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
 * REST controller for return to vendor operations.
 */
@RestController
@RequestMapping("/api/v1/return-to-vendors")
public class ReturnToVendorController {

    private final ReturnToVendorUseCase returnToVendorUseCase;
    private final ReturnToVendorWebMapper webMapper;

    public ReturnToVendorController(ReturnToVendorUseCase returnToVendorUseCase,
                                    ReturnToVendorWebMapper webMapper) {
        this.returnToVendorUseCase = returnToVendorUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping
    public ResponseEntity<ReturnToVendorWebResponse> create(
            @Valid @RequestBody CreateReturnToVendorWebRequest webRequest) {
        CreateReturnToVendorInput input = webMapper.toAppCreateRequest(webRequest);
        ReturnToVendorOutput output = returnToVendorUseCase.create(input);
        return new ResponseEntity<>(webMapper.toWebResponse(output), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnToVendorWebResponse> getById(@PathVariable UUID id) {
        ReturnToVendorOutput output = returnToVendorUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ReturnToVendorWebResponse> getByCode(@PathVariable String code) {
        ReturnToVendorOutput output = returnToVendorUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @GetMapping
    public ResponseEntity<List<ReturnToVendorWebResponse>> getAll() {
        List<ReturnToVendorOutput> outputs = returnToVendorUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(outputs));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReturnToVendorWebResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReturnToVendorWebRequest webRequest) {
        UpdateReturnToVendorInput input = webMapper.toAppUpdateRequest(webRequest);
        ReturnToVendorOutput output = returnToVendorUseCase.update(id, input);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestParam String userId) {
        returnToVendorUseCase.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // QUERY ENDPOINTS
    // ============================================================

    @GetMapping("/goods-receipt/{goodsReceiptId}")
    public ResponseEntity<List<ReturnToVendorWebResponse>> getByGoodsReceiptId(
            @PathVariable String goodsReceiptId) {
        List<ReturnToVendorOutput> outputs = returnToVendorUseCase.getByGoodsReceiptId(goodsReceiptId);
        return ResponseEntity.ok(webMapper.toWebResponseList(outputs));
    }

    @GetMapping("/purchase-order/{purchaseOrderId}")
    public ResponseEntity<List<ReturnToVendorWebResponse>> getByPurchaseOrderId(
            @PathVariable String purchaseOrderId) {
        List<ReturnToVendorOutput> outputs = returnToVendorUseCase.getByPurchaseOrderId(purchaseOrderId);
        return ResponseEntity.ok(webMapper.toWebResponseList(outputs));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<ReturnToVendorWebResponse>> getBySupplierId(
            @PathVariable String supplierId) {
        List<ReturnToVendorOutput> outputs = returnToVendorUseCase.getBySupplierId(supplierId);
        return ResponseEntity.ok(webMapper.toWebResponseList(outputs));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReturnToVendorWebResponse>> getByStatus(
            @PathVariable ReturnStatus status) {
        List<ReturnToVendorOutput> outputs = returnToVendorUseCase.getByStatus(status);
        return ResponseEntity.ok(webMapper.toWebResponseList(outputs));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReturnToVendorWebResponse>> search(
            @RequestParam String keyword) {
        List<ReturnToVendorOutput> outputs = returnToVendorUseCase.search(keyword);
        return ResponseEntity.ok(webMapper.toWebResponseList(outputs));
    }

    // ============================================================
    // LINE MANAGEMENT ENDPOINTS
    // ============================================================

    @PostMapping("/{id}/lines")
    public ResponseEntity<ReturnToVendorWebResponse> addLine(
            @PathVariable UUID id,
            @Valid @RequestBody ReturnToVendorLineWebRequest lineRequest,
            @RequestParam String userId) {
        ReturnToVendorLineInput lineInput = webMapper.toLineInput(lineRequest);
        ReturnToVendorOutput output = returnToVendorUseCase.addLine(id, lineInput, userId);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @DeleteMapping("/{id}/lines/{lineIndex}")
    public ResponseEntity<ReturnToVendorWebResponse> removeLine(
            @PathVariable UUID id,
            @PathVariable int lineIndex,
            @RequestParam String userId) {
        ReturnToVendorOutput output = returnToVendorUseCase.removeLine(id, lineIndex, userId);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    // ============================================================
    // STATUS TRANSITION ENDPOINTS
    // ============================================================

    @PatchMapping("/{id}/submit")
    public ResponseEntity<ReturnToVendorWebResponse> submit(
            @PathVariable UUID id,
            @RequestParam String userId) {
        ReturnToVendorOutput output = returnToVendorUseCase.submit(id, userId);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ReturnToVendorWebResponse> resolve(
            @PathVariable UUID id,
            @RequestParam String userId,
            @RequestParam ResolutionType resolutionType,
            @RequestParam String reference) {
        ReturnToVendorOutput output = returnToVendorUseCase.resolve(id, userId, resolutionType, reference);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReturnToVendorWebResponse> cancel(
            @PathVariable UUID id,
            @RequestParam String userId,
            @RequestParam String reason) {
        ReturnToVendorOutput output = returnToVendorUseCase.cancel(id, userId, reason);
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }
}
