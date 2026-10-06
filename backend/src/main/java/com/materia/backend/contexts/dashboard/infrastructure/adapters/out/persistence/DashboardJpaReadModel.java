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

    private void addCurrencyCounts(Map<String, Long> counts, String entity) {
        List<Object[]> rows = em.createQuery("select upper(d.currencyCode), count(d) from " + entity + " d "
                + "where d.currencyCode is not null group by upper(d.currencyCode)", Object[].class).getResultList();
        rows.forEach(r -> counts.merge((String) r[0], (Long) r[1], Long::sum));
    }

    // ---- Requisitions ----

    @Override
    public long countRequisitions(RequisitionStatus status) {
        return em.createQuery("select count(r) from RequisitionJpaEntity r where r.status = :status", Long.class)
                .setParameter("status", status).getSingleResult();
    }

    // ---- Purchase orders ----

    @Override
    public long countOrders(Set<OrderStatus> statuses) {
        if (statuses.isEmpty()) return 0;
        return em.createQuery("select count(o) from PurchaseOrderJpaEntity o where o.status in :statuses", Long.class)
                .setParameter("statuses", statuses).getSingleResult();
    }

    @Override
    public long countOrdersExpectedBefore(Set<OrderStatus> statuses, LocalDate date) {
        if (statuses.isEmpty()) return 0;
        return em.createQuery("select count(o) from PurchaseOrderJpaEntity o where o.status in :statuses "
                        + "and o.expectedDeliveryDate < :date", Long.class)
                .setParameter("statuses", statuses).setParameter("date", date).getSingleResult();
    }

    @Override
    public BigDecimal sumOrders(Set<OrderStatus> statuses, String currency) {
        if (statuses.isEmpty()) return BigDecimal.ZERO;
        return em.createQuery("select coalesce(sum(o.grandTotal), 0) from PurchaseOrderJpaEntity o "
                        + "where o.status in :statuses and upper(o.currencyCode) = :currency", BigDecimal.class)
                .setParameter("statuses", statuses).setParameter("currency", upper(currency)).getSingleResult();
    }

    @Override
    public List<MonthAmount> ordersByMonth(Set<OrderStatus> statuses, String currency, LocalDate from, LocalDate to) {
        if (statuses.isEmpty()) return List.of();
        return months(em.createQuery("select extract(year from o.orderDate), extract(month from o.orderDate), "
                        + "coalesce(sum(o.grandTotal), 0) from PurchaseOrderJpaEntity o "
                        + "where o.status in :statuses and upper(o.currencyCode) = :currency "
                        + "and o.orderDate between :from and :to "
                        + "group by extract(year from o.orderDate), extract(month from o.orderDate)", Object[].class)
                .setParameter("statuses", statuses).setParameter("currency", upper(currency))
                .setParameter("from", from).setParameter("to", to));
    }

    @Override
    public Map<OrderStatus, Long> ordersByStatus() {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        em.createQuery("select o.status, count(o) from PurchaseOrderJpaEntity o where o.status is not null "
                        + "group by o.status", Object[].class)
                .getResultList()
                .forEach(r -> counts.put((OrderStatus) r[0], (Long) r[1]));
        return counts;
    }

    @Override
    public List<Group> spendByCategory(Set<OrderStatus> statuses, String currency) {
        if (statuses.isEmpty()) return List.of();
        return em.createQuery("select " + CATEGORY + ", count(l), coalesce(sum(" + LINE_VALUE + "), 0) "
                        + "from PurchaseOrderLineJpaEntity l join l.purchaseOrder o "
                        + "left join MaterialJpaEntity m on (m.id = l.materialId "
                        + "or (l.materialId is null and m.code = l.materialCode)) "
                        + "where o.status in :statuses and upper(o.currencyCode) = :currency "
                        + "group by " + CATEGORY, Object[].class)
                .setParameter("statuses", statuses).setParameter("currency", upper(currency))
                .getResultList().stream()
                .map(r -> new Group((String) r[0], (String) r[0], (Long) r[1], amount(r[2])))
                .toList();
    }

    @Override
    public List<Group> spendBySupplier(Set<OrderStatus> statuses, String currency) {
        if (statuses.isEmpty()) return List.of();
        return em.createQuery("select o.supplierId, max(o.supplierName), count(o), coalesce(sum(o.grandTotal), 0) "
                        + "from PurchaseOrderJpaEntity o where o.status in :statuses and upper(o.currencyCode) = :currency "
                        + "and o.supplierId is not null group by o.supplierId", Object[].class)
                .setParameter("statuses", statuses).setParameter("currency", upper(currency))
                .getResultList().stream()
                .map(r -> new Group(r[0].toString(), (String) r[1], (Long) r[2], amount(r[3])))
                .toList();
    }

    // ---- Goods receipts ----

    @Override
    public long countReceipts(Set<ReceiptStatus> statuses, LocalDate from, LocalDate to, boolean withRejectionOnly) {
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
    public ReceiptTotals receiptTotals(Set<ReceiptStatus> statuses) {
        if (statuses.isEmpty()) return ReceiptTotals.NONE;
        Object[] r = em.createQuery("select " + RECEIPT_TOTALS + " from GoodsReceiptJpaEntity r where r.status in :statuses",
                        Object[].class)
                .setParameter("statuses", statuses).getSingleResult();
        return receiptTotals(r, 0);
    }

    @Override
    public Map<UUID, ReceiptTotals> receiptTotalsBySupplier(Set<ReceiptStatus> statuses) {
        Map<UUID, ReceiptTotals> totals = new HashMap<>();
        if (statuses.isEmpty()) return totals;
        em.createQuery("select r.supplierId, " + RECEIPT_TOTALS + " from GoodsReceiptJpaEntity r "
                        + "where r.status in :statuses and r.supplierId is not null group by r.supplierId", Object[].class)
                .setParameter("statuses", statuses)
                .getResultList()
                .forEach(r -> totals.put((UUID) r[0], receiptTotals(r, 1)));
        return totals;
    }

    private static ReceiptTotals receiptTotals(Object[] r, int offset) {
        return new ReceiptTotals(number(r[offset]), number(r[offset + 1]), number(r[offset + 2]), number(r[offset + 3]),
                number(r[offset + 4]), number(r[offset + 5]));
    }

    // ---- Invoices ----

    @Override
    public List<DueGroup> outstandingByDueDate(InvoiceType type, Set<InvoiceStatus> statuses) {
        if (statuses.isEmpty()) return List.of();
        return em.createQuery("select i.dueDate, upper(i.currencyCode), count(i), sum(" + OUTSTANDING + ") "
                        + "from InvoiceJpaEntity i where i.invoiceType = :type and i.status in :statuses "
                        + "and " + OUTSTANDING + " > 0 group by i.dueDate, upper(i.currencyCode)", Object[].class)
                .setParameter("type", type).setParameter("statuses", statuses)
                .getResultList().stream()
                .map(r -> new DueGroup((LocalDate) r[0], (String) r[1], (Long) r[2], amount(r[3])))
                .toList();
    }

    @Override
    public List<MonthAmount> invoicedByMonth(Set<InvoiceStatus> excluded, String currency, LocalDate from, LocalDate to) {
        return months(em.createQuery("select extract(year from i.invoiceDate), extract(month from i.invoiceDate), "
                        + "coalesce(sum(case when i.invoiceType = :credit then 0 - i.totalAmountWithTax "
                        + "else i.totalAmountWithTax end), 0) from InvoiceJpaEntity i "
                        + "where i.status not in :excluded and upper(i.currencyCode) = :currency "
                        + "and i.invoiceDate between :from and :to "
                        + "group by extract(year from i.invoiceDate), extract(month from i.invoiceDate)", Object[].class)
                .setParameter("credit", InvoiceType.CREDIT_NOTE).setParameter("excluded", excluded)
                .setParameter("currency", upper(currency)).setParameter("from", from).setParameter("to", to));
    }

    // ---- Payments ----

    @Override
    public long countPayments(PaymentStatus status) {
        return em.createQuery("select count(p) from PaymentJpaEntity p where p.status = :status", Long.class)
                .setParameter("status", status).getSingleResult();
    }

    @Override
    public List<MonthAmount> paymentsByMonth(PaymentStatus status, String currency, LocalDate from, LocalDate to) {
        return months(em.createQuery("select extract(year from p.confirmedDate), extract(month from p.confirmedDate), "
                        + "coalesce(sum(p.totalAmount), 0) from PaymentJpaEntity p "
                        + "where p.status = :status and upper(p.currencyCode) = :currency "
                        + "and p.confirmedDate >= :from and p.confirmedDate < :to "
                        + "group by extract(year from p.confirmedDate), extract(month from p.confirmedDate)", Object[].class)
                .setParameter("status", status).setParameter("currency", upper(currency))
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
    public long countReturns(ReturnStatus status) {
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
}
