package com.materia.backend.support;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves the integration infrastructure works before any story builds on it (T031).
 *
 * <p>If this class passes, the container started, every test migration applied, and
 * {@code ddl-auto=validate} accepted the result for all 22 mapped entities. Validation
 * runs during context startup, so reaching any test method here is itself that proof.
 */
class InfrastructureSmokeTest extends AbstractIntegrationTest {

    /** The 7 original migrations, the drift fix, and the 3 table migrations. */
    private static final int EXPECTED_MIGRATIONS = 12;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("schema: every test migration applied successfully, in order")
    void allMigrationsApplied() {
        MigrationInfo[] applied = flyway.info().applied();

        assertEquals(EXPECTED_MIGRATIONS, applied.length,
                () -> "Applied: " + Arrays.stream(applied).map(MigrationInfo::getScript).toList());
        assertTrue(Arrays.stream(applied).allMatch(m -> m.getState().isApplied()),
                "A migration was recorded but did not apply cleanly");
    }

    @Test
    @DisplayName("schema: all 22 entity tables exist, including out-of-scope ones that validation requires")
    void allEntityTablesExist() {
        Integer tables = jdbc.queryForObject(
                "select count(*) from information_schema.tables "
                        + "where table_schema = 'public' and table_type = 'BASE TABLE' "
                        + "and table_name <> 'flyway_schema_history'",
                Integer.class);

        assertEquals(22, tables);
    }

    @Test
    @DisplayName("schema: the drift fix restored the columns the original migrations omitted (FINDING-010)")
    void driftFixColumnsExist() {
        List<String> missing = List.of(
                        "users.must_change_password",
                        "refresh_tokens.created_by",
                        "refresh_tokens.updated_by")
                .stream()
                .filter(qualified -> {
                    String[] parts = qualified.split("\\.");
                    Integer found = jdbc.queryForObject(
                            "select count(*) from information_schema.columns "
                                    + "where table_schema = 'public' and table_name = ? and column_name = ?",
                            Integer.class, parts[0], parts[1]);
                    return found == null || found == 0;
                })
                .toList();

        assertTrue(missing.isEmpty(), () -> "Columns still missing: " + missing);
    }

    @Test
    @DisplayName("schema: seed accounts exist, so fixtures must use unique emails to avoid them")
    void seedAccountsExist() {
        Integer seeded = jdbc.queryForObject(
                "select count(*) from public.users where email in "
                        + "('admin@materia.com','purchaser@materia.com','receiver@materia.com')",
                Integer.class);

        assertEquals(3, seeded);
    }
}
