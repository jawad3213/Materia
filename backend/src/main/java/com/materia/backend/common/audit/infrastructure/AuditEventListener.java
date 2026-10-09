package com.materia.backend.common.audit.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.common.audit.application.AuditContext;
import com.materia.backend.common.audit.infrastructure.AuditedEntities.Spec;
import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.Hibernate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Records the history of the audited entities (AuditedEntities): every insert, update and delete Hibernate
 * performs, with the fields that changed (before and after), the user, and the reason in AuditContext.
 *
 * <p>Each entry is written on the transaction's own connection just before it commits, so a change and its history
 * are committed together, and a rolled-back change leaves no history.
 */
public class AuditEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    static final String SYSTEM_ACTOR = "system";
    private static final int MAX_VALUE_LENGTH = 500;
    private static final int MAX_SUMMARY_FIELDS = 5;
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String INSERT = "insert into audit_log (id, occurred_at, actor_id, entity_type, entity_id, "
            + "parent_type, parent_id, action, summary, reason, changes) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, cast(? as jsonb))";

    /** A field before and after the change. */
    public record Change(Object from, Object to) {
    }

    record Entry(UUID id, LocalDateTime occurredAt, String actor, String entityType, UUID entityId, String parentType,
                 UUID parentId, String action, String summary, String reason, Map<String, Change> changes) {
    }

    // ============================================================
    // EVENTS
    // ============================================================

    @Override
    public void onPostInsert(PostInsertEvent event) {
        spec(event.getEntity()).ifPresent(spec -> {
            String[] names = event.getPersister().getPropertyNames();
            Map<String, Change> changes = new LinkedHashMap<>();
            for (int i = 0; i < names.length; i++) {
                Object value = plain(event.getState()[i]);
                if (recordable(names[i], value) && value != null) changes.put(names[i], new Change(null, value));
            }
            String code = code(spec, names, event.getState());
            record(event.getSession(), spec, event.getEntity(), event.getId(), names, event.getState(), "CREATE",
                    "Created" + (code != null ? " " + code : ""), changes);
        });
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        spec(event.getEntity()).ifPresent(spec -> {
            String[] names = event.getPersister().getPropertyNames();
            Object[] before = event.getOldState();
            Object[] after = event.getState();
            if (before == null) return; // nothing to compare with: the change cannot be described
            Map<String, Change> changes = new LinkedHashMap<>();
            for (int i : candidates(event.getDirtyProperties(), names.length)) {
                Object from = plain(before[i]);
                Object to = plain(after[i]);
                if (recordable(names[i], from) && recordable(names[i], to) && !same(from, to)) {
                    changes.put(names[i], new Change(from, to));
                }
            }
            if (changes.isEmpty()) return;
            Change status = changes.get("status");
            String action = status != null ? "STATUS_CHANGE" : "UPDATE";
            String summary = status != null
                    ? "Status " + status.from() + " → " + status.to()
                    : "Changed " + String.join(", ", changes.keySet().stream().limit(MAX_SUMMARY_FIELDS).toList())
                    + (changes.size() > MAX_SUMMARY_FIELDS ? " and " + (changes.size() - MAX_SUMMARY_FIELDS) + " more" : "");
            record(event.getSession(), spec, event.getEntity(), event.getId(), names, after, action, summary, changes);
        });
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        spec(event.getEntity()).ifPresent(spec -> {
            String[] names = event.getPersister().getPropertyNames();
            Object[] state = event.getDeletedState();
            Map<String, Change> changes = new LinkedHashMap<>();
            if (state != null) {
                for (int i = 0; i < names.length; i++) {
                    Object value = plain(state[i]);
                    if (recordable(names[i], value) && value != null) changes.put(names[i], new Change(value, null));
                }
            }
            String code = state != null ? code(spec, names, state) : null;
            record(event.getSession(), spec, event.getEntity(), event.getId(), names, state, "DELETE",
                    "Deleted" + (code != null ? " " + code : ""), changes);
        });
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    // ============================================================
    // RECORDING
    // ============================================================

    private void record(EventSource session, Spec spec, Object entity, Object id, String[] names,
                        Object[] state, String action, String summary, Map<String, Change> changes) {
        UUID entityId = id instanceof UUID uuid ? uuid : null;
        if (entityId == null) return;
        String parentType = null;
        UUID parentId = null;
        if (spec.isLine() && state != null) {
            Object parent = valueOf(names, state, spec.parentProperty());
            if (parent instanceof BaseJpaEntity parentEntity) {
                parentType = spec.parentType();
                parentId = parentEntity.getId();
            }
        } else if ("Attachment".equals(spec.type()) && state != null) {
            Object ownerType = valueOf(names, state, "ownerType");
            Object ownerId = valueOf(names, state, "ownerId");
            if (ownerType != null && ownerId instanceof UUID owner) {
                parentType = AuditedEntities.historyTypeOf(ownerType.toString());
                parentId = parentType != null ? owner : null;
            }
        }
        Entry entry = new Entry(UUID.randomUUID(), LocalDateTime.now(), actor(), spec.type(), entityId, parentType, parentId,
                action, truncate(summary, 500), AuditContext.reason(), changes);
        session.getActionQueue().registerProcess((BeforeTransactionCompletionProcess) s -> write(s, entry));
    }

    private static void write(SharedSessionContractImplementor session, Entry entry) {
        String changes;
        try {
            changes = JSON.writeValueAsString(entry.changes());
        } catch (JsonProcessingException e) {
            changes = "{}";
        }
        String json = changes;
        session.doWork(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                statement.setObject(1, entry.id());
                statement.setTimestamp(2, Timestamp.valueOf(entry.occurredAt()));
                statement.setString(3, entry.actor());
                statement.setString(4, entry.entityType());
                statement.setObject(5, entry.entityId());
                statement.setString(6, entry.parentType());
                statement.setObject(7, entry.parentId());
                statement.setString(8, entry.action());
                statement.setString(9, entry.summary());
                statement.setString(10, entry.reason());
                statement.setString(11, json);
                statement.executeUpdate();
            }
        });
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private static Optional<Spec> spec(Object entity) {
        if (entity == null || AuditContext.suspended()) return Optional.empty();
        Class<?> type = Hibernate.getClassLazy(entity);
        return AuditedEntities.of(type);
    }

    /** The user making the change: the authenticated principal (a user id), or "system" for jobs and start-up work. */
    static String actor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken || auth.getName() == null) {
            return SYSTEM_ACTOR;
        }
        return auth.getName();
    }

    private static int[] candidates(int[] dirty, int count) {
        if (dirty != null) return dirty;
        int[] all = new int[count];
        for (int i = 0; i < count; i++) all[i] = i;
        return all;
    }

    private static boolean recordable(String name, Object value) {
        return !AuditedEntities.IGNORED_PROPERTIES.contains(name) && value != SKIP;
    }

    private static final Object SKIP = new Object();

    /** A value as it is shown in the history; collections and other entities are not field values and are skipped. */
    static Object plain(Object value) {
        if (value == null) return null;
        if (value instanceof Collection<?> || value instanceof Map<?, ?> || value instanceof BaseJpaEntity) return SKIP;
        if (value instanceof BigDecimal decimal) return decimal.stripTrailingZeros().toPlainString();
        if (value instanceof Number || value instanceof Boolean) return value;
        if (value instanceof Enum<?> e) return e.name();
        if (value instanceof TemporalAccessor || value instanceof UUID) return value.toString();
        if (value instanceof CharSequence text) return truncate(text.toString(), MAX_VALUE_LENGTH);
        return truncate(value.toString(), MAX_VALUE_LENGTH);
    }

    private static boolean same(Object a, Object b) {
        if (a instanceof Number x && b instanceof Number y) {
            return new BigDecimal(x.toString()).compareTo(new BigDecimal(y.toString())) == 0;
        }
        return Objects.equals(a, b);
    }

    private static String code(Spec spec, String[] names, Object[] state) {
        if (spec.codeProperty() == null) return null;
        Object code = valueOf(names, state, spec.codeProperty());
        return code != null ? code.toString() : null;
    }

    private static Object valueOf(String[] names, Object[] state, String property) {
        List<String> list = List.of(names);
        int i = list.indexOf(property);
        return i >= 0 ? state[i] : null;
    }

    private static String truncate(String value, int length) {
        return value == null || value.length() <= length ? value : value.substring(0, length - 1) + "…";
    }
}
