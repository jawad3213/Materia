package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence;

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
 * [T063–T065] The production purchase order migration installs safely everywhere (US8, FR-016, FR-017).
 *
 * <p>The suite builds its schema from {@code db/test-migration}, so nothing else ever runs the
 * production script. Each test applies it to a fresh, throwaway schema on the shared PostgreSQL
 * container: empty, pre-populated with the previous (Hibernate-generated) structure, and twice in a row.
 * The script names {@code public} explicitly, so it is retargeted at the throwaway schema first.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PurchaseOrderMigrationIT extends AbstractIntegrationTest {

    private static final Path MIGRATION =
            Path.of("src/main/resources/db/migration/V20261003_01__create_purchase_order_tables.sql");
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
        // Executed as one statement so the PL/pgSQL DO block is not split on its inner semicolons.
        jdbc.execute(retarget(Files.readString(MIGRATION, StandardCharsets.UTF_8)));
    }

    /** The previous structure: the purchase order DDL Hibernate generated under ddl-auto. */
    private void applyPreviousStructure() throws IOException {
        String ddl = Files.readString(PREVIOUS_STRUCTURE, StandardCharsets.UTF_8);
        List<String> wanted = List.of(
                "create table public.purchase_orders ",
                "create table public.purchase_order_lines ",
                "alter table if exists public.purchase_order_lines add constraint");
        for (String start : wanted) {
            Matcher m = Pattern.compile(Pattern.quote(start) + "[^;]*;").matcher(ddl);
            assertTrue(m.find(), () -> "previous structure is missing: " + start);
            jdbc.execute(retarget(m.group()));
        }
    }

    private UUID insertOrder(String code) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into " + schema + ".purchase_orders (id, created_at, order_code, status, delivery_status, "
                        + "supplier_id, supplier_name, currency_code, total_amount, notes) "
                        + "values (?, now(), ?, 'CONFIRMED', 'SHIPPED', ?, 'Acme', 'MAD', 50.0000, 'keep me')",
                id, code, UUID.randomUUID());
        jdbc.update("insert into " + schema + ".purchase_order_lines (id, created_at, purchase_order_id, line_number, "
                        + "material_code, quantity, unit_price, line_total, currency_code) "
                        + "values (?, now(), ?, 1, 'MAT-1', 10, 5.0000, 50.0000, 'MAD')",
                UUID.randomUUID(), id);
        return id;
    }

    private int count(String table) {
        return jdbc.queryForObject("select count(*) from " + schema + "." + table, Integer.class);
    }

    private boolean hasForeignKey() {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "select exists (select 1 from pg_constraint c join pg_class t on t.oid = c.conrelid "
                        + "join pg_namespace n on n.oid = t.relnamespace where c.contype = 'f' "
                        + "and n.nspname = ? and t.relname = 'purchase_order_lines')", Boolean.class, schema));
    }

    private Set<String> columns(String inSchema, String table) {
        return new TreeSet<>(jdbc.queryForList(
                "select column_name || ' ' || data_type || ' ' || is_nullable from information_schema.columns "
                        + "where table_schema = ? and table_name = ?", String.class, inSchema, table));
    }

    @Test
    @DisplayName("empty environment: the migration creates both tables, the foreign key and indexes; orders can be stored (US8-1)")
    void emptyEnvironment_isMigrated() throws IOException {
        applyMigration();

        UUID id = insertOrder("PO-2026-0001");

        assertEquals(1, count("purchase_orders"));
        assertEquals(1, count("purchase_order_lines"));
        assertTrue(hasForeignKey(), "lines must reference their order");
        Integer indexes = jdbc.queryForObject("select count(*) from pg_indexes where schemaname = ? and indexname like 'idx_po%'",
                Integer.class, schema);
        assertTrue(indexes >= 9, () -> "expected at least 9 purchase order indexes, found " + indexes);
        assertThrows(Exception.class, () -> jdbc.update("insert into " + schema + ".purchase_order_lines "
                + "(id, created_at, purchase_order_id) values (?, now(), ?)", UUID.randomUUID(), UUID.randomUUID()),
                "a line for a missing order must be refused");
        assertEquals("keep me", jdbc.queryForObject("select notes from " + schema + ".purchase_orders where id = ?",
                String.class, id));
    }

    @Test
    @DisplayName("existing environment: the migration leaves every order and line from the previous structure intact (US8-2)")
    void existingEnvironment_keepsData() throws IOException {
        applyPreviousStructure();
        insertOrder("PO-2026-0001");
        insertOrder("PO-2026-0002");
        UUID third = insertOrder("PO-2026-0003");
        List<Map<String, Object>> before = jdbc.queryForList("select * from " + schema + ".purchase_orders order by order_code");

        applyMigration();

        assertEquals(before, jdbc.queryForList("select * from " + schema + ".purchase_orders order by order_code"));
        assertEquals(3, count("purchase_order_lines"));
        assertEquals("SHIPPED", jdbc.queryForObject("select delivery_status from " + schema + ".purchase_orders where id = ?",
                String.class, third));
        assertEquals(1, jdbc.queryForObject("select count(*) from pg_constraint c join pg_class t on t.oid = c.conrelid "
                + "join pg_namespace n on n.oid = t.relnamespace where c.contype = 'f' and n.nspname = ? "
                + "and t.relname = 'purchase_order_lines'", Integer.class, schema), "no duplicate foreign key");
    }

    @Test
    @DisplayName("re-application: running the migration a second time succeeds and changes nothing (US8-3)")
    void reapplication_isHarmless() throws IOException {
        applyMigration();
        insertOrder("PO-2026-0001");

        assertDoesNotThrow(this::applyMigration);

        assertEquals(1, count("purchase_orders"));
        assertTrue(hasForeignKey());
    }

    @Test
    @DisplayName("structure: the migrated columns match the schema the entities are validated against (FR-016, FR-017)")
    void structure_matchesValidatedSchema() throws IOException {
        applyMigration();

        for (String table : List.of("purchase_orders", "purchase_order_lines")) {
            Set<String> migrated = columns(schema, table);
            Set<String> validated = columns("public", table);
            assertEquals(validated, migrated, () -> table + " differs from the schema ddl-auto=validate checks");
        }
    }
}
