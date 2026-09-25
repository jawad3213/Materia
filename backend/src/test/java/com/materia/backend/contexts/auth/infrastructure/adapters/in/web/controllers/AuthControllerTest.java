package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.auth.application.dtos.AuthOutput;
import com.materia.backend.contexts.auth.application.dtos.ForgotPasswordInput;
import com.materia.backend.contexts.auth.application.dtos.LoginInput;
import com.materia.backend.contexts.auth.application.dtos.RefreshTokenInput;
import com.materia.backend.contexts.auth.application.dtos.ResetPasswordInput;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.ports.in.AuthUseCase;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.ForgotPasswordWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.LoginWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.RefreshTokenWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.ResetPasswordWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.response.AuthWebResponse;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.response.MessageWebResponse;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.mappers.AuthWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthUseCase authUseCase;

    private AuthWebMapper webMapper;
    private AuthController authController;

    private final UUID testUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        webMapper = new AuthWebMapper();
        authController = new AuthController(authUseCase, webMapper);
    }

    @Test
    @DisplayName("login: returns 200 OK with AuthWebResponse and sets HttpOnly refresh_token cookie")
    void login_returnsOk() {
        LoginWebRequest request = new LoginWebRequest("admin@materia.com", "admin123");
        AuthOutput output = new AuthOutput("acc.token", "ref.token", testUserId, "admin@materia.com", Role.ADMIN, Set.of("user:read"));
        org.springframework.mock.web.MockHttpServletRequest httpRequest = new org.springframework.mock.web.MockHttpServletRequest();

        when(authUseCase.login(any(LoginInput.class))).thenReturn(output);

        ResponseEntity<AuthWebResponse> response = authController.login(request, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("acc.token", response.getBody().getAccessToken());
        assertNull(response.getBody().getRefreshToken(), "Refresh token must not be exposed in JSON body");
        assertEquals(Role.ADMIN, response.getBody().getRole());
        assertEquals("admin@materia.com", response.getBody().getEmail());
        assertNotNull(response.getBody().getUser());
        assertEquals("admin@materia.com", response.getBody().getUser().email());

        // Verify Set-Cookie header
        String setCookie = response.getHeaders().getFirst(org.springframework.http.HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("refresh_token=ref.token"));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("SameSite=Strict"));

        verify(authUseCase).login(any(LoginInput.class));
    }

    @Test
    @DisplayName("refresh: returns 200 OK with new tokens and rotated cookie")
    void refresh_returnsOk() {
        AuthOutput output = new AuthOutput("new.acc", "new.ref", testUserId, "admin@materia.com", Role.ADMIN, Set.of("user:read"));
        org.springframework.mock.web.MockHttpServletRequest httpRequest = new org.springframework.mock.web.MockHttpServletRequest();

        when(authUseCase.refreshToken(any(RefreshTokenInput.class))).thenReturn(output);

        ResponseEntity<AuthWebResponse> response = authController.refresh("ref.token", null, null, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new.acc", response.getBody().getAccessToken());
        assertNull(response.getBody().getRefreshToken());

        String setCookie = response.getHeaders().getFirst(org.springframework.http.HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("refresh_token=new.ref"));
        verify(authUseCase).refreshToken(any(RefreshTokenInput.class));
    }

    @Test
    @DisplayName("logout: revokes token and clears refresh_token cookie")
    void logout_returnsOk() {
        org.springframework.mock.web.MockHttpServletRequest httpRequest = new org.springframework.mock.web.MockHttpServletRequest();

        ResponseEntity<MessageWebResponse> response = authController.logout("active.ref.token", null, null, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Logged out successfully", response.getBody().getMessage());

        String setCookie = response.getHeaders().getFirst(org.springframework.http.HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("Max-Age=0"));
        verify(authUseCase).logout("active.ref.token");
    }

    @Test
    @DisplayName("forgotPassword: returns 200 OK with MessageWebResponse")
    void forgotPassword_returnsOk() {
        ForgotPasswordWebRequest request = new ForgotPasswordWebRequest("user@materia.com");
        when(authUseCase.forgotPassword(any(ForgotPasswordInput.class))).thenReturn("Password reset link sent to user@materia.com");

        ResponseEntity<MessageWebResponse> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Password reset link sent to user@materia.com", response.getBody().getMessage());
        verify(authUseCase).forgotPassword(any(ForgotPasswordInput.class));
    }

    @Test
    @DisplayName("resetPassword: returns 200 OK with MessageWebResponse")
    void resetPassword_returnsOk() {
        ResetPasswordWebRequest request = new ResetPasswordWebRequest("user@materia.com", "token", "newPass123", "newPass123");
        when(authUseCase.resetPassword(any(ResetPasswordInput.class))).thenReturn("Your password has been successfully reset.");

        ResponseEntity<MessageWebResponse> response = authController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Your password has been successfully reset.", response.getBody().getMessage());
        verify(authUseCase).resetPassword(any(ResetPasswordInput.class));
    }
}
