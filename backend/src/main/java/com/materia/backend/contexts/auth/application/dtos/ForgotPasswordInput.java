package com.materia.backend.contexts.auth.application.dtos;

import com.materia.backend.common.application.BaseInput;

/**
 * 🔹 FORGOT PASSWORD INPUT DTO
 * 
 * Application input containing user email to initiate password reset.
 */
public class ForgotPasswordInput extends BaseInput {

    private String email;

    public ForgotPasswordInput() {
        super();
    }

    public ForgotPasswordInput(String email) {
        super();
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
