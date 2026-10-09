package com.materia.backend.common.attachment.infrastructure.adapters.out.lookup;

import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentOwnerDirectory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Answers from the other contexts' tables directly, so the attachment context does not depend on their services.
 * Table names are fixed here, never taken from a request.
 */
@Component
public class JdbcAttachmentOwnerDirectory implements AttachmentOwnerDirectory {

    private static final Map<AttachmentOwnerType, String> TABLES = Map.of(
            AttachmentOwnerType.PURCHASE_ORDER, "purchase_orders",
            AttachmentOwnerType.GOODS_RECEIPT, "goods_receipts",
            AttachmentOwnerType.INVOICE, "invoices",
            AttachmentOwnerType.RETURN_TO_VENDOR, "return_to_vendor"
    );

    private final JdbcTemplate jdbc;

    public JdbcAttachmentOwnerDirectory(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean exists(AttachmentOwnerType ownerType, UUID ownerId) {
        Boolean found = jdbc.queryForObject(
                "select exists(select 1 from " + TABLES.get(ownerType) + " where id = ?)", Boolean.class, ownerId);
        return Boolean.TRUE.equals(found);
    }

    @Override
    public Optional<String> userDisplayName(String userId) {
        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
        return jdbc.query("select coalesce(nullif(trim(full_name), ''), email) from users where id = ?",
                        (rs, i) -> rs.getString(1), id)
                .stream().findFirst();
    }
}
