package com.materia.backend.contexts.settings.infrastructure.adapters.in.web;

import com.materia.backend.contexts.settings.application.dtos.SettingOutput;
import com.materia.backend.contexts.settings.domain.ports.in.SettingsUseCase;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The settings endpoints: setting:read to see them, setting:write to change them. */
@WebMvcTest(SettingsController.class)
class SettingsControllerTest extends AbstractWebMvcTest {

    @MockBean
    private SettingsUseCase settings;

    private static final SettingOutput NAME = new SettingOutput(null, "company.name", "Company", "Company name", "", "TEXT",
            200, "Atlas Industries", "Materia", true);

    @Test
    @DisplayName("access: setting:read lists the settings; without it the list is refused")
    void read() throws Exception {
        when(settings.getAll()).thenReturn(List.of(NAME));

        mockMvc.perform(get("/api/v1/settings").with(user("admin").authorities(() -> "setting:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("Atlas Industries"));
        mockMvc.perform(get("/api/v1/settings").with(user("buyer").authorities(() -> "order:read")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("access: saving needs setting:write and records who saved")
    void write() throws Exception {
        when(settings.update(any(), any())).thenReturn(List.of(NAME));

        mockMvc.perform(put("/api/v1/settings").with(user("admin-1").authorities(() -> "setting:write")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"company.name\":\"Atlas Industries\"}"))
                .andExpect(status().isOk());
        verify(settings).update(eq(Map.of("company.name", "Atlas Industries")), eq("admin-1"));

        mockMvc.perform(put("/api/v1/settings").with(user("viewer").authorities(() -> "setting:read")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"company.name\":\"X\"}"))
                .andExpect(status().isForbidden());
        verifyNoMoreInteractions(settings);
    }
}
