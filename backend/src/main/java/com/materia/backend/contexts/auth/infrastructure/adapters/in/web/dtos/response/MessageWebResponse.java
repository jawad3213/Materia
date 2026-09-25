package com.materia.backend.contexts.auth.infrastructure.adapters.in.web.dtos.response;

/**
 * 🔹 MESSAGE WEB RESPONSE
 * 
 * Standard HTTP response for operation messages (e.g. forgot/reset password).
 */
public class MessageWebResponse {

    private String message;

    public MessageWebResponse() {}

    public MessageWebResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
