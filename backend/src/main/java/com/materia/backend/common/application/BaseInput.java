package com.materia.backend.common.application;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Request DTO
 * All request DTOs should extend this class
 * Used for input validation and data transfer from clients
 */
public abstract class BaseInput {

    private final String requestId;
    private final LocalDateTime timestamp;
    private String userId;

    protected BaseInput() {
        this.requestId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    protected BaseInput(String userId) {
        this();
        this.userId = userId;
    }


    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    /**
     * Validate the request
     * Override to add custom validation
     */
    public void validate() {
        // Default: no validation
    }
}

