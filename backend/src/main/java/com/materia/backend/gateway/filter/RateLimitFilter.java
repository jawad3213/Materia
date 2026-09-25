package com.materia.backend.gateway.filter;

import com.materia.backend.gateway.config.RateLimitConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 🔹 GATEWAY RATE LIMIT FILTER
 * 
 * Intercepte les requêtes entrantes pour appliquer un quota de débit (Rate Limiting).
 * Rejette les requêtes excédentaires avec le statut HTTP 429 Too Many Requests.
 */
@Component("gatewayRateLimitFilter")
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitConfig rateLimitConfig;
    private final com.materia.backend.gateway.config.RouteConfig routeConfig;

    public RateLimitFilter(RateLimitConfig rateLimitConfig, com.materia.backend.gateway.config.RouteConfig routeConfig) {
        this.rateLimitConfig = rateLimitConfig;
        this.routeConfig = routeConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String clientIp = extractClientIp(request);

        // Credential endpoints are unauthenticated, so skipping them (as every other
        // public route does) would leave password guessing completely unthrottled.
        // They get their own, much smaller bucket instead.
        boolean isCredentialEndpoint = isCredentialEndpoint(uri);

        if (!isCredentialEndpoint && routeConfig.isPublicRoute(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        int limit = isCredentialEndpoint
                ? rateLimitConfig.getMaxAuthRequestsPerMinute()
                : rateLimitConfig.getMaxRequestsPerMinute();
        String bucketKey = isCredentialEndpoint ? "auth:" + clientIp : clientIp;

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));

        if (!rateLimitConfig.tryConsume(bucketKey, limit)) {
            log.warn("[GATEWAY-RATELIMIT] Quota exceeded for IP: {} on path: {}", clientIp, uri);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("X-RateLimit-Remaining", "0");
            response.getWriter().write(String.format(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Maximum %d requests per minute.\",\"path\":\"%s\"}",
                    limit,
                    uri
            ));
            return;
        }

        response.setHeader("X-RateLimit-Remaining",
                String.valueOf(rateLimitConfig.getRemainingRequests(bucketKey, limit)));
        filterChain.doFilter(request, response);
    }

    /**
     * Endpoints qui consomment ou délivrent des identifiants : cibles typiques
     * du bourrage d'identifiants et de l'énumération de comptes.
     */
    private boolean isCredentialEndpoint(String uri) {
        if (uri == null) return false;
        return uri.equals("/api/v1/auth/login")
                || uri.equals("/api/v1/auth/forgot-password")
                || uri.equals("/api/v1/auth/reset-password");
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
