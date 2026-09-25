package com.materia.backend.gateway.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

/**
 * 🔹 SECURITY UTILITIES (API GATEWAY)
 *
 * Helper methods to access the currently authenticated user identity
 * from anywhere in the application.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get the currently authenticated user UUID
     */
    public static Optional<UUID> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return Optional.of(uuid);
        } else if (principal instanceof String str) {
            try {
                return Optional.of(UUID.fromString(str));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    /**
     * Get the currently authenticated email
     */
    public static Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails.getUsername());
        } else if (principal instanceof String str) {
            return Optional.of(str);
        } else if (principal instanceof UUID uuid) {
            return Optional.of(uuid.toString());
        }

        return Optional.empty();
    }

    /**
     * Backward-compatible alias for user identity string
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUserEmail();
    }

    /**
     * Check if a user is currently authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Check if the current user has a specific role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
}
