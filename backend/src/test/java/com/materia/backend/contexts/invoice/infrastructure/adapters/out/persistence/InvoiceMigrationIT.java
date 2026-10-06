package com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence;

import com.materia.backend.support.ReferenceColumnTypes;
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
 * The production invoice migration installs safely everywhere: on an empty schema, over the structure
 * Hibernate generated under ddl-auto, and twice in a row. Same approach as {@code PurchaseOrderMigrationIT}:
 * the script is retargeted at a throwaway schema on the shared PostgreSQL container.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class InvoiceMigrationIT extends AbstractIntegrationTest {

    private static final Path MIGRATION =
            Path.of("src/main/resources/db/migration/V20261004_01__create_invoice_tables.sql");
    /** Later invoice migrations, applied after the tables exist to reach the current structure. */
    private static final Path PRICE_MATCH_MIGRATION =
            Path.of("src/main/resources/db/migration/V20261005_02__add_invoice_line_price_match.sql");
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
        // One statement, so the PL/pgSQL DO block is not split on its inner semicolons.
        jdbc.execute(retarget(Files.readString(MIGRATION, StandardCharsets.UTF_8)));
    }

    private void applyPreviousStructure() throws IOException {
        String ddl = Files.readString(PREVIOUS_STRUCTURE, StandardCharsets.UTF_8);
        for (String start : List.of("create table public.invoices ", "create table public.invoice_lines ",
                "alter table if exists public.invoice_lines add constraint")) {
            Matcher m = Pattern.compile(Pattern.quote(start) + "[^;]*;").matcher(ddl);
            assertTrue(m.find(), () -> "previous structure is missing: " + start);
            jdbc.execute(retarget(m.group()));
        }
    }

    private UUID insertInvoice(String code) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into " + schema + ".invoices (id, invoice_code, invoice_type, status, supplier_id, supplier_name, "
                        + "invoice_date, currency_code, total_amount, total_tax_amount, total_amount_with_tax, is_verified, "
                        + "has_discrepancy, created_at, notes) values (?, ?, 'STANDARD', 'SUBMITTED', 'sup-1', 'Acme', "
                        + "current_date, 'MAD', 30.0000, 2.0000, 32.0000, false, false, now(), 'keep me')",
                id, code);
        jdbc.update("insert into " + schema + ".invoice_lines (id, invoice_id, line_number, material_name, quantity_invoiced, "
                        + "unit_price, line_total, currency_code, has_quantity_discrepancy, created_at) "
                        + "values (?, ?, 1, 'Bolts', 6, 5.0000, 30.0000, 'MAD', false, now())",
                UUID.randomUUID(), id);
        return id;
    }

    private int count(String table) {
        return jdbc.queryForObject("select count(*) from " + schema + "." + table, Integer.class);
    }

    private int foreignKeys() {
        return jdbc.queryForObject("select count(*) from pg_constraint c join pg_class t on t.oid = c.conrelid "
                + "join pg_namespace n on n.oid = t.relnamespace where c.contype = 'f' and n.nspname = ? "
                + "and t.relname = 'invoice_lines'", Integer.class, schema);
    }

    private Set<String> columns(String inSchema, String table) {
        return new TreeSet<>(jdbc.queryForList(
                "select column_name || ' ' || data_type || ' ' || is_nullable from information_schema.columns "
                        + "where table_schema = ? and table_name = ?", String.class, inSchema, table));
    }

    @Test
    @DisplayName("empty environment: the migration creates both tables, the foreign key and indexes; invoices can be stored")
    void emptyEnvironment_isMigrated() throws IOException {
        applyMigration();

        UUID id = insertInvoice("INV-2026-0001");

        assertEquals(1, count("invoices"));
        assertEquals(1, count("invoice_lines"));
        assertEquals(1, foreignKeys(), "lines must reference their invoice");
        Integer indexes = jdbc.queryForObject(
                "select count(*) from pg_indexes where schemaname = ? and indexname like 'idx_invoice%'", Integer.class, schema);
        assertTrue(indexes >= 5, () -> "expected at least 5 invoice indexes, found " + indexes);
        assertThrows(Exception.class, () -> insertInvoice("INV-2026-0001"), "invoice codes are unique");
        assertEquals("keep me", jdbc.queryForObject("select notes from " + schema + ".invoices where id = ?", String.class, id));
    }

    @Test
    @DisplayName("existing environment: the migration leaves every invoice and line from the previous structure intact")
    void existingEnvironment_keepsData() throws IOException {
        applyPreviousStructure();
        insertInvoice("INV-2026-0001");
        insertInvoice("INV-2026-0002");
        List<Map<String, Object>> before = jdbc.queryForList("select * from " + schema + ".invoices order by invoice_code");

        applyMigration();

        assertEquals(before, jdbc.queryForList("select * from " + schema + ".invoices order by invoice_code"));
        assertEquals(2, count("invoice_lines"));
        assertEquals(1, foreignKeys(), "no duplicate foreign key");
    }

    @Test
    @DisplayName("re-application: running the migration a second time succeeds and changes nothing")
    void reapplication_isHarmless() throws IOException {
        applyMigration();
        insertInvoice("INV-2026-0001");

        assertDoesNotThrow(this::applyMigration);

        assertEquals(1, count("invoices"));
        assertEquals(1, foreignKeys());
    }

    @Test
    @DisplayName("structure: the migrated columns match the schema the entities are validated against")
    void structure_matchesValidatedSchema() throws IOException {
        applyMigration();
        jdbc.execute(retarget(Files.readString(PRICE_MATCH_MIGRATION, StandardCharsets.UTF_8)));
        ReferenceColumnTypes.convertToUuid(jdbc, schema,
                "invoices.purchase_order_id",
                "invoices.goods_receipt_id",
                "invoices.supplier_id",
                "invoice_lines.purchase_order_line_id",
                "invoice_lines.goods_receipt_line_id");

        for (String table : List.of("invoices", "invoice_lines")) {
            assertEquals(columns("public", table), columns(schema, table),
                    () -> table + " differs from the schema ddl-auto=validate checks");
        }
    }
}
