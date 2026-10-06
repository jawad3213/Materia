package com.materia.backend.support;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Migration tests rebuild one module's tables in a throwaway schema and compare them with the validated schema.
 * Since V20261005_04 the cross-module references in that schema are uuid columns; this applies the same type change
 * to the throwaway tables. The foreign keys themselves are left out: the referenced tables are not in that schema.
 */
public final class ReferenceColumnTypes {

    private ReferenceColumnTypes() {
    }

    /** Converts each {@code "table.column"} of {@code schema} to uuid. */
    public static void convertToUuid(JdbcTemplate jdbc, String schema, String... tableColumns) {
        for (String tableColumn : tableColumns) {
            String[] parts = tableColumn.split("\\.");
            jdbc.execute("alter table " + schema + "." + parts[0] + " alter column " + parts[1]
                    + " type uuid using " + parts[1] + "::uuid");
        }
    }
}
