package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.invoice.application.dtos.CreateInvoiceInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.application.dtos.UpdateInvoiceInput;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceUseCase;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.CreateInvoiceWebRequest;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.InvoiceCancelWebRequest;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.InvoicePayWebRequest;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.InvoiceSubmitWebRequest;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.InvoiceVerifyWebRequest;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.InvoiceWebResponse;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.UpdateInvoiceWebRequest;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.mappers.InvoiceWebMapper;
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
 * REST controller for invoices.
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

    @PostMapping
    public ResponseEntity<InvoiceWebResponse> createInvoice(
            @Valid @RequestBody CreateInvoiceWebRequest webRequest) {
        CreateInvoiceInput request = webMapper.toAppCreateRequest(webRequest);
        InvoiceOutput response = invoiceUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceWebResponse> getInvoiceById(@PathVariable UUID id) {
        InvoiceOutput response = invoiceUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<InvoiceWebResponse> getInvoiceByCode(@PathVariable String code) {
        InvoiceOutput response = invoiceUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceWebResponse>> getAllInvoices() {
        List<InvoiceOutput> responses = invoiceUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceWebResponse> updateInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvoiceWebRequest webRequest) {
        UpdateInvoiceInput request = webMapper.toAppUpdateRequest(webRequest);
        InvoiceOutput response = invoiceUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id) {
        invoiceUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // QUERY ENDPOINTS
    // ============================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceWebResponse>> getInvoicesByStatus(@PathVariable String status) {
        List<InvoiceOutput> responses = invoiceUseCase.getByStatus(status);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<InvoiceWebResponse>> getInvoicesBySupplierId(@PathVariable String supplierId) {
        List<InvoiceOutput> responses = invoiceUseCase.getBySupplierId(supplierId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/purchase-order/{purchaseOrderId}")
    public ResponseEntity<List<InvoiceWebResponse>> getInvoicesByPurchaseOrderId(
            @PathVariable String purchaseOrderId) {
        List<InvoiceOutput> responses = invoiceUseCase.getByPurchaseOrderId(purchaseOrderId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/search/keyword")
    public ResponseEntity<List<InvoiceWebResponse>> searchInvoicesByKeyword(@RequestParam String keyword) {
        List<InvoiceOutput> responses = invoiceUseCase.searchByKeyword(keyword);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    // ============================================================
    // LIFECYCLE ENDPOINTS
    // ============================================================

    @PatchMapping("/{id}/submit")
    public ResponseEntity<InvoiceWebResponse> submitInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceSubmitWebRequest webRequest) {
        InvoiceOutput response = invoiceUseCase.submit(id, webRequest.getUserId());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<InvoiceWebResponse> verifyInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceVerifyWebRequest webRequest) {
        InvoiceOutput response = invoiceUseCase.verify(id, webRequest.getUserId(), webRequest.getUserName());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<InvoiceWebResponse> payInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody InvoicePayWebRequest webRequest) {
        InvoiceOutput response = invoiceUseCase.pay(id, webRequest.getUserId(), webRequest.getUserName(), webRequest.getAmount());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InvoiceWebResponse> cancelInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceCancelWebRequest webRequest) {
        InvoiceOutput response = invoiceUseCase.cancel(id, webRequest.getUserId(), webRequest.getReason());
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }
}
