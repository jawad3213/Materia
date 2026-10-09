package com.materia.backend.contexts.auth.domain.ports.in;

import com.materia.backend.contexts.auth.application.dtos.AuthOutput;
import com.materia.backend.contexts.auth.application.dtos.ForgotPasswordInput;
import com.materia.backend.contexts.auth.application.dtos.LoginInput;
import com.materia.backend.contexts.auth.application.dtos.RefreshTokenInput;
import com.materia.backend.contexts.auth.application.dtos.ResetPasswordInput;

/**
 * 🔹 AUTH USE CASE (INPUT PORT)
 * 
 * Defines incoming use cases for email-based authentication,
 * refresh token issuance, and password recovery.
 */
public interface AuthUseCase {

    AuthOutput login(LoginInput input);

    AuthOutput refreshToken(RefreshTokenInput input);

    void logout(String refreshToken);

    String forgotPassword(ForgotPasswordInput input);

    String resetPassword(ResetPasswordInput input);

    /**
     * Changes the signed-in user's own password, ends all of their sessions and returns a fresh one.
     * The current password is required unless the account is in forced first-login change mode.
     */
    AuthOutput changePassword(java.util.UUID userId, com.materia.backend.contexts.auth.application.dtos.ChangePasswordInput input);
}
