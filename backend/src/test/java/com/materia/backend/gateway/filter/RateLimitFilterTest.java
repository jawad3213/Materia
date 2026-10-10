package com.materia.backend.gateway.filter;

import com.materia.backend.gateway.config.RateLimitConfig;
import com.materia.backend.gateway.config.RouteConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/** Login attempts are throttled per client, and the client cannot choose the address it is counted under. */
class RateLimitFilterTest {

    private static final int AUTH_LIMIT = 3;

    private final RateLimitFilter filter = new RateLimitFilter(rateLimitConfig(), new RouteConfig());

    private static RateLimitConfig rateLimitConfig() {
        RateLimitConfig config = new RateLimitConfig();
        ReflectionTestUtils.setField(config, "enabled", true);
        ReflectionTestUtils.setField(config, "maxRequestsPerMinute", 100);
        ReflectionTestUtils.setField(config, "maxAuthRequestsPerMinute", AUTH_LIMIT);
        ReflectionTestUtils.setField(config, "windowSeconds", 60L);
        return config;
    }

    /** A login attempt arriving through nginx, which appends the address it saw to X-Forwarded-For. */
    private int login(String spoofedForwardedFor, String realAddress) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("172.18.0.5"); // nginx, inside the compose network
        request.addHeader("X-Forwarded-For",
                spoofedForwardedFor == null ? realAddress : spoofedForwardedFor + ", " + realAddress);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> { });
        return response.getStatus();
    }

    @Test
    @DisplayName("rate limit: login attempts above the quota are refused with 429")
    void loginAttemptsAboveTheQuotaAreRefused() throws Exception {
        for (int i = 0; i < AUTH_LIMIT; i++) {
            assertEquals(200, login(null, "203.0.113.7"));
        }
        assertEquals(429, login(null, "203.0.113.7"));
    }

    @Test
    @DisplayName("rate limit: a client cannot escape the quota by sending its own X-Forwarded-For")
    void spoofedForwardedForDoesNotResetTheQuota() throws Exception {
        for (int i = 0; i < AUTH_LIMIT; i++) {
            assertEquals(200, login("10.0.0." + i, "198.51.100.9"));
        }
        assertEquals(429, login("10.0.0.99", "198.51.100.9"));
    }

    @Test
    @DisplayName("rate limit: each real client has its own quota")
    void clientsAreCountedSeparately() throws Exception {
        for (int i = 0; i < AUTH_LIMIT; i++) {
            login(null, "192.0.2.1");
        }
        assertEquals(429, login(null, "192.0.2.1"));
        assertEquals(200, login(null, "192.0.2.2"));
    }

    @Test
    @DisplayName("client ip: the X-Real-IP nginx sets wins over a spoofed X-Forwarded-For")
    void realIpSetByNginxWins() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.9.9.9"); // what Spring's forwarded-header filter derives from the first entry
        request.addHeader("X-Real-IP", "198.51.100.20");
        request.addHeader("X-Forwarded-For", "10.9.9.9");
        assertEquals("198.51.100.20", ClientIp.from(request));
    }

    @Test
    @DisplayName("client ip: without a proxy header the connection's address is used")
    void withoutProxyHeaderTheRemoteAddressIsUsed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.44");
        assertEquals("192.0.2.44", ClientIp.from(request));

        request.addHeader("X-Forwarded-For", " , ");
        assertEquals("192.0.2.44", ClientIp.from(request));
    }
}
