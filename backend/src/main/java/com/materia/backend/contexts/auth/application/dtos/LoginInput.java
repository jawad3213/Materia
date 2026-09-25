package com.materia.backend.contexts.auth.application.dtos;

import com.materia.backend.common.application.BaseInput;

/**
 * 🔹 LOGIN INPUT DTO
 * 
 * Application input containing credentials to authenticate a user strictly by email.
 */
public class LoginInput extends BaseInput {

    private String email;
    private String password;

    public LoginInput() {
        super();
    }

    public LoginInput(String email, String password) {
        super();
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
