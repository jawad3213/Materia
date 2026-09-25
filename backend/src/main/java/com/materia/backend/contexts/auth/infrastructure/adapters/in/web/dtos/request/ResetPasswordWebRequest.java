package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 🔹 RESET PASSWORD WEB REQUEST
 * 
 * HTTP request body for POST /api/v1/auth/reset-password.
 */
public class ResetPasswordWebRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    private String email;

    private String token;

    @NotBlank(message = "New password must not be blank")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    private String confirmPassword;

    public ResetPasswordWebRequest() {}

    public ResetPasswordWebRequest(String email, String token, String password, String confirmPassword) {
        this.email = email;
        this.token = token;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
