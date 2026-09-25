package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.mappers;

import com.materia.backend.contexts.auth.application.dtos.AuthOutput;
import com.materia.backend.contexts.auth.application.dtos.ForgotPasswordInput;
import com.materia.backend.contexts.auth.application.dtos.LoginInput;
import com.materia.backend.contexts.auth.application.dtos.RefreshTokenInput;
import com.materia.backend.contexts.auth.application.dtos.ResetPasswordInput;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.ForgotPasswordWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.LoginWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.RefreshTokenWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request.ResetPasswordWebRequest;
import com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.response.AuthWebResponse;
import org.springframework.stereotype.Component;

/**
 * 🔹 AUTH WEB MAPPER
 * 
 * Maps between HTTP Web DTOs and Application Input/Output DTOs (email-only).
 */
@Component
public class AuthWebMapper {

    public LoginInput toAppLoginInput(LoginWebRequest request) {
        if (request == null) return null;
        return new LoginInput(request.getEmail(), request.getPassword());
    }

    public RefreshTokenInput toAppRefreshTokenInput(RefreshTokenWebRequest request) {
        if (request == null) return null;
        return new RefreshTokenInput(request.getRefreshToken());
    }

    public ForgotPasswordInput toAppForgotPasswordInput(ForgotPasswordWebRequest request) {
        if (request == null) return null;
        return new ForgotPasswordInput(request.getEmail());
    }

    public ResetPasswordInput toAppResetPasswordInput(ResetPasswordWebRequest request) {
        if (request == null) return null;
        return new ResetPasswordInput(
                request.getEmail(),
                request.getToken(),
                request.getPassword(),
                request.getConfirmPassword()
        );
    }

    public AuthWebResponse toWebResponse(AuthOutput output) {
        if (output == null) return null;
        return new AuthWebResponse(
                output.getAccessToken(),
                output.getRefreshToken(),
                output.getTokenType(),
                output.getUserId(),
                output.getEmail(),
                output.getRole(),
                output.getPermissions(),
                output.getFirstName(),
                output.getLastName(),
                output.getFullName(),
                output.getPhone(),
                output.getStatus()
        );
    }
}
