package com.materia.backend.common.audit.application;

import java.util.function.Supplier;

/**
 * What the audit log cannot see on its own: why a change is made. The reason applies to every change recorded on
 * the current thread until it is cleared: a request's reason header (AuditReasonFilter) or the reason a business
 * action carries (an order cancelled because..., a requisition rejected because...).
 */
public final class AuditContext {

    private static final int MAX_REASON_LENGTH = 1000;
    private static final ThreadLocal<String> REASON = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SUSPENDED = new ThreadLocal<>();

    private AuditContext() {
    }

    /** Records why the changes that follow are made; a blank reason leaves the current one in place. */
    public static void because(String reason) {
        if (reason == null || reason.isBlank()) return;
        String trimmed = reason.trim();
        REASON.set(trimmed.length() > MAX_REASON_LENGTH ? trimmed.substring(0, MAX_REASON_LENGTH) : trimmed);
    }

    public static String reason() {
        return REASON.get();
    }

    public static boolean suspended() {
        return Boolean.TRUE.equals(SUSPENDED.get());
    }

    /** Runs {@code work} without recording its changes (bulk loads such as the demo data seeder). */
    public static <T> T withoutAudit(Supplier<T> work) {
        Boolean previous = SUSPENDED.get();
        SUSPENDED.set(Boolean.TRUE);
        try {
            return work.get();
        } finally {
            if (previous == null) SUSPENDED.remove(); else SUSPENDED.set(previous);
        }
    }

    public static void clear() {
        REASON.remove();
        SUSPENDED.remove();
    }
}
