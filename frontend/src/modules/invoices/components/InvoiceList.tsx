import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import invoiceService from "../services/invoiceService";
import { INVOICE_STATUS_LABELS, INVOICE_TYPE_LABELS, type Invoice } from "../types/invoice.types";
import InvoiceFilters, { type InvoiceFilterValues } from "./InvoiceFilters";
import InvoiceStatusBadge, { InvoiceTypeBadge } from "./InvoiceStatusBadge";
import useInvoicePermissions from "../hooks/useInvoice";
import { formatAmount, isOverdue, isPartiallyPaid, outstandingAmount } from "../utils/invoiceLine";
import ListCard, { type FilterPill } from "../../../shared/components/page/ListCard";
import StatFilterCards, { type StatCard } from "../../../shared/components/page/StatFilterCards";
import { InitialsAvatar, ListFooter, StackedCell, TableStateRow, ViewAction } from "../../../shared/components/page/ListParts";
import { FloatingToast } from "../../../shared/components/page/DetailParts";
import { StatIcons } from "../../../shared/components/page/pageIcons";
import { BODY_CELL, HEAD_CELL } from "../../../shared/components/page/pageStyles";
import { useClientPagination } from "../../../shared/components/page/useClientPagination";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";
import { getApiErrorMessage } from "../../../shared/utils/apiError";
import { parseAmount } from "../../../shared/utils/moneyUtils";

type QuickFilter = "ALL" | "TO_VERIFY" | "TO_PAY" | "OVERDUE";

const QUICK_FILTERS: Record<QuickFilter, (i: Invoice) => boolean> = {
  ALL: () => true,
  TO_VERIFY: (i) => i.status === "SUBMITTED",
  TO_PAY: (i) => i.status === "VERIFIED",
  OVERDUE: (i) => isOverdue(i),
};

const EMPTY_FILTERS: InvoiceFilterValues = { status: "", type: "" };

/** Sums per currency so amounts in different currencies are never added together; credit notes subtract. */
function sumByCurrency(invoices: Invoice[], amount: (inv: Invoice) => number = outstandingAmount): string {
  const totals = new Map<string, number>();
  for (const inv of invoices) {
    const sign = inv.invoiceType === "CREDIT_NOTE" ? -1 : 1;
    totals.set(inv.currencyCode, (totals.get(inv.currencyCode) ?? 0) + sign * amount(inv));
  }
  if (totals.size === 0) return formatAmount(0);
  return [...totals.entries()].map(([currency, value]) => formatAmount(value, currency)).join(" + ");
}

