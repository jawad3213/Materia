package com.materia.backend.gateway.config;

import com.materia.backend.gateway.filter.JwtAuthenticationFilter;
import com.materia.backend.gateway.filter.LoggingFilter;
import com.materia.backend.gateway.filter.RateLimitFilter;
import com.materia.backend.gateway.security.JwtAuthEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 🔹 GATEWAY SECURITY CONFIGURATION
 *
 * Configures:
 * - Gateway request tracing and latency logging (LoggingFilter)
 * - Gateway rate limiting and quota enforcement (RateLimitFilter)
 * - JWT-based stateless authentication (JwtAuthenticationFilter)
 * - Custom Authentication Provider (replacing DaoAuthenticationProvider)
 * - Centralized CORS for frontend communication
 * - Public vs protected endpoints
 * - Method-level security (@PreAuthorize)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class GatewaySecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final LoggingFilter loggingFilter;
    private final RateLimitFilter rateLimitFilter;

    public GatewaySecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthEntryPoint jwtAuthEntryPoint,
            LoggingFilter loggingFilter,
            RateLimitFilter rateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.loggingFilter = loggingFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Value("${app.security.public-endpoints}")
    private String[] publicEndpoints;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (stateless JWT does not need it)
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session (no server-side session)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Handle unauthorized access
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthEntryPoint))

                // Endpoint authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicEndpoints).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 1. Gateway Request Logging & Trace ID (first in chain)
                .addFilterBefore(loggingFilter, UsernamePasswordAuthenticationFilter.class)

                // 2. Gateway Rate Limiting (executed before authentication)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)

                // 3. JWT Authentication Filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control",
                "X-Gateway-Trace-Id"
        ));
        configuration.setExposedHeaders(List.of("Authorization", "X-Gateway-Trace-Id"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
