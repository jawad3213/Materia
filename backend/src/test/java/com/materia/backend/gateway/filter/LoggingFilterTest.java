package com.materia.backend.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** The trace id travels with the request: in the response header and in every log line written meanwhile. */
class LoggingFilterTest {

    private final LoggingFilter filter = new LoggingFilter();

    @Test
    @DisplayName("trace: a caller's trace id is echoed and in the logging context while the request is served")
    void callerTraceIdIsKept() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/things");
        request.addHeader(LoggingFilter.TRACE_HEADER, "abc12345");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> seen.set(MDC.get(LoggingFilter.TRACE_MDC_KEY)));

        assertEquals("abc12345", seen.get());
        assertEquals("abc12345", response.getHeader(LoggingFilter.TRACE_HEADER));
        assertNull(MDC.get(LoggingFilter.TRACE_MDC_KEY), "the trace id must not leak to the next request on this thread");
    }

    @Test
    @DisplayName("trace: a request without a trace id gets a new one")
    void missingTraceIdIsGenerated() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();

        filter.doFilter(new MockHttpServletRequest("GET", "/"), response,
                (req, res) -> seen.set(MDC.get(LoggingFilter.TRACE_MDC_KEY)));

        assertNotNull(seen.get());
        assertEquals(seen.get(), response.getHeader(LoggingFilter.TRACE_HEADER));
    }
}
