package com.materia.backend.contexts.dashboard.application.services;

import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * What the dashboard service decides on its own: which aggregates a user's permissions allow it to ask for, the
 * reporting currency, and how groups are ranked. The aggregates themselves are checked against PostgreSQL in
 * DashboardReadModelIT.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    private static final Set<String> RECEIVER = Set.of("requisition:read", "order:read", "receipt:read", "return:read",
            "material:read", "dashboard:read");

    @Mock private DashboardReadModel readModel;
    private DashboardService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new DashboardService(readModel, clock);
        when(readModel.receiptTotals(any())).thenReturn(DashboardReadModel.ReceiptTotals.NONE);
        when(readModel.sumOrders(any(), anyString())).thenReturn(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("access: without any module permission the dashboard is empty, defaults to MAD and queries nothing")
    void noPermissions_emptyDashboard() {
        DashboardOutput out = service.getOverview(Set.of("dashboard:read"));

        assertTrue(out.kpis().isEmpty());
        assertNull(out.monthlyTrend());
        assertNull(out.receiptQuality());
        assertTrue(out.recentActivity().isEmpty());
        assertEquals("MAD", out.currency());
        verifyNoInteractions(readModel);
    }

    @Test
    @DisplayName("access: for a receiver, invoice and payment figures are not even queried")
    void receiver_queriesNoFinance() {
        service.getOverview(RECEIVER);

        verify(readModel).documentsByCurrency(true, false, false);
        verify(readModel, never()).outstandingByDueDate(any(), any());
        verify(readModel, never()).invoicedByMonth(any(), anyString(), any(), any());
        verify(readModel, never()).paymentsByMonth(any(), anyString(), any(), any());
        verify(readModel, never()).countPayments(any());
        verify(readModel, never()).latest(DashboardReadModel.ActivitySource.INVOICE, 10);
        verify(readModel, never()).latest(DashboardReadModel.ActivitySource.PAYMENT, 10);
    }

    @Test
    @DisplayName("categories: the five largest are shown and the rest are grouped as Other")
    void spendByCategory_groupsTheRest() {
        when(readModel.documentsByCurrency(anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(Map.of("MAD", 7L));
        when(readModel.spendByCategory(any(), anyString())).thenReturn(List.of(
                group("A", 10), group("B", 70), group("C", 30), group("D", 60), group("E", 50), group("F", 40), group("G", 20)));

        List<DashboardOutput.Slice> slices = service.getOverview(Set.of("order:read")).spendByCategory();

        assertEquals(List.of("B", "D", "E", "F", "C", "OTHER"), slices.stream().map(DashboardOutput.Slice::key).toList());
        assertEquals(0, new BigDecimal("30.00").compareTo(slices.get(5).amount()), "A (10) and G (20)");
        assertEquals(2, slices.get(5).count());
    }

    @Test
    @DisplayName("currency: the most used currency wins; a tie goes to the first in alphabetical order")
    void dominantCurrency() {
        assertEquals("MAD", DashboardService.dominantCurrency(Map.of("MAD", 5L, "EUR", 2L)));
        Map<String, Long> tie = new LinkedHashMap<>();
        tie.put("MAD", 3L);
        tie.put("EUR", 3L);
        assertEquals("EUR", DashboardService.dominantCurrency(tie));
        assertEquals("MAD", DashboardService.dominantCurrency(Map.of()));
    }

    @Test
    @DisplayName("activity: only the modules the user may read are asked for their latest documents")
    void activity_followsAccess() {
        service.getOverview(Set.of("requisition:read"));

        verify(readModel).latest(DashboardReadModel.ActivitySource.REQUISITION, 10);
        verify(readModel, never()).latest(DashboardReadModel.ActivitySource.PURCHASE_ORDER, 10);
        verify(readModel, never()).stockAlerts(anyInt());
    }

    private static Group group(String name, int amount) {
        return new Group(name, name, 1, BigDecimal.valueOf(amount));
    }
}
