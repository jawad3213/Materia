package com.materia.backend.contexts.purchaseOrder;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.UserFixtures.aReceiver;
import static org.junit.jupiter.api.Assertions.*;

/** [T024, T025] The purchase order lifecycle against the real migrated schema (US1). */
class PurchaseOrderLifecycleIT extends AbstractIntegrationTest {

    @Autowired private PurchaseOrderUseCase orders;
    @Autowired private PurchaseOrderRepository repository;
    @Autowired private UserRepository users;
    @PersistenceContext private EntityManager em;

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private PurchaseOrder reload(UUID id) {
        flushAndClear();
        return repository.findById(id).orElseThrow();
    }

    private static PurchaseOrderLineInput line(UUID id, String material, int quantity) {
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setId(id);
        line.setMaterialCode(material);
        line.setMaterialName("Material " + material);
        line.setUnitOfMeasure("PCE");
        line.setQuantity(quantity);
        line.setUnitPrice(Money.of("5.00", CurrencyCode.MAD));
        return line;
    }

    private PurchaseOrderOutput createDraft(PurchaseOrderLineInput... lines) {
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setSupplierId(UUID.randomUUID());
        input.setSupplierName("Acme Supplies");
        input.setOrderedBy("buyer-1");
        input.setCurrencyCode("MAD");
        input.setLines(new ArrayList<>(List.of(lines)));
        input.setUserId("buyer-1");
        return orders.create(input);
    }

    @Test
    @DisplayName("lifecycle: draft → submitted → confirmed → ready for receipt → tracked → cancelled is persisted at each step (US1)")
    void lifecycle_isPersistedAtEachStep() {
        User receiver = users.save(aReceiver().build());
        UUID id = createDraft(line(null, "MAT-A", 10)).getId();
        assertEquals(OrderStatus.DRAFT, reload(id).getStatus());

        orders.submit(id, "buyer-1");
        assertEquals(OrderStatus.SUBMITTED, reload(id).getStatus());

        orders.confirm(id, "admin-1");
        PurchaseOrder confirmed = reload(id);
        assertEquals(OrderStatus.CONFIRMED, confirmed.getStatus());
        assertNotNull(confirmed.getConfirmedDeliveryDate());

        orders.assignReceiver(id, "buyer-1", "Bob Buyer", receiver.getId().toString(), "ignored");
        PurchaseOrder ready = reload(id);
        assertEquals(OrderStatus.READY_FOR_RECEIPT, ready.getStatus());
        assertEquals(receiver.getId().toString(), ready.getAssignedTo());
        assertEquals(receiver.getFullName(), ready.getAssignedToName());

        orders.updateDeliveryStatus(id, "IN_TRANSIT", "buyer-1");
        PurchaseOrder tracked = reload(id);
        assertEquals(OrderStatus.READY_FOR_RECEIPT, tracked.getStatus());
        assertEquals(DeliveryStatus.IN_TRANSIT, tracked.getDeliveryStatus());

        orders.cancel(id, "buyer-1", "Supplier delay");
        PurchaseOrder cancelled = reload(id);
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertTrue(cancelled.getNotes().contains("Supplier delay"));
    }

    @Test
    @DisplayName("edit: a draft edit keeps the lines sent back with their ids and drops the lines left out (edge case)")
    void edit_keepsReturnedLinesAndDropsOthers() {
        PurchaseOrderOutput draft = createDraft(line(null, "MAT-KEEP", 4), line(null, "MAT-DROP", 2));
        UUID keptId = draft.getLines().get(0).getId();

        UpdatePurchaseOrderInput edit = new UpdatePurchaseOrderInput();
        edit.setLines(new ArrayList<>(List.of(line(keptId, "MAT-KEEP", 6), line(null, "MAT-NEW", 1))));
        edit.setUserId("buyer-1");
        orders.update(draft.getId(), edit);

        PurchaseOrder saved = reload(draft.getId());
        assertEquals(2, saved.getLines().size());
        assertTrue(saved.getLines().stream().anyMatch(l -> l.getId().equals(keptId) && l.getQuantity() == 6));
        assertTrue(saved.getLines().stream().noneMatch(l -> "MAT-DROP".equals(l.getMaterialCode())));
        assertTrue(saved.getLines().stream().anyMatch(l -> "MAT-NEW".equals(l.getMaterialCode())));
    }

    @Test
    @DisplayName("delete: a submitted order can be deleted and is gone afterwards (US1-14)")
    void delete_submittedOrder_removesIt() {
        UUID id = createDraft(line(null, "MAT-X", 1)).getId();
        orders.submit(id, "buyer-1");
        flushAndClear();

        orders.delete(id, "admin-1");

        flushAndClear();
        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    @DisplayName("F-003: cancelling with a reason that would overflow the notes is refused as invalid input, order unchanged")
    void cancel_withOverflowingReason_isRefused() {
        UUID id = createDraft(line(null, "MAT-N", 1)).getId();
        UpdatePurchaseOrderInput edit = new UpdatePurchaseOrderInput();
        edit.setNotes("n".repeat(900));
        edit.setLines(new ArrayList<>(List.of(line(null, "MAT-N", 1))));
        edit.setUserId("buyer-1");
        orders.update(id, edit);
        flushAndClear();

        assertThrows(PurchaseOrderValidationException.class, () -> {
            orders.cancel(id, "buyer-1", "r".repeat(500));
            em.flush();
        });

        flushAndClear();
        PurchaseOrder unchanged = reload(id);
        assertEquals(OrderStatus.DRAFT, unchanged.getStatus());
        assertEquals("n".repeat(900), unchanged.getNotes());
    }
}
