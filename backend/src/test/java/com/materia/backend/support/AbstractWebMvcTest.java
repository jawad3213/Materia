package com.materia.backend.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.gateway.config.GatewaySecurityConfig;
import com.materia.backend.gateway.config.RateLimitConfig;
import com.materia.backend.gateway.config.RouteConfig;
import com.materia.backend.gateway.security.JwtAuthEntryPoint;
import com.materia.backend.gateway.security.JwtTokenProvider;
import com.materia.backend.gateway.security.SecurityBeansConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base for web-layer slice tests.
 *
 * <p>Subclasses declare {@code @WebMvcTest(SomeController.class)}, mock the controller's
 * use-case port with {@code @MockBean}, and import its web mapper.
 *
 * <p>The <strong>real</strong> security configuration is imported rather than disabled, so
 * slice tests exercise the actual rules: public endpoints stay reachable, everything else
 * demands authentication (FR-010). The gateway filters are real too. A Mockito mock of a
 * {@code Filter} never calls {@code chain.doFilter}, so mocking them would make every
 * request silently return an empty 200 instead of reaching the controller.
 *
 * <p>Use {@code @WithMockUser(authorities = "...")} with permission strings from the
 * {@code Role} enum to act as a signed-in user.
 */
@Import({
        GatewaySecurityConfig.class,
        SecurityBeansConfig.class,
        JwtTokenProvider.class,
        JwtAuthEntryPoint.class,
        RateLimitConfig.class,
        RouteConfig.class
})
public abstract class AbstractWebMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /** Serialises a request body using the application's own Jackson configuration. */
    protected String json(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise request body", e);
        }
    }
}
