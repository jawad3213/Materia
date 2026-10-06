package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.auth.application.dtos.ProfileOutput;
import com.materia.backend.contexts.auth.application.dtos.UpdateProfileInput;
import com.materia.backend.contexts.auth.domain.ports.in.ProfileUseCase;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The profile endpoints: any signed-in user, always their own account (taken from the token, never the request). */
@WebMvcTest(ProfileController.class)
class ProfileControllerTest extends AbstractWebMvcTest {

    @MockBean
    private ProfileUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    private ProfileOutput profile(String firstName, String lastName, String phone) {
        return new ProfileOutput(userId, "sara@materia.ma", firstName, lastName, firstName + " " + lastName, phone,
                "Procurement", "PURCHASER", "Acheteur", "ACTIVE", false, List.of("order:read"),
                LocalDateTime.of(2026, 9, 1, 9, 0), LocalDateTime.of(2026, 10, 1, 9, 0));
    }

    @Test
    @DisplayName("access: a signed-in user without any permission reads their own profile")
    void getProfile_ownAccount() throws Exception {
        when(useCase.getProfile(userId)).thenReturn(profile("Sara", "Alami", null));

        mockMvc.perform(get("/api/v1/profile").with(user(userId.toString()).authorities(() -> "ROLE_RECEIVER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("sara@materia.ma"))
                .andExpect(jsonPath("$.fullName").value("Sara Alami"))
                .andExpect(jsonPath("$.role").value("PURCHASER"));

        verify(useCase).getProfile(userId);
    }

    @Test
    @DisplayName("access: without a session the profile is refused with 401")
    void getProfile_requiresSession() throws Exception {
        mockMvc.perform(get("/api/v1/profile").with(anonymous()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("rule: an update changes the caller's own name and phone")
    void updateProfile_ownAccount() throws Exception {
        when(useCase.updateProfile(eq(userId), any())).thenReturn(profile("Salma", "Bennani", "0522 11 22 33"));

        mockMvc.perform(put("/api/v1/profile")
                        .with(user(userId.toString()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Salma\",\"lastName\":\"Bennani\",\"phone\":\"0522 11 22 33\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Salma Bennani"));

        verify(useCase).updateProfile(userId, new UpdateProfileInput("Salma", "Bennani", "0522 11 22 33"));
    }

    @Test
    @DisplayName("rule: a missing name or an invalid phone is refused with 400")
    void updateProfile_validates() throws Exception {
        mockMvc.perform(put("/api/v1/profile")
                        .with(user(userId.toString()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"Bennani\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/v1/profile")
                        .with(user(userId.toString()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Salma\",\"lastName\":\"Bennani\",\"phone\":\"call me\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(useCase);
    }
}
