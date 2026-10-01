package com.materia.backend.support;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Deterministic assertions for code that reads the system clock.
 *
 * <p>The in-scope code calls {@code LocalDateTime.now()} directly, over 100 times, with no
 * injectable {@code Clock}. A timestamp cannot be asserted by exact value, but it can be
 * bounded: capture the time before the action and after it, and require the recorded value
 * to fall between them. That is deterministic without refactoring every call site (FR-011).
 *
 * <pre>{@code
 * LocalDateTime before = LocalDateTime.now();
 * requisition.submit(userId);
 * LocalDateTime after = LocalDateTime.now();
 * TimeAssertions.assertWithin(requisition.getSubmittedAt(), before, after);
 * }</pre>
 */
public final class TimeAssertions {

    private TimeAssertions() {
    }

    /** Asserts a timestamp was recorded within the window in which the action ran. */
    public static void assertWithin(LocalDateTime actual, LocalDateTime before, LocalDateTime after) {
        assertNotNull(actual, "Expected a timestamp to be recorded, but it was null");
        assertFalse(actual.isBefore(before), () -> "Timestamp " + actual + " precedes the action window starting " + before);
        assertFalse(actual.isAfter(after), () -> "Timestamp " + actual + " follows the action window ending " + after);
    }

    /**
     * Asserts a date was recorded within the window in which the action ran. A test that
     * straddles midnight can legitimately see either date, so both ends are inclusive.
     */
    public static void assertWithin(LocalDate actual, LocalDateTime before, LocalDateTime after) {
        assertNotNull(actual, "Expected a date to be recorded, but it was null");
        assertFalse(actual.isBefore(before.toLocalDate()), () -> "Date " + actual + " precedes " + before.toLocalDate());
        assertFalse(actual.isAfter(after.toLocalDate()), () -> "Date " + actual + " follows " + after.toLocalDate());
    }
}
