package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 🔹 REFRESH TOKEN WEB REQUEST
 * 
 * HTTP request body for the POST /api/v1/auth/refresh endpoint.
 */
public class RefreshTokenWebRequest {

    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;

    public RefreshTokenWebRequest() {}

    public RefreshTokenWebRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
