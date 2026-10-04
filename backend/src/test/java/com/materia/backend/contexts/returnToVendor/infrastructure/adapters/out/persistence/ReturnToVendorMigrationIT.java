package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence;

import com.materia.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The production return-to-vendor migration installs safely on an empty schema, over the structure Hibernate
 * generated under ddl-auto (adding the chain columns), and twice in a row. Same approach as PaymentMigrationIT.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ReturnToVendorMigrationIT extends AbstractIntegrationTest {

    private static final Path MIGRATION =
            Path.of("src/main/resources/db/migration/V20261004_03__create_return_to_vendor_tables.sql");
    private static final Path PREVIOUS_STRUCTURE =
            Path.of("src/test/resources/db/test-migration/V20260925_04__create_out_of_scope_tables.sql");

    @Autowired private JdbcTemplate jdbc;

    private String schema;

    @BeforeEach
    void createSchema() {
        schema = "mig_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        jdbc.execute("create schema " + schema);
    }

    @AfterEach
    void dropSchema() {
        jdbc.execute("drop schema if exists " + schema + " cascade");
    }

    private String retarget(String sql) {
        return sql.replace("public.", schema + ".").replace("'public'", "'" + schema + "'");
    }

    private void applyMigration() throws IOException {
        jdbc.execute(retarget(Files.readString(MIGRATION, StandardCharsets.UTF_8)));
    }

    private void applyPreviousStructure() throws IOException {
        String ddl = Files.readString(PREVIOUS_STRUCTURE, StandardCharsets.UTF_8);
        for (String start : List.of("create table public.return_to_vendor ", "create table public.return_to_vendor_lines ",
                "alter table if exists public.return_to_vendor_lines add constraint")) {
            Matcher m = Pattern.compile(Pattern.quote(start) + "[^;]*;").matcher(ddl);
            assertTrue(m.find(), () -> "previous structure is missing: " + start);
            jdbc.execute(retarget(m.group()));
        }
    }

    private UUID insertReturn(String code) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into " + schema + ".return_to_vendor (id, return_code, goods_receipt_id, supplier_id, "
                + "supplier_name, status, return_reason, created_at, notes) values (?, ?, 'gr-1', 'sup-1', 'Acme', "
                + "'PENDING', 'Scratched', now(), 'keep me')", id, code);
        jdbc.update("insert into " + schema + ".return_to_vendor_lines (id, return_to_vendor_id, line_number, "
                + "material_code, material_name, quantity_to_return, rejection_reason, created_at) values "
                + "(?, ?, 1, 'MAT-1', 'Bolts', 3, 'Scratched', now())", UUID.randomUUID(), id);
        return id;
    }

    private int count(String table) {
        return jdbc.queryForObject("select count(*) from " + schema + "." + table, Integer.class);
    }

    private int foreignKeys() {
        return jdbc.queryForObject("select count(*) from pg_constraint c join pg_class t on t.oid = c.conrelid "
                + "join pg_namespace n on n.oid = t.relnamespace where c.contype = 'f' and n.nspname = ? "
                + "and t.relname = 'return_to_vendor_lines'", Integer.class, schema);
    }

    private Set<String> columns(String inSchema, String table) {
        return new TreeSet<>(jdbc.queryForList(
                "select column_name || ' ' || data_type || ' ' || is_nullable from information_schema.columns "
                        + "where table_schema = ? and table_name = ?", String.class, inSchema, table));
    }

    @Test
    @DisplayName("empty environment: the migration creates both tables, the foreign key and indexes; returns can be stored")
    void emptyEnvironment_isMigrated() throws IOException {
        applyMigration();

        insertReturn("RTN-2026-0001");

        assertEquals(1, count("return_to_vendor"));
        assertEquals(1, count("return_to_vendor_lines"));
        assertEquals(1, foreignKeys());
        assertThrows(Exception.class, () -> insertReturn("RTN-2026-0001"), "return codes are unique");
    }

    @Test
    @DisplayName("existing environment: the chain columns are added and every return and line is kept")
    void existingEnvironment_keepsDataAndAddsColumns() throws IOException {
        applyPreviousStructure();
        insertReturn("RTN-2026-0001");
        List<Map<String, Object>> before = jdbc.queryForList("select id, return_code, notes from " + schema + ".return_to_vendor");

        applyMigration();

        assertEquals(before, jdbc.queryForList("select id, return_code, notes from " + schema + ".return_to_vendor"));
        assertEquals(1, count("return_to_vendor_lines"));
        assertEquals(1, foreignKeys(), "no duplicate foreign key");
        assertTrue(columns(schema, "return_to_vendor_lines").stream().anyMatch(c -> c.startsWith("unit_price numeric")));
        assertTrue(columns(schema, "return_to_vendor").stream().anyMatch(c -> c.startsWith("currency_code ")));
    }

    @Test
    @DisplayName("re-application: running the migration a second time succeeds and changes nothing")
    void reapplication_isHarmless() throws IOException {
        applyMigration();
        insertReturn("RTN-2026-0001");

        assertDoesNotThrow(this::applyMigration);

        assertEquals(1, count("return_to_vendor"));
        assertEquals(1, foreignKeys());
    }

    @Test
    @DisplayName("structure: the migrated columns match the schema the entities are validated against")
    void structure_matchesValidatedSchema() throws IOException {
        applyMigration();

        for (String table : List.of("return_to_vendor", "return_to_vendor_lines")) {
            assertEquals(columns("public", table), columns(schema, table),
                    () -> table + " differs from the schema ddl-auto=validate checks");
        }
    }
}
