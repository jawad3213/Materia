package com.materia.backend.common.audit.infrastructure;

import com.materia.backend.common.domain.ports.out.UserDisplayNames;
import org.springframework.beans.factory.ObjectProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.common.audit.application.AuditLogReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The change history from the audit_log table. Actor names come from the identity context ({@link UserDisplayNames}),
 * not from a join on its table, so the history keeps working once identity is a separate service.
 */
@Component
public class JdbcAuditLogReader implements AuditLogReader {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<LinkedHashMap<String, Map<String, Object>>> CHANGES = new TypeReference<>() {
    };
    private static final String SELECT = "select a.id, a.occurred_at, a.actor_id, "
            + "a.entity_type, a.entity_id, a.parent_type, "
            + "a.parent_id, a.action, a.summary, a.reason, a.changes::text as changes "
            + "from audit_log a ";

    private final JdbcTemplate jdbc;
    private final ObjectProvider<UserDisplayNames> userNames;

    public JdbcAuditLogReader(JdbcTemplate jdbc, ObjectProvider<UserDisplayNames> userNames) {
        this.jdbc = jdbc;
        this.userNames = userNames;
    }

    /** Fills in each entry's actor name, asking once per distinct actor. */
    private List<AuditEntry> named(List<AuditEntry> entries) {
        UserDisplayNames names = userNames.getIfAvailable();
        if (names == null) return entries;
        Map<String, String> byActor = new java.util.HashMap<>();
        return entries.stream().map(e -> new AuditEntry(e.id(), e.occurredAt(), e.actorId(),
                e.actorId() == null ? null : byActor.computeIfAbsent(e.actorId(), id -> names.displayName(id).orElse(null)),
                e.entityType(), e.entityId(), e.parentType(), e.parentId(), e.action(), e.summary(), e.reason(),
                e.changes())).toList();
    }

    @Override
    public List<AuditEntry> historyOf(String entityType, UUID entityId, int limit) {
        return named(jdbc.query(SELECT
                        + "where (a.entity_type = ? and a.entity_id = ?) or (a.parent_type = ? and a.parent_id = ?) "
                        + "order by a.occurred_at desc limit ?",
                ROW, entityType, entityId, entityType, entityId, limit));
    }

    @Override
    public List<AuditEntry> recent(String entityType, String actorId, int limit) {
        StringBuilder where = new StringBuilder("where 1 = 1 ");
        List<Object> args = new ArrayList<>();
        if (entityType != null && !entityType.isBlank()) {
            where.append("and a.entity_type = ? ");
            args.add(entityType);
        }
        if (actorId != null && !actorId.isBlank()) {
            where.append("and a.actor_id = ? ");
            args.add(actorId);
        }
        args.add(limit);
        return named(jdbc.query(SELECT + where + "order by a.occurred_at desc limit ?", ROW, args.toArray()));
    }

    private static final RowMapper<AuditEntry> ROW = (rs, n) -> new AuditEntry(
            rs.getObject("id", UUID.class),
            toLocal(rs.getTimestamp("occurred_at")),
            rs.getString("actor_id"),
            null,
            rs.getString("entity_type"),
            rs.getObject("entity_id", UUID.class),
            rs.getString("parent_type"),
            rs.getObject("parent_id", UUID.class),
            rs.getString("action"),
            rs.getString("summary"),
            rs.getString("reason"),
            changes(rs.getString("changes")));

    private static java.time.LocalDateTime toLocal(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private static Map<String, FieldChange> changes(String json) {
        Map<String, FieldChange> result = new LinkedHashMap<>();
        if (json == null || json.isBlank()) return result;
        try {
            JSON.readValue(json, CHANGES).forEach((field, change) ->
                    result.put(field, new FieldChange(change.get("from"), change.get("to"))));
        } catch (Exception e) {
            // An unreadable entry still shows its summary.
        }
        return result;
    }
}
