package com.materia.backend.gateway.filter;

import com.materia.backend.gateway.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 🔹 JWT AUTHENTICATION FILTER — STATELESS (API GATEWAY)
 *
 * Intercepts every incoming request at the API Gateway level to:
 * 1. Extract the Bearer token from the Authorization header.
 * 2. Validate token integrity and expiration via JwtTokenProvider.
 * 3. Extract User ID (UUID) and Roles directly from the JWT claims.
 * 4. Populate the SecurityContext — ZERO database call.
 *
 * In a microservice architecture, the Gateway never queries the auth database.
 * All user identity information needed for authorization is embedded in the
 * self-contained JWT token and verified via the shared secret key.
 *
 * ┌────────────────────────────────────────────────────────────┐
 * │  JWT Payload (claims)                                       │
 * │  { sub: "uuid", roles: "ROLE_ADMIN,ROLE_PURCHASER", exp }  │
 * └────────────────────────────────────────────────────────────┘
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip if already authenticated (e.g. from a previous filter)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            if (jwtTokenProvider.validateAccessToken(token)) {
                try {
                    // Extract identity directly from JWT claims — NO database call
                    UUID userId = jwtTokenProvider.getUserIdFromAccessToken(token);
                    List<GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromAccessToken(token);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Gateway: authenticated user [{}] with roles {} for path [{}]",
                            userId, authorities, request.getRequestURI());

                } catch (Exception e) {
                    log.error("Gateway: failed to authenticate user from JWT token: {}", e.getMessage());
                }
            } else {
                log.debug("Gateway: invalid or expired JWT token for path [{}]", request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
