package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.invoice.application.dtos.CreateInvoiceInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.application.dtos.UpdateInvoiceInput;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceUseCase;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.CreateInvoiceWebRequest;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.InvoiceCancelWebRequest;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.InvoiceWebResponse;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.UpdateInvoiceWebRequest;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.mappers.InvoiceWebMapper;
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
 * REST controller for invoices. Permissions follow Role.java: purchasers record and submit invoices
 * ({@code invoice:write}) and administrators verify them ({@code invoice:validate}). Invoices are paid only
 * through the payments module, which records each payment on the invoice.
 * The acting user is always the authenticated principal, and names shown on the invoice come from that
 * user's account; user ids and names sent in request bodies are ignored.
 */
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceUseCase invoiceUseCase;
    private final InvoiceWebMapper webMapper;

    public InvoiceController(InvoiceUseCase invoiceUseCase, InvoiceWebMapper webMapper) {
        this.invoiceUseCase = invoiceUseCase;
        this.webMapper = webMapper;
    }

    // ============================================================
    // CRUD ENDPOINTS
    // ============================================================

    @PreAuthorize("hasAuthority('invoice:write')")
    @PostMapping
    public ResponseEntity<InvoiceWebResponse> createInvoice(
            @Valid @RequestBody CreateInvoiceWebRequest webRequest,
            Authentication authentication) {
        webRequest.setCreatedBy(principal(authentication));
        CreateInvoiceInput request = webMapper.toAppCreateRequest(webRequest);
        InvoiceOutput response = invoiceUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceWebResponse> getInvoiceById(@PathVariable UUID id) {
        InvoiceOutput response = invoiceUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/code/{code}")
    public ResponseEntity<InvoiceWebResponse> getInvoiceByCode(@PathVariable String code) {
        InvoiceOutput response = invoiceUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping
    public ResponseEntity<List<InvoiceWebResponse>> getAllInvoices() {
        List<InvoiceOutput> responses = invoiceUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('invoice:write')")
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceWebResponse> updateInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvoiceWebRequest webRequest,
            Authentication authentication) {
        webRequest.setUpdatedBy(principal(authentication));
        UpdateInvoiceInput request = webMapper.toAppUpdateRequest(webRequest);
        InvoiceOutput response = invoiceUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('invoice:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id) {
        invoiceUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // QUERY ENDPOINTS
    // ============================================================

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceWebResponse>> getInvoicesByStatus(@PathVariable String status) {
        List<InvoiceOutput> responses = invoiceUseCase.getByStatus(status);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<InvoiceWebResponse>> getInvoicesBySupplierId(@PathVariable String supplierId) {
        List<InvoiceOutput> responses = invoiceUseCase.getBySupplierId(supplierId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/purchase-order/{purchaseOrderId}")
    public ResponseEntity<List<InvoiceWebResponse>> getInvoicesByPurchaseOrderId(
            @PathVariable String purchaseOrderId) {
        List<InvoiceOutput> responses = invoiceUseCase.getByPurchaseOrderId(purchaseOrderId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/search/keyword")
    public ResponseEntity<List<InvoiceWebResponse>> searchInvoicesByKeyword(@RequestParam String keyword) {
        List<InvoiceOutput> responses = invoiceUseCase.searchByKeyword(keyword);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    // ============================================================
    // LIFECYCLE ENDPOINTS
    // ============================================================

    @PreAuthorize("hasAuthority('invoice:write')")
    @PatchMapping("/{id}/submit")
    public ResponseEntity<InvoiceWebResponse> submitInvoice(@PathVariable UUID id, Authentication authentication) {
        InvoiceOutput response = invoiceUseCase.submit(id, principal(authentication));
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PreAuthorize("hasAuthority('invoice:validate')")
    @PatchMapping("/{id}/verify")
    public ResponseEntity<InvoiceWebResponse> verifyInvoice(
            @PathVariable UUID id,
            Authentication authentication) {
        InvoiceOutput response = invoiceUseCase.verify(id, principal(authentication));
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    /** Draft and submitted invoices are cancelled by their recorders; a verified one needs a verifier. */
    @PreAuthorize("hasAuthority('invoice:write') or hasAuthority('invoice:validate')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InvoiceWebResponse> cancelInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceCancelWebRequest webRequest,
            Authentication authentication) {
        InvoiceStatus status = invoiceUseCase.getById(id).getStatus();
        String required = status == InvoiceStatus.VERIFIED ? "invoice:validate" : "invoice:write";
        if (!hasAuthority(authentication, required)) {
            throw new AccessDeniedException("Cancelling a " + status + " invoice requires " + required);
        }
        InvoiceOutput response = invoiceUseCase.cancel(id, principal(authentication), webRequest.getReason());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    private static String principal(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("An authenticated user is required");
        }
        return authentication.getName();
    }

    private static boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream().anyMatch(a -> authority.equals(a.getAuthority()));
    }
}
