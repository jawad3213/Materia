package com.materia.backend.common.audit.infrastructure.web;

import com.materia.backend.common.infrastructure.web.CsvWriter;
import com.materia.backend.common.audit.application.AuditLogReader;
import com.materia.backend.common.audit.application.AuditLogReader.AuditEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The change history. A record's history is visible to whoever may read the record itself; the platform-wide
 * feed is for users with {@code log:read}.
 */
@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private static final int MAX_ENTRIES = 500;
    private static final int MAX_EXPORT = 5000;

    /** The permission that reads each kind of record, and therefore its history. */
    static final Map<String, String> READ_PERMISSION = Map.ofEntries(
            Map.entry("PurchaseOrder", "order:read"),
            Map.entry("Requisition", "requisition:read"),
            Map.entry("GoodsReceipt", "receipt:read"),
            Map.entry("Invoice", "invoice:read"),
            Map.entry("Payment", "payment:read"),
            Map.entry("ReturnToVendor", "return:read"),
            Map.entry("Supplier", "supplier:read"),
            Map.entry("Material", "material:read"),
            Map.entry("Category", "category:read"),
            Map.entry("User", "user:read"),
            Map.entry("Employee", "user:read"),
            Map.entry("Setting", "setting:read"));

    private final AuditLogReader reader;

    public AuditController(AuditLogReader reader) {
        this.reader = reader;
    }

    /** What happened to a record (and its lines), newest first: who, when, which fields, and why. */
    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<List<AuditEntry>> history(@PathVariable String entityType, @PathVariable UUID entityId,
                                                    Authentication authentication) {
        String permission = READ_PERMISSION.get(entityType);
        if (permission == null) {
            throw new IllegalArgumentException("Unknown record type: " + entityType);
        }
        if (!holds(authentication, permission) && !holds(authentication, "log:read")) {
            throw new AccessDeniedException("Reading the history of " + entityType + " requires " + permission);
        }
        return ResponseEntity.ok(reader.historyOf(entityType, entityId, MAX_ENTRIES));
    }

    /** The latest changes across the platform, optionally of one type of record or by one user. */
    @GetMapping
    @PreAuthorize("hasAuthority('log:read')")
    public ResponseEntity<List<AuditEntry>> recent(@RequestParam(required = false) String entityType,
                                                   @RequestParam(required = false) String actorId,
                                                   @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(reader.recent(entityType, actorId, Math.max(1, Math.min(limit, MAX_ENTRIES))));
    }

    /** The same feed as a CSV download (log:export), up to 5000 changes. */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('log:export')")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String entityType,
                                         @RequestParam(required = false) String actorId,
                                         @RequestParam(defaultValue = "1000") int limit) {
        CsvWriter csv = new CsvWriter().row("Date", "User", "User id", "Record type", "Record id", "Document type",
                "Document id", "Action", "Summary", "Reason", "Changes");
        for (AuditEntry e : reader.recent(entityType, actorId, Math.max(1, Math.min(limit, MAX_EXPORT)))) {
            csv.row(e.occurredAt(), e.actorName(), e.actorId(), e.entityType(), e.entityId(), e.parentType(), e.parentId(),
                    e.action(), e.summary(), e.reason(), changes(e));
        }
        return csv.download("audit-log-" + java.time.LocalDate.now() + ".csv");
    }

    /** "field: before -> after; ..." */
    static String changes(AuditEntry entry) {
        if (entry.changes() == null || entry.changes().isEmpty()) return "";
        return entry.changes().entrySet().stream()
                .map(c -> c.getKey() + ": " + (c.getValue().from() == null ? "" : c.getValue().from())
                        + " -> " + (c.getValue().to() == null ? "" : c.getValue().to()))
                .collect(java.util.stream.Collectors.joining("; "));
    }

    private static boolean holds(Authentication authentication, String authority) {
        return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}
