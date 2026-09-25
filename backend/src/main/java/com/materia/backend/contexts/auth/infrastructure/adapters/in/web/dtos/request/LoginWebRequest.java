package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 🔹 LOGIN WEB REQUEST
 * 
 * HTTP request body for the POST /api/v1/auth/login endpoint (email-only).
 */
public class LoginWebRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password must not be blank")
    private String password;

    public LoginWebRequest() {}

    public LoginWebRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
