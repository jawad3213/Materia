package com.materia.backend.contexts.payment.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.payment.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payment.application.dtos.PaymentOutput;
import com.materia.backend.contexts.payment.application.dtos.UpdatePaymentInput;
import com.materia.backend.contexts.payment.domain.ports.in.PaymentUseCase;
import com.materia.backend.contexts.payment.infrastructure.adapters.in.web.dtos.CreatePaymentWebRequest;
import com.materia.backend.contexts.payment.infrastructure.adapters.in.web.dtos.PaymentCancelWebRequest;
import com.materia.backend.contexts.payment.infrastructure.adapters.in.web.dtos.PaymentCompleteWebRequest;
import com.materia.backend.contexts.payment.infrastructure.adapters.in.web.dtos.PaymentWebResponse;
import com.materia.backend.contexts.payment.infrastructure.adapters.in.web.dtos.UpdatePaymentWebRequest;
import com.materia.backend.contexts.payment.infrastructure.adapters.in.web.mappers.PaymentWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
 * REST controller for supplier payments. Following Role.java, administrators manage payments
 * ({@code payment:write}) and purchasers consult them ({@code payment:read}). Payments are the only way
 * an invoice gets paid; the acting user is always the authenticated principal.
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

    @PreAuthorize("hasAuthority('payment:write')")
    @PostMapping
    public ResponseEntity<PaymentWebResponse> createPayment(
            @Valid @RequestBody CreatePaymentWebRequest webRequest,
            Authentication authentication) {
        CreatePaymentInput request = webMapper.toAppCreateRequest(webRequest);
        request.setUserId(principal(authentication));
        PaymentOutput response = paymentUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('payment:read')")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentWebResponse> getPaymentById(@PathVariable UUID id) {
        return ResponseEntity.ok(webMapper.toWebResponse(paymentUseCase.getById(id)));
    }

    @PreAuthorize("hasAuthority('payment:read')")
    @GetMapping("/code/{code}")
    public ResponseEntity<PaymentWebResponse> getPaymentByCode(@PathVariable String code) {
        return ResponseEntity.ok(webMapper.toWebResponse(paymentUseCase.getByCode(code)));
    }

    @PreAuthorize("hasAuthority('payment:read')")
    @GetMapping
    public ResponseEntity<List<PaymentWebResponse>> getAllPayments() {
        return ResponseEntity.ok(webMapper.toWebResponseList(paymentUseCase.getAll()));
    }

    @PreAuthorize("hasAuthority('payment:write')")
    @PutMapping("/{id}")
    public ResponseEntity<PaymentWebResponse> updatePayment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePaymentWebRequest webRequest,
            Authentication authentication) {
        UpdatePaymentInput request = webMapper.toAppUpdateRequest(webRequest);
        request.setUserId(principal(authentication));
        return ResponseEntity.ok(webMapper.toWebResponse(paymentUseCase.update(id, request)));
    }

    @PreAuthorize("hasAuthority('payment:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable UUID id) {
        paymentUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // QUERY ENDPOINTS
    // ============================================================

    @PreAuthorize("hasAuthority('payment:read')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentWebResponse>> getPaymentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(webMapper.toWebResponseList(paymentUseCase.getByStatus(status)));
    }

    @PreAuthorize("hasAuthority('payment:read')")
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PaymentWebResponse>> getPaymentsBySupplierId(@PathVariable String supplierId) {
        return ResponseEntity.ok(webMapper.toWebResponseList(paymentUseCase.getBySupplierId(supplierId)));
    }

    @PreAuthorize("hasAuthority('payment:read')")
    @GetMapping("/search/keyword")
    public ResponseEntity<List<PaymentWebResponse>> searchPaymentsByKeyword(@RequestParam String keyword) {
        return ResponseEntity.ok(webMapper.toWebResponseList(paymentUseCase.searchByKeyword(keyword)));
    }

    // ============================================================
    // LIFECYCLE ENDPOINTS
    // ============================================================

    @PreAuthorize("hasAuthority('payment:write')")
    @PatchMapping("/{id}/prepare")
    public ResponseEntity<PaymentWebResponse> preparePayment(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(webMapper.toWebResponse(paymentUseCase.prepare(id, principal(authentication))));
    }

    @PreAuthorize("hasAuthority('payment:write')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<PaymentWebResponse> completePayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentCompleteWebRequest webRequest,
            Authentication authentication) {
        PaymentOutput response = paymentUseCase.complete(
                id,
                principal(authentication),
                webRequest.getBankReference(),
                webRequest.getTransactionId(),
                webRequest.getPaymentMethod());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('payment:write')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PaymentWebResponse> cancelPayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentCancelWebRequest webRequest,
            Authentication authentication) {
        return ResponseEntity.ok(webMapper.toWebResponse(
                paymentUseCase.cancel(id, principal(authentication), webRequest.getReason())));
    }

    private static String principal(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("An authenticated user is required");
        }
        return authentication.getName();
    }
}
