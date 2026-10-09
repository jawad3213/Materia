package com.materia.backend.contexts.dashboard.application.services;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.fx.FxValuation;
import com.materia.backend.common.domain.fx.ReportingFx;

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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
    @Mock private ReportingFx fx;
    private DashboardService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new DashboardService(readModel, fx, clock);
        when(fx.reportingCurrency()).thenReturn(CurrencyCode.MAD);
        when(readModel.receiptTotals(any())).thenReturn(DashboardReadModel.ReceiptTotals.NONE);
        when(readModel.sumOrders(any(), any())).thenReturn(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("access: without any module permission the dashboard is empty, in the reporting currency, and queries nothing")
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
        verify(readModel, never()).outstandingByDueDate(any(), any(), any());
        verify(readModel, never()).invoicedByMonth(any(), any(), any(), any());
        verify(readModel, never()).paymentsByMonth(any(), any(), any(), any());
        verify(readModel, never()).countPayments(any());
        verify(readModel, never()).latest(DashboardReadModel.ActivitySource.INVOICE, 10);
        verify(readModel, never()).latest(DashboardReadModel.ActivitySource.PAYMENT, 10);
    }

    @Test
    @DisplayName("categories: the five largest are shown and the rest are grouped as Other")
    void spendByCategory_groupsTheRest() {
        when(readModel.documentsByCurrency(anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(Map.of("MAD", 7L));
        when(readModel.spendByCategory(any(), any())).thenReturn(List.of(
                group("A", 10), group("B", 70), group("C", 30), group("D", 60), group("E", 50), group("F", 40), group("G", 20)));

        List<DashboardOutput.Slice> slices = service.getOverview(Set.of("order:read")).spendByCategory();

        assertEquals(List.of("B", "D", "E", "F", "C", "OTHER"), slices.stream().map(DashboardOutput.Slice::key).toList());
        assertEquals(0, new BigDecimal("30.00").compareTo(slices.get(5).amount()), "A (10) and G (20)");
        assertEquals(2, slices.get(5).count());
    }

    @Test
    @DisplayName("currency: the configured reporting currency is used even when most documents are in another one")
    void reportingCurrency_isConfigured() {
        when(readModel.documentsByCurrency(anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(Map.of("EUR", 9L, "MAD", 1L));
        when(fx.currentRates()).thenReturn(Map.of(CurrencyCode.EUR, eur("10.80", FxValuation.LIVE)));

        DashboardOutput out = service.getOverview(Set.of("order:read"));

        assertEquals("MAD", out.currency());
        assertEquals(0, out.otherCurrencyDocuments(), "every EUR document can be converted");
        org.mockito.ArgumentCaptor<DashboardReadModel.Rates> rates = org.mockito.ArgumentCaptor.forClass(DashboardReadModel.Rates.class);
        verify(readModel).sumOrders(any(), rates.capture());
        assertEquals("MAD", rates.getValue().reportingCurrency());
        assertEquals(0, BigDecimal.ONE.compareTo(rates.getValue().current().get("MAD")));
        assertEquals(0, new BigDecimal("10.80").compareTo(rates.getValue().current().get("EUR")));
    }

    @Test
    @DisplayName("currency: the conversion note names the converted currencies, the fallback rates and what was left out")
    void conversion_isExplained() {
        when(readModel.documentsByCurrency(anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(Map.of("MAD", 4L, "EUR", 3L, "USD", 2L));
        when(readModel.documentsValuedAtOwnDate(eq("MAD"), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(Map.of("EUR", 2L));
        when(fx.currentRates()).thenReturn(Map.of(CurrencyCode.EUR, eur("10.85", FxValuation.FALLBACK)));

        DashboardOutput out = service.getOverview(Set.of("order:read"));

        assertEquals(2, out.otherCurrencyDocuments(), "the USD documents have no rate");
        DashboardOutput.CurrencyConversion conversion = out.conversion();
        assertEquals("MAD", conversion.reportingCurrency());
        assertTrue(conversion.fallbackRates());
        assertEquals(List.of("EUR", "USD"), conversion.currencies().stream().map(DashboardOutput.ConvertedCurrency::currency).toList());
        DashboardOutput.ConvertedCurrency euro = conversion.currencies().get(0);
        assertEquals(3, euro.documents());
        assertEquals(2, euro.valuedAtDocumentDate());
        assertEquals(0, new BigDecimal("10.85").compareTo(euro.latestRate()));
        assertNull(conversion.currencies().get(1).latestRate());
        assertTrue(conversion.note().contains("EUR converted to MAD at the rate of each document's date"), conversion.note());
        assertTrue(conversion.note().contains("fallback rates"), conversion.note());
        assertTrue(conversion.note().contains("2 document(s) in USD left out"), conversion.note());
    }

    @Test
    @DisplayName("currency: with documents in the reporting currency only, no rate is looked up and no note is given")
    void singleCurrency_noConversion() {
        when(readModel.documentsByCurrency(anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(Map.of("MAD", 7L));

        DashboardOutput out = service.getOverview(Set.of("order:read"));

        assertNull(out.conversion());
        verify(fx, never()).currentRates();
    }

    private static FxValuation eur(String rate, String source) {
        return new FxValuation(CurrencyCode.EUR, CurrencyCode.MAD, new BigDecimal(rate), TODAY, source);
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
