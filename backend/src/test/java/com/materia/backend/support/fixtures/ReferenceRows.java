package com.materia.backend.support.fixtures;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rows that documents reference across modules. The database enforces those references with foreign keys
 * (V20261005_04), so a fixture can no longer point at an invented supplier, category or material.
 *
 * <p>{@link #CATEGORY_ID}, {@link #SUPPLIER_ID} and {@link #MATERIAL_ID} exist in every test database (test migration
 * V20261005_90) and are the fixtures' defaults. A test that needs suppliers of its own (to tell their documents
 * apart) creates them with {@link #newSupplier(JdbcTemplate)}.
 */
public final class ReferenceRows {

    public static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-4000-8000-00000000ca01");
    public static final UUID SUPPLIER_ID = UUID.fromString("00000000-0000-4000-8000-00000000b001");
    public static final UUID MATERIAL_ID = UUID.fromString("00000000-0000-4000-8000-00000000a001");

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    private ReferenceRows() {
    }

    /** A new active supplier, inserted directly; rolled back with the test's transaction when it has one. */
    public static UUID newSupplier(JdbcTemplate jdbc) {
        UUID id = UUID.randomUUID();
        int n = SEQUENCE.getAndIncrement();
        jdbc.update("insert into public.suppliers (id, code, name, status, currency_code, created_at, version) "
                        + "values (?, ?, ?, 'ACTIVE', 'MAD', now(), 0)",
                id, "SUP-REF-" + n + "-" + id.toString().substring(0, 8), "Reference supplier " + n);
        return id;
    }
}
