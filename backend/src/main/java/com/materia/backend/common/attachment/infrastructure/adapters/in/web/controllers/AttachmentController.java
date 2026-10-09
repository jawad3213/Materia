package com.materia.backend.common.attachment.infrastructure.adapters.in.web.controllers;

import com.materia.backend.common.attachment.application.dtos.AttachmentContent;
import com.materia.backend.common.attachment.application.dtos.UploadAttachmentInput;
import com.materia.backend.common.attachment.domain.enums.AttachmentCategory;
import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import com.materia.backend.common.attachment.domain.ports.in.AttachmentUseCase;
import com.materia.backend.common.attachment.infrastructure.adapters.in.web.dtos.AttachmentWebResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Files attached to purchase orders, goods receipts, invoices and returns. Each call needs the read or write
 * permission of the document type the file belongs to (see AttachmentOwnerType), so the check is made here
 * rather than in a fixed {@code @PreAuthorize}.
 */
@RestController
@RequestMapping("/api/v1/attachments")
public class AttachmentController {

    private final AttachmentUseCase useCase;

    public AttachmentController(AttachmentUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttachmentWebResponse>> list(@RequestParam AttachmentOwnerType ownerType,
                                                            @RequestParam UUID ownerId,
                                                            Authentication authentication) {
        require(authentication, ownerType.getReadPermission());
        return ResponseEntity.ok(useCase.list(ownerType, ownerId).stream().map(AttachmentWebResponse::from).toList());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttachmentWebResponse> upload(@RequestParam AttachmentOwnerType ownerType,
                                                        @RequestParam UUID ownerId,
                                                        @RequestParam(required = false) AttachmentCategory category,
                                                        @RequestParam("file") MultipartFile file,
                                                        Authentication authentication) throws IOException {
        require(authentication, ownerType.getWritePermission());
        var input = new UploadAttachmentInput(ownerType, ownerId, category, file.getOriginalFilename(), file.getBytes(),
                principal(authentication));
        return new ResponseEntity<>(AttachmentWebResponse.from(useCase.upload(input)), HttpStatus.CREATED);
    }

    /** The file itself; {@code inline=true} lets the browser show it in place instead of saving it. */
    @GetMapping("/{id}/content")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> content(@PathVariable UUID id,
                                          @RequestParam(defaultValue = "false") boolean inline,
                                          Authentication authentication) {
        require(authentication, useCase.get(id).ownerType().getReadPermission());
        AttachmentContent content = useCase.content(id);
        ContentDisposition disposition = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(content.attachment().fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.attachment().contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore().cachePrivate())
                .body(content.bytes());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        require(authentication, useCase.get(id).ownerType().getWritePermission());
        useCase.delete(id, principal(authentication), hasAuthority(authentication, "ROLE_ADMIN"));
        return ResponseEntity.noContent().build();
    }

    private static void require(Authentication authentication, String permission) {
        if (!hasAuthority(authentication, permission)) {
            throw new AccessDeniedException("Missing permission " + permission);
        }
    }

    private static boolean hasAuthority(Authentication authentication, String authority) {
        return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    private static String principal(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("An authenticated user is required");
        }
        return authentication.getName();
    }
}
