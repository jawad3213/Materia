package com.materia.backend.contexts.dashboard.application.services;

import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.fx.FxValuation;
import com.materia.backend.common.domain.fx.ReportingFx;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.Activity;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.ConvertedCurrency;
import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput.CurrencyConversion;
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
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel.Rates;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel.ReceiptTotals;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel.StockRow;
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

    static final int TREND_MONTHS = 12;
    static final int TOP_SUPPLIERS = 5;
    static final int TOP_CATEGORIES = 5;
    static final int MAX_STOCK_ALERTS = 8;
    static final int MAX_ACTIVITY = 10;

    /** Orders that commit spend with a supplier: sent and not cancelled or rejected. */
    private static final Set<String> COMMITTED_ORDERS = Set.of("SUBMITTED", "CONFIRMED",
            "READY_FOR_RECEIPT", "PARTIALLY_RECEIVED", "COMPLETED");
    /** Orders still expected from the supplier. */
    private static final Set<String> OPEN_ORDERS = Set.of("SUBMITTED", "CONFIRMED",
            "READY_FOR_RECEIPT", "PARTIALLY_RECEIVED");
    private static final Set<String> DONE_RECEIPTS = Set.of("COMPLETED", "PARTIAL");
    /** Invoices that still have to be paid. */
    private static final Set<String> PAYABLE_INVOICES = Set.of("SUBMITTED", "VERIFIED");
    /** Invoices that do not count as invoiced: not yet submitted, or withdrawn. */
    private static final Set<String> NOT_INVOICED = Set.of("DRAFT", "CANCELLED");

    private static final Map<String, String> ORDER_LABELS = Map.of(
            "DRAFT", "Draft", "SUBMITTED", "Submitted", "CONFIRMED", "Confirmed",
            "READY_FOR_RECEIPT", "Ready for Receipt", "PARTIALLY_RECEIVED", "Partly Received",
            "COMPLETED", "Completed", "CANCELLED", "Cancelled", "REJECTED", "Rejected");

    private final DashboardReadModel readModel;
    private final ReportingFx fx;
    private final Clock clock;

    @Autowired
    public DashboardService(DashboardReadModel readModel, ReportingFx fx) {
        this(readModel, fx, Clock.systemDefaultZone());
    }

    public DashboardService(DashboardReadModel readModel, ReportingFx fx, Clock clock) {
        this.readModel = readModel;
        this.fx = fx;
        this.clock = clock;
    }

    @Override
    public DashboardOutput getOverview(Set<String> permissions) {
        Access access = new Access(permissions == null ? Set.of() : permissions);
        LocalDate today = LocalDate.now(clock);

        Map<String, Long> byCurrency = access.orders || access.invoices || access.payments
                ? readModel.documentsByCurrency(access.orders, access.invoices, access.payments)
                : Map.of();
        String currency = fx.reportingCurrency().getCode();
        boolean foreign = byCurrency.keySet().stream().anyMatch(c -> !c.equalsIgnoreCase(currency));
        // Stored rates only; the exchange-rate service is not called on every dashboard load.
        Map<CurrencyCode, FxValuation> latest = foreign ? fx.currentRates() : Map.of();
        Rates rates = rates(currency, latest);
        int unconverted = (int) byCurrency.entrySet().stream()
                .filter(e -> !rates.current().containsKey(e.getKey().toUpperCase()))
                .mapToLong(Map.Entry::getValue)
                .sum();

        List<DueGroup> payable = access.invoices
                ? readModel.outstandingByDueDate("STANDARD", PAYABLE_INVOICES, rates)
                : List.of();

        return new DashboardOutput(
                LocalDateTime.now(clock),
                currency,
                unconverted,
                kpis(access, today, rates, payable),
                access.orders || access.invoices || access.payments ? monthlyTrend(access, today, rates) : null,
                access.orders ? orderStatus() : List.of(),
                access.orders ? spendByCategory(rates) : List.of(),
                access.orders ? topSuppliers(rates) : List.of(),
                access.receipts ? receiptQuality(readModel.receiptTotals(DONE_RECEIPTS)) : null,
                access.invoices ? invoiceAging(today, payable) : List.of(),
                access.materials ? stockAlerts() : List.of(),
                recentActivity(access),
                foreign ? conversion(access, currency, byCurrency, latest, unconverted) : null);
    }

    // ============================================================
    // CURRENCY
    // ============================================================

    /** The reporting currency at 1 and the latest stored rate of every other currency that has one. */
    static Rates rates(String reporting, Map<CurrencyCode, FxValuation> latest) {
        Map<String, BigDecimal> current = new LinkedHashMap<>();
        current.put(reporting.toUpperCase(), BigDecimal.ONE);
        latest.forEach((code, valuation) -> current.put(code.getCode(), valuation.rate()));
        return new Rates(reporting.toUpperCase(), current);
    }

    private CurrencyConversion conversion(Access access, String reporting, Map<String, Long> byCurrency,
                                          Map<CurrencyCode, FxValuation> latest, int unconverted) {
        Map<String, Long> ownDate = readModel.documentsValuedAtOwnDate(reporting, access.orders, access.invoices, access.payments);
        List<ConvertedCurrency> currencies = new ArrayList<>();
        List<String> leftOut = new ArrayList<>();
        byCurrency.entrySet().stream()
                .filter(e -> !e.getKey().equalsIgnoreCase(reporting))
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    FxValuation rate = latest.entrySet().stream()
                            .filter(l -> l.getKey().getCode().equalsIgnoreCase(e.getKey()))
                            .map(Map.Entry::getValue).findFirst().orElse(null);
                    if (rate == null) leftOut.add(e.getKey());
                    currencies.add(new ConvertedCurrency(e.getKey(), e.getValue(), ownDate.getOrDefault(e.getKey(), 0L),
                            rate != null ? rate.rate() : null, rate != null ? rate.rateDate() : null,
                            rate != null ? rate.source() : null));
                });
        boolean fallback = latest.values().stream().anyMatch(FxValuation::isFallback);

        List<String> converted = currencies.stream().map(ConvertedCurrency::currency).filter(c -> !leftOut.contains(c)).toList();
        StringBuilder note = new StringBuilder("All amounts in ").append(reporting).append('.');
        if (!converted.isEmpty()) {
            note.append(' ').append(String.join(", ", converted)).append(" converted to ").append(reporting)
                    .append(" at the rate of each document's date; documents not yet committed use the latest rate.");
        }
        if (fallback) {
            note.append(" The exchange-rate service was unavailable: built-in fallback rates are in use.");
        }
        if (unconverted > 0) {
            note.append(' ').append(unconverted).append(" document(s) in ").append(String.join(", ", leftOut))
                    .append(" left out: no exchange rate available.");
        }
        return new CurrencyConversion(reporting, currencies, fallback, note.toString());
    }

    // ============================================================
    // KPIs
    // ============================================================

    private List<Kpi> kpis(Access access, LocalDate today, Rates rates, List<DueGroup> payable) {
        String currency = rates.reportingCurrency();
        List<Kpi> kpis = new ArrayList<>();
        YearMonth thisMonth = YearMonth.from(today);

        if (access.requisitions) {
            long awaiting = readModel.countRequisitions("SUBMITTED");
            long approved = readModel.countRequisitions("APPROVED");
            kpis.add(new Kpi("requisitionsAwaiting", "Awaiting Approval", BigDecimal.valueOf(awaiting), "requisitions",
                    approved + " approved, not yet ordered", "/requisitions/approvals", "amber"));
        }

        if (access.orders) {
            long open = readModel.countOrders(OPEN_ORDERS);
            long late = readModel.countOrdersExpectedBefore(OPEN_ORDERS, today);
            kpis.add(new Kpi("openOrders", "Open Orders", scale(readModel.sumOrders(OPEN_ORDERS, rates)),
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
                toPay = toPay.add(group.outstanding());
                if (late) overdueAmount = overdueAmount.add(group.outstanding());
            }
            kpis.add(new Kpi("invoicesToPay", "Invoices to Pay", scale(toPay), currency,
                    overdue + " overdue: " + scale(overdueAmount) + " " + currency, "/invoices", overdue == 0 ? "blue" : "red"));
        }

        if (access.payments) {
            BigDecimal paid = readModel.paymentsByMonth("COMPLETED", rates, thisMonth.atDay(1), thisMonth.atEndOfMonth())
                    .stream().map(MonthAmount::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
            long toExecute = readModel.countPayments("PENDING");
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
            long withSupplier = readModel.countReturns("PENDING");
            long drafts = readModel.countReturns("DRAFT");
            kpis.add(new Kpi("returnsOpen", "Returns with Suppliers", BigDecimal.valueOf(withSupplier), "returns",
                    drafts + " draft(s) to ship", "/returns", withSupplier > 0 ? "amber" : "green"));
        }

        return kpis;
    }

    // ============================================================
    // CHARTS
    // ============================================================

    /** Ordered (committed orders), invoiced (net of credit notes) and paid (executed payments) per month. */
    private MonthlyTrend monthlyTrend(Access access, LocalDate today, Rates rates) {
        YearMonth last = YearMonth.from(today);
        YearMonth first = last.minusMonths(TREND_MONTHS - 1L);
        List<YearMonth> months = Stream.iterate(first, m -> m.plusMonths(1)).limit(TREND_MONTHS).toList();
        LocalDate from = first.atDay(1);
        LocalDate to = last.atEndOfMonth();

        return new MonthlyTrend(
                months.stream().map(YearMonth::toString).toList(),
                access.orders ? perMonth(months, readModel.ordersByMonth(COMMITTED_ORDERS, rates, from, to)) : List.of(),
                access.invoices ? perMonth(months, readModel.invoicedByMonth(NOT_INVOICED, rates, from, to)) : List.of(),
                access.payments ? perMonth(months, readModel.paymentsByMonth("COMPLETED", rates, from, to)) : List.of());
    }

    /** Number of orders in each status, in lifecycle order, statuses with no order left out. */
    private List<Slice> orderStatus() {
        return readModel.ordersByStatus().entrySet().stream()
                .map(e -> new Slice(e.getKey(), ORDER_LABELS.getOrDefault(e.getKey(), e.getKey()), e.getValue(), null))
                .toList();
    }

    /** Committed spend per material category (top five, the rest grouped as "Other"). */
    private List<Slice> spendByCategory(Rates rates) {
        List<Group> ranked = readModel.spendByCategory(COMMITTED_ORDERS, rates).stream()
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
    private List<SupplierStats> topSuppliers(Rates rates) {
        List<Group> top = readModel.spendBySupplier(COMMITTED_ORDERS, rates).stream()
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

    /** Outstanding amounts of unpaid standard invoices by days past due, in the reporting currency. */
    private static List<AgingBucket> invoiceAging(LocalDate today, List<DueGroup> payable) {
        String[][] buckets = {{"NOT_DUE", "Not due"}, {"1_30", "1-30 days"}, {"31_60", "31-60 days"}, {"60_PLUS", "Over 60 days"}};
        long[] counts = new long[4];
        BigDecimal[] amounts = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        for (DueGroup group : payable) {
            if (group.outstanding().signum() <= 0) continue;
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
                        stockStatus(m),
                        m.currentStock(),
                        m.reorderPoint(),
                        m.safetyStock(),
                        m.stockOnOrder(),
                        m.unit()))
                .toList();
    }

    /** Same rule as Material#getStockStatus, for a row that is known to be an alert. */
    private static String stockStatus(StockRow m) {
        if (m.currentStock() <= 0) return "OUT_OF_STOCK";
        if (m.safetyStock() != null && m.currentStock() <= m.safetyStock()) return "CRITICAL";
        return "REORDER_NEEDED";
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
