package com.materia.backend.support;

import com.materia.backend.contexts.masterData.domain.ports.in.CategoryUseCase;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.controllers.CategoryController;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.mappers.CategoryWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards against the suite silently depending on the machine it runs on (FR-011, FR-012).
 *
 * <p>Today a developer's .env never reaches the test context. {@code DotenvEnvironmentPostProcessor}
 * is registered in a {@code META-INF/spring/*.imports} file, a location Spring Boot does not
 * read post-processors from, so its highest-precedence {@code addFirst} never runs. The .env
 * file is loaded only by {@code Backend2Application.main()}, which tests never call.
 *
 * <p>That is fragile. If someone corrects the registration (see FINDING-009), .env would load
 * at highest precedence in every test, could activate the dev profile, and could point the
 * suite at a real database and SMTP server. The suite would then also behave differently
 * locally than in CI, where no .env exists. These checks are the tripwire for that change.
 */
@WebMvcTest(CategoryController.class)
@Import(CategoryWebMapper.class)
class HermeticEnvironmentTest extends AbstractWebMvcTest {

    @MockBean
    private CategoryUseCase categoryUseCase;

    @Autowired
    private ConfigurableEnvironment environment;

    @Test
    @DisplayName("environment: the suite runs under the test profile and no other")
    void onlyTestProfileIsActive() {
        assertArrayEquals(new String[]{"test"}, environment.getActiveProfiles(),
                "A profile other than 'test' is active; a developer .env may be leaking in");
    }

    @Test
    @DisplayName("environment: a developer's .env file is never loaded into the test context")
    void dotenvIsNotLoaded() {
        assertFalse(environment.getPropertySources().contains("dotenvProperties"),
                "The .env property source is present; tests would be reading local developer config");
    }

    @Test
    @DisplayName("environment: rate limiting is off, so results never depend on how fast tests run")
    void rateLimitingIsDisabled() {
        assertEquals("false", environment.getProperty("gateway.rate-limit.enabled"));
    }
}
