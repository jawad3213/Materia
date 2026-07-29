package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.masterdata.application.dtos.material.CreateMaterialInput;
import com.materia.backend.contexts.masterdata.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterdata.domain.ports.in.MaterialUseCase;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.material.CreateMaterialWebRequest;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.material.MaterialWebResponse;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.mappers.MaterialWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/masterdata/materials")
public class MaterialController {

    private final MaterialUseCase materialUseCase;
    private final MaterialWebMapper webMapper;

    public MaterialController(MaterialUseCase materialUseCase, MaterialWebMapper webMapper) {
        this.materialUseCase = materialUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping
    public ResponseEntity<MaterialWebResponse> createMaterial(@Valid @RequestBody CreateMaterialWebRequest webRequest) {
        CreateMaterialInput request = webMapper.toAppCreateRequest(webRequest);
        MaterialOutput response = materialUseCase.create(request);
        return new ResponseEntity<>(webMapper.toWebResponse(response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialWebResponse> getMaterial(@PathVariable UUID id) {
        MaterialOutput response = materialUseCase.getById(id);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<MaterialWebResponse> getMaterialByCode(@PathVariable String code) {
        MaterialOutput response = materialUseCase.getByCode(code);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @GetMapping
    public ResponseEntity<List<MaterialWebResponse>> getAllMaterials() {
        List<MaterialOutput> responses = materialUseCase.getAll();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialWebResponse> updateMaterial(
            @PathVariable UUID id,
            @Valid @RequestBody CreateMaterialWebRequest webRequest) { // Using CreateMaterialWebRequest as per interface
        CreateMaterialInput request = webMapper.toAppCreateRequest(webRequest);
        MaterialOutput response = materialUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable UUID id) {
        materialUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Custom Endpoints ----

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MaterialWebResponse>> getMaterialsByCategory(@PathVariable UUID categoryId) {
        List<MaterialOutput> responses = materialUseCase.getMaterialsByCategory(categoryId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<MaterialWebResponse>> getMaterialsBySupplier(@PathVariable UUID supplierId) {
        List<MaterialOutput> responses = materialUseCase.getMaterialsBySupplier(supplierId);
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/alerts/low-stock")
    public ResponseEntity<List<MaterialWebResponse>> getMaterialsBelowMinimumStock() {
        List<MaterialOutput> responses = materialUseCase.getMaterialsBelowMinimumStock();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @PatchMapping("/{id}/stock/increase")
    public ResponseEntity<MaterialWebResponse> increaseStock(
            @PathVariable UUID id,
            @RequestParam int quantity) {
        MaterialOutput response = materialUseCase.increaseStock(id, quantity);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @PatchMapping("/{id}/stock/decrease")
    public ResponseEntity<MaterialWebResponse> decreaseStock(
            @PathVariable UUID id,
            @RequestParam int quantity) {
        MaterialOutput response = materialUseCase.decreaseStock(id, quantity);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }
}
