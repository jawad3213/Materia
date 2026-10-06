package com.materia.backend.common.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

/**
 * Converts identifiers between the domain, which carries references to other modules as text, and the database,
 * where they are UUID columns with foreign keys.
 */
public final class PersistenceIds {

    private PersistenceIds() {
    }

    /** The UUID of a reference; null when absent. A value that is not a UUID can match no row, so it is refused. */
    public static UUID toUuid(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(id.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid reference '" + id + "': expected a UUID", e);
        }
    }

    /** The UUID of a reference used to search; a value that is not a UUID matches nothing, so it gives null. */
    public static UUID toUuidOrNull(String id) {
        try {
            return toUuid(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static List<UUID> toUuids(List<String> ids) {
        return ids == null ? List.of() : ids.stream().map(PersistenceIds::toUuidOrNull).filter(java.util.Objects::nonNull).toList();
    }

    public static String toText(UUID id) {
        return id != null ? id.toString() : null;
    }

    /** A UUID read from a query result row (Object[] column), as text. */
    public static String toText(Object id) {
        return id != null ? id.toString() : null;
    }
}
