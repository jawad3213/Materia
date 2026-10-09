package com.materia.backend.contexts.dashboard.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.dashboard.domain.ports.out.DashboardReadModel;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.payment.domain.enums.PaymentStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** The dashboard's aggregates as JPQL over the modules' persistence entities, computed by the database. */
@Component
public class DashboardJpaReadModel implements DashboardReadModel {

    /** Value of an order line: its total, or price × quantity when the total is missing. */
    private static final String LINE_VALUE = "coalesce(l.lineTotal, l.unitPrice * l.quantity, 0)";
    private static final String CATEGORY = "case when m.categoryName is null or trim(m.categoryName) = '' "
            + "then 'Uncategorized' else m.categoryName end";
    /** What remains to be paid on an invoice. */
    private static final String OUTSTANDING = "(i.totalAmountWithTax - coalesce(i.paidAmount, 0))";
    /** An active material at or below its reorder level (out of stock, below safety stock or reorder point). */
    private static final String STOCK_ALERT = "m.status = :active and m.currentStock is not null and (m.currentStock <= 0 "
            + "or (m.safetyStock is not null and m.currentStock <= m.safetyStock) "
            + "or (m.reorderPoint is not null and m.currentStock <= m.reorderPoint))";

    @PersistenceContext
    private EntityManager em;

    // ---- Currency ----

    @Override
    public Map<String, Long> documentsByCurrency(boolean orders, boolean invoices, boolean payments) {
        Map<String, Long> counts = new LinkedHashMap<>();
        if (orders) addCurrencyCounts(counts, "PurchaseOrderJpaEntity");
        if (invoices) addCurrencyCounts(counts, "InvoiceJpaEntity");
        if (payments) addCurrencyCounts(counts, "PaymentJpaEntity");
        return counts;
    }

    @Override
    public Map<String, Long> documentsValuedAtOwnDate(String reportingCurrency, boolean orders, boolean invoices, boolean payments) {
        Map<String, Long> counts = new LinkedHashMap<>();
        if (orders) addValuedCounts(counts, "PurchaseOrderJpaEntity", reportingCurrency);
        if (invoices) addValuedCounts(counts, "InvoiceJpaEntity", reportingCurrency);
        if (payments) addValuedCounts(counts, "PaymentJpaEntity", reportingCurrency);
        return counts;
    }

    private void addValuedCounts(Map<String, Long> counts, String entity, String reportingCurrency) {
        em.createQuery("select upper(d.currencyCode), count(d) from " + entity + " d where d.currencyCode is not null "
                        + "and upper(d.currencyCode) <> :reporting and d.fxReportingCurrency = :reporting "
                        + "and d.fxRateToReporting is not null group by upper(d.currencyCode)", Object[].class)
                .setParameter("reporting", upper(reportingCurrency))
                .getResultList()
                .forEach(r -> counts.merge((String) r[0], (Long) r[1], Long::sum));
    }

    private void addCurrencyCounts(Map<String, Long> counts, String entity) {
        List<Object[]> rows = em.createQuery("select upper(d.currencyCode), count(d) from " + entity + " d "
                + "where d.currencyCode is not null group by upper(d.currencyCode)", Object[].class).getResultList();
        rows.forEach(r -> counts.merge((String) r[0], (Long) r[1], Long::sum));
    }

    // ---- Requisitions ----

    @Override
    public long countRequisitions(String statusName) {
        RequisitionStatus status = RequisitionStatus.valueOf(statusName);
        return em.createQuery("select count(r) from RequisitionJpaEntity r where r.status = :status", Long.class)
                .setParameter("status", status).getSingleResult();
    }

    // ---- Purchase orders ----

