package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.masterData.application.dtos.material.CreateMaterialInput;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterData.application.dtos.material.UpdateMaterialInput;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialUseCase;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.CreateMaterialWebRequest;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialWebResponse;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.UpdateMaterialWebRequest;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.mappers.MaterialWebMapper;
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
    public ResponseEntity<MaterialWebResponse> getMaterialById(@PathVariable UUID id) {
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
    public ResponseEntity<MaterialWebResponse> updateMaterialById(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMaterialWebRequest webRequest) {
        UpdateMaterialInput request = webMapper.toAppUpdateRequest(webRequest);
        MaterialOutput response = materialUseCase.update(id, request);
        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable UUID id) {
        materialUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Custom Endpoints ----


    @GetMapping("/stock/reorder-needed")
    public ResponseEntity<List<MaterialWebResponse>> getMaterialsNeedingReorder() {
        List<MaterialOutput> responses = materialUseCase.getMaterialsNeedingReorder();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/stock/critical")
    public ResponseEntity<List<MaterialWebResponse>> getCriticalMaterials() {
        List<MaterialOutput> responses = materialUseCase.getCriticalMaterials();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping({"/stock/out", "/stock/out-of-stock"})
    public ResponseEntity<List<MaterialWebResponse>> getOutOfStockMaterials() {
        List<MaterialOutput> responses = materialUseCase.getOutOfStockMaterials();
        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }

    @GetMapping("/{id}/reorder-recommendation")
    public ResponseEntity<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.ReorderRecommendationWebResponse> getReorderRecommendation(
            @PathVariable UUID id) {
        var response = materialUseCase.getReorderRecommendation(id);
        return ResponseEntity.ok(webMapper.toReorderRecommendationWebResponse(response));
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.ManualReorderWebResponse> triggerReorder(
            @PathVariable UUID id,
            @RequestBody(required = false) com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.ManualReorderWebRequest request) {
        Integer qty = request != null ? request.getQuantity() : null;
        String reason = request != null ? request.getReason() : null;
        var response = materialUseCase.triggerReorder(id, qty, reason);
        return ResponseEntity.ok(webMapper.toManualReorderWebResponse(response));
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



    @GetMapping("/list")
    public ResponseEntity<com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialListWebResponse>> getAllMaterialsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.application.dtos.material.MaterialListOutput> appPage = materialUseCase.getAllList(page, size);
        
        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialListWebResponse> webPage = new com.materia.backend.common.application.PageResponse<>(
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
    public ResponseEntity<com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialListWebResponse>> searchAdvancedList(
            @RequestBody com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialSearchWebRequest webRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        com.materia.backend.contexts.masterData.application.dtos.material.MaterialSearchCriteria criteria = webMapper.toAppSearchCriteria(webRequest);
        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.application.dtos.material.MaterialListOutput> appPage = materialUseCase.searchAdvancedList(criteria, page, size);
        
        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialListWebResponse> webPage = new com.materia.backend.common.application.PageResponse<>(
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
    public ResponseEntity<com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialListWebResponse>> filterList(
            @RequestBody com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialFilterWebRequest webRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        com.materia.backend.contexts.masterData.application.dtos.material.MaterialFilterCriteria criteria = webMapper.toAppFilterCriteria(webRequest);
        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.application.dtos.material.MaterialListOutput> appPage = materialUseCase.filterList(criteria, page, size);
        
        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialListWebResponse> webPage = new com.materia.backend.common.application.PageResponse<>(
                webMapper.toWebListResponseList(appPage.getContent()),
                appPage.getPageNumber(),
                appPage.getPageSize(),
                appPage.getTotalElements(),
                appPage.getTotalPages(),
                appPage.isLast()
        );
        
        return ResponseEntity.ok(webPage);
    }
}
