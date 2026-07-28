package com.materia.backend.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 🔹 JWT TOKEN PROVIDER
 *
 * Handles:
 * - Generating Access and Refresh JWT tokens using User UUID strictly as subject
 * - Extracting User UUID (subject) from Access and Refresh tokens
 * - Validating token integrity and expiration
 */
@Component
public class JwtTokenProvider {

    private enum TokenType {
        ACCESS,
        REFRESH
    }

    private record TokenConfig(String secret, long expirationMs) {}

    private final Map<TokenType, TokenConfig> tokenConfigs = new EnumMap<>(TokenType.class);

    public JwtTokenProvider(
            @Value("${jwt.access-secret}") String accessSecret,
            @Value("${jwt.refresh-secret}") String refreshSecret,
            @Value("${jwt.expiration-ms:86400000}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {

        tokenConfigs.put(TokenType.ACCESS, new TokenConfig(accessSecret, accessExpirationMs));
        tokenConfigs.put(TokenType.REFRESH, new TokenConfig(refreshSecret, refreshExpirationMs));
    }

    // ===================================================================
    // 🔹 ACCESS TOKEN METHODS
    // ===================================================================

    public String generateAccessToken(UUID userUuid, Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = (authentication != null) ? authentication.getAuthorities() : null;
        return generateToken(userUuid, authorities, TokenType.ACCESS);
    }

    public String generateAccessToken(UUID userUuid) {
        return generateToken(userUuid, null, TokenType.ACCESS);
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, TokenType.ACCESS);
    }

    public UUID getUserIdFromAccessToken(String token) {
        return getUserIdFromToken(token, TokenType.ACCESS);
    }

    // ===================================================================
    // 🔹 REFRESH TOKEN METHODS
    // ===================================================================

    public String generateRefreshToken(UUID userUuid) {
        return generateToken(userUuid, null, TokenType.REFRESH);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, TokenType.REFRESH);
    }

    public UUID getUserIdFromRefreshToken(String token) {
        return getUserIdFromToken(token, TokenType.REFRESH);
    }

    // ===================================================================
    // 🔹 PRIVATE CORE METHODS
    // ===================================================================

    private String generateToken(UUID userUuid, Collection<? extends GrantedAuthority> authorities, TokenType tokenType) {
        Objects.requireNonNull(userUuid, "User UUID must not be null");
        TokenConfig config = getConfig(tokenType);

        JwtBuilder builder = Jwts.builder()
                .subject(userUuid.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + config.expirationMs()))
                .signWith(getSigningKey(tokenType));

        if (tokenType == TokenType.ACCESS && authorities != null && !authorities.isEmpty()) {
            String roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            builder.claim("roles", roles);
        }

        return builder.compact();
    }

    private UUID getUserIdFromToken(String token, TokenType tokenType) {
        String subject = parseClaims(token, tokenType).getSubject();
        return UUID.fromString(subject);
    }

    private boolean validateToken(String token, TokenType tokenType) {
        try {
            parseClaims(token, tokenType);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token, TokenType tokenType) {
        return Jwts.parser()
                .verifyWith(getSigningKey(tokenType))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey(TokenType tokenType) {
        TokenConfig config = getConfig(tokenType);
        byte[] keyBytes = Decoders.BASE64.decode(config.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private TokenConfig getConfig(TokenType tokenType) {
        TokenConfig config = tokenConfigs.get(tokenType);
        if (config == null) {
            throw new IllegalArgumentException("Unsupported TokenType: " + tokenType);
        }
        return config;
    }
}
