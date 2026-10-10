package com.materia.backend.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 🔹 GATEWAY LOGGING FILTER
 * 
 * Enregistre les métadonnées de chaque requête transitant par la Gateway :
 * - Identifiant unique de traçabilité (X-Gateway-Trace-Id)
 * - Méthode HTTP et URI
 * - Adresse IP cliente
 * - Temps d'exécution (latence en ms)
 * - Code de statut HTTP final
 */
@Component("gatewayLoggingFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    public static final String TRACE_HEADER = "X-Gateway-Trace-Id";
    /** MDC key of the trace id: every log line written while serving the request carries it (JSON field "traceId"). */
    public static final String TRACE_MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String traceId = request.getHeader(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }

        response.setHeader(TRACE_HEADER, traceId);
        MDC.put(TRACE_MDC_KEY, traceId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String clientIp = ClientIp.from(request);

        log.info("[GATEWAY-REQ] [{}] {} {} from IP={}", traceId, method, uri, clientIp);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            log.info("[GATEWAY-RES] [{}] {} {} -> Status={} ({} ms)", traceId, method, uri, status, duration);
            MDC.remove(TRACE_MDC_KEY);
        }
    }
}