    @Override
    public long countOrders(Set<String> statusNames) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return 0;
        return em.createQuery("select count(o) from PurchaseOrderJpaEntity o where o.status in :statuses", Long.class)
                .setParameter("statuses", statuses).getSingleResult();
    }

    @Override
    public long countOrdersExpectedBefore(Set<String> statusNames, LocalDate date) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return 0;
        return em.createQuery("select count(o) from PurchaseOrderJpaEntity o where o.status in :statuses "
                        + "and o.expectedDeliveryDate < :date", Long.class)
                .setParameter("statuses", statuses).setParameter("date", date).getSingleResult();
    }

    @Override
    public BigDecimal sumOrders(Set<String> statusNames, Rates rates) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return BigDecimal.ZERO;
        return amount(em.createQuery("select coalesce(sum(" + converted("o", "o.grandTotal", rates) + "), 0) "
                        + "from PurchaseOrderJpaEntity o where o.status in :statuses", Object.class)
                .setParameter("statuses", statuses).getSingleResult());
    }

    @Override
    public List<MonthAmount> ordersByMonth(Set<String> statusNames, Rates rates, LocalDate from, LocalDate to) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return List.of();
        return months(em.createQuery("select extract(year from o.orderDate), extract(month from o.orderDate), "
                        + "coalesce(sum(" + converted("o", "o.grandTotal", rates) + "), 0) from PurchaseOrderJpaEntity o "
                        + "where o.status in :statuses and o.orderDate between :from and :to "
                        + "group by extract(year from o.orderDate), extract(month from o.orderDate)", Object[].class)
                .setParameter("statuses", statuses).setParameter("from", from).setParameter("to", to));
    }

    @Override
    public Map<String, Long> ordersByStatus() {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        em.createQuery("select o.status, count(o) from PurchaseOrderJpaEntity o where o.status is not null "
                        + "group by o.status", Object[].class)
                .getResultList()
                .forEach(r -> counts.put((OrderStatus) r[0], (Long) r[1]));
        // The EnumMap iterates in lifecycle order; keep that order for the names.
        Map<String, Long> byName = new LinkedHashMap<>();
        counts.forEach((status, count) -> byName.put(status.name(), count));
        return byName;
    }

    @Override
    public List<Group> spendByCategory(Set<String> statusNames, Rates rates) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return List.of();
        return em.createQuery("select " + CATEGORY + ", count(l), coalesce(sum(" + converted("o", LINE_VALUE, rates) + "), 0) "
                        + "from PurchaseOrderLineJpaEntity l join l.purchaseOrder o "
                        + "left join MaterialJpaEntity m on (m.id = l.materialId "
                        + "or (l.materialId is null and m.code = l.materialCode)) "
                        + "where o.status in :statuses "
                        + "group by " + CATEGORY, Object[].class)
                .setParameter("statuses", statuses)
                .getResultList().stream()
                .map(r -> new Group((String) r[0], (String) r[0], (Long) r[1], amount(r[2])))
                .toList();
    }

    @Override
    public List<Group> spendBySupplier(Set<String> statusNames, Rates rates) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return List.of();
        return em.createQuery("select o.supplierId, max(o.supplierName), count(o), "
                        + "coalesce(sum(" + converted("o", "o.grandTotal", rates) + "), 0) "
                        + "from PurchaseOrderJpaEntity o where o.status in :statuses "
                        + "and o.supplierId is not null group by o.supplierId", Object[].class)
                .setParameter("statuses", statuses)
                .getResultList().stream()
                .map(r -> new Group(r[0].toString(), (String) r[1], (Long) r[2], amount(r[3])))
                .toList();
    }

    // ---- Goods receipts ----

    @Override
    public long countReceipts(Set<String> statusNames, LocalDate from, LocalDate to, boolean withRejectionOnly) {
        Set<ReceiptStatus> statuses = enums(ReceiptStatus.class, statusNames);
        if (statuses.isEmpty()) return 0;
        return em.createQuery("select count(r) from GoodsReceiptJpaEntity r where r.status in :statuses "
                        + "and r.receiptDate between :from and :to"
                        + (withRejectionOnly ? " and r.totalQuantityRejected > 0" : ""), Long.class)
                .setParameter("statuses", statuses).setParameter("from", from).setParameter("to", to)
                .getSingleResult();
    }

    private static final String RECEIPT_TOTALS = "count(r), coalesce(sum(r.totalQuantityReceived), 0), "
            + "coalesce(sum(r.totalQuantityAccepted), 0), coalesce(sum(r.totalQuantityRejected), 0), "
            + "coalesce(sum(case when r.receiptDate is not null and r.expectedDeliveryDate is not null then 1 else 0 end), 0), "
            + "coalesce(sum(case when r.receiptDate is not null and r.expectedDeliveryDate is not null "
            + "and r.receiptDate <= r.expectedDeliveryDate then 1 else 0 end), 0)";

    @Override
    public ReceiptTotals receiptTotals(Set<String> statusNames) {
        Set<ReceiptStatus> statuses = enums(ReceiptStatus.class, statusNames);
        if (statuses.isEmpty()) return ReceiptTotals.NONE;
        Object[] r = em.createQuery("select " + RECEIPT_TOTALS + " from GoodsReceiptJpaEntity r where r.status in :statuses",
                        Object[].class)
                .setParameter("statuses", statuses).getSingleResult();
        return receiptTotals(r, 0);
    }

    @Override
    public Map<UUID, ReceiptTotals> receiptTotalsBySupplier(Set<String> statusNames) {
        Set<ReceiptStatus> statuses = enums(ReceiptStatus.class, statusNames);
        Map<UUID, ReceiptTotals> totals = new HashMap<>();
        if (statuses.isEmpty()) return totals;
        em.createQuery("select r.supplierId, " + RECEIPT_TOTALS + " from GoodsReceiptJpaEntity r "
                        + "where r.status in :statuses and r.supplierId is not null group by r.supplierId", Object[].class)
                .setParameter("statuses", statuses)
                .getResultList()
                .forEach(r -> {
                    UUID supplierId = uuid(r[0]);
                    if (supplierId != null) totals.merge(supplierId, receiptTotals(r, 1), DashboardJpaReadModel::sum);
                });
        return totals;
    }

    /** A supplier id read from a row; a value that is not a UUID cannot match a supplier and is skipped. */
    private static UUID uuid(Object value) {
        if (value instanceof UUID id) return id;
        try {
            return value == null ? null : UUID.fromString(value.toString().trim());
        } catch (IllegalArgumentException notAUuid) {
            return null;
        }
    }

    /** Two spellings of the same id are grouped apart by the database; add them back together. */
    private static ReceiptTotals sum(ReceiptTotals a, ReceiptTotals b) {
        return new ReceiptTotals(a.receipts() + b.receipts(), a.unitsReceived() + b.unitsReceived(),
                a.unitsAccepted() + b.unitsAccepted(), a.unitsRejected() + b.unitsRejected(),
                a.datedReceipts() + b.datedReceipts(), a.onTimeReceipts() + b.onTimeReceipts());
    }

    private static ReceiptTotals receiptTotals(Object[] r, int offset) {
        return new ReceiptTotals(number(r[offset]), number(r[offset + 1]), number(r[offset + 2]), number(r[offset + 3]),
                number(r[offset + 4]), number(r[offset + 5]));
    }

    // ---- Invoices ----

    @Override
    public List<DueGroup> outstandingByDueDate(String typeName, Set<String> statusNames, Rates rates) {
        InvoiceType type = InvoiceType.valueOf(typeName);
        Set<InvoiceStatus> statuses = enums(InvoiceStatus.class, statusNames);
        if (statuses.isEmpty()) return List.of();
        String reporting = upper(rates.reportingCurrency());
        return em.createQuery("select i.dueDate, count(i), coalesce(sum(" + converted("i", OUTSTANDING, rates) + "), 0) "
                        + "from InvoiceJpaEntity i where i.invoiceType = :type and i.status in :statuses "
                        + "and " + OUTSTANDING + " > 0 group by i.dueDate", Object[].class)
                .setParameter("type", type).setParameter("statuses", statuses)
                .getResultList().stream()
                .map(r -> new DueGroup((LocalDate) r[0], reporting, (Long) r[1], amount(r[2])))
                .toList();
    }

    @Override
    public List<MonthAmount> invoicedByMonth(Set<String> excludedNames, Rates rates, LocalDate from, LocalDate to) {
        Set<InvoiceStatus> excluded = enums(InvoiceStatus.class, excludedNames);
        String signedTotal = "(case when i.invoiceType = :credit then 0 - i.totalAmountWithTax else i.totalAmountWithTax end)";
        return months(em.createQuery("select extract(year from i.invoiceDate), extract(month from i.invoiceDate), "
                        + "coalesce(sum(" + converted("i", signedTotal, rates) + "), 0) from InvoiceJpaEntity i "
                        + "where i.status not in :excluded and i.invoiceDate between :from and :to "
                        + "group by extract(year from i.invoiceDate), extract(month from i.invoiceDate)", Object[].class)
                .setParameter("credit", InvoiceType.CREDIT_NOTE).setParameter("excluded", excluded)
                .setParameter("from", from).setParameter("to", to));
    }

    // ---- Payments ----

    @Override
    public long countPayments(String statusName) {
        PaymentStatus status = PaymentStatus.valueOf(statusName);
        return em.createQuery("select count(p) from PaymentJpaEntity p where p.status = :status", Long.class)
                .setParameter("status", status).getSingleResult();
    }

    @Override
    public List<MonthAmount> paymentsByMonth(String statusName, Rates rates, LocalDate from, LocalDate to) {
        PaymentStatus status = PaymentStatus.valueOf(statusName);
        return months(em.createQuery("select extract(year from p.confirmedDate), extract(month from p.confirmedDate), "
                        + "coalesce(sum(" + converted("p", "p.totalAmount", rates) + "), 0) from PaymentJpaEntity p "
                        + "where p.status = :status and p.confirmedDate >= :from and p.confirmedDate < :to "
                        + "group by extract(year from p.confirmedDate), extract(month from p.confirmedDate)", Object[].class)
                .setParameter("status", status)
                .setParameter("from", from.atStartOfDay()).setParameter("to", to.plusDays(1).atStartOfDay()));
    }

    // ---- Stock ----

    @Override
    public long countStockAlerts(boolean outOfStockOnly) {
        return em.createQuery("select count(m) from MaterialJpaEntity m where " + STOCK_ALERT
                        + (outOfStockOnly ? " and m.currentStock <= 0" : ""), Long.class)
                .setParameter("active", MaterialStatus.ACTIVE).getSingleResult();
    }

    @Override
    public List<StockRow> stockAlerts(int limit) {
        return em.createQuery("select m.id, m.code, m.name, m.currentStock, m.reorderPoint, m.safetyStock, "
                        + "coalesce(m.stockOnOrder, 0), m.unitOfMeasure from MaterialJpaEntity m where " + STOCK_ALERT
                        + " order by case when m.currentStock <= 0 then 0 "
                        + "when m.safetyStock is not null and m.currentStock <= m.safetyStock then 1 else 2 end, "
                        + "m.currentStock - coalesce(m.reorderPoint, 0)", Object[].class)
                .setParameter("active", MaterialStatus.ACTIVE)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(r -> new StockRow((UUID) r[0], (String) r[1], (String) r[2], (Integer) r[3], (Integer) r[4],
                        (Integer) r[5], (Integer) r[6], r[7] != null ? r[7].toString() : null))
                .toList();
    }

    // ---- Returns ----

    @Override
    public long countReturns(String statusName) {
        ReturnStatus status = ReturnStatus.valueOf(statusName);
        return em.createQuery("select count(r) from ReturnToVendorJpaEntity r where r.status = :status", Long.class)
                .setParameter("status", status.name()).getSingleResult();
    }

    // ---- Activity ----

    @Override
    public List<ActivityRow> latest(ActivitySource source, int limit) {
        String select = switch (source) {
            case REQUISITION -> "select d.id, d.requisitionCode, d.title, d.status, d.createdAt from RequisitionJpaEntity d";
            case PURCHASE_ORDER -> "select d.id, d.orderCode, d.supplierName, d.status, d.createdAt from PurchaseOrderJpaEntity d";
            case GOODS_RECEIPT -> "select d.id, d.receiptCode, d.supplierName, d.status, d.createdAt from GoodsReceiptJpaEntity d";
            case INVOICE -> "select d.id, d.invoiceCode, d.supplierName, d.status, d.createdAt from InvoiceJpaEntity d";
            case PAYMENT -> "select d.id, d.paymentCode, d.supplierName, d.status, d.createdAt from PaymentJpaEntity d";
            case RETURN -> "select d.id, d.returnCode, d.supplierName, d.status, d.createdAt from ReturnToVendorJpaEntity d";
        };
        return em.createQuery(select + " where d.createdAt is not null order by d.createdAt desc", Object[].class)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(r -> new ActivityRow((UUID) r[0], (String) r[1], (String) r[2],
                        r[3] != null ? r[3].toString() : null, (LocalDateTime) r[4]))
                .toList();
    }

    // ---- Helpers ----

    /**
     * {@code amount} in the reporting currency: times the document's own rate copy when it was made for this reporting
     * currency, otherwise times the latest rate of the document's currency. Without any rate the product is null and
     * SUM leaves the document out. Currency codes come from CurrencyCode and rates are numbers, never user input.
     */
    static String converted(String alias, String amount, Rates rates) {
        String reporting = upper(rates.reportingCurrency());
        StringBuilder latest = new StringBuilder("case upper(").append(alias).append(".currencyCode)");
        latest.append(" when '").append(reporting).append("' then 1bd");
        rates.current().forEach((currency, rate) -> {
            if (rate != null && !reporting.equalsIgnoreCase(currency) && currency.matches("[A-Za-z]{3}")) {
                latest.append(" when '").append(currency.toUpperCase()).append("' then ")
                        .append(rate.toPlainString()).append("bd");
            }
        });
        latest.append(" else null end");
        return "((" + amount + ") * (case when " + alias + ".fxReportingCurrency = '" + reporting + "' and "
                + alias + ".fxRateToReporting is not null then " + alias + ".fxRateToReporting else " + latest + " end))";
    }

    private static List<MonthAmount> months(TypedQuery<Object[]> query) {
        return query.getResultList().stream()
                .map(r -> new MonthAmount(YearMonth.of(((Number) r[0]).intValue(), ((Number) r[1]).intValue()), amount(r[2])))
                .toList();
    }

    private static BigDecimal amount(Object value) {
        if (value == null) return BigDecimal.ZERO;
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString());
    }

    private static long number(Object value) {
        return value != null ? ((Number) value).longValue() : 0L;
    }

    private static String upper(String currency) {
        return currency != null ? currency.toUpperCase() : null;
    }

    /** The enum constants named, for a query parameter; an unknown name is a programming error and fails fast. */
    private static <E extends Enum<E>> Set<E> enums(Class<E> type, Set<String> names) {
        Set<E> values = EnumSet.noneOf(type);
        names.forEach(name -> values.add(Enum.valueOf(type, name)));
        return values;
    }
}
