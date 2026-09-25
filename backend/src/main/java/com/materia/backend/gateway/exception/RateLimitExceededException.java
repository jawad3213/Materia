package com.materia.backend.gateway.exception;

/**
 * Exception levée lorsque le quota de requêtes par minute est dépassé.
 */
public class RateLimitExceededException extends RuntimeException {

    private final String clientKey;
    private final int limit;

    public RateLimitExceededException(String clientKey, int limit) {
        super(String.format("Rate limit exceeded for client [%s]. Maximum allowed: %d requests per minute.", clientKey, limit));
        this.clientKey = clientKey;
        this.limit = limit;
    }

    public String getClientKey() {
        return clientKey;
    }

    public int getLimit() {
        return limit;
    }
}
