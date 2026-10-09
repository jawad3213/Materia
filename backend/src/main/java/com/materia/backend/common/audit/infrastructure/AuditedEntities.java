package com.materia.backend.common.audit.infrastructure;

import com.materia.backend.common.audit.application.Audited;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The history settings of persistence entities, read from the {@link Audited} annotation each context puts on its
 * own entities: this class names no context's classes or tables.
 */
public final class AuditedEntities {

    /** How an entity is recorded: its history type, the property that names it, and for a line its document. */
    public record Spec(String type, String codeProperty, String parentProperty, String parentType, String readPermission) {
        boolean isLine() {
            return parentProperty != null;
        }
    }

    /** Fields never recorded: bookkeeping, and secrets. */
    public static final Set<String> IGNORED_PROPERTIES = Set.of(
            "version", "createdAt", "updatedAt", "createdBy", "updatedBy", "passwordHash", "storageKey");

    private static final Map<Class<?>, Optional<Spec>> CACHE = new ConcurrentHashMap<>();

    private AuditedEntities() {
    }

    /** The history settings of a persistence entity class; empty when its changes are not recorded. */
    public static Optional<Spec> of(Class<?> entityClass) {
        return CACHE.computeIfAbsent(entityClass, AuditedEntities::read);
    }

    private static Optional<Spec> read(Class<?> entityClass) {
        for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
            Audited audited = c.getAnnotation(Audited.class);
            if (audited != null) {
                return Optional.of(new Spec(audited.type(), audited.code(),
                        audited.parent().isEmpty() ? null : audited.parent(),
                        audited.parentType().isEmpty() ? null : audited.parentType(),
                        audited.readPermission().isEmpty() ? null : audited.readPermission()));
            }
        }
        return Optional.empty();
    }

    /**
     * The history type of an attachment owner, by naming convention: {@code PURCHASE_ORDER} is {@code PurchaseOrder}.
     */
    public static String historyTypeOf(String ownerType) {
        if (ownerType == null || ownerType.isBlank()) return null;
        StringBuilder type = new StringBuilder();
        for (String word : ownerType.toLowerCase().split("_")) {
            if (!word.isEmpty()) type.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return type.toString();
    }
}