export default function InvoiceList() {
  const permissions = useInvoicePermissions();
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [keyword, setKeyword] = useState("");
  const [quick, setQuick] = useState<QuickFilter>("ALL");
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [draftFilters, setDraftFilters] = useState<InvoiceFilterValues>(EMPTY_FILTERS);
  const [filters, setFilters] = useState<InvoiceFilterValues>(EMPTY_FILTERS);

  useEffect(() => {
    let cancelled = false;
    invoiceService
      .getAll()
      .then((res) => {
        if (!cancelled) setInvoices(res.data);
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load invoices."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const toVerify = invoices.filter(QUICK_FILTERS.TO_VERIFY);
  const toPay = invoices.filter(QUICK_FILTERS.TO_PAY);
  const overdue = invoices.filter(QUICK_FILTERS.OVERDUE);
  const cards: StatCard<QuickFilter>[] = [
    { id: "ALL", title: "All Invoices", value: invoices.length, unit: "invoices", subtitle: "Every supplier invoice", tone: "brand", icon: StatIcons.all },
    {
      id: "TO_VERIFY",
      title: "To Verify",
      value: sumByCurrency(toVerify, (i) => parseAmount(i.totalAmountWithTax ?? 0)),
      subtitle: `${toVerify.length} submitted`,
      tone: "blue",
      icon: StatIcons.pending,
      badge: toVerify.length,
    },
    { id: "TO_PAY", title: "To Pay", value: sumByCurrency(toPay), subtitle: `${toPay.length} verified`, tone: "green", icon: StatIcons.money, badge: toPay.length },
    { id: "OVERDUE", title: "Overdue", value: sumByCurrency(overdue), subtitle: "Past due date", tone: "red", icon: StatIcons.overdue, badge: overdue.length },
  ];

  const visible = useMemo(() => {
    const term = keyword.trim().toLowerCase();
    return invoices
      .filter(QUICK_FILTERS[quick])
      .filter((i) => !filters.status || i.status === filters.status)
      .filter((i) => !filters.type || i.invoiceType === filters.type)
      .filter(
        (i) =>
          !term ||
          [i.invoiceCode, i.externalReference, i.purchaseOrderCode, i.supplierName]
            .filter(Boolean)
            .some((v) => String(v).toLowerCase().includes(term))
      )
      .sort((a, b) => String(b.invoiceDate ?? "").localeCompare(String(a.invoiceDate ?? "")));
  }, [invoices, quick, filters, keyword]);

  const paging = useClientPagination(visible);
  const pills: FilterPill[] = [
    ...(filters.status
      ? [{ label: `Status: ${INVOICE_STATUS_LABELS[filters.status]}`, onRemove: () => setFilters({ ...filters, status: "" }) }]
      : []),
    ...(filters.type ? [{ label: `Type: ${INVOICE_TYPE_LABELS[filters.type]}`, onRemove: () => setFilters({ ...filters, type: "" }) }] : []),
  ];

  return (
    <>
      <FloatingToast feedback={error ? { type: "error", text: error } : null} onClose={() => setError(null)} />

      <StatFilterCards cards={cards} active={quick} onSelect={setQuick} />

      <ListCard
        title="Invoices List"
        search={{ value: keyword, onChange: setKeyword, placeholder: "Search invoices..." }}
        filter={{
          activeCount: pills.length,
          isOpen: isFilterOpen,
          onToggle: () => {
            setDraftFilters(filters);
            setIsFilterOpen(!isFilterOpen);
          },
          panel: (
            <InvoiceFilters
              isOpen={isFilterOpen}
              onClose={() => setIsFilterOpen(false)}
              value={draftFilters}
              onChange={setDraftFilters}
              onApply={() => {
                setFilters(draftFilters);
                setIsFilterOpen(false);
              }}
              onClear={() => {
                setDraftFilters(EMPTY_FILTERS);
                setFilters(EMPTY_FILTERS);
                setIsFilterOpen(false);
              }}
            />
          ),
        }}
        action={permissions.canRecord ? { label: "New Invoice", to: "/invoices/create" } : undefined}
        pills={pills}
        onClearPills={() => setFilters(EMPTY_FILTERS)}
        footer={
          <ListFooter page={paging.page} size={paging.size} total={paging.total} totalPages={paging.totalPages} onPageChange={paging.setPage} />
        }
      >
        <div className="max-w-full overflow-x-auto">
          <Table>
            <TableHeader className="border-b border-gray-100 bg-gray-50/50 dark:border-white/[0.05] dark:bg-gray-900/50">
              <TableRow>
                <TableCell isHeader className={HEAD_CELL}>Invoice</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Supplier</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Purchase Order</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Date / Due</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Amount</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Status</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Action</TableCell>
              </TableRow>
            </TableHeader>
            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {loading ? (
                <TableStateRow colSpan={7} loading message="Fetching invoices..." />
              ) : paging.pageItems.length === 0 ? (
                <TableStateRow colSpan={7} message="No invoices found." />
              ) : (
                paging.pageItems.map((inv) => (
                  <TableRow key={inv.id}>
                    <TableCell className={BODY_CELL}>
                      <div className="flex items-center gap-2">
                        <StackedCell
                          main={
                            <Link to={`/invoices/${inv.id}`} className="font-mono hover:text-brand-500">
                              {inv.invoiceCode}
                            </Link>
                          }
                          sub={inv.externalReference ? `Ref. ${inv.externalReference}` : "No supplier reference"}
                        />
                        {inv.invoiceType === "CREDIT_NOTE" && <InvoiceTypeBadge type={inv.invoiceType} />}
                      </div>
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <div className="flex items-center gap-3">
                        <InitialsAvatar name={inv.supplierName} />
                        <StackedCell main={inv.supplierName} sub={inv.supplierCode} />
                      </div>
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      {inv.purchaseOrderId ? (
                        <Link to={`/purchase-orders/${inv.purchaseOrderId}`} className="font-mono hover:text-brand-500">
                          {inv.purchaseOrderCode || "—"}
                        </Link>
                      ) : (
                        "—"
                      )}
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <StackedCell
                        main={inv.invoiceDate}
                        sub={
                          <span className={isOverdue(inv) ? "font-semibold text-error-500" : ""}>
                            {inv.dueDate ? `Due ${inv.dueDate}` : "No due date"}
                          </span>
                        }
                      />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <StackedCell
                        main={<span className="whitespace-nowrap">{formatAmount(inv.totalAmountWithTax, inv.currencyCode)}</span>}
                        sub={
                          isPartiallyPaid(inv)
                            ? `${formatAmount(outstandingAmount(inv), inv.currencyCode)} left to pay`
                            : inv.hasDiscrepancy
                              ? "Discrepancies found"
                              : undefined
                        }
                      />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <InvoiceStatusBadge status={inv.status} />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <ViewAction to={`/invoices/${inv.id}`} title="View Invoice" />
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </ListCard>
    </>
  );
}
