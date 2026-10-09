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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The audit log export: a CSV of the feed for users with log:export. */
@WebMvcTest(AuditController.class)
class AuditExportWebTest extends AbstractWebMvcTest {

    @MockBean
    private AuditLogReader reader;

    @Test
    @DisplayName("export: log:export downloads the filtered feed as CSV, with who, what, the reason and the field changes")
    @WithMockUser(authorities = "log:export")
    void export_downloadsCsv() throws Exception {
        AuditEntry entry = new AuditEntry(UUID.randomUUID(), LocalDateTime.of(2026, 10, 6, 9, 30), "user-1", "Nadia Tazi",
                "Supplier", UUID.randomUUID(), null, null, "UPDATE", "Edited SUP-0001", "Bank details, per supplier letter",
                Map.of("phone", new FieldChange("0522 00 00 00", "0522 11 11 11")));
        when(reader.recent(eq("Supplier"), eq(null), eq(1000))).thenReturn(List.of(entry));

        byte[] body = mockMvc.perform(get("/api/v1/audit/export").param("entityType", "Supplier"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("audit-log-")))
                .andReturn().getResponse().getContentAsByteArray();

        String csv = new String(body, StandardCharsets.UTF_8);
        assertThat(csv).startsWith("﻿Date,User,");
        assertThat(csv).contains("Nadia Tazi").contains("\"Bank details, per supplier letter\"")
                .contains("phone: 0522 00 00 00 -> 0522 11 11 11");
    }

    @Test
    @DisplayName("export: reading the feed (log:read) is not enough to export it, and the size is capped at 5000")
    @WithMockUser(authorities = "log:read")
    void export_needsLogExport() throws Exception {
        mockMvc.perform(get("/api/v1/audit/export")).andExpect(status().isForbidden());
        verifyNoInteractions(reader);
    }

    @Test
    @DisplayName("export: the requested size is capped at 5000 changes")
    @WithMockUser(authorities = "log:export")
    void export_capped() throws Exception {
        when(reader.recent(null, null, 5000)).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/audit/export").param("limit", "999999")).andExpect(status().isOk());
        verify(reader).recent(null, null, 5000);
    }
}
