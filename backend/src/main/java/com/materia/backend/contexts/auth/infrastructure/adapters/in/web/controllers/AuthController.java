package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.auth.application.dtos.AuthOutput;
import com.materia.backend.contexts.auth.application.dtos.ForgotPasswordInput;
import com.materia.backend.contexts.auth.application.dtos.LoginInput;
import com.materia.backend.contexts.auth.application.dtos.RefreshTokenInput;
import com.materia.backend.contexts.auth.application.dtos.ResetPasswordInput;
import com.materia.backend.contexts.auth.domain.ports.in.AuthUseCase;
import com.materia.backend.contexts.auth.domain.exceptions.AuthenticationFailedException;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.ForgotPasswordWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.LoginWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.RefreshTokenWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.ResetPasswordWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.response.AuthWebResponse;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.response.MessageWebResponse;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.mappers.AuthWebMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 🔹 AUTH REST CONTROLLER (INBOUND ADAPTER)
 * 
 * Exposes email-based authentication and recovery endpoints for the auth bounded context.
 * Accessible publicly through the API Gateway at /api/v1/auth/**.
 * Handles HttpOnly Secure cookies for refresh tokens.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final long REFRESH_TOKEN_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 days in seconds

    private final AuthUseCase authUseCase;
    private final AuthWebMapper webMapper;

    /**
     * Cookie attributes are configuration, not request-derived:
     * - behind a TLS-terminating proxy {@code request.isSecure()} is false, so deriving
     *   `Secure` from it silently ships a non-Secure session cookie in production;
     * - `SameSite=Strict` only works while the SPA and the API share a site. A deployment
     *   that splits them needs `None`, which in turn requires `Secure`.
     */
    @Value("${app.auth.refresh-cookie.secure:false}")
    private boolean refreshCookieSecure = false;

    @Value("${app.auth.refresh-cookie.same-site:Strict}")
    private String refreshCookieSameSite = "Strict";

    public AuthController(AuthUseCase authUseCase, AuthWebMapper webMapper) {
        this.authUseCase = authUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthWebResponse> login(
            @Valid @RequestBody LoginWebRequest request,
            HttpServletRequest httpRequest) {
        LoginInput input = webMapper.toAppLoginInput(request);
        AuthOutput output = authUseCase.login(input);

        ResponseCookie cookie = buildRefreshTokenCookie(output.getRefreshToken(), httpRequest.isSecure(), REFRESH_TOKEN_COOKIE_MAX_AGE);
        AuthWebResponse responseBody = webMapper.toWebResponse(output);
        // Exclude refresh token from JSON response body as per security standard
        responseBody.setRefreshToken(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }

    @PostMapping(value = "/refresh", consumes = { org.springframework.http.MediaType.APPLICATION_JSON_VALUE, org.springframework.http.MediaType.ALL_VALUE })
    public ResponseEntity<AuthWebResponse> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieRefreshToken,
            @CookieValue(name = "refreshToken", required = false) String altCookieRefreshToken,
            @RequestBody(required = false) RefreshTokenWebRequest request,
            HttpServletRequest httpRequest) {

        String token = StringUtils.hasText(cookieRefreshToken) ? cookieRefreshToken :
                (StringUtils.hasText(altCookieRefreshToken) ? altCookieRefreshToken : null);

        // Fallback: inspect raw servlet cookies
        if (!StringUtils.hasText(token) && httpRequest.getCookies() != null) {
            for (jakarta.servlet.http.Cookie c : httpRequest.getCookies()) {
                if (REFRESH_TOKEN_COOKIE_NAME.equalsIgnoreCase(c.getName()) || "refreshToken".equalsIgnoreCase(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        // Fallback: inspect request body
        if (!StringUtils.hasText(token) && request != null) {
            token = request.getRefreshToken();
        }

        if (!StringUtils.hasText(token)) {
            throw new AuthenticationFailedException("Refresh token is required", "AUTH_REFRESH_TOKEN_REQUIRED");
        }

        AuthOutput output = authUseCase.refreshToken(new RefreshTokenInput(token));

        ResponseCookie cookie = buildRefreshTokenCookie(output.getRefreshToken(), httpRequest.isSecure(), REFRESH_TOKEN_COOKIE_MAX_AGE);
        AuthWebResponse responseBody = webMapper.toWebResponse(output);
        responseBody.setRefreshToken(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }

    @PostMapping(value = "/logout", consumes = { org.springframework.http.MediaType.APPLICATION_JSON_VALUE, org.springframework.http.MediaType.ALL_VALUE })
    public ResponseEntity<MessageWebResponse> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieRefreshToken,
            @CookieValue(name = "refreshToken", required = false) String altCookieRefreshToken,
            @RequestBody(required = false) RefreshTokenWebRequest request,
            HttpServletRequest httpRequest) {

        String token = StringUtils.hasText(cookieRefreshToken) ? cookieRefreshToken :
                (StringUtils.hasText(altCookieRefreshToken) ? altCookieRefreshToken : null);

        if (!StringUtils.hasText(token) && httpRequest.getCookies() != null) {
            for (jakarta.servlet.http.Cookie c : httpRequest.getCookies()) {
                if (REFRESH_TOKEN_COOKIE_NAME.equalsIgnoreCase(c.getName()) || "refreshToken".equalsIgnoreCase(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        if (!StringUtils.hasText(token) && request != null) {
            token = request.getRefreshToken();
        }

        if (StringUtils.hasText(token)) {
            authUseCase.logout(token);
        }

        ResponseCookie clearCookie = buildRefreshTokenCookie("", httpRequest.isSecure(), 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(new MessageWebResponse("Logged out successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageWebResponse> forgotPassword(@Valid @RequestBody ForgotPasswordWebRequest request) {
        ForgotPasswordInput input = webMapper.toAppForgotPasswordInput(request);
        String message = authUseCase.forgotPassword(input);
        return ResponseEntity.ok(new MessageWebResponse(message));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageWebResponse> resetPassword(@Valid @RequestBody ResetPasswordWebRequest request) {
        ResetPasswordInput input = webMapper.toAppResetPasswordInput(request);
        String message = authUseCase.resetPassword(input);
        return ResponseEntity.ok(new MessageWebResponse(message));
    }

    private ResponseCookie buildRefreshTokenCookie(String tokenValue, boolean isSecure, long maxAge) {
        // SameSite=None is meaningless (and rejected by browsers) without Secure.
        boolean secure = refreshCookieSecure || isSecure || "None".equalsIgnoreCase(refreshCookieSameSite);
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, tokenValue)
                .httpOnly(true)
                .secure(secure)
                .sameSite(refreshCookieSameSite)
                .path("/api/v1/auth")
                .maxAge(maxAge)
                .build();
    }
}
