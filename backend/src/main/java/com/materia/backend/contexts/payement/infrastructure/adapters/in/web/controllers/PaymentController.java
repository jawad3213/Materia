package com.materia.backend.contexts.payement.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.payement.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payement.application.dtos.PaymentOutput;
import com.materia.backend.contexts.payement.application.dtos.UpdatePaymentInput;
import com.materia.backend.contexts.payement.domain.ports.in.PaymentUseCase;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos.CreatePaymentWebRequest;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos.PaymentCancelWebRequest;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos.PaymentCompleteWebRequest;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos.PaymentPrepareWebRequest;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos.PaymentWebResponse;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos.UpdatePaymentWebRequest;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.mappers.PaymentWebMapper;
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
 * REST controller for payments.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;
    private final PaymentWebMapper webMapper;

    public PaymentController(PaymentUseCase paymentUseCase, PaymentWebMapper webMapper) {
        this.paymentUseCase = paymentUseCase;
        this.webMapper = webMapper;
    }

    // ============================================================
    // CRUD ENDPOINTS
    // ============================================================

    @PostMapping
    public ResponseEntity<PaymentWebResponse> createPayment(
            @Valid @RequestBody CreatePaymentWebRequest webRequest) {
        CreatePaymentInput request = webMapper.toAppCreateRequest(webRequest);
        PaymentOutput response = paymentUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentWebResponse> getPaymentById(@PathVariable UUID id) {
        PaymentOutput response = paymentUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PaymentWebResponse> getPaymentByCode(@PathVariable String code) {
        PaymentOutput response = paymentUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping
    public ResponseEntity<List<PaymentWebResponse>> getAllPayments() {
        List<PaymentOutput> responses = paymentUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentWebResponse> updatePayment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePaymentWebRequest webRequest) {
        UpdatePaymentInput request = webMapper.toAppUpdateRequest(webRequest);
        PaymentOutput response = paymentUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable UUID id) {
        paymentUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // QUERY ENDPOINTS
    // ============================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentWebResponse>> getPaymentsByStatus(@PathVariable String status) {
        List<PaymentOutput> responses = paymentUseCase.getByStatus(status);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PaymentWebResponse>> getPaymentsBySupplierId(@PathVariable String supplierId) {
        List<PaymentOutput> responses = paymentUseCase.getBySupplierId(supplierId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/search/keyword")
    public ResponseEntity<List<PaymentWebResponse>> searchPaymentsByKeyword(@RequestParam String keyword) {
        List<PaymentOutput> responses = paymentUseCase.searchByKeyword(keyword);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    // ============================================================
    // LIFECYCLE ENDPOINTS
    // ============================================================

    @PatchMapping("/{id}/prepare")
    public ResponseEntity<PaymentWebResponse> preparePayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentPrepareWebRequest webRequest) {
        PaymentOutput response = paymentUseCase.prepare(id, webRequest.getUserId());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<PaymentWebResponse> completePayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentCompleteWebRequest webRequest) {
        PaymentOutput response = paymentUseCase.complete(
                id,
                webRequest.getUserId(),
                webRequest.getBankReference(),
                webRequest.getTransactionId(),
                webRequest.getPaymentMethod()
        );
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PaymentWebResponse> cancelPayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentCancelWebRequest webRequest) {
        PaymentOutput response = paymentUseCase.cancel(id, webRequest.getUserId(), webRequest.getReason());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }
}
