package com.materia.backend.contexts.dashboard.application.services;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.Kpi;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.invoice.domain.entities.Invoice;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.ports.out.InvoiceRepository;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.StockStatus;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.payement.domain.entities.Payment;
import com.materia.backend.contexts.payement.domain.enums.PaymentStatus;
import com.materia.backend.contexts.payement.domain.ports.out.PaymentPort;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionRepository;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.ports.out.ReturnToVendorRepository;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** The dashboard figures, computed from the documents of each module and limited by the user's permissions. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    private static final Set<String> ADMIN = Set.of("requisition:read", "order:read", "receipt:read", "invoice:read",
            "payment:read", "return:read", "material:read", "dashboard:read");
    private static final Set<String> RECEIVER = Set.of("requisition:read", "order:read", "receipt:read", "return:read",
            "material:read", "dashboard:read");

    @Mock private RequisitionRepository requisitions;
    @Mock private PurchaseOrderRepository orders;
    @Mock private GoodsReceiptRepository receipts;
    @Mock private InvoiceRepository invoices;
    @Mock private PaymentPort payments;
    @Mock private ReturnToVendorRepository returns;
    @Mock private MaterialRepository materials;

    private DashboardService service;
    private final UUID acme = UUID.randomUUID();
    private final UUID globex = UUID.randomUUID();
    private final UUID boltsId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new DashboardService(requisitions, orders, receipts, invoices, payments, returns, materials, clock);

        List<?> requisitionsData = List.of(
                requisition(RequisitionStatus.SUBMITTED, 1), requisition(RequisitionStatus.SUBMITTED, 2), requisition(RequisitionStatus.APPROVED, 3));
        doReturn(requisitionsData).when(requisitions).findAll();
        List<?> ordersData = List.of(
                order(acme, "Acme", OrderStatus.CONFIRMED, "1000.00", "MAD", TODAY.minusDays(3), TODAY.minusDays(1), "bolts"),
                order(acme, "Acme", OrderStatus.COMPLETED, "500.00", "MAD", TODAY.minusMonths(2), TODAY.minusMonths(1), "bolts"),
                order(globex, "Globex", OrderStatus.READY_FOR_RECEIPT, "300.00", "MAD", TODAY.minusDays(10), TODAY.plusDays(5), "paint"),
                order(globex, "Globex", OrderStatus.CANCELLED, "999.00", "MAD", TODAY.minusDays(9), null, "paint"),
                order(globex, "Globex", OrderStatus.CONFIRMED, "80.00", "EUR", TODAY.minusDays(2), null, "paint"));
        doReturn(ordersData).when(orders).findAll();
        List<?> receiptsData = List.of(
                receipt(acme, ReceiptStatus.COMPLETED, 10, 8, TODAY.minusDays(1), TODAY),
                receipt(acme, ReceiptStatus.PARTIAL, 10, 10, TODAY.minusMonths(1), TODAY.minusMonths(1)),
                receipt(globex, ReceiptStatus.DRAFT, 5, 5, null, null));
        doReturn(receiptsData).when(receipts).findAll();
        List<?> invoicesData = List.of(
                invoice(InvoiceType.STANDARD, InvoiceStatus.VERIFIED, "400.00", "400.00", TODAY.minusDays(40), TODAY.minusDays(30)),
                invoice(InvoiceType.STANDARD, InvoiceStatus.SUBMITTED, "200.00", "150.00", TODAY.plusDays(10), TODAY.minusDays(5)),
                invoice(InvoiceType.CREDIT_NOTE, InvoiceStatus.VERIFIED, "50.00", "50.00", null, TODAY.minusDays(1)),
                invoice(InvoiceType.STANDARD, InvoiceStatus.PAID, "100.00", "0.00", TODAY.minusDays(70), TODAY.minusMonths(3)));
        doReturn(invoicesData).when(invoices).findAll();
        List<?> paymentsData = List.of(
                payment(PaymentStatus.COMPLETED, "250.00", TODAY.minusDays(2)),
                payment(PaymentStatus.COMPLETED, "100.00", TODAY.minusMonths(1)),
                payment(PaymentStatus.PENDING, "75.00", null));
        doReturn(paymentsData).when(payments).findAll();
        List<?> returnsData = List.of(returnIn(ReturnStatus.PENDING), returnIn(ReturnStatus.DRAFT));
        doReturn(returnsData).when(returns).findAll();
        List<?> materialsData = List.of(
                material(boltsId, "MAT-1", "Bolts", "Fasteners", StockStatus.REORDER_NEEDED, 8, 10, MaterialStatus.ACTIVE),
                material(UUID.randomUUID(), "MAT-2", "Paint", "Coatings", StockStatus.OUT_OF_STOCK, 0, 5, MaterialStatus.ACTIVE),
                material(UUID.randomUUID(), "MAT-3", "Glue", null, StockStatus.CRITICAL, 1, 4, MaterialStatus.ACTIVE),
                material(UUID.randomUUID(), "MAT-4", "Old part", null, StockStatus.OUT_OF_STOCK, 0, 2, MaterialStatus.OBSOLETE),
                material(UUID.randomUUID(), "MAT-5", "Screws", "Fasteners", StockStatus.IN_STOCK, 50, 10, MaterialStatus.ACTIVE));
        doReturn(materialsData).when(materials).findAll();
    }

    // ---- Document doubles ----

    private Requisition requisition(RequisitionStatus status, int hoursAgo) {
        Requisition r = mock(Requisition.class);
        when(r.getStatus()).thenReturn(status);
        when(r.getTitle()).thenReturn("Request " + hoursAgo);
        when(r.getId()).thenReturn(UUID.randomUUID());
        when(r.getCreatedAt()).thenReturn(TODAY.atTime(9, 0).minusHours(hoursAgo));
        return r;
    }

    private PurchaseOrder order(UUID supplier, String name, OrderStatus status, String total, String currency,
                                LocalDate ordered, LocalDate expected, String material) {
        PurchaseOrder o = mock(PurchaseOrder.class);
        PurchaseOrderLine line = mock(PurchaseOrderLine.class);
        Money money = Money.of(new BigDecimal(total), CurrencyCode.fromCode(currency));
        when(line.getLineTotal()).thenReturn(money);
        when(line.getMaterialId()).thenReturn(material.equals("bolts") ? boltsId : null);
        when(line.getMaterialCode()).thenReturn(material.equals("bolts") ? "MAT-1" : "MAT-2");
        when(o.getId()).thenReturn(UUID.randomUUID());
        when(o.getSupplierId()).thenReturn(supplier);
        when(o.getSupplierName()).thenReturn(name);
        when(o.getStatus()).thenReturn(status);
        when(o.getGrandTotal()).thenReturn(money);
        when(o.getCurrencyCode()).thenReturn(currency);
        when(o.getOrderDate()).thenReturn(ordered);
        when(o.getExpectedDeliveryDate()).thenReturn(expected);
        when(o.getLines()).thenReturn(List.of(line));
        when(o.getCreatedAt()).thenReturn(ordered.atStartOfDay());
        return o;
    }

    private GoodsReceipt receipt(UUID supplier, ReceiptStatus status, int received, int accepted, LocalDate expected, LocalDate receivedOn) {
        GoodsReceipt r = mock(GoodsReceipt.class);
        when(r.getSupplierId()).thenReturn(supplier.toString());
        when(r.getStatus()).thenReturn(status);
        when(r.getTotalQuantityReceived()).thenReturn(received);
        when(r.getTotalQuantityAccepted()).thenReturn(accepted);
        when(r.getTotalQuantityRejected()).thenReturn(received - accepted);
        when(r.getExpectedDeliveryDate()).thenReturn(expected);
        when(r.getReceiptDate()).thenReturn(receivedOn);
        when(r.getId()).thenReturn(UUID.randomUUID());
        when(r.getCreatedAt()).thenReturn(receivedOn != null ? receivedOn.atStartOfDay() : null);
        return r;
    }

    private Invoice invoice(InvoiceType type, InvoiceStatus status, String total, String outstanding, LocalDate due, LocalDate date) {
        Invoice i = mock(Invoice.class);
        when(i.getInvoiceType()).thenReturn(type);
        when(i.getStatus()).thenReturn(status);
        when(i.getTotalAmountWithTax()).thenReturn(Money.of(total, CurrencyCode.MAD));
        when(i.getOutstandingAmount()).thenReturn(Money.of(outstanding, CurrencyCode.MAD));
        when(i.getCurrencyCode()).thenReturn("MAD");
        when(i.getDueDate()).thenReturn(due);
        when(i.getInvoiceDate()).thenReturn(date);
        when(i.getId()).thenReturn(UUID.randomUUID());
        when(i.getCreatedAt()).thenReturn(date.atStartOfDay());
        return i;
    }

    private Payment payment(PaymentStatus status, String total, LocalDate confirmed) {
        Payment p = mock(Payment.class);
        when(p.getStatus()).thenReturn(status);
        when(p.getTotalAmount()).thenReturn(Money.of(total, CurrencyCode.MAD));
        when(p.getCurrencyCode()).thenReturn("MAD");
        when(p.getConfirmedDate()).thenReturn(confirmed != null ? confirmed.atTime(12, 0) : null);
        when(p.getId()).thenReturn(UUID.randomUUID());
        when(p.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 1, 1, 0, 0));
        return p;
    }

    private ReturnToVendor returnIn(ReturnStatus status) {
        ReturnToVendor r = mock(ReturnToVendor.class);
        when(r.getStatus()).thenReturn(status);
        when(r.getId()).thenReturn(UUID.randomUUID());
        return r;
    }

    private Material material(UUID id, String code, String name, String category, StockStatus stock, int current, int reorder,
                              MaterialStatus status) {
        Material m = mock(Material.class);
        when(m.getId()).thenReturn(id);
        when(m.getName()).thenReturn(name);
        when(m.getCategoryName()).thenReturn(category);
        when(m.getStockStatus()).thenReturn(stock);
        when(m.getCurrentStock()).thenReturn(current);
        when(m.getReorderPoint()).thenReturn(reorder);
        when(m.getStatus()).thenReturn(status);
        com.materia.backend.contexts.masterData.domain.valueObjects.MaterialCode materialCode =
                mock(com.materia.backend.contexts.masterData.domain.valueObjects.MaterialCode.class);
        when(materialCode.getValue()).thenReturn(code);
        when(m.getCode()).thenReturn(materialCode);
        return m;
    }

    private static Map<String, Kpi> byKey(DashboardOutput out) {
        return out.kpis().stream().collect(Collectors.toMap(Kpi::key, k -> k));
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    // ---- Tests ----

    @Test
    @DisplayName("currency: money figures use the most common currency and the others are counted, not added")
    void currency_isTheDominantOne() {
        DashboardOutput out = service.getOverview(ADMIN);

        assertEquals("MAD", out.currency());
        assertEquals(1, out.otherCurrencyDocuments());
        assertEquals(0, bd("1300.00").compareTo(byKey(out).get("openOrders").value()), "the EUR order is not added");
    }

    @Test
    @DisplayName("KPIs: approvals, open and late orders, receipts, invoices to pay and overdue, payments, stock and returns")
    void kpis() {
        Map<String, Kpi> kpis = byKey(service.getOverview(ADMIN));

        assertEquals(7, kpis.size());
        assertEquals(0, bd("2").compareTo(kpis.get("requisitionsAwaiting").value()));
        assertEquals("1 approved, not yet ordered", kpis.get("requisitionsAwaiting").hint());
        assertEquals("3 order(s), 1 late", kpis.get("openOrders").hint());
        assertEquals("red", kpis.get("openOrders").tone());
        assertEquals(0, bd("1").compareTo(kpis.get("receiptsThisMonth").value()));
        assertEquals(0, bd("550.00").compareTo(kpis.get("invoicesToPay").value()), "credit notes and paid invoices excluded");
        assertTrue(kpis.get("invoicesToPay").hint().startsWith("1 overdue: 400.00"));
        assertEquals(0, bd("250.00").compareTo(kpis.get("paidThisMonth").value()));
        assertEquals("1 payment(s) prepared, to execute", kpis.get("paidThisMonth").hint());
        assertEquals(0, bd("3").compareTo(kpis.get("stockAlerts").value()), "the obsolete material is not an alert");
        assertEquals(0, bd("1").compareTo(kpis.get("returnsOpen").value()));
    }

    @Test
    @DisplayName("trend: twelve months, ordered from committed orders, invoiced net of credit notes, paid from executed payments")
    void monthlyTrend() {
        DashboardOutput.MonthlyTrend trend = service.getOverview(ADMIN).monthlyTrend();

        assertEquals(12, trend.months().size());
        assertEquals("2025-11", trend.months().get(0));
        assertEquals("2026-10", trend.months().get(11));
        assertEquals(0, bd("1300.00").compareTo(trend.ordered().get(11)), "cancelled and EUR orders excluded");
        assertEquals(0, bd("500.00").compareTo(trend.ordered().get(9)));
        assertEquals(0, bd("150.00").compareTo(trend.invoiced().get(11)), "200 invoiced, 50 credited this month");
        assertEquals(0, bd("400.00").compareTo(trend.invoiced().get(10)));
        assertEquals(0, bd("250.00").compareTo(trend.paid().get(11)));
        assertEquals(0, bd("100.00").compareTo(trend.paid().get(10)));
    }

    @Test
    @DisplayName("orders: status distribution, top suppliers with quality, and spend by material category")
    void ordersSections() {
        DashboardOutput out = service.getOverview(ADMIN);

        Map<String, Long> status = out.orderStatus().stream().collect(Collectors.toMap(DashboardOutput.Slice::key, DashboardOutput.Slice::count));
        assertEquals(Map.of("CONFIRMED", 2L, "READY_FOR_RECEIPT", 1L, "COMPLETED", 1L, "CANCELLED", 1L), status);

        assertEquals("Acme", out.topSuppliers().get(0).supplierName());
        assertEquals(0, bd("1500.00").compareTo(out.topSuppliers().get(0).spend()));
        assertEquals(2, out.topSuppliers().get(0).orders());
        assertEquals(90.0, out.topSuppliers().get(0).acceptanceRate());
        assertEquals(50.0, out.topSuppliers().get(0).onTimeRate());
        assertNull(out.topSuppliers().get(1).acceptanceRate(), "Globex has no completed receipt");

        assertEquals("Fasteners", out.spendByCategory().get(0).label());
        assertEquals(0, bd("1500.00").compareTo(out.spendByCategory().get(0).amount()));
        assertEquals("Coatings", out.spendByCategory().get(1).label());
    }

    @Test
    @DisplayName("receipts and invoices: acceptance and on-time rates, and outstanding amounts by days overdue")
    void qualityAndAging() {
        DashboardOutput out = service.getOverview(ADMIN);

        DashboardOutput.ReceiptQuality q = out.receiptQuality();
        assertEquals(2, q.receipts(), "draft receipts are not counted");
        assertEquals(20, q.unitsReceived());
        assertEquals(2, q.unitsRejected());
        assertEquals(90.0, q.acceptanceRate());
        assertEquals(50.0, q.onTimeRate());

        Map<String, BigDecimal> aging = out.invoiceAging().stream()
                .collect(Collectors.toMap(DashboardOutput.AgingBucket::key, DashboardOutput.AgingBucket::amount));
        assertEquals(0, bd("150.00").compareTo(aging.get("NOT_DUE")));
        assertEquals(0, bd("400.00").compareTo(aging.get("31_60")));
        assertEquals(0, BigDecimal.ZERO.compareTo(aging.get("60_PLUS")), "paid invoices are not outstanding");
    }

    @Test
    @DisplayName("stock and activity: active alerts most urgent first, and the latest documents across modules")
    void stockAndActivity() {
        DashboardOutput out = service.getOverview(ADMIN);

        assertEquals(List.of("MAT-2", "MAT-3", "MAT-1"), out.stockAlerts().stream().map(DashboardOutput.StockAlert::code).toList());
        assertEquals(10, out.recentActivity().size());
        assertEquals("REQUISITION", out.recentActivity().get(0).type());
        assertTrue(out.recentActivity().get(0).link().startsWith("/requisitions/view/"));
        for (int i = 1; i < out.recentActivity().size(); i++) {
            assertFalse(out.recentActivity().get(i).date().isAfter(out.recentActivity().get(i - 1).date()), "newest first");
        }
    }

    @Test
    @DisplayName("access: a receiver sees no invoice or payment figures, and those documents are not even read")
    void receiver_seesNoFinance() {
        DashboardOutput out = service.getOverview(RECEIVER);

        Set<String> keys = byKey(out).keySet();
        assertFalse(keys.contains("invoicesToPay"));
        assertFalse(keys.contains("paidThisMonth"));
        assertTrue(out.invoiceAging().isEmpty());
        assertTrue(out.monthlyTrend().invoiced().isEmpty());
        assertTrue(out.monthlyTrend().paid().isEmpty());
        assertFalse(out.monthlyTrend().ordered().isEmpty());
        verify(invoices, never()).findAll();
        verify(payments, never()).findAll();
    }

    @Test
    @DisplayName("access: without any module permission the dashboard is empty and defaults to MAD")
    void noPermissions_emptyDashboard() {
        DashboardOutput out = service.getOverview(Set.of("dashboard:read"));

        assertTrue(out.kpis().isEmpty());
        assertNull(out.monthlyTrend());
        assertNull(out.receiptQuality());
        assertTrue(out.recentActivity().isEmpty());
        assertEquals("MAD", out.currency());
        verifyNoInteractions(requisitions, orders, receipts, invoices, payments, returns, materials);
    }
}
