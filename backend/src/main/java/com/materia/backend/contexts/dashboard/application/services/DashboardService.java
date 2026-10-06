package com.materia.backend.contexts.dashboard.application.services;

import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.Activity;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.AgingBucket;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.Kpi;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.MonthlyTrend;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.ReceiptQuality;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.Slice;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.StockAlert;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.SupplierStats;
import com.materia.backend.contexts.dashboard.domain.ports.in.DashboardUseCase;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel.ActivitySource;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel.DueGroup;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel.Group;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel.MonthAmount;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel.ReceiptTotals;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel.StockRow;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.masterData.domain.enums.StockStatus;
import com.materia.backend.contexts.payment.domain.enums.PaymentStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Builds the procurement dashboard from aggregates the database computes ({@link DashboardReadModel}), so its cost
 * does not grow with the number of documents. Each section is computed only when the user holds the permission that
 * reads its source (Role.java), so a receiver never sees invoice or payment figures.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService implements DashboardUseCase {

    static final String DEFAULT_CURRENCY = "MAD";
    static final int TREND_MONTHS = 12;
    static final int TOP_SUPPLIERS = 5;
    static final int TOP_CATEGORIES = 5;
    static final int MAX_STOCK_ALERTS = 8;
    static final int MAX_ACTIVITY = 10;

    /** Orders that commit spend with a supplier: sent and not cancelled or rejected. */
    private static final Set<OrderStatus> COMMITTED_ORDERS = EnumSet.of(OrderStatus.SUBMITTED, OrderStatus.CONFIRMED,
            OrderStatus.READY_FOR_RECEIPT, OrderStatus.PARTIALLY_RECEIVED, OrderStatus.COMPLETED);
    /** Orders still expected from the supplier. */
    private static final Set<OrderStatus> OPEN_ORDERS = EnumSet.of(OrderStatus.SUBMITTED, OrderStatus.CONFIRMED,
            OrderStatus.READY_FOR_RECEIPT, OrderStatus.PARTIALLY_RECEIVED);
    private static final Set<ReceiptStatus> DONE_RECEIPTS = EnumSet.of(ReceiptStatus.COMPLETED, ReceiptStatus.PARTIAL);
    /** Invoices that still have to be paid. */
    private static final Set<InvoiceStatus> PAYABLE_INVOICES = EnumSet.of(InvoiceStatus.SUBMITTED, InvoiceStatus.VERIFIED);
    /** Invoices that do not count as invoiced: not yet submitted, or withdrawn. */
    private static final Set<InvoiceStatus> NOT_INVOICED = EnumSet.of(InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED);

    private static final Map<OrderStatus, String> ORDER_LABELS = Map.of(
            OrderStatus.DRAFT, "Draft", OrderStatus.SUBMITTED, "Submitted", OrderStatus.CONFIRMED, "Confirmed",
            OrderStatus.READY_FOR_RECEIPT, "Ready for Receipt", OrderStatus.PARTIALLY_RECEIVED, "Partly Received",
            OrderStatus.COMPLETED, "Completed", OrderStatus.CANCELLED, "Cancelled", OrderStatus.REJECTED, "Rejected");

    private final DashboardReadModel readModel;
    private final Clock clock;

    @Autowired
    public DashboardService(DashboardReadModel readModel) {
        this(readModel, Clock.systemDefaultZone());
    }

    public DashboardService(DashboardReadModel readModel, Clock clock) {
        this.readModel = readModel;
        this.clock = clock;
    }

    @Override
    public DashboardOutput getOverview(Set<String> permissions) {
        Access access = new Access(permissions == null ? Set.of() : permissions);
        LocalDate today = LocalDate.now(clock);

        Map<String, Long> byCurrency = access.orders || access.invoices || access.payments
                ? readModel.documentsByCurrency(access.orders, access.invoices, access.payments)
                : Map.of();
        String currency = dominantCurrency(byCurrency);
        int otherCurrency = (int) byCurrency.entrySet().stream()
                .filter(e -> !e.getKey().equalsIgnoreCase(currency))
                .mapToLong(Map.Entry::getValue)
                .sum();

        List<DueGroup> payable = access.invoices
                ? readModel.outstandingByDueDate(InvoiceType.STANDARD, PAYABLE_INVOICES)
                : List.of();

        return new DashboardOutput(
                LocalDateTime.now(clock),
                currency,
                otherCurrency,
                kpis(access, today, currency, payable),
                access.orders || access.invoices || access.payments ? monthlyTrend(access, today, currency) : null,
                access.orders ? orderStatus() : List.of(),
                access.orders ? spendByCategory(currency) : List.of(),
                access.orders ? topSuppliers(currency) : List.of(),
                access.receipts ? receiptQuality(readModel.receiptTotals(DONE_RECEIPTS)) : null,
                access.invoices ? invoiceAging(today, currency, payable) : List.of(),
                access.materials ? stockAlerts() : List.of(),
                recentActivity(access));
    }

    // ============================================================
    // KPIs
    // ============================================================

    private List<Kpi> kpis(Access access, LocalDate today, String currency, List<DueGroup> payable) {
        List<Kpi> kpis = new ArrayList<>();
        YearMonth thisMonth = YearMonth.from(today);

        if (access.requisitions) {
            long awaiting = readModel.countRequisitions(RequisitionStatus.SUBMITTED);
            long approved = readModel.countRequisitions(RequisitionStatus.APPROVED);
            kpis.add(new Kpi("requisitionsAwaiting", "Awaiting Approval", BigDecimal.valueOf(awaiting), "requisitions",
                    approved + " approved, not yet ordered", "/requisitions/approvals", "amber"));
        }

        if (access.orders) {
            long open = readModel.countOrders(OPEN_ORDERS);
            long late = readModel.countOrdersExpectedBefore(OPEN_ORDERS, today);
            kpis.add(new Kpi("openOrders", "Open Orders", scale(readModel.sumOrders(OPEN_ORDERS, currency)),
                    currency, open + " order(s), " + late + " late", "/purchase-orders", late > 0 ? "red" : "brand"));
        }

        if (access.receipts) {
            LocalDate from = thisMonth.atDay(1);
            LocalDate to = thisMonth.atEndOfMonth();
            long received = readModel.countReceipts(DONE_RECEIPTS, from, to, false);
            long rejected = readModel.countReceipts(DONE_RECEIPTS, from, to, true);
            kpis.add(new Kpi("receiptsThisMonth", "Receipts This Month", BigDecimal.valueOf(received), "receipts",
                    rejected + " with rejected goods", "/goods-receipts", rejected > 0 ? "orange" : "green"));
        }

        if (access.invoices) {
            BigDecimal toPay = BigDecimal.ZERO;
            BigDecimal overdueAmount = BigDecimal.ZERO;
            long overdue = 0;
            for (DueGroup group : payable) {
                boolean late = group.dueDate() != null && group.dueDate().isBefore(today);
                if (late) overdue += group.invoices();
                if (!currency.equalsIgnoreCase(group.currency())) continue;
                toPay = toPay.add(group.outstanding());
                if (late) overdueAmount = overdueAmount.add(group.outstanding());
            }
            kpis.add(new Kpi("invoicesToPay", "Invoices to Pay", scale(toPay), currency,
                    overdue + " overdue: " + scale(overdueAmount) + " " + currency, "/invoices", overdue == 0 ? "blue" : "red"));
        }

        if (access.payments) {
            BigDecimal paid = readModel.paymentsByMonth(PaymentStatus.COMPLETED, currency, thisMonth.atDay(1), thisMonth.atEndOfMonth())
                    .stream().map(MonthAmount::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
            long toExecute = readModel.countPayments(PaymentStatus.PENDING);
            kpis.add(new Kpi("paidThisMonth", "Paid This Month", scale(paid), currency,
                    toExecute + " payment(s) prepared, to execute", "/payments", "green"));
        }

        if (access.materials) {
            long alerts = readModel.countStockAlerts(false);
            long out = readModel.countStockAlerts(true);
            kpis.add(new Kpi("stockAlerts", "Stock Alerts", BigDecimal.valueOf(alerts), "materials", out + " out of stock",
                    "/materials", alerts > 0 ? "orange" : "green"));
        }

        if (access.returns) {
            long withSupplier = readModel.countReturns(ReturnStatus.PENDING);
            long drafts = readModel.countReturns(ReturnStatus.DRAFT);
            kpis.add(new Kpi("returnsOpen", "Returns with Suppliers", BigDecimal.valueOf(withSupplier), "returns",
                    drafts + " draft(s) to ship", "/returns", withSupplier > 0 ? "amber" : "green"));
        }

        return kpis;
    }

    // ============================================================
    // CHARTS
    // ============================================================

    /** Ordered (committed orders), invoiced (net of credit notes) and paid (executed payments) per month. */
    private MonthlyTrend monthlyTrend(Access access, LocalDate today, String currency) {
        YearMonth last = YearMonth.from(today);
        YearMonth first = last.minusMonths(TREND_MONTHS - 1L);
        List<YearMonth> months = Stream.iterate(first, m -> m.plusMonths(1)).limit(TREND_MONTHS).toList();
        LocalDate from = first.atDay(1);
        LocalDate to = last.atEndOfMonth();

        return new MonthlyTrend(
                months.stream().map(YearMonth::toString).toList(),
                access.orders ? perMonth(months, readModel.ordersByMonth(COMMITTED_ORDERS, currency, from, to)) : List.of(),
                access.invoices ? perMonth(months, readModel.invoicedByMonth(NOT_INVOICED, currency, from, to)) : List.of(),
                access.payments ? perMonth(months, readModel.paymentsByMonth(PaymentStatus.COMPLETED, currency, from, to)) : List.of());
    }

    /** Number of orders in each status, in lifecycle order, statuses with no order left out. */
    private List<Slice> orderStatus() {
        return readModel.ordersByStatus().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new Slice(e.getKey().name(), ORDER_LABELS.getOrDefault(e.getKey(), e.getKey().name()), e.getValue(), null))
                .toList();
    }

    /** Committed spend per material category (top five, the rest grouped as "Other"). */
    private List<Slice> spendByCategory(String currency) {
        List<Group> ranked = readModel.spendByCategory(COMMITTED_ORDERS, currency).stream()
                .sorted(Comparator.comparing(Group::amount).reversed())
                .toList();
        List<Slice> slices = new ArrayList<>();
        BigDecimal other = BigDecimal.ZERO;
        long otherLines = 0;
        for (int i = 0; i < ranked.size(); i++) {
            Group g = ranked.get(i);
            if (i < TOP_CATEGORIES) {
                slices.add(new Slice(g.key(), g.label(), g.count(), scale(g.amount())));
            } else {
                other = other.add(g.amount());
                otherLines += g.count();
            }
        }
        if (otherLines > 0) {
            slices.add(new Slice("OTHER", "Other", otherLines, scale(other)));
        }
        return slices;
    }

    /** The suppliers with the most committed spend, with the quality and punctuality of their deliveries. */
    private List<SupplierStats> topSuppliers(String currency) {
        List<Group> top = readModel.spendBySupplier(COMMITTED_ORDERS, currency).stream()
                .sorted(Comparator.comparing(Group::amount).reversed())
                .limit(TOP_SUPPLIERS)
                .toList();
        if (top.isEmpty()) return List.of();
        Map<UUID, ReceiptTotals> receipts = readModel.receiptTotalsBySupplier(DONE_RECEIPTS);
        return top.stream()
                .map(g -> {
                    ReceiptQuality quality = receiptQuality(receipts.getOrDefault(UUID.fromString(g.key()), ReceiptTotals.NONE));
                    return new SupplierStats(g.key(), g.label(), scale(g.amount()), g.count(),
                            quality.acceptanceRate(), quality.onTimeRate());
                })
                .toList();
    }

    /** Units accepted against units received, and receipts made by the expected delivery date. */
    private static ReceiptQuality receiptQuality(ReceiptTotals t) {
        return new ReceiptQuality(t.receipts(), t.unitsReceived(), t.unitsAccepted(), t.unitsRejected(),
                rate(t.unitsAccepted(), t.unitsReceived()), t.onTimeReceipts(), rate(t.onTimeReceipts(), t.datedReceipts()));
    }

    /** Outstanding amounts of unpaid standard invoices by days past due. */
    private static List<AgingBucket> invoiceAging(LocalDate today, String currency, List<DueGroup> payable) {
        String[][] buckets = {{"NOT_DUE", "Not due"}, {"1_30", "1-30 days"}, {"31_60", "31-60 days"}, {"60_PLUS", "Over 60 days"}};
        long[] counts = new long[4];
        BigDecimal[] amounts = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        for (DueGroup group : payable) {
            if (!currency.equalsIgnoreCase(group.currency()) || group.outstanding().signum() <= 0) continue;
            long late = group.dueDate() == null ? 0 : ChronoUnit.DAYS.between(group.dueDate(), today);
            int bucket = late <= 0 ? 0 : late <= 30 ? 1 : late <= 60 ? 2 : 3;
            counts[bucket] += group.invoices();
            amounts[bucket] = amounts[bucket].add(group.outstanding());
        }
        List<AgingBucket> result = new ArrayList<>();
        for (int b = 0; b < buckets.length; b++) {
            result.add(new AgingBucket(buckets[b][0], buckets[b][1], counts[b], scale(amounts[b])));
        }
        return result;
    }

    /** Active materials at or below their reorder level, the most urgent first. */
    private List<StockAlert> stockAlerts() {
        return readModel.stockAlerts(MAX_STOCK_ALERTS).stream()
                .map(m -> new StockAlert(
                        m.id() != null ? m.id().toString() : null,
                        m.code(),
                        m.name(),
                        stockStatus(m).name(),
                        m.currentStock(),
                        m.reorderPoint(),
                        m.safetyStock(),
                        m.stockOnOrder(),
                        m.unit()))
                .toList();
    }

    /** Same rule as Material#getStockStatus, for a row that is known to be an alert. */
    private static StockStatus stockStatus(StockRow m) {
        if (m.currentStock() <= 0) return StockStatus.OUT_OF_STOCK;
        if (m.safetyStock() != null && m.currentStock() <= m.safetyStock()) return StockStatus.CRITICAL;
        return StockStatus.REORDER_NEEDED;
    }

    /** The latest documents created in the modules the user can read. */
    private List<Activity> recentActivity(Access access) {
        List<Activity> all = new ArrayList<>();
        if (access.requisitions) addActivity(all, ActivitySource.REQUISITION, "REQUISITION", "/requisitions/view/");
        if (access.orders) addActivity(all, ActivitySource.PURCHASE_ORDER, "PURCHASE_ORDER", "/purchase-orders/");
        if (access.receipts) addActivity(all, ActivitySource.GOODS_RECEIPT, "GOODS_RECEIPT", "/goods-receipts/");
        if (access.invoices) addActivity(all, ActivitySource.INVOICE, "INVOICE", "/invoices/");
        if (access.payments) addActivity(all, ActivitySource.PAYMENT, "PAYMENT", "/payments/");
        if (access.returns) addActivity(all, ActivitySource.RETURN, "RETURN", "/returns/");
        return all.stream()
                .sorted(Comparator.comparing(Activity::date).reversed())
                .limit(MAX_ACTIVITY)
                .toList();
    }

    private void addActivity(List<Activity> all, ActivitySource source, String type, String link) {
        readModel.latest(source, MAX_ACTIVITY).forEach(row -> all.add(new Activity(type,
                row.code() != null ? row.code() : "—", row.title(), row.status(), row.createdAt(), link + row.id())));
    }

    // ============================================================
    // HELPERS
    // ============================================================

    /** The currency most orders, invoices and payments use; amounts in it are comparable. */
    static String dominantCurrency(Map<String, Long> documentsByCurrency) {
        return documentsByCurrency.entrySet().stream()
                .max(Map.Entry.<String, Long>comparingByValue().thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .map(Map.Entry::getKey)
                .orElse(DEFAULT_CURRENCY);
    }

    /** One amount per month of the trend, zero where the database returned nothing. */
    private static List<BigDecimal> perMonth(List<YearMonth> months, List<MonthAmount> amounts) {
        Map<YearMonth, BigDecimal> byMonth = new LinkedHashMap<>();
        months.forEach(m -> byMonth.put(m, BigDecimal.ZERO));
        amounts.forEach(a -> byMonth.computeIfPresent(a.month(), (m, v) -> v.add(a.amount())));
        return byMonth.values().stream().map(DashboardService::scale).toList();
    }

    private static BigDecimal scale(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private static Double rate(long part, long whole) {
        if (whole <= 0) return null;
        return BigDecimal.valueOf(part * 100.0 / whole).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /** What the user may read, from their permissions. */
    private static final class Access {
        final boolean requisitions;
        final boolean orders;
        final boolean receipts;
        final boolean invoices;
        final boolean payments;
        final boolean returns;
        final boolean materials;

        Access(Set<String> permissions) {
            this.requisitions = permissions.contains("requisition:read");
            this.orders = permissions.contains("order:read");
            this.receipts = permissions.contains("receipt:read");
            this.invoices = permissions.contains("invoice:read");
            this.payments = permissions.contains("payment:read");
            this.returns = permissions.contains("return:read");
            this.materials = permissions.contains("material:read");
        }
    }
}
