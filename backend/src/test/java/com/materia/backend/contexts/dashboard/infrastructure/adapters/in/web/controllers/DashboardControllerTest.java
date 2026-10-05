package com.materia.backend.contexts.dashboard.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput;
import com.materia.backend.contexts.dashboard.domain.ports.in.DashboardUseCase;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The dashboard endpoint: dashboard:read is required and the caller's own permissions shape the content. */
@WebMvcTest(DashboardController.class)
class DashboardControllerTest extends AbstractWebMvcTest {

    @MockBean
    private DashboardUseCase useCase;

    private static DashboardOutput sample() {
        return new DashboardOutput(LocalDateTime.of(2026, 10, 15, 10, 0), "MAD", 0,
                List.of(new DashboardOutput.Kpi("openOrders", "Open Orders", new BigDecimal("1300.00"), "MAD", "3 order(s)", "/purchase-orders", "brand")),
                null, List.of(), List.of(), List.of(), null, List.of(), List.of(), List.of());
    }

    @Test
    @DisplayName("access: a user with dashboard:read gets the overview built from their own permissions")
    void overview_usesCallerPermissions() throws Exception {
        when(useCase.getOverview(any())).thenReturn(sample());

        mockMvc.perform(get("/api/v1/dashboard").with(user("rita").authorities(
                        () -> "dashboard:read", () -> "order:read", () -> "ROLE_RECEIVER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("MAD"))
                .andExpect(jsonPath("$.kpis[0].key").value("openOrders"))
                .andExpect(jsonPath("$.kpis[0].value").value(1300.00));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> permissions = ArgumentCaptor.forClass(Set.class);
        verify(useCase).getOverview(permissions.capture());
        assertEquals(Set.of("dashboard:read", "order:read", "ROLE_RECEIVER"), permissions.getValue());
    }

    @Test
    @DisplayName("access: without dashboard:read the overview is refused with 403, without a session with 401")
    void overview_requiresDashboardRead() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard").with(user("someone").authorities(() -> "order:read")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/dashboard").with(anonymous()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(useCase);
    }
}
