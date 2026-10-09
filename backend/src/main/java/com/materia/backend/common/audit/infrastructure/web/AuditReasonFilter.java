package com.materia.backend.common.audit.infrastructure.web;

import com.materia.backend.common.audit.application.AuditContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Lets a client say why it changes something: the optional {@value #HEADER} header (URL-encoded, so it may carry
 * any text) becomes the reason recorded with every change of the request. The context is always cleared afterwards,
 * so a reason never leaks to the next request served by the same thread.
 */
@Component
public class AuditReasonFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Change-Reason";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String reason = request.getHeader(HEADER);
            if (reason != null && !reason.isBlank()) {
                AuditContext.because(URLDecoder.decode(reason, StandardCharsets.UTF_8));
            }
            chain.doFilter(request, response);
        } finally {
            AuditContext.clear();
        }
    }
}
