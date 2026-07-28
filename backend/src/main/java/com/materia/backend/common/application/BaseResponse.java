package com.materia.backend.common.application;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Response DTO
 * All response DTOs should extend this class
 * Used for returning data to clients
 */
public abstract class BaseResponse {

    private final String responseId;
    private final LocalDateTime timestamp;
    private boolean success;
    private String message;
    private String errorCode;

    protected BaseResponse() {
        this(true, "Success");
    }

    protected BaseResponse(boolean success, String message) {
        this.responseId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.success = success;
        this.message = message;
    }

    protected BaseResponse(boolean success, String message, String errorCode) {
        this(success, message);
        this.errorCode = errorCode;
    }

    // Getters and Setters
    public String getResponseId() {
        return responseId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }


    /**
     * Create a success response
     */
    public static <T extends BaseResponse> T success(T response) {
        response.setSuccess(true);
        response.setMessage("Success");
        return response;
    }

    /**
     * Create a failure response
     */
    public static <T extends BaseResponse> T failure(T response, String message) {
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    /**
     * Create a failure response with error code
     */
    public static <T extends BaseResponse> T failure(T response, String message, String errorCode) {
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        return response;
    }
}