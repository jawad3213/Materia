package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.ports.in.ReturnToVendorUseCase;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.CancelReturnToVendorWebRequest;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.CreateReturnToVendorWebRequest;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.ResolveReturnToVendorWebRequest;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.ReturnToVendorWebResponse;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.UpdateReturnToVendorWebRequest;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.mappers.ReturnToVendorWebMapper;
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
 * Returns to vendor. Permissions follow Role.java ({@code return:read} / {@code return:write}); the acting user
 * is always the authenticated principal, never a request parameter.
 */
@RestController
@RequestMapping("/api/v1/return-to-vendors")
public class ReturnToVendorController {

    private final ReturnToVendorUseCase useCase;
    private final ReturnToVendorWebMapper webMapper;

    public ReturnToVendorController(ReturnToVendorUseCase useCase, ReturnToVendorWebMapper webMapper) {
        this.useCase = useCase;
        this.webMapper = webMapper;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('return:write')")
    public ResponseEntity<ReturnToVendorWebResponse> create(@Valid @RequestBody CreateReturnToVendorWebRequest request,
                                                            Authentication authentication) {
        var output = useCase.create(webMapper.toCreateInput(request, principal(authentication)));
        return new ResponseEntity<>(webMapper.toWebResponse(output), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('return:read')")
    public ResponseEntity<ReturnToVendorWebResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(webMapper.toWebResponse(useCase.getById(id)));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('return:read')")
    public ResponseEntity<ReturnToVendorWebResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(webMapper.toWebResponse(useCase.getByCode(code)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('return:read')")
    public ResponseEntity<List<ReturnToVendorWebResponse>> getAll() {
        return ResponseEntity.ok(webMapper.toWebResponseList(useCase.getAll()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('return:write')")
    public ResponseEntity<ReturnToVendorWebResponse> update(@PathVariable UUID id,
                                                            @Valid @RequestBody UpdateReturnToVendorWebRequest request,
                                                            Authentication authentication) {
        var output = useCase.update(id, webMapper.toUpdateInput(request, principal(authentication)));
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('return:write')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        useCase.delete(id, principal(authentication));
        return ResponseEntity.noContent().build();
    }

    // ---- Queries ----

    @GetMapping("/goods-receipt/{goodsReceiptId}")
    @PreAuthorize("hasAuthority('return:read')")
    public ResponseEntity<List<ReturnToVendorWebResponse>> getByGoodsReceiptId(@PathVariable String goodsReceiptId) {
        return ResponseEntity.ok(webMapper.toWebResponseList(useCase.getByGoodsReceiptId(goodsReceiptId)));
    }

    @GetMapping("/purchase-order/{purchaseOrderId}")
    @PreAuthorize("hasAuthority('return:read')")
    public ResponseEntity<List<ReturnToVendorWebResponse>> getByPurchaseOrderId(@PathVariable String purchaseOrderId) {
        return ResponseEntity.ok(webMapper.toWebResponseList(useCase.getByPurchaseOrderId(purchaseOrderId)));
    }

    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAuthority('return:read')")
    public ResponseEntity<List<ReturnToVendorWebResponse>> getBySupplierId(@PathVariable String supplierId) {
        return ResponseEntity.ok(webMapper.toWebResponseList(useCase.getBySupplierId(supplierId)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('return:read')")
    public ResponseEntity<List<ReturnToVendorWebResponse>> getByStatus(@PathVariable ReturnStatus status) {
        return ResponseEntity.ok(webMapper.toWebResponseList(useCase.getByStatus(status)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('return:read')")
    public ResponseEntity<List<ReturnToVendorWebResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(webMapper.toWebResponseList(useCase.search(keyword)));
    }

    // ---- Status transitions ----

    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('return:write')")
    public ResponseEntity<ReturnToVendorWebResponse> submit(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(webMapper.toWebResponse(useCase.submit(id, principal(authentication))));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('return:write')")
    public ResponseEntity<ReturnToVendorWebResponse> resolve(@PathVariable UUID id,
                                                             @Valid @RequestBody ResolveReturnToVendorWebRequest request,
                                                             Authentication authentication) {
        var output = useCase.resolve(id, principal(authentication), request.resolutionType(), request.reference(),
                request.supplierResponse());
        return ResponseEntity.ok(webMapper.toWebResponse(output));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('return:write')")
    public ResponseEntity<ReturnToVendorWebResponse> cancel(@PathVariable UUID id,
                                                            @Valid @RequestBody CancelReturnToVendorWebRequest request,
                                                            Authentication authentication) {
        return ResponseEntity.ok(webMapper.toWebResponse(useCase.cancel(id, principal(authentication), request.reason())));
    }

    private static String principal(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("An authenticated user is required");
        }
        return authentication.getName();
    }
}
