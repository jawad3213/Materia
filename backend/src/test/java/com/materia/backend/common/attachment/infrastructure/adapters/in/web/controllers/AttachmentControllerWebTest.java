package com.materia.backend.common.attachment.infrastructure.adapters.in.web.controllers;

import com.materia.backend.common.attachment.application.dtos.AttachmentContent;
import com.materia.backend.common.attachment.domain.entities.Attachment;
import com.materia.backend.common.attachment.domain.enums.AttachmentCategory;
import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import com.materia.backend.common.attachment.domain.exceptions.AttachmentAccessDeniedException;
import com.materia.backend.common.attachment.domain.ports.in.AttachmentUseCase;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The attachment HTTP contract: per-document-type permissions, upload, download headers and removal. */
@WebMvcTest(AttachmentController.class)
class AttachmentControllerWebTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/attachments";

    @MockBean
    private AttachmentUseCase useCase;

    private final UUID ownerId = UUID.randomUUID();

    private Attachment attachment(AttachmentOwnerType type) {
        return new Attachment(UUID.randomUUID(), type, ownerId, AttachmentCategory.SUPPLIER_INVOICE, "facture mars.pdf",
                "application/pdf", 9, "key", "user", "Jane Buyer", LocalDateTime.of(2026, 10, 6, 9, 30));
    }

    @Test
    @DisplayName("list: a user with the document's read permission sees its files, without the storage key")
    @WithMockUser(authorities = "invoice:read")
    void list_withReadPermission() throws Exception {
        when(useCase.list(AttachmentOwnerType.INVOICE, ownerId)).thenReturn(List.of(attachment(AttachmentOwnerType.INVOICE)));

        mockMvc.perform(get(BASE).param("ownerType", "INVOICE").param("ownerId", ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("facture mars.pdf"))
                .andExpect(jsonPath("$[0].category").value("SUPPLIER_INVOICE"))
                .andExpect(jsonPath("$[0].uploadedByName").value("Jane Buyer"))
                .andExpect(jsonPath("$[0].storageKey").doesNotExist());
    }

    @Test
    @DisplayName("list: read permission on another document type is not enough")
    @WithMockUser(authorities = "receipt:read")
    void list_withoutReadPermission_isForbidden() throws Exception {
        mockMvc.perform(get(BASE).param("ownerType", "INVOICE").param("ownerId", ownerId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("list: an anonymous caller is refused")
    void list_anonymous_isUnauthorized() throws Exception {
        mockMvc.perform(get(BASE).param("ownerType", "INVOICE").param("ownerId", ownerId.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("upload: a receiver can attach a delivery note to a goods receipt; the actor is the signed-in user")
    @WithMockUser(username = "receiver-1", authorities = "receipt:write")
    void upload_withWritePermission() throws Exception {
        when(useCase.upload(any())).thenReturn(attachment(AttachmentOwnerType.GOODS_RECEIPT));
        var file = new MockMultipartFile("file", "bl.pdf", "application/pdf", "%PDF-1.7".getBytes());

        mockMvc.perform(multipart(BASE).file(file)
                        .param("ownerType", "GOODS_RECEIPT")
                        .param("ownerId", ownerId.toString())
                        .param("category", "DELIVERY_NOTE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerType").value("GOODS_RECEIPT"));

        verify(useCase).upload(org.mockito.ArgumentMatchers.argThat(in ->
                in.actorId().equals("receiver-1") && in.category() == AttachmentCategory.DELIVERY_NOTE
                        && in.fileName().equals("bl.pdf")));
    }

    @Test
    @DisplayName("upload: read permission alone cannot add a file")
    @WithMockUser(authorities = "receipt:read")
    void upload_withoutWritePermission_isForbidden() throws Exception {
        var file = new MockMultipartFile("file", "bl.pdf", "application/pdf", "%PDF-1.7".getBytes());

        mockMvc.perform(multipart(BASE).file(file).param("ownerType", "GOODS_RECEIPT").param("ownerId", ownerId.toString()))
                .andExpect(status().isForbidden());
        verify(useCase, never()).upload(any());
    }

    @Test
    @DisplayName("content: the file is sent with its stored type, a UTF-8 file name and nosniff")
    @WithMockUser(authorities = "invoice:read")
    void content_returnsFileWithSafeHeaders() throws Exception {
        Attachment a = attachment(AttachmentOwnerType.INVOICE);
        when(useCase.get(a.id())).thenReturn(a);
        when(useCase.content(a.id())).thenReturn(new AttachmentContent(a, "%PDF-1.7".getBytes()));

        mockMvc.perform(get(BASE + "/" + a.id() + "/content"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString("filename*=UTF-8''facture%20mars.pdf")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes("%PDF-1.7".getBytes()));
    }

    @Test
    @DisplayName("content: a user who cannot read the document cannot download its files")
    @WithMockUser(authorities = "order:read")
    void content_withoutReadPermission_isForbidden() throws Exception {
        Attachment a = attachment(AttachmentOwnerType.INVOICE);
        when(useCase.get(a.id())).thenReturn(a);

        mockMvc.perform(get(BASE + "/" + a.id() + "/content")).andExpect(status().isForbidden());
        verify(useCase, never()).content(any());
    }

    @Test
    @DisplayName("delete: the actor and whether they are an administrator are passed to the use case")
    @WithMockUser(username = "admin-1", authorities = {"ROLE_ADMIN", "return:write"})
    void delete_passesActorAndAdminFlag() throws Exception {
        Attachment a = attachment(AttachmentOwnerType.RETURN_TO_VENDOR);
        when(useCase.get(a.id())).thenReturn(a);

        mockMvc.perform(delete(BASE + "/" + a.id())).andExpect(status().isNoContent());
        verify(useCase).delete(a.id(), "admin-1", true);
    }

    @Test
    @DisplayName("delete: removing someone else's file is answered with 403")
    @WithMockUser(username = "user-2", authorities = "return:write")
    void delete_someoneElsesFile_isForbidden() throws Exception {
        Attachment a = attachment(AttachmentOwnerType.RETURN_TO_VENDOR);
        when(useCase.get(a.id())).thenReturn(a);
        doThrow(new AttachmentAccessDeniedException("not yours")).when(useCase).delete(eq(a.id()), anyString(), anyBoolean());

        mockMvc.perform(delete(BASE + "/" + a.id()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("not yours"));
    }
}
