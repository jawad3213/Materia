package com.materia.backend.contexts.auth.application.dtos;

import com.materia.backend.common.application.BaseInput;

/**
 * 🔹 RESET PASSWORD INPUT DTO
 * 
 * Application input containing reset token, email, and new password details.
 */
public class ResetPasswordInput extends BaseInput {

    private String email;
    private String token;
    private String password;
    private String confirmPassword;

    public ResetPasswordInput() {
        super();
    }

    public ResetPasswordInput(String email, String token, String password, String confirmPassword) {
        super();
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
