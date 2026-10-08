package com.materia.backend.contexts.auth.infrastructure.adapters.out.token;

import com.materia.backend.contexts.auth.domain.ports.out.TokenIssuer;
import com.materia.backend.gateway.security.JwtTokenProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

/** Issues tokens with the JWT provider the gateway validates them with. */
@Component
public class JwtTokenIssuer implements TokenIssuer {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenIssuer(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public String generateAccessTokenWithAuthorities(UUID userUuid, String email,
                                                     Collection<? extends GrantedAuthority> authorities) {
        return jwtTokenProvider.generateAccessTokenWithAuthorities(userUuid, email, authorities);
    }

    @Override
    public String generateRandomRefreshToken() {
        return jwtTokenProvider.generateRandomRefreshToken();
    }

    @Override
    public String hashToken(String rawToken) {
        return jwtTokenProvider.hashToken(rawToken);
    }
}
