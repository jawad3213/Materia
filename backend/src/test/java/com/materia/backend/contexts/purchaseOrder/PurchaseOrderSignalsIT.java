package com.materia.backend.contexts.purchaseOrder;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderConfirmedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderDeliveredEvent;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderSubmittedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.UserFixtures.aReceiver;
import static org.junit.jupiter.api.Assertions.*;

/** [T062] Order signals reach the real Spring event bus exactly once per triggering action (US5, research R6). */
@RecordApplicationEvents
class PurchaseOrderSignalsIT extends AbstractIntegrationTest {

    @Autowired private PurchaseOrderUseCase orders;
    @Autowired private MaterialRepository materials;
    @Autowired private UserRepository users;
    @Autowired private ApplicationEvents events;

    private UUID draftOrder(Material material) {
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setMaterialId(material.getId());
        line.setMaterialCode(material.getCode().getValue());
        line.setQuantity(5);
        line.setUnitPrice(Money.of("2.00", CurrencyCode.MAD));
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setSupplierId(UUID.randomUUID());
        input.setSupplierName("Acme");
        input.setOrderedBy("buyer-1");
        input.setCurrencyCode("MAD");
        input.setLines(new ArrayList<>(List.of(line)));
        input.setUserId("buyer-1");
        return orders.create(input).getId();
    }

    @Test
    @DisplayName("signals: submit, confirm and receipt each publish exactly one event on the event bus (US5-1 to US5-3)")
    void lifecycle_publishesEachSignalOnce() {
        Material material = materials.save(aMaterial().stock(0).build());
        User receiver = users.save(aReceiver().build());
        UUID id = draftOrder(material);

        orders.submit(id, "buyer-1");
        orders.confirm(id, "admin-1");
        orders.assignReceiver(id, "buyer-1", "Bob", receiver.getId().toString(), null);
        orders.confirmReceipt(id, receiver.getId().toString(), "Rita");

        assertEquals(1, events.stream(PurchaseOrderSubmittedEvent.class).count());
        assertEquals(1, events.stream(PurchaseOrderConfirmedEvent.class).count());
        List<PurchaseOrderDeliveredEvent> delivered = events.stream(PurchaseOrderDeliveredEvent.class).toList();
        assertEquals(1, delivered.size());
        assertEquals(DeliveryStatus.DELIVERED, delivered.get(0).getDeliveryStatus());
        assertEquals(id, delivered.get(0).getPurchaseOrderId());
    }

    @Test
    @DisplayName("signals: a refused action puts nothing on the event bus (US5-4)")
    void refusedAction_publishesNothing() {
        UUID id = draftOrder(materials.save(aMaterial().stock(0).build()));

        assertThrows(RuntimeException.class, () -> orders.confirm(id, "admin-1"));

        assertEquals(0, events.stream(PurchaseOrderConfirmedEvent.class).count());
        assertEquals(0, events.stream(PurchaseOrderSubmittedEvent.class).count());
    }
}
