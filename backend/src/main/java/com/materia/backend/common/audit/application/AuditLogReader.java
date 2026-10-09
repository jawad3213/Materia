package com.materia.backend.common.audit.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Reads the change history. */
public interface AuditLogReader {

    /** A field before and after a change. */
    record FieldChange(Object from, Object to) {
    }

    /** One recorded change, with the name of the user who made it. */
    record AuditEntry(UUID id, LocalDateTime occurredAt, String actorId, String actorName, String entityType,
                      UUID entityId, String parentType, UUID parentId, String action, String summary, String reason,
                      Map<String, FieldChange> changes) {
    }

    /** The history of a record and of its lines (rows whose parent it is), newest first. */
    List<AuditEntry> historyOf(String entityType, UUID entityId, int limit);

    /** The latest changes across the platform, optionally of one entity type or by one user, newest first. */
    List<AuditEntry> recent(String entityType, String actorId, int limit);
}
