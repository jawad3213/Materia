package com.materia.backend.gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 🔹 JWT TOKEN PROVIDER (API GATEWAY)
 *
 * Handles:
 * - Generating Access and Refresh JWT tokens using User UUID strictly as subject and embedding email
 * - Extracting User UUID (subject) and email from Access and Refresh tokens
 * - Validating token integrity and expiration statelessly without database access
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
            @Value("${jwt.expiration-ms}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {

        tokenConfigs.put(TokenType.ACCESS, new TokenConfig(accessSecret, accessExpirationMs));
        tokenConfigs.put(TokenType.REFRESH, new TokenConfig(refreshSecret, refreshExpirationMs));
    }

    // ===================================================================
    // 🔹 ACCESS TOKEN METHODS
    // ===================================================================

    public String generateAccessToken(UUID userUuid, Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = (authentication != null) ? authentication.getAuthorities() : null;
        return generateToken(userUuid, null, authorities, TokenType.ACCESS);
    }

    public String generateAccessToken(UUID userUuid, Collection<? extends GrantedAuthority> authorities) {
        return generateToken(userUuid, null, authorities, TokenType.ACCESS);
    }

    public String generateAccessTokenWithAuthorities(UUID userUuid, Collection<? extends GrantedAuthority> authorities) {
        return generateToken(userUuid, null, authorities, TokenType.ACCESS);
    }

    public String generateAccessTokenWithAuthorities(UUID userUuid, String email, Collection<? extends GrantedAuthority> authorities) {
        return generateToken(userUuid, email, authorities, TokenType.ACCESS);
    }

    public String generateAccessToken(UUID userUuid) {
        return generateToken(userUuid, null, null, TokenType.ACCESS);
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, TokenType.ACCESS);
    }

    public UUID getUserIdFromAccessToken(String token) {
        return getUserIdFromToken(token, TokenType.ACCESS);
    }

    public String getEmailFromAccessToken(String token) {
        Claims claims = parseClaims(token, TokenType.ACCESS);
        return claims.get("email", String.class);
    }

    /**
     * Extracts the user's granted authorities (roles) directly from the JWT access token claims.
     * Used by the Gateway to build the SecurityContext WITHOUT any database call.
     */
    public List<GrantedAuthority> getAuthoritiesFromAccessToken(String token) {
        Claims claims = parseClaims(token, TokenType.ACCESS);
        String roles = claims.get("roles", String.class);
        if (!StringUtils.hasText(roles)) {
            return Collections.emptyList();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 🔹 REFRESH TOKEN METHODS
    // ===================================================================

    /**
     * Generates a cryptographically secure random 256-bit string (32 bytes)
     * formatted as a URL-safe Base64 string without padding.
     */
    public String generateRandomRefreshToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Hashes a raw refresh token using SHA-256 and returns a 64-character lowercase hex string.
     */
    public String hashToken(String rawToken) {
        if (rawToken == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public String generateRefreshToken(UUID userUuid) {
        return generateToken(userUuid, null, null, TokenType.REFRESH);
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

    private String generateToken(UUID userUuid, String email, Collection<? extends GrantedAuthority> authorities, TokenType tokenType) {
        Objects.requireNonNull(userUuid, "User UUID must not be null");
        TokenConfig config = getConfig(tokenType);

        JwtBuilder builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userUuid.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + config.expirationMs()))
                .signWith(getSigningKey(tokenType));

        if (StringUtils.hasText(email)) {
            builder.claim("email", email);
        }

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
