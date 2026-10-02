package com.materia.backend.support;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.gateway.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [T115] End-to-end token authority test.
 *
 * <p>Proves that a real signed JWT carries all fine-grained permissions for each
 * of the three system roles (ADMIN, PURCHASER, RECEIVER), and that the JWT token provider
 * reconstructs the exact authorities without database lookups.
 */
class AuthorizationEndToEndTest {

    private static final String ACCESS_SECRET = "dGVzdC1hY2Nlc3Mtc2VjcmV0LWZvci1sb2NhbC10ZXN0aW5nLW9ubHktbm90LWEtcmVhbC1rZXk=";
    private static final String REFRESH_SECRET = "dGVzdC1yZWZyZXNoLXNlY3JldC1mb3ItbG9jYWwtdGVzdGluZy1vbmx5LW5vdC1hLXJlYWwta2V5";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(ACCESS_SECRET, REFRESH_SECRET, 900_000L, 604_800_000L);
    }

    private List<GrantedAuthority> buildAuthorities(Role role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
        for (String perm : role.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(perm));
        }
        return authorities;
    }

    @Test
    @DisplayName("E2E security: ADMIN token embeds all admin authorities and permissions")
    void adminToken_embedsAllPermissions() {
        UUID userId = UUID.randomUUID();
        String email = "admin@example.com";
        List<GrantedAuthority> granted = buildAuthorities(Role.ADMIN);

        String token = jwtTokenProvider.generateAccessTokenWithAuthorities(userId, email, granted);
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateAccessToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromAccessToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getEmailFromAccessToken(token)).isEqualTo(email);

        List<GrantedAuthority> extracted = jwtTokenProvider.getAuthoritiesFromAccessToken(token);
        Set<String> authorityStrings = extracted.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorityStrings).contains(
                "ROLE_ADMIN",
                "user:read", "user:write", "user:delete",
                "category:read", "category:write", "category:delete",
                "supplier:read", "supplier:write", "supplier:delete",
                "material:read", "material:write", "material:delete",
                "material:stock:read", "material:stock:write",
                "requisition:read", "requisition:write", "requisition:validate", "requisition:convert"
        );
    }

    @Test
    @DisplayName("E2E security: PURCHASER token embeds purchasing permissions and lacks admin/receiver permissions")
    void purchaserToken_embedsPurchaserPermissions() {
        UUID userId = UUID.randomUUID();
        String email = "purchaser@example.com";
        List<GrantedAuthority> granted = buildAuthorities(Role.PURCHASER);

        String token = jwtTokenProvider.generateAccessTokenWithAuthorities(userId, email, granted);
        assertThat(token).isNotBlank();

        List<GrantedAuthority> extracted = jwtTokenProvider.getAuthoritiesFromAccessToken(token);
        Set<String> authorityStrings = extracted.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorityStrings).contains(
                "ROLE_PURCHASER",
                "category:read", "category:write",
                "supplier:read", "supplier:write",
                "requisition:read", "requisition:write", "requisition:validate", "requisition:convert",
                "material:read", "material:write-price"
        );

        // Lacks admin privileges and stock writes
        assertThat(authorityStrings).doesNotContain(
                "user:write", "user:delete",
                "category:delete", "supplier:delete", "material:delete",
                "material:write", "material:stock:read", "material:stock:write"
        );
    }

    @Test
    @DisplayName("E2E security: RECEIVER token embeds stock permissions and lacks write privileges on master data")
    void receiverToken_embedsStockPermissions() {
        UUID userId = UUID.randomUUID();
        String email = "receiver@example.com";
        List<GrantedAuthority> granted = buildAuthorities(Role.RECEIVER);

        String token = jwtTokenProvider.generateAccessTokenWithAuthorities(userId, email, granted);
        assertThat(token).isNotBlank();

        List<GrantedAuthority> extracted = jwtTokenProvider.getAuthoritiesFromAccessToken(token);
        Set<String> authorityStrings = extracted.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorityStrings).contains(
                "ROLE_RECEIVER",
                "material:read", "material:stock:read", "material:stock:write",
                "category:read", "supplier:read", "requisition:read"
        );

        // Lacks mutations on catalogue, requisitions, users
        assertThat(authorityStrings).doesNotContain(
                "user:read", "user:write", "user:delete",
                "category:write", "category:delete",
                "supplier:write", "supplier:delete",
                "material:write", "material:delete",
                "requisition:write", "requisition:validate", "requisition:convert"
        );
    }
}
