package com.materia.backend.gateway.filter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The address of the client that called us, as the rate limit and the request log see it.
 *
 * <p>The backend is only reachable through the frontend's nginx, which sets {@code X-Real-IP} to the address it
 * received the request from, overwriting anything the client sent: that header is used first. Without it, the
 * <em>last</em> {@code X-Forwarded-For} entry is the one the nearest proxy wrote; the entries before it come from the
 * client and can be anything. (Taking the first entry let a client pick a new address on every request and so
 * escape the login rate limit. In the staging and prod profiles Spring's forwarded-header support also replaces
 * the remote address with that first entry, which is why the remote address comes last.)
 */
final class ClientIp {

    private ClientIp() {
    }

    static String from(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] hops = forwardedFor.split(",");
            String last = hops[hops.length - 1].trim();
            if (!last.isEmpty()) {
                return last;
            }
        }
        return request.getRemoteAddr();
    }
}
