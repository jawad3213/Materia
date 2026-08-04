package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.UpdateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.AddGoodsReceiptLineWebRequest;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.CreateGoodsReceiptWebRequest;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.GoodsReceiptCancelWebRequest;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.GoodsReceiptCompleteWebRequest;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.GoodsReceiptWebResponse;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.RemoveGoodsReceiptLineWebRequest;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.UpdateGoodsReceiptWebRequest;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.mappers.GoodsReceiptWebMapper;
import com.materia.backend.infrastructure.security.SecurityUtils;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goods-receipts")
public class GoodsReceiptController {

    private final GoodsReceiptUseCase goodsReceiptUseCase;
    private final GoodsReceiptWebMapper webMapper;

    public GoodsReceiptController(GoodsReceiptUseCase goodsReceiptUseCase,
                                  GoodsReceiptWebMapper webMapper) {
        this.goodsReceiptUseCase = goodsReceiptUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping
    public ResponseEntity<GoodsReceiptWebResponse> createGoodsReceipt(
            @Valid @RequestBody CreateGoodsReceiptWebRequest webRequest) {
        CreateGoodsReceiptInput request = webMapper.toAppCreateRequest(webRequest);
        applyAuthenticatedReceiver(request);
        GoodsReceiptOutput response = goodsReceiptUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoodsReceiptWebResponse> getGoodsReceiptById(@PathVariable UUID id) {
        GoodsReceiptOutput response = goodsReceiptUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<GoodsReceiptWebResponse> getGoodsReceiptByCode(@PathVariable String code) {
        GoodsReceiptOutput response = goodsReceiptUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping
    public ResponseEntity<List<GoodsReceiptWebResponse>> getAllGoodsReceipts() {
        List<GoodsReceiptOutput> responses = goodsReceiptUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoodsReceiptWebResponse> updateGoodsReceipt(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGoodsReceiptWebRequest webRequest) {
        UpdateGoodsReceiptInput request = webMapper.toAppUpdateRequest(webRequest);
        applyAuthenticatedReceiver(request);
        GoodsReceiptOutput response = goodsReceiptUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoodsReceipt(@PathVariable UUID id) {
        goodsReceiptUseCase.delete(id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<GoodsReceiptWebResponse>> getGoodsReceiptsByStatus(@PathVariable String status) {
        ReceiptStatus receiptStatus = ReceiptStatus.fromCode(status);
        List<GoodsReceiptOutput> responses = goodsReceiptUseCase.getByStatus(receiptStatus);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/purchase-order/{purchaseOrderId}")
    public ResponseEntity<List<GoodsReceiptWebResponse>> getGoodsReceiptsByPurchaseOrderId(
            @PathVariable String purchaseOrderId) {
        List<GoodsReceiptOutput> responses = goodsReceiptUseCase.getByPurchaseOrderId(purchaseOrderId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<GoodsReceiptWebResponse>> getGoodsReceiptsByReceiverId(
            @PathVariable String receiverId) {
        List<GoodsReceiptOutput> responses = goodsReceiptUseCase.getByReceiverId(receiverId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/search/keyword")
    public ResponseEntity<List<GoodsReceiptWebResponse>> searchGoodsReceiptsByKeyword(
            @RequestParam String keyword) {
        List<GoodsReceiptOutput> responses = goodsReceiptUseCase.search(keyword);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PatchMapping("/{id}/lines")
    public ResponseEntity<GoodsReceiptWebResponse> addLine(
            @PathVariable UUID id,
            @Valid @RequestBody AddGoodsReceiptLineWebRequest webRequest) {
        GoodsReceiptLineInput line = webMapper.toAppLine(webRequest.getLine());
        GoodsReceiptOutput response = goodsReceiptUseCase.addLine(id, line, currentUserId());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @DeleteMapping("/{id}/lines/{lineIndex}")
    public ResponseEntity<GoodsReceiptWebResponse> removeLine(
            @PathVariable UUID id,
            @PathVariable int lineIndex,
            @Valid @RequestBody RemoveGoodsReceiptLineWebRequest webRequest) {
        GoodsReceiptOutput response = goodsReceiptUseCase.removeLine(id, lineIndex, currentUserId());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<GoodsReceiptWebResponse> completeGoodsReceipt(
            @PathVariable UUID id,
            @Valid @RequestBody GoodsReceiptCompleteWebRequest webRequest) {
        GoodsReceiptOutput response = goodsReceiptUseCase.complete(id, currentUserId());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<GoodsReceiptWebResponse> cancelGoodsReceipt(
            @PathVariable UUID id,
            @Valid @RequestBody GoodsReceiptCancelWebRequest webRequest) {
        GoodsReceiptOutput response = goodsReceiptUseCase.cancel(id, currentUserId(), webRequest.getReason());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    private void applyAuthenticatedReceiver(CreateGoodsReceiptInput request) {
        String userId = currentUserId();
        request.setUserId(userId);
        request.setReceivedBy(userId);
        request.setReceivedByName(userId);
    }

    private String currentUserId() {
        return SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required"));
    }
}
