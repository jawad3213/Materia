package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InvoiceVerifyWebRequest {

    @NotBlank(message = "User ID is mandatory")
    private String userId;

    @NotBlank(message = "User name is mandatory")
    @Size(max = 255, message = "User name must not exceed 255 characters")
    private String userName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
