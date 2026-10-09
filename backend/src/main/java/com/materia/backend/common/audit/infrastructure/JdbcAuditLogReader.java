package com.materia.backend.common.audit.infrastructure;

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

/** The change history from the audit_log table, with the actor's name from the users table. */
@Component
public class JdbcAuditLogReader implements AuditLogReader {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<LinkedHashMap<String, Map<String, Object>>> CHANGES = new TypeReference<>() {
    };
    private static final String SELECT = "select a.id, a.occurred_at, a.actor_id, "
            + "coalesce(nullif(u.full_name, ''), u.email) as actor_name, a.entity_type, a.entity_id, a.parent_type, "
            + "a.parent_id, a.action, a.summary, a.reason, a.changes::text as changes "
            + "from audit_log a left join users u on u.id::text = a.actor_id ";

    private final JdbcTemplate jdbc;

    public JdbcAuditLogReader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<AuditEntry> historyOf(String entityType, UUID entityId, int limit) {
        return jdbc.query(SELECT
                        + "where (a.entity_type = ? and a.entity_id = ?) or (a.parent_type = ? and a.parent_id = ?) "
                        + "order by a.occurred_at desc limit ?",
                ROW, entityType, entityId, entityType, entityId, limit);
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
        return jdbc.query(SELECT + where + "order by a.occurred_at desc limit ?", ROW, args.toArray());
    }

    private static final RowMapper<AuditEntry> ROW = (rs, n) -> new AuditEntry(
            rs.getObject("id", UUID.class),
            toLocal(rs.getTimestamp("occurred_at")),
            rs.getString("actor_id"),
            rs.getString("actor_name"),
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
