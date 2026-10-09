package com.materia.backend.common.audit;

import com.materia.backend.common.audit.application.AuditContext;
import com.materia.backend.common.audit.application.AuditLogReader;
import com.materia.backend.common.audit.application.AuditLogReader.AuditEntry;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The change history recorded by Hibernate for real service calls. History is written as a transaction commits,
 * so these tests let each service call commit ({@code NOT_SUPPORTED}) and remove what they created afterwards.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AuditLogIT extends AbstractIntegrationTest {

    private static final String USER = "7a60fe1d-a6d2-45a9-b728-95faa9a00f93";

    @Autowired private PurchaseOrderUseCase orders;
    @Autowired private AuditLogReader reader;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;

    private final List<UUID> created = new ArrayList<>();

    @BeforeEach
    void signIn() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                USER, null, List.of(new SimpleGrantedAuthority("order:write"))));
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        AuditContext.clear();
        for (UUID id : created) {
            jdbc.update("delete from audit_log where entity_id = ? or parent_id = ?", id, id);
            // These calls commit, so the events they raised are in the shared outbox: leave it as found.
            jdbc.update("delete from outbox_messages where aggregate_id = ?", id.toString());
            jdbc.update("delete from purchase_order_lines where purchase_order_id = ?", id);
            jdbc.update("delete from purchase_orders where id = ?", id);
        }
    }

    private UUID draftOrder(int quantity) {
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setMaterialCode("MAT-AUD");
        line.setMaterialName("Audited material");
        line.setUnitOfMeasure("PCE");
        line.setQuantity(quantity);
        line.setUnitPrice(Money.of("5.00", CurrencyCode.MAD));
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setSupplierId(UUID.randomUUID()); // no cross-module foreign keys: any supplier id is accepted
        input.setSupplierName("Acme Supplies");
        input.setOrderedBy("buyer-1");
        input.setCurrencyCode("MAD");
        input.setLines(new ArrayList<>(List.of(line)));
        input.setUserId("buyer-1");
        UUID id = orders.create(input).getId();
        created.add(id);
        return id;
    }

    private List<AuditEntry> history(UUID orderId) {
        return reader.historyOf("PurchaseOrder", orderId, 100);
    }

    @Test
    @DisplayName("history: creating an order records the order and its lines, with the user who did it")
    void create_isRecorded() {
        UUID id = draftOrder(10);

        List<AuditEntry> history = history(id);

        AuditEntry order = history.stream().filter(e -> e.entityType().equals("PurchaseOrder")).findFirst().orElseThrow();
        assertEquals("CREATE", order.action());
        assertTrue(order.summary().startsWith("Created PO-"), order.summary());
        assertEquals(USER, order.actorId());
        assertEquals("DRAFT", order.changes().get("status").to());
        assertNull(order.changes().get("status").from());
        assertFalse(order.changes().containsKey("version"), "bookkeeping fields are not history");

        AuditEntry line = history.stream().filter(e -> e.entityType().equals("PurchaseOrderLine")).findFirst().orElseThrow();
        assertEquals(id, line.parentId(), "a line's history belongs to its order");
        assertEquals(10, ((Number) line.changes().get("quantity").to()).intValue());
    }

    @Test
    @DisplayName("history: a cancellation records the status change, the fields it touched, and the reason given")
    void cancel_recordsWhatAndWhy() {
        UUID id = draftOrder(4);
        orders.submit(id, "buyer-1");
        orders.cancel(id, "buyer-1", "Supplier closed for inventory");

        AuditEntry latest = history(id).get(0);

        assertEquals("STATUS_CHANGE", latest.action());
        assertEquals("Status SUBMITTED → CANCELLED", latest.summary());
        assertEquals("SUBMITTED", latest.changes().get("status").from());
        assertEquals("CANCELLED", latest.changes().get("status").to());
        assertEquals("Supplier closed for inventory", latest.reason());
        assertTrue(latest.changes().containsKey("notes"), "the note the cancellation added is part of the change");

        AuditEntry submitted = history(id).get(1);
        assertEquals("Status DRAFT → SUBMITTED", submitted.summary());
        assertNull(submitted.reason(), "a reason is only recorded with the change it explains");
    }

    @Test
    @DisplayName("history: a change that is rolled back leaves no history")
    void rolledBack_leavesNoHistory() {
        UUID id = draftOrder(2);
        int before = history(id).size();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            orders.submit(id, "buyer-1");
            throw new IllegalStateException("abort");
        }));

        assertEquals(before, history(id).size());
    }

    @Test
    @DisplayName("history: work run without audit (bulk loads) is not recorded")
    void withoutAudit_isNotRecorded() {
        UUID id = AuditContext.withoutAudit(() -> draftOrder(3));

        assertTrue(history(id).isEmpty());
    }
}
