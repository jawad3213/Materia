package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.masterdata.application.dtos.supplier.CreateSupplierInput;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.SupplierOutput;
import com.materia.backend.contexts.masterdata.domain.ports.in.SupplierUseCase;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.supplier.CreateSupplierWebRequest;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.supplier.SupplierWebResponse;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.mappers.SupplierWebMapper;
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
    public ResponseEntity<List<SupplierWebResponse>> getAllSuppliers() {
        List<SupplierOutput> responses = supplierUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierWebResponse> updateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSupplierWebRequest webRequest) {
        CreateSupplierInput request = webMapper.toAppCreateRequest(webRequest);
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
}
