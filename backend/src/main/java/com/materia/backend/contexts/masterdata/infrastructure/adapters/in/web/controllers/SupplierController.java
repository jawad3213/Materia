package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.masterdata.application.dtos.supplier.requests.CreateSupplierRequest;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.responses.SupplierResponseDto;
import com.materia.backend.contexts.masterdata.domain.ports.in.SupplierUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/masterdata/suppliers")
public class SupplierController {

    private final SupplierUseCase supplierUseCase;

    public SupplierController(SupplierUseCase supplierUseCase) {
        this.supplierUseCase = supplierUseCase;
    }

    @PostMapping
    public ResponseEntity<SupplierResponseDto> createSupplier(@RequestBody CreateSupplierRequest request) {
        SupplierResponseDto response = supplierUseCase.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponseDto> getSupplier(@PathVariable UUID id) {
        SupplierResponseDto response = supplierUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<SupplierResponseDto> getSupplierByCode(@PathVariable String code) {
        SupplierResponseDto response = supplierUseCase.getByCode(code);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponseDto>> getAllSuppliers() {
        List<SupplierResponseDto> responses = supplierUseCase.getAll();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponseDto> updateSupplier(
            @PathVariable UUID id,
            @RequestBody CreateSupplierRequest request) {
        SupplierResponseDto response = supplierUseCase.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        supplierUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Custom Endpoints ----

    @GetMapping("/search")
    public ResponseEntity<List<SupplierResponseDto>> searchSuppliers(@RequestParam String keyword) {
        List<SupplierResponseDto> responses = supplierUseCase.searchSuppliers(keyword);
        return ResponseEntity.ok(responses);
    }
}
