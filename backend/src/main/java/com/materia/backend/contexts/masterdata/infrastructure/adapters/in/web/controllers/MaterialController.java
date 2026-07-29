package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.masterdata.application.dtos.material.requests.CreateMaterialRequest;
import com.materia.backend.contexts.masterdata.application.dtos.material.requests.UpdateMaterialRequest;
import com.materia.backend.contexts.masterdata.application.dtos.material.responses.MaterialResponseDto;
import com.materia.backend.contexts.masterdata.domain.ports.in.MaterialUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/masterdata/materials")
public class MaterialController {

    private final MaterialUseCase materialUseCase;

    public MaterialController(MaterialUseCase materialUseCase) {
        this.materialUseCase = materialUseCase;
    }

    @PostMapping
    public ResponseEntity<MaterialResponseDto> createMaterial(@RequestBody CreateMaterialRequest request) {
        MaterialResponseDto response = materialUseCase.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponseDto> getMaterial(@PathVariable UUID id) {
        MaterialResponseDto response = materialUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<MaterialResponseDto> getMaterialByCode(@PathVariable String code) {
        MaterialResponseDto response = materialUseCase.getByCode(code);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MaterialResponseDto>> getAllMaterials() {
        List<MaterialResponseDto> responses = materialUseCase.getAll();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialResponseDto> updateMaterial(
            @PathVariable UUID id,
            @RequestBody CreateMaterialRequest request) { // Using CreateMaterialRequest as per the interface signature
        MaterialResponseDto response = materialUseCase.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable UUID id) {
        materialUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Custom Endpoints ----

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MaterialResponseDto>> getMaterialsByCategory(@PathVariable UUID categoryId) {
        List<MaterialResponseDto> responses = materialUseCase.getMaterialsByCategory(categoryId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<MaterialResponseDto>> getMaterialsBySupplier(@PathVariable UUID supplierId) {
        List<MaterialResponseDto> responses = materialUseCase.getMaterialsBySupplier(supplierId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/alerts/low-stock")
    public ResponseEntity<List<MaterialResponseDto>> getMaterialsBelowMinimumStock() {
        List<MaterialResponseDto> responses = materialUseCase.getMaterialsBelowMinimumStock();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/stock/increase")
    public ResponseEntity<MaterialResponseDto> increaseStock(
            @PathVariable UUID id,
            @RequestParam int quantity) {
        MaterialResponseDto response = materialUseCase.increaseStock(id, quantity);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/stock/decrease")
    public ResponseEntity<MaterialResponseDto> decreaseStock(
            @PathVariable UUID id,
            @RequestParam int quantity) {
        MaterialResponseDto response = materialUseCase.decreaseStock(id, quantity);
        return ResponseEntity.ok(response);
    }
}
