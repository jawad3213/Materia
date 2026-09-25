package com.materia.backend.contexts.auth.application.dtos;

import com.materia.backend.common.application.BaseInput;

/**
 * 🔹 REFRESH TOKEN INPUT DTO
 * 
 * Application input containing the refresh token used to obtain a new access token.
 */
public class RefreshTokenInput extends BaseInput {

    private String refreshToken;

    public RefreshTokenInput() {
        super();
    }

    public RefreshTokenInput(String refreshToken) {
        super();
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
