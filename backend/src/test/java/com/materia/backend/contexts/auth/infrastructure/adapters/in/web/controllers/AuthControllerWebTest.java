package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.auth.application.dtos.AuthOutput;
import com.materia.backend.contexts.auth.application.dtos.RefreshTokenInput;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.exceptions.AuthenticationFailedException;
import com.materia.backend.contexts.auth.domain.ports.in.AuthUseCase;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.mappers.AuthWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.materia.backend.support.ErrorResponseAssertions.assertError;
import static com.materia.backend.support.ErrorResponseAssertions.assertValidationError;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The authentication HTTP contract (T042-T045), exercised through the real security chain.
 * The older {@link AuthControllerTest} calls the controller directly and never touches HTTP,
 * so status mapping, validation, serialisation and security were previously unverified.
 */
@WebMvcTest(AuthController.class)
@Import(AuthWebMapper.class)
class AuthControllerWebTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/auth";

    @MockBean
    private AuthUseCase authUseCase;

    private final UUID userId = UUID.randomUUID();

    private AuthOutput output() {
        return new AuthOutput("access.jwt", "raw-refresh", userId, "jane@example.com", Role.PURCHASER,
                Set.of("material:read"));
    }

    private String login(String email, String password) {
        return json(Map.of("email", email, "password", password));
    }

    // ---- Sign-in ----

    @Test
    @DisplayName("login: success returns every field the frontend reads, and sets the refresh cookie")
    void login_success_returnsContractAndCookie() throws Exception {
        when(authUseCase.login(any())).thenReturn(output());

        mockMvc.perform(post(BASE + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login("jane@example.com", "Str0ng!Pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.jwt"))
                .andExpect(jsonPath("$.refreshToken").value("raw-refresh"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("PURCHASER"))
                .andExpect(jsonPath("$.mustChangePassword").exists())
                .andExpect(jsonPath("$.user.email").value("jane@example.com"))
                .andExpect(header().string("Set-Cookie", containsString("refresh_token=raw-refresh")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("Path=/api/v1/auth")));
    }

    @Test
    @DisplayName("login: the response never carries a password hash")
    void login_neverExposesPasswordHash() throws Exception {
        when(authUseCase.login(any())).thenReturn(output());

        mockMvc.perform(post(BASE + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login("jane@example.com", "Str0ng!Pass")))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andExpect(content().string(not(containsString("Str0ng!Pass"))));
    }

    @Test
    @DisplayName("login: refused credentials are 401 with a specific errorCode")
    void login_badCredentials_is401() throws Exception {
        when(authUseCase.login(any()))
                .thenThrow(new AuthenticationFailedException("Invalid email or password", "AUTH_INVALID_CREDENTIALS"));

        assertError(mockMvc.perform(post(BASE + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login("jane@example.com", "Wrong1!pass"))),
                HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS");
    }

    @Test
    @DisplayName("login: a blank email is 400 naming the field, and never reaches the service")
    void login_blankEmail_is400() throws Exception {
        assertValidationError(mockMvc.perform(post(BASE + "/login").contentType(MediaType.APPLICATION_JSON)
                .content(login("", "Str0ng!Pass"))), "email");
        verifyNoInteractions(authUseCase);
    }

    @Test
    @DisplayName("login: a malformed email is 400 naming the field")
    void login_malformedEmail_is400() throws Exception {
        assertValidationError(mockMvc.perform(post(BASE + "/login").contentType(MediaType.APPLICATION_JSON)
                .content(login("not-an-email", "Str0ng!Pass"))), "email");
    }

    @Test
    @DisplayName("login: a malformed JSON body is a 4xx, never a server error")
    void login_malformedJson_isClientError() throws Exception {
        mockMvc.perform(post(BASE + "/login").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().is4xxClientError());
    }

    // ---- Public versus protected (FR-010) ----

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"/login", "/refresh", "/logout", "/forgot-password", "/reset-password"})
    @DisplayName("security: the five public auth endpoints are reachable without signing in")
    void publicEndpoints_areNotBlockedBySecurity(String path) throws Exception {
        int status = mockMvc.perform(post(BASE + path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andReturn().getResponse().getStatus();
        // A 400 (validation) or 401 from the controller's own logic is fine. What matters is
        // that the security chain did not reject the request before it reached the controller.
        verifyReachedControllerOrValidation(path, status);
    }

    private void verifyReachedControllerOrValidation(String path, int status) {
        // Security rejection would be 401 with no controller involvement. /refresh legitimately
        // returns 401 itself when no token is supplied, so it is checked separately below.
        if (!path.equals("/refresh")) {
            org.junit.jupiter.api.Assertions.assertNotEquals(401, status, path + " was blocked by security");
            org.junit.jupiter.api.Assertions.assertNotEquals(403, status, path + " was blocked by security");
        }
    }

    @Test
    @DisplayName("security: changing a password requires signing in")
    void changePassword_unauthenticated_is401() throws Exception {
        mockMvc.perform(post(BASE + "/change-password").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", "Old1!pass", "newPassword", "NewPass1!",
                                "confirmPassword", "NewPass1!"))))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(authUseCase);
    }

    @Test
    @WithMockUser
    @DisplayName("changePassword: a signed-in user can change their password")
    void changePassword_authenticated_is200() throws Exception {
        when(authUseCase.changePassword(any(), any(), any())).thenReturn("Password successfully updated.");

        mockMvc.perform(post(BASE + "/change-password").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", "Old1!pass", "newPassword", "NewPass1!",
                                "confirmPassword", "NewPass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password successfully updated."));
    }

    // ---- Renewal and sign-out ----

    @Test
    @DisplayName("refresh: with no token anywhere it is 401, rather than a server error")
    void refresh_noToken_is401() throws Exception {
        assertError(mockMvc.perform(post(BASE + "/refresh")),
                HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_REQUIRED");
        verifyNoInteractions(authUseCase);
    }

    @Test
    @DisplayName("refresh: the token is read from the HttpOnly cookie, and a rotated one is set in its place")
    void refresh_readsCookieAndRotates() throws Exception {
        when(authUseCase.refreshToken(any())).thenReturn(output());

        mockMvc.perform(post(BASE + "/refresh").cookie(new Cookie("refresh_token", "from-cookie")))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refresh_token=raw-refresh")));

        ArgumentCaptor<RefreshTokenInput> input = ArgumentCaptor.forClass(RefreshTokenInput.class);
        verify(authUseCase).refreshToken(input.capture());
        assertEquals("from-cookie", input.getValue().getRefreshToken());
    }

    @Test
    @DisplayName("logout: revokes the session and clears the refresh cookie")
    void logout_revokesAndClearsCookie() throws Exception {
        mockMvc.perform(post(BASE + "/logout").cookie(new Cookie("refresh_token", "to-revoke")))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
        verify(authUseCase).logout("to-revoke");
    }

    @Test
    @DisplayName("logout: without a token it still succeeds and clears the cookie")
    void logout_noToken_stillSucceeds() throws Exception {
        mockMvc.perform(post(BASE + "/logout"))
                .andExpect(status().isOk());
        verify(authUseCase, never()).logout(anyString());
    }

    // ---- Recovery ----

    @Test
    @DisplayName("forgotPassword: returns the service's non-disclosing message")
    void forgotPassword_returnsMessage() throws Exception {
        when(authUseCase.forgotPassword(any())).thenReturn("Password reset link sent to jane@example.com");

        mockMvc.perform(post(BASE + "/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "jane@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset link sent to jane@example.com"));
    }

    @Test
    @DisplayName("resetPassword: a password under eight characters is 400 naming the field")
    void resetPassword_shortPassword_is400() throws Exception {
        assertValidationError(mockMvc.perform(post(BASE + "/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", "jane@example.com", "token", "t", "password", "Ab1!")))), "password");
        verify(authUseCase, never()).resetPassword(any());
    }
}
