package com.materia.backend.contexts.dashboard.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.Kpi;
import com.materia.backend.contexts.dashboard.application.services.DashboardService;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel;
import com.materia.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The dashboard computed by the database: one scenario of every document type, checked figure by figure.
 *
 * <p>The test runs in a transaction that is rolled back. It first empties the document and material tables (inside
 * that transaction), so documents other tests committed to the shared database cannot change the totals.
 */
class DashboardReadModelIT extends AbstractIntegrationTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    private static final Set<String> ADMIN = Set.of("requisition:read", "order:read", "receipt:read", "invoice:read",
            "payment:read", "return:read", "material:read", "dashboard:read");
    private static final Set<String> RECEIVER = Set.of("requisition:read", "order:read", "receipt:read", "return:read",
            "material:read", "dashboard:read");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private DashboardReadModel readModel;

    private DashboardService service;
    private final UUID acme = UUID.randomUUID();
    private final UUID globex = UUID.randomUUID();
    private final UUID bolts = UUID.randomUUID();
    private int sequence;

    @BeforeEach
    void scenario() {
        Clock clock = Clock.fixed(TODAY.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new DashboardService(readModel, clock);
        emptyDocumentTables();

        supplier(acme, "Acme");
        supplier(globex, "Globex");
        material(bolts, "MAT-1", "Bolts", "Fasteners", 8, 10, null, "ACTIVE");
        material(UUID.randomUUID(), "MAT-2", "Paint", "Coatings", 0, 5, null, "ACTIVE");
        material(UUID.randomUUID(), "MAT-3", "Glue", null, 1, 4, 2, "ACTIVE");
        material(UUID.randomUUID(), "MAT-4", "Old part", null, 0, 2, null, "OBSOLETE");
        material(UUID.randomUUID(), "MAT-5", "Screws", "Fasteners", 50, 10, null, "ACTIVE");

        requisition("SUBMITTED", 1);
        requisition("SUBMITTED", 2);
        requisition("APPROVED", 3);

        UUID confirmed = order(acme, "Acme", "CONFIRMED", "1000.00", "MAD", TODAY.minusDays(3), TODAY.minusDays(1), bolts, "MAT-1");
        order(acme, "Acme", "COMPLETED", "500.00", "MAD", TODAY.minusMonths(2), TODAY.minusMonths(1), bolts, "MAT-1");
        UUID ready = order(globex, "Globex", "READY_FOR_RECEIPT", "300.00", "MAD", TODAY.minusDays(10), TODAY.plusDays(5), null, "MAT-2");
        order(globex, "Globex", "CANCELLED", "999.00", "MAD", TODAY.minusDays(9), null, null, "MAT-2");
        order(globex, "Globex", "CONFIRMED", "80.00", "EUR", TODAY.minusDays(2), null, null, "MAT-2");

        receipt(confirmed, acme, "COMPLETED", 10, 8, TODAY.minusDays(1), TODAY);
        receipt(confirmed, acme, "PARTIAL", 10, 10, TODAY.minusMonths(1), TODAY.minusMonths(1));
        receipt(ready, globex, "DRAFT", 5, 5, null, null);

        invoice("STANDARD", "VERIFIED", "400.00", null, TODAY.minusDays(40), TODAY.minusDays(30));
        invoice("STANDARD", "SUBMITTED", "200.00", "50.00", TODAY.plusDays(10), TODAY.minusDays(5));
        invoice("CREDIT_NOTE", "VERIFIED", "50.00", null, null, TODAY.minusDays(1));
        invoice("STANDARD", "PAID", "100.00", "100.00", TODAY.minusDays(70), TODAY.minusMonths(3));

        payment("COMPLETED", "250.00", TODAY.minusDays(2));
        payment("COMPLETED", "100.00", TODAY.minusMonths(1));
        payment("PENDING", "75.00", null);

        returnToVendor("PENDING");
        returnToVendor("DRAFT");
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
        assertTrue(kpis.get("invoicesToPay").hint().startsWith("1 overdue: 400.00"), kpis.get("invoicesToPay").hint());
        assertEquals(0, bd("250.00").compareTo(kpis.get("paidThisMonth").value()));
        assertEquals("1 payment(s) prepared, to execute", kpis.get("paidThisMonth").hint());
        assertEquals(0, bd("3").compareTo(kpis.get("stockAlerts").value()), "the obsolete material is not an alert");
        assertEquals("1 out of stock", kpis.get("stockAlerts").hint());
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
        assertEquals(0, bd("100.00").compareTo(trend.invoiced().get(8)), "a paid invoice still counts as invoiced");
        assertEquals(0, bd("250.00").compareTo(trend.paid().get(11)));
        assertEquals(0, bd("100.00").compareTo(trend.paid().get(10)));
    }

    @Test
    @DisplayName("orders: status distribution, top suppliers with quality, and spend by material category")
    void ordersSections() {
        DashboardOutput out = service.getOverview(ADMIN);

        Map<String, Long> status = out.orderStatus().stream().collect(Collectors.toMap(DashboardOutput.Slice::key, DashboardOutput.Slice::count));
        assertEquals(Map.of("CONFIRMED", 2L, "READY_FOR_RECEIPT", 1L, "COMPLETED", 1L, "CANCELLED", 1L), status);
        assertEquals(List.of("CONFIRMED", "READY_FOR_RECEIPT", "COMPLETED", "CANCELLED"),
                out.orderStatus().stream().map(DashboardOutput.Slice::key).toList(), "lifecycle order");

        assertEquals("Acme", out.topSuppliers().get(0).supplierName());
        assertEquals(0, bd("1500.00").compareTo(out.topSuppliers().get(0).spend()));
        assertEquals(2, out.topSuppliers().get(0).orders());
        assertEquals(90.0, out.topSuppliers().get(0).acceptanceRate());
        assertEquals(50.0, out.topSuppliers().get(0).onTimeRate());
        assertEquals("Globex", out.topSuppliers().get(1).supplierName());
        assertNull(out.topSuppliers().get(1).acceptanceRate(), "Globex has no completed receipt");

        assertEquals("Fasteners", out.spendByCategory().get(0).label());
        assertEquals(0, bd("1500.00").compareTo(out.spendByCategory().get(0).amount()));
        assertEquals("Coatings", out.spendByCategory().get(1).label(), "matched by material code when the line has no material id");
        assertEquals(0, bd("300.00").compareTo(out.spendByCategory().get(1).amount()));
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
        assertEquals(List.of("OUT_OF_STOCK", "CRITICAL", "REORDER_NEEDED"),
                out.stockAlerts().stream().map(DashboardOutput.StockAlert::status).toList());
        assertEquals(10, out.recentActivity().size());
        assertEquals("REQUISITION", out.recentActivity().get(0).type());
        assertTrue(out.recentActivity().get(0).link().startsWith("/requisitions/view/"));
        for (int i = 1; i < out.recentActivity().size(); i++) {
            assertFalse(out.recentActivity().get(i).date().isAfter(out.recentActivity().get(i - 1).date()), "newest first");
        }
    }

    @Test
    @DisplayName("access: a receiver sees no invoice or payment figures")
    void receiver_seesNoFinance() {
        DashboardOutput out = service.getOverview(RECEIVER);

        Set<String> keys = byKey(out).keySet();
        assertFalse(keys.contains("invoicesToPay"));
        assertFalse(keys.contains("paidThisMonth"));
        assertTrue(out.invoiceAging().isEmpty());
        assertTrue(out.monthlyTrend().invoiced().isEmpty());
        assertTrue(out.monthlyTrend().paid().isEmpty());
        assertFalse(out.monthlyTrend().ordered().isEmpty());
        assertTrue(out.recentActivity().stream().noneMatch(a -> a.type().equals("INVOICE") || a.type().equals("PAYMENT")));
    }

    // ---- Scenario ----

    /** Removes every document and material inside the test's transaction; the rollback brings them back. */
    private void emptyDocumentTables() {
        for (String sql : List.of(
                "delete from payment_lines", "delete from payments",
                "delete from invoice_lines", "delete from invoices",
                "delete from return_to_vendor_lines", "delete from return_to_vendor",
                "delete from goods_receipt_lines", "delete from goods_receipts",
                "update purchase_requisitions set purchase_order_id = null",
                "delete from purchase_order_lines", "delete from purchase_orders",
                "delete from purchase_requisition_lines", "delete from purchase_requisitions",
                "delete from material_stock_movements", "delete from materials")) {
            jdbc.update(sql);
        }
    }

    private String code(String prefix) {
        return prefix + "-2026-" + String.format("%04d", 9000 + ++sequence);
    }

    private void supplier(UUID id, String name) {
        jdbc.update("insert into suppliers (id, code, name, status, currency_code, created_at, version) "
                + "values (?, ?, ?, 'ACTIVE', 'MAD', now(), 0)", id, code("SUP"), name);
    }

    private void material(UUID id, String code, String name, String category, int stock, int reorder, Integer safety, String status) {
        jdbc.update("insert into materials (id, code, name, category_name, material_type, status, current_stock, "
                        + "available_stock, reorder_point, safety_stock, stock_on_order, unit_of_measure, created_at, version) "
                        + "values (?, ?, ?, ?, 'RAW_MATERIAL', ?, ?, ?, ?, ?, 0, 'PCE', now(), 0)",
                id, code, name, category, status, stock, stock, reorder, safety);
    }

    private void requisition(String status, int hoursAgo) {
        jdbc.update("insert into purchase_requisitions (id, requisition_code, title, status, requester_id, requester_name, "
                        + "created_at, version) values (?, ?, ?, ?, 'requester-1', 'Rita', ?, 0)",
                UUID.randomUUID(), code("REQ"), "Request " + hoursAgo, status,
                Timestamp.valueOf(TODAY.atTime(9, 0).minusHours(hoursAgo)));
    }

    private UUID order(UUID supplier, String name, String status, String total, String currency, LocalDate ordered,
                       LocalDate expected, UUID material, String materialCode) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into purchase_orders (id, order_code, status, delivery_status, supplier_id, supplier_name, "
                        + "order_date, expected_delivery_date, currency_code, total_amount, grand_total, created_at, version) "
                        + "values (?, ?, ?, 'NOT_SHIPPED', ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                id, code("PO"), status, supplier, name, Date.valueOf(ordered), expected != null ? Date.valueOf(expected) : null,
                currency, new BigDecimal(total), new BigDecimal(total), Timestamp.valueOf(ordered.atStartOfDay()));
        jdbc.update("insert into purchase_order_lines (id, purchase_order_id, line_number, material_id, material_code, "
                        + "quantity, unit_price, line_total, currency_code, created_at, version) "
                        + "values (?, ?, 1, ?, ?, 1, ?, ?, ?, now(), 0)",
                UUID.randomUUID(), id, material, materialCode, new BigDecimal(total), new BigDecimal(total), currency);
        return id;
    }

    private void receipt(UUID order, UUID supplier, String status, int received, int accepted, LocalDate expected, LocalDate receivedOn) {
        jdbc.update("insert into goods_receipts (id, receipt_code, purchase_order_id, status, received_by, received_by_name, "
                        + "supplier_id, total_quantity_received, total_quantity_accepted, total_quantity_rejected, "
                        + "expected_delivery_date, receipt_date, has_discrepancy, created_at, version) "
                        + "values (?, ?, ?, ?, 'receiver-1', 'Rita', ?, ?, ?, ?, ?, ?, false, ?, 0)",
                UUID.randomUUID(), code("GR"), order, status, supplier, received, accepted, received - accepted,
                expected != null ? Date.valueOf(expected) : null, receivedOn != null ? Date.valueOf(receivedOn) : null,
                Timestamp.valueOf((receivedOn != null ? receivedOn : TODAY.minusDays(20)).atStartOfDay()));
    }

    private void invoice(String type, String status, String total, String paid, LocalDate due, LocalDate date) {
        jdbc.update("insert into invoices (id, invoice_code, invoice_type, status, supplier_id, supplier_name, invoice_date, "
                        + "due_date, total_amount, total_tax_amount, total_amount_with_tax, paid_amount, currency_code, "
                        + "is_verified, has_discrepancy, created_at, version) "
                        + "values (?, ?, ?, ?, ?, 'Acme', ?, ?, ?, 0, ?, ?, 'MAD', false, false, ?, 0)",
                UUID.randomUUID(), code("INV"), type, status, acme, Date.valueOf(date), due != null ? Date.valueOf(due) : null,
                new BigDecimal(total), new BigDecimal(total), paid != null ? new BigDecimal(paid) : null,
                Timestamp.valueOf(date.atStartOfDay()));
    }

    private void payment(String status, String total, LocalDate confirmed) {
        jdbc.update("insert into payments (id, payment_code, status, supplier_id, supplier_name, total_amount, currency_code, "
                        + "confirmed_date, created_at, version) values (?, ?, ?, ?, 'Acme', ?, 'MAD', ?, ?, 0)",
                UUID.randomUUID(), code("PAY"), status, acme, new BigDecimal(total),
                confirmed != null ? Timestamp.valueOf(confirmed.atTime(12, 0)) : null,
                Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 0, 0)));
    }

    private void returnToVendor(String status) {
        jdbc.update("insert into return_to_vendor (id, return_code, status, supplier_id, supplier_name, created_at, version) "
                        + "values (?, ?, ?, ?, 'Acme', ?, 0)",
                UUID.randomUUID(), code("RTN"), status, acme, Timestamp.valueOf(TODAY.minusDays(20).atStartOfDay()));
    }

    private static Map<String, Kpi> byKey(DashboardOutput out) {
        return out.kpis().stream().collect(Collectors.toMap(Kpi::key, k -> k));
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
