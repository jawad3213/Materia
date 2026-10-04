package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.StockStatus;
import com.materia.backend.contexts.masterData.domain.events.MaterialBelowReorderPointEvent;
import com.materia.backend.contexts.masterData.domain.events.MaterialReorderedEvent;
import com.materia.backend.contexts.purchaseRequisition.application.services.RequisitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** [T079] Reorder orchestration paths and the stock domain service (feature 001 coverage, F-020). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReorderServiceCoverageTest {

    @Mock private com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository materials;
    @Mock private NotificationService notifications;
    @Mock private RequisitionService requisitions;
    @Mock private ApplicationEventPublisher events;

    private final MaterialStockDomainService stock = new MaterialStockDomainService();
    private ReorderService reorder;

    @BeforeEach
    void setUp() {
        reorder = new ReorderService(materials, stock, notifications, requisitions, events);
        when(requisitions.createRequisitionFromReorder(any(), anyInt(), anyString(), anyBoolean())).thenReturn("req-1");
        when(requisitions.createRequisitionFromReorder(any(), anyInt(), anyString(), anyBoolean(), any())).thenReturn("req-1");
    }

    private Material stored(Material m) {
        m.setId(UUID.randomUUID());
        when(materials.findById(m.getId())).thenReturn(Optional.of(m));
        return m;
    }

    // ---- Automatic (event-driven) ----

    @Test
    @DisplayName("auto-reorder: no recommendation means no requisition, no stock on order, no notification")
    void auto_noRecommendation_doesNothing() {
        Material m = stored(aMaterial().stock(50).reorderPoint(20).build());

        reorder.onMaterialBelowReorderPoint(new MaterialBelowReorderPointEvent(m.getId(), "MAT-1", "Bolts",
                50, 20, 5, null, StockStatus.IN_STOCK));

        verifyNoInteractions(requisitions, notifications, events);
    }

    @Test
    @DisplayName("auto-reorder: an event for a material that no longer exists fails")
    void auto_missingMaterial_fails() {
        UUID id = UUID.randomUUID();
        when(materials.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reorder.onMaterialBelowReorderPoint(
                new MaterialBelowReorderPointEvent(id, "MAT-1", "Bolts", 1, 20, 5, null, StockStatus.CRITICAL)));
    }

    @Test
    @DisplayName("auto-reorder: an urgent shortfall raises a requisition, records stock on order, publishes and notifies")
    void auto_urgent_raisesEverything() {
        Material m = stored(aMaterial().stock(2).reorderPoint(20).safety(5).build());

        reorder.onMaterialBelowReorderPoint(new MaterialBelowReorderPointEvent(m.getId(), "MAT-1", "Bolts",
                2, 20, 5, null, StockStatus.CRITICAL));

        verify(requisitions).createRequisitionFromReorder(eq(m), anyInt(), anyString(), eq(true));
        assertEquals(0, m.getStockOnOrder(), "stock on order is recorded by the purchase order, not the requisition");
        verify(events).publishEvent(any(MaterialReorderedEvent.class));
        verify(notifications).sendAlert(anyString(), contains("automatique"), contains("OUI"));
    }

    // ---- Manual (1-click) ----

    @Test
    @DisplayName("manual reorder: without a quantity, the recommendation is used, with the caller's reason when given")
    void manual_usesRecommendation() {
        Material m = stored(aMaterial().stock(10).reorderPoint(20).safety(5).build());

        assertEquals("req-1", reorder.triggerManualReorder(m.getId(), null, "Customer rush"));

        verify(requisitions).createRequisitionFromReorder(eq(m), eq(100), eq("Customer rush"), eq(false), isNull());
    }

    @Test
    @DisplayName("manual reorder: without a quantity or recommendation, the EOQ is ordered, or 100 when none is set")
    void manual_fallsBackToEoqThen100() {
        Material withEoq = stored(aMaterial().stock(50).reorderPoint(20).build());
        withEoq.setEconomicOrderQuantity(40);
        reorder.triggerManualReorder(withEoq.getId(), 0, " ");
        verify(requisitions).createRequisitionFromReorder(eq(withEoq), eq(40), contains("manuel"), eq(false), isNull());

        Material noEoq = stored(aMaterial().stock(50).reorderPoint(20).build());
        noEoq.setEconomicOrderQuantity(null);
        reorder.triggerManualReorder(noEoq.getId(), null, null);
        verify(requisitions).createRequisitionFromReorder(eq(noEoq), eq(100), anyString(), eq(false), isNull());
    }

    @Test
    @DisplayName("manual reorder: a custom quantity on a critical material is urgent, with the default reason when none given")
    void manual_customQuantity_criticalIsUrgent() {
        Material m = stored(aMaterial().stock(3).reorderPoint(20).safety(5).build());

        reorder.triggerManualReorder(m.getId(), 12, null);

        verify(requisitions).createRequisitionFromReorder(eq(m), eq(12), contains("manuel"), eq(true), isNull());
        ArgumentCaptor<MaterialReorderedEvent> event = ArgumentCaptor.forClass(MaterialReorderedEvent.class);
        verify(events).publishEvent(event.capture());
        assertEquals(12, event.getValue().getReorderQuantity());
        assertEquals(0, m.getStockOnOrder(), "stock on order is recorded by the purchase order, not the requisition");
    }

    // ---- Nightly ----

    @Test
    @DisplayName("nightly: with nothing to reorder, no requisition and no report are produced")
    void nightly_nothingToReorder() {
        when(materials.findByStatus(MaterialStatus.ACTIVE)).thenReturn(List.of(aMaterial().stock(500).reorderPoint(20).build()));

        reorder.nightlyReorderCheck();

        verifyNoInteractions(requisitions, notifications);
    }

    @Test
    @DisplayName("nightly: materials needing reorder are grouped into one requisition per supplier and a summary is sent")
    void nightly_groupsBySupplier() {
        Material a = aMaterial().stock(15).reorderPoint(20).safety(5).supplier("sup-1").build();
        Material b = aMaterial().stock(12).reorderPoint(20).safety(5).supplier("sup-1").build();
        Material c = aMaterial().stock(18).reorderPoint(20).safety(5).supplier("sup-2").build();
        when(materials.findByStatus(MaterialStatus.ACTIVE)).thenReturn(List.of(a, b, c));

        reorder.nightlyReorderCheck();

        verify(requisitions).createGroupedRequisition(eq("sup-1"), argThat(lines -> lines.size() == 2));
        verify(requisitions).createGroupedRequisition(eq("sup-2"), argThat(lines -> lines.size() == 1));
        verify(notifications).sendReport(anyString(), contains("Résumé"), contains(a.getCode().getValue()));
    }

    @Test
    @DisplayName("F-020: a material without a supplier gets its own requisition during the nightly check")
    void nightly_materialWithoutSupplier_getsOwnRequisition() {
        Material lone = aMaterial().stock(15).reorderPoint(20).safety(5).build();
        lone.setSupplierId(null);
        when(materials.findByStatus(MaterialStatus.ACTIVE)).thenReturn(List.of(lone));

        reorder.nightlyReorderCheck();

        verify(requisitions).createRequisitionFromReorder(eq(lone), anyInt(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("automatic reorder: a material with a requisition already in progress gets no second one, by event or at night")
    void openRequisition_preventsASecondOne() {
        Material m = stored(aMaterial().stock(2).reorderPoint(20).safety(5).build());
        when(requisitions.hasOpenRequisitionFor(eq(m.getId()), any())).thenReturn(true);
        when(materials.findByStatus(MaterialStatus.ACTIVE)).thenReturn(List.of(m));

        reorder.onMaterialBelowReorderPoint(new MaterialBelowReorderPointEvent(m.getId(), "MAT-1", "Bolts",
                2, 20, 5, null, StockStatus.CRITICAL));
        reorder.nightlyReorderCheck();

        verify(requisitions, never()).createRequisitionFromReorder(any(), anyInt(), anyString(), anyBoolean());
        verify(requisitions, never()).createGroupedRequisition(any(), any());
    }

    @Test
    @DisplayName("manual reorder: the user who clicked becomes the requester, even when a requisition is already in progress")
    void manualReorder_requesterIsTheUser() {
        Material m = stored(aMaterial().stock(10).reorderPoint(20).safety(5).build());
        when(requisitions.hasOpenRequisitionFor(any(), any())).thenReturn(true);

        reorder.triggerManualReorder(m.getId(), 12, "rush", "user-7");

        verify(requisitions).createRequisitionFromReorder(eq(m), eq(12), eq("rush"), anyBoolean(), eq("user-7"));
    }

    // ---- Stock domain service ----

    @Test
    @DisplayName("stock domain: missing materials or prices yield no status, recommendation or value")
    void stockDomain_nulls() {
        assertNull(stock.getStockStatus(null));
        assertNull(stock.getRecommendedReorderQuantity(null));
        assertNull(stock.calculateStockValue(null));
        Material noPrice = aMaterial().stock(4).build();
        noPrice.setStandardPrice(null);
        assertNull(stock.calculateStockValue(noPrice));
        assertTrue(stock.getCriticalMaterials(null).isEmpty());
        assertTrue(stock.getOutOfStockMaterials(List.of()).isEmpty());
    }

    @Test
    @DisplayName("stock domain: stock value is quantity times standard price; missing stock counts as zero")
    void stockDomain_value() {
        Material m = aMaterial().stock(4).price("2.50").build();
        assertEquals(0, new java.math.BigDecimal("10").compareTo(stock.calculateStockValue(m).getAmount()));
        m.setCurrentStock(null);
        assertEquals(0, stock.calculateStockValue(m).getAmount().signum());
    }

    @Test
    @DisplayName("stock domain: a recommendation without price has no estimated cost; missing thresholds count as zero")
    void stockDomain_recommendationEdges() {
        Material m = aMaterial().stock(1).reorderPoint(20).safety(5).build();
        m.setStandardPrice(null);
        m.setEconomicOrderQuantity(null);
        var q = stock.getRecommendedReorderQuantity(m);
        assertNull(q.getEstimatedCost());
        assertTrue(q.isUrgent());
        assertEquals(100, q.getQuantity());

        Material critical = aMaterial().stock(0).reorderPoint(20).safety(5).build();
        critical.setStatus(MaterialStatus.ACTIVE);
        assertEquals(List.of(critical), stock.getOutOfStockMaterials(List.of(critical)));
    }
}
