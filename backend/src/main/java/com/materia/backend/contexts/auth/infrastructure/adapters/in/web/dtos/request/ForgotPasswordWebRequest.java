package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 🔹 FORGOT PASSWORD WEB REQUEST
 * 
 * HTTP request body for POST /api/v1/auth/forgot-password.
 */
public class ForgotPasswordWebRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    private String email;

    public ForgotPasswordWebRequest() {}

    public ForgotPasswordWebRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
