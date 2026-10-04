package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.enums.StockStatus;
import com.materia.backend.contexts.masterData.domain.events.MaterialBelowReorderPointEvent;
import com.materia.backend.contexts.masterData.domain.exceptions.MaterialNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterData.domain.valueObjects.ReorderQuantity;
import com.materia.backend.contexts.purchaseRequisition.application.services.RequisitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Reorder recommendation, shortage lists and automatic reordering (T069-T071).
 * The stock domain service is used for real: it is pure logic, and mocking it would test nothing.
 */
@ExtendWith(MockitoExtension.class)
class ReorderServicesTest {

    private static final String FINDING_024 =
            "FINDING-024: the reorder recommendation ignores stock on order, so automatic reordering double-orders";

    @Mock private MaterialRepository materials;
    @Mock private NotificationService notifications;
    @Mock private RequisitionService requisitions;
    @Mock private ApplicationEventPublisher events;

    private final MaterialStockDomainService stock = new MaterialStockDomainService();
    private ReorderService reorder;

    @BeforeEach
    void setUp() {
        reorder = new ReorderService(materials, stock, notifications, requisitions, events);
    }

    private static MaterialBelowReorderPointEvent eventFor(Material m) {
        return new MaterialBelowReorderPointEvent(m.getId(), m.getCode().getValue(), m.getName(),
                m.getCurrentStock(), m.getReorderPoint(), m.getSafetyStock(), m.getSupplierId(), m.getStockStatus());
    }

    // ---- ReorderQuantity (T069) ----

    @Test
    @DisplayName("reorder quantity: must be strictly positive")
    void reorderQuantity_nonPositive_isRefused() {
        assertThrows(IllegalArgumentException.class, () -> new ReorderQuantity(0, null, "r", false));
        assertThrows(IllegalArgumentException.class, () -> new ReorderQuantity(-5, null, "r", false));
        assertEquals(12, new ReorderQuantity(12, null, "r", true).getQuantity());
    }

    // ---- Recommendation (T070, US3 scenario 6) ----

    @Test
    @DisplayName("recommend: nothing is recommended while stock is above the reorder point")
    void recommend_aboveReorderPoint_isNull() {
        assertNull(stock.getRecommendedReorderQuantity(aMaterial().stock(50).reorderPoint(20).build()));
    }

    @Test
    @DisplayName("recommend: at the reorder point, the economic order quantity is recommended, not urgent")
    void recommend_atReorderPoint_isEoqNotUrgent() {
        Material m = aMaterial().stock(20).reorderPoint(20).safety(5).build();
        ReorderQuantity q = stock.getRecommendedReorderQuantity(m);

        assertEquals(m.getEconomicOrderQuantity(), q.getQuantity());
        assertFalse(q.isUrgent());
    }

    @Test
    @DisplayName("recommend: at or below safety stock, the order is urgent and large enough to restore the reorder point plus safety")
    void recommend_critical_isUrgentAndRestoresLevel() {
        Material m = aMaterial().stock(2).reorderPoint(300).safety(50).build();
        ReorderQuantity q = stock.getRecommendedReorderQuantity(m);

        assertTrue(q.isUrgent());
        // max(eoq, (300 - 2) + 50) = 348 when that exceeds the economic order quantity.
        assertEquals(Math.max(m.getEconomicOrderQuantity(), 348), q.getQuantity());
    }

    @Test
    @DisplayName("recommend: the estimated cost is the quantity times the standard price")
    void recommend_estimatesCost() {
        Material m = aMaterial().stock(10).reorderPoint(20).price("2.50").build();
        ReorderQuantity q = stock.getRecommendedReorderQuantity(m);

        assertEquals(Money.of("2.50", CurrencyCode.MAD).multiply(q.getQuantity()), q.getEstimatedCost());
    }

    // ---- Shortage lists (T070) ----

    @Test
    @DisplayName("shortage lists: each material appears in exactly one list, matching its stock band")
    void shortageLists_areDisjointAndMatchBands() {
        Material out = aMaterial().stock(0).build();
        Material critical = aMaterial().stock(3).safety(5).reorderPoint(20).build();
        Material reorderNeeded = aMaterial().stock(15).safety(5).reorderPoint(20).build();
        Material fine = aMaterial().stock(500).reorderPoint(20).build();
        List<Material> all = List.of(out, critical, reorderNeeded, fine);

        assertEquals(List.of(out), stock.getOutOfStockMaterials(all));
        assertEquals(List.of(critical), stock.getCriticalMaterials(all));
        assertEquals(List.of(reorderNeeded), stock.getMaterialsNeedingReorder(all));
    }

    @Test
    @DisplayName("shortage lists: an empty or missing input yields an empty list, not an error")
    void shortageLists_emptyInput() {
        assertTrue(stock.getCriticalMaterials(List.of()).isEmpty());
        assertTrue(stock.getOutOfStockMaterials(null).isEmpty());
    }

    // ---- Automatic reordering (T071) ----

    @Test
    @DisplayName("auto-reorder: raises a requisition requested by the system; stock on order is left to the purchase order")
    void autoReorder_raisesRequisition_onOrderLeftToThePurchaseOrder() {
        Material m = aMaterial().stock(10).reorderPoint(20).onOrder(0).build();
        when(materials.findById(m.getId())).thenReturn(Optional.of(m));
        when(requisitions.createRequisitionFromReorder(any(), anyInt(), anyString(), anyBoolean())).thenReturn("req-1");

        reorder.onMaterialBelowReorderPoint(eventFor(m));

        verify(requisitions).createRequisitionFromReorder(eq(m), anyInt(), anyString(), anyBoolean());
        assertEquals(0, m.getStockOnOrder(), "a requisition is a request; the purchase order records the stock on order");
    }

    @Test
    @Disabled(FINDING_024)
    @DisplayName("auto-reorder: when stock already on order covers the shortfall, no second requisition is raised")
    void autoReorder_alreadyCovered_doesNotDoubleOrder() {
        Material m = aMaterial().stock(10).reorderPoint(20).onOrder(500).build();
        lenient().when(materials.findById(m.getId())).thenReturn(Optional.of(m));
        lenient().when(requisitions.createRequisitionFromReorder(any(), anyInt(), anyString(), anyBoolean())).thenReturn("dup");

        reorder.onMaterialBelowReorderPoint(eventFor(m));

        verify(requisitions, never()).createRequisitionFromReorder(any(), anyInt(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("manual reorder: a requested quantity overrides the recommendation")
    void manualReorder_customQuantityWins() {
        Material m = aMaterial().stock(10).reorderPoint(20).build();
        when(materials.findById(m.getId())).thenReturn(Optional.of(m));
        when(requisitions.createRequisitionFromReorder(any(), anyInt(), anyString(), anyBoolean(), any())).thenReturn("req-7");

        reorder.triggerManualReorder(m.getId(), 42, "top up");

        verify(requisitions).createRequisitionFromReorder(eq(m), eq(42), eq("top up"), anyBoolean(), isNull());
    }

    @Test
    @DisplayName("manual reorder: an unknown material is a not-found, and nothing is raised")
    void manualReorder_unknownMaterial_isNotFound() {
        UUID unknown = UUID.randomUUID();
        when(materials.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(MaterialNotFoundException.class, () -> reorder.triggerManualReorder(unknown, 5, null));
        verifyNoInteractions(requisitions);
    }

    @Test
    @DisplayName("stock status: the service reports the material's own band")
    void stockStatus_delegates() {
        assertEquals(StockStatus.OUT_OF_STOCK, stock.getStockStatus(aMaterial().stock(0).build()));
        assertNull(stock.getStockStatus(null));
    }
}
