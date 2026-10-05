package com.materia.backend.contexts.dashboard.application.services;

import com.materia.backend.common.domain.valueObjects.Money;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds the procurement dashboard from the documents of every module. Each section is computed only when the
 * user holds the permission that reads its source (Role.java), so a receiver never sees invoice or payment figures.
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
            OrderStatus.READY_FOR_RECEIPT, OrderStatus.PARTIALLY_RECEIVED, OrderStatus.RECEIVED, OrderStatus.COMPLETED);
    /** Orders still expected from the supplier. */
    private static final Set<OrderStatus> OPEN_ORDERS = EnumSet.of(OrderStatus.SUBMITTED, OrderStatus.CONFIRMED,
            OrderStatus.READY_FOR_RECEIPT, OrderStatus.PARTIALLY_RECEIVED);
    private static final Set<ReceiptStatus> DONE_RECEIPTS = EnumSet.of(ReceiptStatus.COMPLETED, ReceiptStatus.PARTIAL);
    /** Invoices that still have to be paid. */
    private static final Set<InvoiceStatus> PAYABLE_INVOICES = EnumSet.of(InvoiceStatus.SUBMITTED, InvoiceStatus.VERIFIED);

    private static final Map<OrderStatus, String> ORDER_LABELS = Map.of(
            OrderStatus.DRAFT, "Draft", OrderStatus.SUBMITTED, "Submitted", OrderStatus.CONFIRMED, "Confirmed",
            OrderStatus.READY_FOR_RECEIPT, "Ready for Receipt", OrderStatus.PARTIALLY_RECEIVED, "Partly Received",
            OrderStatus.RECEIVED, "Received", OrderStatus.COMPLETED, "Completed", OrderStatus.CANCELLED, "Cancelled",
            OrderStatus.REJECTED, "Rejected");

    private final RequisitionRepository requisitions;
    private final PurchaseOrderRepository orders;
    private final GoodsReceiptRepository receipts;
    private final InvoiceRepository invoices;
    private final PaymentPort payments;
    private final ReturnToVendorRepository returns;
    private final MaterialRepository materials;
    private final Clock clock;

    @Autowired
    public DashboardService(RequisitionRepository requisitions, PurchaseOrderRepository orders, GoodsReceiptRepository receipts,
                            InvoiceRepository invoices, PaymentPort payments, ReturnToVendorRepository returns,
                            MaterialRepository materials) {
        this(requisitions, orders, receipts, invoices, payments, returns, materials, Clock.systemDefaultZone());
    }

    public DashboardService(RequisitionRepository requisitions, PurchaseOrderRepository orders, GoodsReceiptRepository receipts,
                            InvoiceRepository invoices, PaymentPort payments, ReturnToVendorRepository returns,
                            MaterialRepository materials, Clock clock) {
        this.requisitions = requisitions;
        this.orders = orders;
        this.receipts = receipts;
        this.invoices = invoices;
        this.payments = payments;
        this.returns = returns;
        this.materials = materials;
        this.clock = clock;
    }

    @Override
    public DashboardOutput getOverview(Set<String> permissions) {
        Access access = new Access(permissions == null ? Set.of() : permissions);
        LocalDate today = LocalDate.now(clock);

        List<Requisition> reqs = access.requisitions ? requisitions.findAll() : List.of();
        List<PurchaseOrder> pos = access.orders ? orders.findAll() : List.of();
        List<GoodsReceipt> grs = access.receipts ? receipts.findAll() : List.of();
        List<Invoice> invs = access.invoices ? invoices.findAll() : List.of();
        List<Payment> pays = access.payments ? payments.findAll() : List.of();
        List<ReturnToVendor> rtvs = access.returns ? returns.findAll() : List.of();
        List<Material> mats = access.materials || access.orders ? materials.findAll() : List.of();

        String currency = dominantCurrency(pos, invs, pays);
        int otherCurrency = (int) (Stream.of(
                        pos.stream().map(PurchaseOrder::getCurrencyCode),
                        invs.stream().map(Invoice::getCurrencyCode),
                        pays.stream().map(Payment::getCurrencyCode))
                .flatMap(Function.identity())
                .filter(c -> c != null && !c.equalsIgnoreCase(currency))
                .count());

        return new DashboardOutput(
                LocalDateTime.now(clock),
                currency,
                otherCurrency,
                kpis(access, today, currency, reqs, pos, grs, invs, pays, rtvs, mats),
                access.orders || access.invoices || access.payments ? monthlyTrend(access, today, currency, pos, invs, pays) : null,
                access.orders ? orderStatus(pos) : List.of(),
                access.orders ? spendByCategory(currency, pos, mats) : List.of(),
                access.orders ? topSuppliers(currency, pos, grs) : List.of(),
                access.receipts ? receiptQuality(grs) : null,
                access.invoices ? invoiceAging(today, currency, invs) : List.of(),
                access.materials ? stockAlerts(mats) : List.of(),
                recentActivity(reqs, pos, grs, invs, pays, rtvs));
    }

    // ============================================================
    // KPIs
    // ============================================================

    private List<Kpi> kpis(Access access, LocalDate today, String currency, List<Requisition> reqs, List<PurchaseOrder> pos,
                           List<GoodsReceipt> grs, List<Invoice> invs, List<Payment> pays, List<ReturnToVendor> rtvs,
                           List<Material> mats) {
        List<Kpi> kpis = new ArrayList<>();
        YearMonth thisMonth = YearMonth.from(today);

        if (access.requisitions) {
            List<Requisition> awaiting = reqs.stream().filter(r -> r.getStatus() == RequisitionStatus.SUBMITTED).toList();
            long approved = reqs.stream().filter(r -> r.getStatus() == RequisitionStatus.APPROVED).count();
            kpis.add(new Kpi("requisitionsAwaiting", "Awaiting Approval", BigDecimal.valueOf(awaiting.size()), "requisitions",
                    approved + " approved, not yet ordered", "/requisitions/approvals", "amber"));
        }
        if (access.orders) {
            List<PurchaseOrder> open = pos.stream().filter(o -> OPEN_ORDERS.contains(o.getStatus())).toList();
            long late = open.stream().filter(o -> o.getExpectedDeliveryDate() != null && o.getExpectedDeliveryDate().isBefore(today)).count();
            kpis.add(new Kpi("openOrders", "Open Orders", sum(open, PurchaseOrder::getGrandTotal, PurchaseOrder::getCurrencyCode, currency),
                    currency, open.size() + " order(s), " + late + " late", "/purchase-orders", late > 0 ? "red" : "brand"));
        }
        if (access.receipts) {
            List<GoodsReceipt> monthReceipts = grs.stream()
                    .filter(r -> DONE_RECEIPTS.contains(r.getStatus()) && r.getReceiptDate() != null
                            && YearMonth.from(r.getReceiptDate()).equals(thisMonth))
                    .toList();
            long rejected = monthReceipts.stream().filter(r -> nz(r.getTotalQuantityRejected()) > 0).count();
            kpis.add(new Kpi("receiptsThisMonth", "Receipts This Month", BigDecimal.valueOf(monthReceipts.size()), "receipts",
                    rejected + " with rejected goods", "/goods-receipts", rejected > 0 ? "orange" : "green"));
        }
        if (access.invoices) {
            List<Invoice> payable = invs.stream()
                    .filter(i -> i.getInvoiceType() == InvoiceType.STANDARD && PAYABLE_INVOICES.contains(i.getStatus()))
                    .toList();
            List<Invoice> overdue = payable.stream().filter(i -> isOverdue(i, today)).toList();
            kpis.add(new Kpi("invoicesToPay", "Invoices to Pay", sum(payable, Invoice::getOutstandingAmount, Invoice::getCurrencyCode, currency),
                    currency, overdue.size() + " overdue: " + sum(overdue, Invoice::getOutstandingAmount, Invoice::getCurrencyCode, currency)
                    + " " + currency, "/invoices", overdue.isEmpty() ? "blue" : "red"));
        }
        if (access.payments) {
            List<Payment> paidThisMonth = pays.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.COMPLETED && p.getConfirmedDate() != null
                            && YearMonth.from(p.getConfirmedDate()).equals(thisMonth))
                    .toList();
            long toExecute = pays.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count();
            kpis.add(new Kpi("paidThisMonth", "Paid This Month", sum(paidThisMonth, Payment::getTotalAmount, Payment::getCurrencyCode, currency),
                    currency, toExecute + " payment(s) prepared, to execute", "/payments", "green"));
        }
        if (access.materials) {
            long alerts = mats.stream().filter(DashboardService::isStockAlert).count();
            long out = mats.stream().filter(m -> isStockAlert(m) && m.getStockStatus() == StockStatus.OUT_OF_STOCK).count();
            kpis.add(new Kpi("stockAlerts", "Stock Alerts", BigDecimal.valueOf(alerts), "materials", out + " out of stock",
                    "/materials", alerts > 0 ? "orange" : "green"));
        }
        if (access.returns) {
            long withSupplier = rtvs.stream().filter(r -> r.getStatus() == ReturnStatus.PENDING).count();
            long drafts = rtvs.stream().filter(r -> r.getStatus() == ReturnStatus.DRAFT).count();
            kpis.add(new Kpi("returnsOpen", "Returns with Suppliers", BigDecimal.valueOf(withSupplier), "returns",
                    drafts + " draft(s) to ship", "/returns", withSupplier > 0 ? "amber" : "green"));
        }
        return kpis;
    }

    // ============================================================
    // CHARTS
    // ============================================================

    /** Ordered (committed orders), invoiced (net of credit notes) and paid (executed payments) per month. */
    private MonthlyTrend monthlyTrend(Access access, LocalDate today, String currency, List<PurchaseOrder> pos,
                                      List<Invoice> invs, List<Payment> pays) {
        YearMonth last = YearMonth.from(today);
        List<YearMonth> months = Stream.iterate(last.minusMonths(TREND_MONTHS - 1L), m -> m.plusMonths(1)).limit(TREND_MONTHS).toList();
        Map<YearMonth, BigDecimal> ordered = zeroes(months);
        Map<YearMonth, BigDecimal> invoiced = zeroes(months);
        Map<YearMonth, BigDecimal> paid = zeroes(months);

        if (access.orders) {
            pos.stream()
                    .filter(o -> COMMITTED_ORDERS.contains(o.getStatus()) && o.getOrderDate() != null && sameCurrency(o.getCurrencyCode(), currency))
                    .forEach(o -> ordered.computeIfPresent(YearMonth.from(o.getOrderDate()), (m, v) -> v.add(amount(o.getGrandTotal()))));
        }
        if (access.invoices) {
            invs.stream()
                    .filter(i -> i.getStatus() != InvoiceStatus.DRAFT && i.getStatus() != InvoiceStatus.CANCELLED
                            && i.getInvoiceDate() != null && sameCurrency(i.getCurrencyCode(), currency))
                    .forEach(i -> {
                        BigDecimal value = amount(i.getTotalAmountWithTax());
                        BigDecimal signed = i.getInvoiceType() == InvoiceType.CREDIT_NOTE ? value.negate() : value;
                        invoiced.computeIfPresent(YearMonth.from(i.getInvoiceDate()), (m, v) -> v.add(signed));
                    });
        }
        if (access.payments) {
            pays.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.COMPLETED && p.getConfirmedDate() != null && sameCurrency(p.getCurrencyCode(), currency))
                    .forEach(p -> paid.computeIfPresent(YearMonth.from(p.getConfirmedDate()), (m, v) -> v.add(amount(p.getTotalAmount()))));
        }

        return new MonthlyTrend(
                months.stream().map(YearMonth::toString).toList(),
                access.orders ? scaled(ordered.values()) : List.of(),
                access.invoices ? scaled(invoiced.values()) : List.of(),
                access.payments ? scaled(paid.values()) : List.of());
    }

    /** Number of orders in each status, in lifecycle order, statuses with no order left out. */
    private List<Slice> orderStatus(List<PurchaseOrder> pos) {
        Map<OrderStatus, Long> counts = pos.stream()
                .filter(o -> o.getStatus() != null)
                .collect(Collectors.groupingBy(PurchaseOrder::getStatus, () -> new java.util.EnumMap<>(OrderStatus.class), Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> new Slice(e.getKey().name(), ORDER_LABELS.getOrDefault(e.getKey(), e.getKey().name()), e.getValue(), null))
                .toList();
    }

    /** Committed spend per material category (top five, the rest grouped as "Other"). */
    private List<Slice> spendByCategory(String currency, List<PurchaseOrder> pos, List<Material> mats) {
        Map<String, String> categoryById = new HashMap<>();
        Map<String, String> categoryByCode = new HashMap<>();
        for (Material m : mats) {
            String category = m.getCategoryName() != null && !m.getCategoryName().isBlank() ? m.getCategoryName() : "Uncategorized";
            if (m.getId() != null) categoryById.put(m.getId().toString(), category);
            if (m.getCode() != null) categoryByCode.put(m.getCode().getValue(), category);
        }

        Map<String, BigDecimal> spend = new HashMap<>();
        Map<String, Long> lines = new HashMap<>();
        pos.stream()
                .filter(o -> COMMITTED_ORDERS.contains(o.getStatus()) && sameCurrency(o.getCurrencyCode(), currency))
                .flatMap(o -> o.getLines().stream())
                .forEach(line -> {
                    String category = Optional.ofNullable(line.getMaterialId()).map(id -> categoryById.get(id.toString()))
                            .or(() -> Optional.ofNullable(categoryByCode.get(line.getMaterialCode())))
                            .orElse("Uncategorized");
                    spend.merge(category, lineValue(line), BigDecimal::add);
                    lines.merge(category, 1L, Long::sum);
                });

        List<Map.Entry<String, BigDecimal>> ranked = spend.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .toList();
        List<Slice> slices = new ArrayList<>();
        BigDecimal other = BigDecimal.ZERO;
        long otherLines = 0;
        for (int i = 0; i < ranked.size(); i++) {
            Map.Entry<String, BigDecimal> e = ranked.get(i);
            if (i < TOP_CATEGORIES) {
                slices.add(new Slice(e.getKey(), e.getKey(), lines.get(e.getKey()), scale(e.getValue())));
            } else {
                other = other.add(e.getValue());
                otherLines += lines.get(e.getKey());
            }
        }
        if (otherLines > 0) {
            slices.add(new Slice("OTHER", "Other", otherLines, scale(other)));
        }
        return slices;
    }

    /** The suppliers with the most committed spend, with the quality and punctuality of their deliveries. */
    private List<SupplierStats> topSuppliers(String currency, List<PurchaseOrder> pos, List<GoodsReceipt> grs) {
        Map<String, List<PurchaseOrder>> bySupplier = pos.stream()
                .filter(o -> COMMITTED_ORDERS.contains(o.getStatus()) && o.getSupplierId() != null && sameCurrency(o.getCurrencyCode(), currency))
                .collect(Collectors.groupingBy(o -> o.getSupplierId().toString()));
        Map<String, List<GoodsReceipt>> receiptsBySupplier = grs.stream()
                .filter(r -> DONE_RECEIPTS.contains(r.getStatus()) && r.getSupplierId() != null)
                .collect(Collectors.groupingBy(GoodsReceipt::getSupplierId));

        return bySupplier.entrySet().stream()
                .map(e -> {
                    List<PurchaseOrder> supplierOrders = e.getValue();
                    List<GoodsReceipt> supplierReceipts = receiptsBySupplier.getOrDefault(e.getKey(), List.of());
                    ReceiptQuality quality = receiptQuality(supplierReceipts);
                    return new SupplierStats(e.getKey(), supplierOrders.get(0).getSupplierName(),
                            scale(supplierOrders.stream().map(o -> amount(o.getGrandTotal())).reduce(BigDecimal.ZERO, BigDecimal::add)),
                            supplierOrders.size(), quality.acceptanceRate(), quality.onTimeRate());
                })
                .sorted(Comparator.comparing(SupplierStats::spend).reversed())
                .limit(TOP_SUPPLIERS)
                .toList();
    }

    /** Units accepted against units received, and receipts made by the expected delivery date. */
    private ReceiptQuality receiptQuality(List<GoodsReceipt> grs) {
        List<GoodsReceipt> done = grs.stream().filter(r -> DONE_RECEIPTS.contains(r.getStatus())).toList();
        long received = done.stream().mapToLong(r -> nz(r.getTotalQuantityReceived())).sum();
        long accepted = done.stream().mapToLong(r -> nz(r.getTotalQuantityAccepted())).sum();
        long rejected = done.stream().mapToLong(r -> nz(r.getTotalQuantityRejected())).sum();
        List<GoodsReceipt> dated = done.stream().filter(r -> r.getReceiptDate() != null && r.getExpectedDeliveryDate() != null).toList();
        long onTime = dated.stream().filter(r -> !r.getReceiptDate().isAfter(r.getExpectedDeliveryDate())).count();
        return new ReceiptQuality(done.size(), received, accepted, rejected, rate(accepted, received), onTime, rate(onTime, dated.size()));
    }

    /** Outstanding amounts of unpaid standard invoices by days past due. */
    private List<AgingBucket> invoiceAging(LocalDate today, String currency, List<Invoice> invs) {
        String[][] buckets = {{"NOT_DUE", "Not due"}, {"1_30", "1-30 days"}, {"31_60", "31-60 days"}, {"60_PLUS", "Over 60 days"}};
        long[] counts = new long[4];
        BigDecimal[] amounts = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        invs.stream()
                .filter(i -> i.getInvoiceType() == InvoiceType.STANDARD && PAYABLE_INVOICES.contains(i.getStatus())
                        && sameCurrency(i.getCurrencyCode(), currency))
                .forEach(i -> {
                    BigDecimal outstanding = amount(i.getOutstandingAmount());
                    if (outstanding.signum() <= 0) return;
                    long late = i.getDueDate() == null ? 0 : ChronoUnit.DAYS.between(i.getDueDate(), today);
                    int bucket = late <= 0 ? 0 : late <= 30 ? 1 : late <= 60 ? 2 : 3;
                    counts[bucket]++;
                    amounts[bucket] = amounts[bucket].add(outstanding);
                });
        List<AgingBucket> result = new ArrayList<>();
        for (int b = 0; b < buckets.length; b++) {
            result.add(new AgingBucket(buckets[b][0], buckets[b][1], counts[b], scale(amounts[b])));
        }
        return result;
    }

    /** Active materials at or below their reorder level, the most urgent first. */
    private List<StockAlert> stockAlerts(List<Material> mats) {
        Map<StockStatus, Integer> severity = Map.of(StockStatus.OUT_OF_STOCK, 0, StockStatus.CRITICAL, 1, StockStatus.REORDER_NEEDED, 2);
        return mats.stream()
                .filter(DashboardService::isStockAlert)
                .sorted(Comparator.<Material>comparingInt(m -> severity.getOrDefault(m.getStockStatus(), 3))
                        .thenComparingInt(m -> nz(m.getCurrentStock()) - nz(m.getReorderPoint())))
                .limit(MAX_STOCK_ALERTS)
                .map(m -> new StockAlert(
                        m.getId() != null ? m.getId().toString() : null,
                        m.getCode() != null ? m.getCode().getValue() : null,
                        m.getName(),
                        m.getStockStatus().name(),
                        nz(m.getCurrentStock()),
                        m.getReorderPoint(),
                        m.getSafetyStock(),
                        nz(m.getStockOnOrder()),
                        m.getUnitOfMeasure() != null ? m.getUnitOfMeasure().name() : null))
                .toList();
    }

    /** The latest documents created in the modules the user can read. */
    private List<Activity> recentActivity(List<Requisition> reqs, List<PurchaseOrder> pos, List<GoodsReceipt> grs,
                                          List<Invoice> invs, List<Payment> pays, List<ReturnToVendor> rtvs) {
        Stream<Activity> all = Stream.of(
                reqs.stream().map(r -> new Activity("REQUISITION", code(r.getRequisitionCode() == null ? null : r.getRequisitionCode().getValue()),
                        r.getTitle(), name(r.getStatus()), r.getCreatedAt(), "/requisitions/view/" + r.getId())),
                pos.stream().map(o -> new Activity("PURCHASE_ORDER", code(o.getOrderCode() == null ? null : o.getOrderCode().getValue()),
                        o.getSupplierName(), name(o.getStatus()), o.getCreatedAt(), "/purchase-orders/" + o.getId())),
                grs.stream().map(r -> new Activity("GOODS_RECEIPT", code(r.getReceiptCode() == null ? null : r.getReceiptCode().getValue()),
                        r.getSupplierName(), name(r.getStatus()), r.getCreatedAt(), "/goods-receipts/" + r.getId())),
                invs.stream().map(i -> new Activity("INVOICE", code(i.getInvoiceCode() == null ? null : i.getInvoiceCode().getValue()),
                        i.getSupplierName(), name(i.getStatus()), i.getCreatedAt(), "/invoices/" + i.getId())),
                pays.stream().map(p -> new Activity("PAYMENT", code(p.getPaymentCode() == null ? null : p.getPaymentCode().getValue()),
                        p.getSupplierName(), name(p.getStatus()), p.getCreatedAt(), "/payments/" + p.getId())),
                rtvs.stream().map(r -> new Activity("RETURN", code(r.getReturnCode() == null ? null : r.getReturnCode().getValue()),
                        r.getSupplierName(), name(r.getStatus()), r.getCreatedAt(), "/returns/" + r.getId())))
                .flatMap(Function.identity());
        return all.filter(a -> a.date() != null)
                .sorted(Comparator.comparing(Activity::date).reversed())
                .limit(MAX_ACTIVITY)
                .toList();
    }

    // ============================================================
    // HELPERS
    // ============================================================

    /** The currency most orders, invoices and payments use; amounts in it are comparable. */
    static String dominantCurrency(List<PurchaseOrder> pos, List<Invoice> invs, List<Payment> pays) {
        return Stream.of(
                        pos.stream().map(PurchaseOrder::getCurrencyCode),
                        invs.stream().map(Invoice::getCurrencyCode),
                        pays.stream().map(Payment::getCurrencyCode))
                .flatMap(Function.identity())
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.<String, Long>comparingByValue().thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .map(Map.Entry::getKey)
                .orElse(DEFAULT_CURRENCY);
    }

    private static boolean isStockAlert(Material m) {
        return m.getStatus() == MaterialStatus.ACTIVE && m.getStockStatus() != null && m.getStockStatus() != StockStatus.IN_STOCK;
    }

    private static boolean isOverdue(Invoice invoice, LocalDate today) {
        return invoice.getDueDate() != null && invoice.getDueDate().isBefore(today) && amount(invoice.getOutstandingAmount()).signum() > 0;
    }

    private static <T> BigDecimal sum(List<T> items, Function<T, Money> money, Function<T, String> currencyOf, String currency) {
        return scale(items.stream()
                .filter(item -> sameCurrency(currencyOf.apply(item), currency))
                .map(item -> amount(money.apply(item)))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static BigDecimal lineValue(PurchaseOrderLine line) {
        if (line.getLineTotal() != null) return amount(line.getLineTotal());
        if (line.getUnitPrice() == null || line.getQuantity() == null) return BigDecimal.ZERO;
        return amount(line.getUnitPrice()).multiply(BigDecimal.valueOf(line.getQuantity()));
    }

    private static boolean sameCurrency(String code, String currency) {
        return code != null && code.equalsIgnoreCase(currency);
    }

    private static BigDecimal amount(Money money) {
        return money != null && money.getAmount() != null ? money.getAmount() : BigDecimal.ZERO;
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static List<BigDecimal> scaled(java.util.Collection<BigDecimal> values) {
        return values.stream().map(DashboardService::scale).toList();
    }

    private static Map<YearMonth, BigDecimal> zeroes(List<YearMonth> months) {
        Map<YearMonth, BigDecimal> map = new LinkedHashMap<>();
        months.forEach(m -> map.put(m, BigDecimal.ZERO));
        return map;
    }

    private static Double rate(long part, long whole) {
        if (whole <= 0) return null;
        return BigDecimal.valueOf(part * 100.0 / whole).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private static int nz(Integer value) {
        return value != null ? value : 0;
    }

    private static String code(String value) {
        return value != null ? value : "—";
    }

    private static String name(Enum<?> value) {
        return value != null ? value.name() : null;
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
