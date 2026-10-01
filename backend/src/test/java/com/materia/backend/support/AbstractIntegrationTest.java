package com.materia.backend.support;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base for tests that need the full application context and a real database.
 *
 * <p><strong>One container for the whole suite.</strong> Starting PostgreSQL is the dominant
 * cost of an integration test, so a single container is started once, in a static
 * initialiser, and shared by every subclass. It is not annotated {@code @Container}, which
 * would stop and restart it for each test class. Testcontainers removes it when the JVM
 * exits (SC-005).
 *
 * <p><strong>Isolation by rollback.</strong> Each test runs in a transaction that is rolled
 * back afterwards, so no test sees another's data (FR-013). This does not cover work that
 * commits on its own: {@code REQUIRES_NEW} propagation, asynchronous event listeners, or
 * code on another thread. A test exercising those must clean up what it commits in an
 * {@code @AfterEach}.
 *
 * <p><strong>Schema.</strong> Flyway applies {@code classpath:db/test-migration} at startup,
 * then {@code ddl-auto=validate} checks every entity against the result. Any drift between
 * an entity and its migration fails context startup, and so fails every integration test.
 *
 * <p>The image matches production's major version: PostgreSQL 16 in both RDS (16.1) and the
 * local docker-compose setup.
 *
 * <p>The seed migration creates admin@, purchaser@ and receiver@materia.com. Use unique
 * addresses from the fixture builders to avoid colliding with them.
 */
@SpringBootTest
@Transactional
@ExtendWith(DockerRequiredInCi.class)
public abstract class AbstractIntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("materia_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
