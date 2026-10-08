package com.materia.backend.contexts.auth.domain.ports.out;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

/** Issues the tokens a user signs in with; the gateway validates the access tokens it issues. */
public interface TokenIssuer {

    /** A signed access token carrying the user's id, e-mail and authorities. */
    String generateAccessTokenWithAuthorities(UUID userUuid, String email, Collection<? extends GrantedAuthority> authorities);

    /** A random, URL-safe refresh token; only its hash is stored. */
    String generateRandomRefreshToken();

    /** The stored form of a refresh or reset token. */
    String hashToken(String rawToken);
}
