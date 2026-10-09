package com.materia.backend.common.audit.infrastructure.web;

import com.materia.backend.common.audit.application.AuditLogReader;
import com.materia.backend.common.audit.application.AuditLogReader.AuditEntry;
import com.materia.backend.common.audit.application.AuditLogReader.FieldChange;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The history HTTP contract: a record's history needs the permission that reads the record; the feed needs log:read. */
@WebMvcTest(AuditController.class)
class AuditControllerWebTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/audit";

    @MockBean
    private AuditLogReader reader;

    private final UUID orderId = UUID.randomUUID();

    private AuditEntry cancelled() {
        return new AuditEntry(UUID.randomUUID(), LocalDateTime.of(2026, 10, 6, 9, 30), "user-1", "Nadia Tazi",
                "PurchaseOrder", orderId, null, null, "STATUS_CHANGE", "Status SUBMITTED → CANCELLED",
                "Supplier closed for inventory", Map.of("status", new FieldChange("SUBMITTED", "CANCELLED")));
    }

    @Test
    @DisplayName("history: a user who may read orders sees what changed on one, by whom and why")
    @WithMockUser(authorities = "order:read")
    void history_withReadPermission() throws Exception {
        when(reader.historyOf(eq("PurchaseOrder"), eq(orderId), anyInt())).thenReturn(List.of(cancelled()));

        mockMvc.perform(get(BASE + "/PurchaseOrder/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].actorName").value("Nadia Tazi"))
                .andExpect(jsonPath("$[0].action").value("STATUS_CHANGE"))
                .andExpect(jsonPath("$[0].reason").value("Supplier closed for inventory"))
                .andExpect(jsonPath("$[0].changes.status.from").value("SUBMITTED"))
                .andExpect(jsonPath("$[0].changes.status.to").value("CANCELLED"));
    }

    @Test
    @DisplayName("history: read permission on another kind of record is not enough")
    @WithMockUser(authorities = "receipt:read")
    void history_withoutReadPermission_isForbidden() throws Exception {
        mockMvc.perform(get(BASE + "/PurchaseOrder/" + orderId)).andExpect(status().isForbidden());
        verifyNoInteractions(reader);
    }

    @Test
    @DisplayName("history: an auditor (log:read) may read the history of any record")
    @WithMockUser(authorities = "log:read")
    void history_auditor() throws Exception {
        when(reader.historyOf(eq("Invoice"), any(), anyInt())).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/Invoice/" + UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("history: an unknown kind of record is a bad request")
    @WithMockUser(authorities = "log:read")
    void history_unknownType() throws Exception {
        mockMvc.perform(get(BASE + "/Spaceship/" + UUID.randomUUID())).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("history: an anonymous caller is refused")
    void history_anonymous_isUnauthorized() throws Exception {
        mockMvc.perform(get(BASE + "/PurchaseOrder/" + orderId)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("feed: the latest changes across the platform need log:read, and the limit is capped")
    @WithMockUser(authorities = "log:read")
    void feed_withLogRead() throws Exception {
        when(reader.recent(any(), any(), anyInt())).thenReturn(List.of(cancelled()));

        mockMvc.perform(get(BASE).param("limit", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].summary").value("Status SUBMITTED → CANCELLED"));
        verify(reader).recent(isNull(), isNull(), eq(500));
    }

    @Test
    @DisplayName("feed: reading a document is not enough to read everyone's changes")
    @WithMockUser(authorities = "order:read")
    void feed_withoutLogRead_isForbidden() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }
}
