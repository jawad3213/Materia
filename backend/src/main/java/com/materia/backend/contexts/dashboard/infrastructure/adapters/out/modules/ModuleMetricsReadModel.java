package com.materia.backend.contexts.dashboard.infrastructure.adapters.out.modules;

import com.materia.backend.common.application.metrics.CountedAmount;
import com.materia.backend.common.application.metrics.RecentDocument;
import com.materia.backend.common.application.metrics.ReportingRates;
import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptMetricsUseCase;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceMetricsUseCase;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialMetricsUseCase;
import com.materia.backend.contexts.payment.domain.ports.in.PaymentMetricsUseCase;
import com.materia.backend.contexts.purchaseOrder.application.dtos.MaterialSpend;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderMetricsUseCase;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionMetricsUseCase;
import com.materia.backend.contexts.returnToVendor.domain.ports.in.ReturnMetricsUseCase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The dashboard's figures, composed from each context's own reporting use case (API composition): no context's
 * table is read here, so each call becomes a request to that context's service once the contexts are split.
 */
@Component
public class ModuleMetricsReadModel implements DashboardReadModel {

    private static final String UNCATEGORIZED = "Uncategorized";

    private final RequisitionMetricsUseCase requisitions;
    private final PurchaseOrderMetricsUseCase orders;
    private final GoodsReceiptMetricsUseCase receipts;
    private final InvoiceMetricsUseCase invoices;
    private final PaymentMetricsUseCase payments;
    private final MaterialMetricsUseCase materials;
    private final ReturnMetricsUseCase returns;

    public ModuleMetricsReadModel(RequisitionMetricsUseCase requisitions, PurchaseOrderMetricsUseCase orders,
                                  GoodsReceiptMetricsUseCase receipts, InvoiceMetricsUseCase invoices,
                                  PaymentMetricsUseCase payments, MaterialMetricsUseCase materials,
                                  ReturnMetricsUseCase returns) {
        this.requisitions = requisitions;
        this.orders = orders;
        this.receipts = receipts;
        this.invoices = invoices;
        this.payments = payments;
        this.materials = materials;
        this.returns = returns;
    }

    private static ReportingRates reporting(Rates rates) {
        return new ReportingRates(rates.reportingCurrency(), rates.current());
    }

    private static List<MonthAmount> months(List<com.materia.backend.common.application.metrics.MonthAmount> months) {
        return months.stream().map(m -> new MonthAmount(m.month(), m.amount())).toList();
    }

    // ---- Currency ----

    @Override
    public Map<String, Long> documentsByCurrency(boolean withOrders, boolean withInvoices, boolean withPayments) {
        Map<String, Long> counts = new LinkedHashMap<>();
        if (withOrders) orders.documentsByCurrency().forEach((c, n) -> counts.merge(c, n, Long::sum));
        if (withInvoices) invoices.documentsByCurrency().forEach((c, n) -> counts.merge(c, n, Long::sum));
        if (withPayments) payments.documentsByCurrency().forEach((c, n) -> counts.merge(c, n, Long::sum));
        return counts;
    }

    @Override
    public Map<String, Long> documentsValuedAtOwnDate(String reportingCurrency, boolean withOrders, boolean withInvoices,
                                                      boolean withPayments) {
        Map<String, Long> counts = new LinkedHashMap<>();
        if (withOrders) orders.valuedAtOwnDate(reportingCurrency).forEach((c, n) -> counts.merge(c, n, Long::sum));
        if (withInvoices) invoices.valuedAtOwnDate(reportingCurrency).forEach((c, n) -> counts.merge(c, n, Long::sum));
        if (withPayments) payments.valuedAtOwnDate(reportingCurrency).forEach((c, n) -> counts.merge(c, n, Long::sum));
        return counts;
    }

    // ---- Requisitions ----

    @Override
    public long countRequisitions(String status) {
        return requisitions.countInStatus(status);
    }

    // ---- Purchase orders ----

    @Override
    public long countOrders(Set<String> statuses) {
        return orders.count(statuses);
    }

    @Override
    public long countOrdersExpectedBefore(Set<String> statuses, LocalDate date) {
        return orders.countExpectedBefore(statuses, date);
    }

    @Override
    public BigDecimal sumOrders(Set<String> statuses, Rates rates) {
        return orders.sum(statuses, reporting(rates));
    }

    @Override
    public List<MonthAmount> ordersByMonth(Set<String> statuses, Rates rates, LocalDate from, LocalDate to) {
        return months(orders.byMonth(statuses, reporting(rates), from, to));
    }

    @Override
    public Map<String, Long> ordersByStatus() {
        return orders.countByStatus();
    }

