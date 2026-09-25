package com.materia.backend.contexts.auth.application.dtos;

import com.materia.backend.common.application.BaseInput;

/**
 * 🔹 CHANGE PASSWORD INPUT DTO
 * 
 * Carries current and new passwords to update a user's password.
 */
public class ChangePasswordInput extends BaseInput {

    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    public ChangePasswordInput() {
        super();
    }

    public ChangePasswordInput(String currentPassword, String newPassword, String confirmPassword) {
        super();
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
