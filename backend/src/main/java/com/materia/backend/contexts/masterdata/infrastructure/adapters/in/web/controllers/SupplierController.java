package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.masterData.application.dtos.supplier.CreateSupplierInput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierOutput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.UpdateSupplierInput;
import com.materia.backend.contexts.masterData.domain.ports.in.SupplierUseCase;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.CreateSupplierWebRequest;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierWebResponse;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.UpdateSupplierWebRequest;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.mappers.SupplierWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/masterdata/suppliers")
public class SupplierController {

    private final SupplierUseCase supplierUseCase;
    private final SupplierWebMapper webMapper;

    public SupplierController(SupplierUseCase supplierUseCase, SupplierWebMapper webMapper) {
        this.supplierUseCase = supplierUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping
    public ResponseEntity<SupplierWebResponse> createSupplier(@Valid @RequestBody CreateSupplierWebRequest webRequest) {
        CreateSupplierInput request = webMapper.toAppCreateRequest(webRequest);
        SupplierOutput response = supplierUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierWebResponse> getSupplier(@PathVariable UUID id) {
        SupplierOutput response = supplierUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<SupplierWebResponse> getSupplierByCode(@PathVariable String code) {
        SupplierOutput response = supplierUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping
    public ResponseEntity<List<SupplierWebResponse>> getAllSuppliers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String currencyCode) {
        if (status == null && country == null && currencyCode == null) {
            List<SupplierOutput> responses = supplierUseCase.getAll();
            return ResponseEntity.ok(webMapper.toWebResponseList(responses));
        }

        com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierFilterCriteria criteria =
                new com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierFilterCriteria();
        criteria.setStatus(status);
        criteria.setCountry(country);
        criteria.setCurrencyCode(currencyCode);

        // Fetch filtered results
        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierListOutput> pageResult =
                supplierUseCase.filterList(criteria, 0, Integer.MAX_VALUE);

        // Map each item back to SupplierWebResponse for backward compatibility
        List<SupplierWebResponse> responses = pageResult.getContent().stream().map(item -> {
            SupplierOutput output = supplierUseCase.getById(item.getId());
            return webMapper.toWebResponse(output);
        }).toList();

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierWebResponse> updateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplierWebRequest webRequest) {
        UpdateSupplierInput request = webMapper.toAppUpdateRequest(webRequest);
        SupplierOutput response = supplierUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        supplierUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Custom Endpoints ----

    @GetMapping("/search")
    public ResponseEntity<List<SupplierWebResponse>> searchSuppliers(@RequestParam String keyword) {
        List<SupplierOutput> responses = supplierUseCase.searchSuppliers(keyword);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/list")
    public ResponseEntity<com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierListWebResponse>> getAllSuppliersList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierListOutput> appPage = supplierUseCase.getAllList(page, size);

        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierListWebResponse> webPage = new com.materia.backend.common.application.PageResponse<>(
                webMapper.toWebListResponseList(appPage.getContent()),
                appPage.getPageNumber(),
                appPage.getPageSize(),
                appPage.getTotalElements(),
                appPage.getTotalPages(),
                appPage.isLast()
        );
        return ResponseEntity.ok(webPage);
    }

    @PostMapping("/filter/list")
    public ResponseEntity<com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierListWebResponse>> filterList(
            @RequestBody com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierFilterWebRequest webRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierFilterCriteria criteria = webMapper.toAppFilterCriteria(webRequest);
        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierListOutput> appPage = supplierUseCase.filterList(criteria, page, size);

        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierListWebResponse> webPage = new com.materia.backend.common.application.PageResponse<>(
                webMapper.toWebListResponseList(appPage.getContent()),
                appPage.getPageNumber(),
                appPage.getPageSize(),
                appPage.getTotalElements(),
                appPage.getTotalPages(),
                appPage.isLast()
        );

        return ResponseEntity.ok(webPage);
    }

    @PostMapping("/search/list")
    public ResponseEntity<com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierWebResponse>> searchAdvancedList(
            @RequestBody com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierSearchWebRequest webRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierSearchCriteria criteria = webMapper.toAppSearchCriteria(webRequest);
        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierOutput> appPage = supplierUseCase.searchAdvancedList(criteria, page, size);

        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierWebResponse> webPage = new com.materia.backend.common.application.PageResponse<>(
                webMapper.toWebResponseList(appPage.getContent()),
                appPage.getPageNumber(),
                appPage.getPageSize(),
                appPage.getTotalElements(),
                appPage.getTotalPages(),
                appPage.isLast()
        );

        return ResponseEntity.ok(webPage);
    }
}