    /**
     * Order-line value per material category: the order context gives the spend per material, the master data context
     * each material's category (by id, or by code for a line without one). A line whose material is unknown or has no
     * category counts as "Uncategorized".
     */
    @Override
    public List<Group> spendByCategory(Set<String> statuses, Rates rates) {
        List<MaterialSpend> spend = orders.spendByMaterial(statuses, reporting(rates));
        if (spend.isEmpty()) return List.of();
        Map<UUID, String> byId = materials.categoryNamesById(spend.stream()
                .map(MaterialSpend::materialId).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<String, String> byCode = materials.categoryNamesByCode(spend.stream()
                .filter(s -> s.materialId() == null).map(MaterialSpend::materialCode).filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        Map<String, long[]> lines = new LinkedHashMap<>();
        Map<String, BigDecimal> amounts = new LinkedHashMap<>();
        for (MaterialSpend s : spend) {
            String category = s.materialId() != null ? byId.get(s.materialId()) : byCode.get(s.materialCode());
            if (category == null || category.isBlank()) category = UNCATEGORIZED;
            lines.computeIfAbsent(category, k -> new long[1])[0] += s.lines();
            amounts.merge(category, s.amount(), BigDecimal::add);
        }
        return amounts.entrySet().stream()
                .map(e -> new Group(e.getKey(), e.getKey(), lines.get(e.getKey())[0], e.getValue()))
                .toList();
    }

    @Override
    public List<Group> spendBySupplier(Set<String> statuses, Rates rates) {
        return orders.spendBySupplier(statuses, reporting(rates)).stream().map(ModuleMetricsReadModel::group).toList();
    }

    private static Group group(CountedAmount c) {
        return new Group(c.key(), c.label(), c.count(), c.amount());
    }

    // ---- Goods receipts ----

    @Override
    public long countReceipts(Set<String> statuses, LocalDate from, LocalDate to, boolean withRejectionOnly) {
        return receipts.count(statuses, from, to, withRejectionOnly);
    }

    @Override
    public ReceiptTotals receiptTotals(Set<String> statuses) {
        return totals(receipts.totals(statuses));
    }

    @Override
    public Map<UUID, ReceiptTotals> receiptTotalsBySupplier(Set<String> statuses) {
        Map<UUID, ReceiptTotals> result = new LinkedHashMap<>();
        receipts.totalsBySupplier(statuses).forEach((supplier, t) -> result.put(supplier, totals(t)));
        return result;
    }

    private static ReceiptTotals totals(com.materia.backend.contexts.goodsReceipt.application.dtos.ReceiptTotals t) {
        return new ReceiptTotals(t.receipts(), t.unitsReceived(), t.unitsAccepted(), t.unitsRejected(),
                t.datedReceipts(), t.onTimeReceipts());
    }

    // ---- Invoices ----

    @Override
    public List<DueGroup> outstandingByDueDate(String type, Set<String> statuses, Rates rates) {
        return invoices.outstandingByDueDate(type, statuses, reporting(rates)).stream()
                .map(o -> new DueGroup(o.dueDate(), o.currency(), o.invoices(), o.outstanding()))
                .toList();
    }

    @Override
    public List<MonthAmount> invoicedByMonth(Set<String> excluded, Rates rates, LocalDate from, LocalDate to) {
        return months(invoices.invoicedByMonth(excluded, reporting(rates), from, to));
    }

    // ---- Payments ----

    @Override
    public long countPayments(String status) {
        return payments.countInStatus(status);
    }

    @Override
    public List<MonthAmount> paymentsByMonth(String status, Rates rates, LocalDate from, LocalDate to) {
        return months(payments.byMonth(status, reporting(rates), from, to));
    }

    // ---- Stock ----

    @Override
    public long countStockAlerts(boolean outOfStockOnly) {
        return materials.countStockAlerts(outOfStockOnly);
    }

    @Override
    public List<StockRow> stockAlerts(int limit) {
        return materials.stockAlerts(limit).stream()
                .map(a -> new StockRow(a.id(), a.code(), a.name(), a.currentStock(), a.reorderPoint(), a.safetyStock(),
                        a.stockOnOrder(), a.unit()))
                .toList();
    }

    // ---- Returns ----

    @Override
    public long countReturns(String status) {
        return returns.countInStatus(status);
    }

    // ---- Activity ----

    @Override
    public List<ActivityRow> latest(ActivitySource source, int limit) {
        List<RecentDocument> documents = switch (source) {
            case REQUISITION -> requisitions.latest(limit);
            case PURCHASE_ORDER -> orders.latest(limit);
            case GOODS_RECEIPT -> receipts.latest(limit);
            case INVOICE -> invoices.latest(limit);
            case PAYMENT -> payments.latest(limit);
            case RETURN -> returns.latest(limit);
        };
        return documents.stream()
                .map(d -> new ActivityRow(d.id(), d.code(), d.title(), d.status(), d.createdAt()))
                .toList();
    }
}
