package com.materia.backend.common.application.metrics;

import java.time.LocalDateTime;
import java.util.UUID;

/** One of a context's latest documents, for an activity feed. */
public record RecentDocument(UUID id, String code, String title, String status, LocalDateTime createdAt) {
}
