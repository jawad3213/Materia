import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import paymentService from "../services/paymentService";
import { PAYMENT_METHOD_LABELS, PAYMENT_STATUS_LABELS, type Payment } from "../types/payment.types";
import PaymentFilters, { type PaymentFilterValues } from "./PaymentFilters";
import PaymentStatusBadge from "./PaymentStatusBadge";
import usePaymentPermissions from "../hooks/usePayment";
import { formatAmount } from "../../invoices/utils/invoiceLine";
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

type QuickFilter = "ALL" | "DRAFT" | "PENDING" | "PAID_THIS_MONTH";

const thisMonth = () => new Date().toISOString().slice(0, 7);

const QUICK_FILTERS: Record<QuickFilter, (p: Payment) => boolean> = {
  ALL: () => true,
  DRAFT: (p) => p.status === "DRAFT",
  PENDING: (p) => p.status === "PENDING",
  PAID_THIS_MONTH: (p) => p.status === "COMPLETED" && String(p.confirmedDate ?? "").startsWith(thisMonth()),
};

const EMPTY_FILTERS: PaymentFilterValues = { status: "", method: "" };

/** Sums amounts per currency, so totals in different currencies are never added together. */
function sumByCurrency(payments: Payment[]): string {
  const totals = new Map<string, number>();
  for (const p of payments) {
    totals.set(p.currencyCode, (totals.get(p.currencyCode) ?? 0) + parseAmount(p.totalAmount));
  }
  if (totals.size === 0) return formatAmount(0);
  return [...totals.entries()].map(([currency, amount]) => formatAmount(amount, currency)).join(" + ");
}

export default function PaymentList() {
  const permissions = usePaymentPermissions();
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [keyword, setKeyword] = useState("");
  const [quick, setQuick] = useState<QuickFilter>("ALL");
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [draftFilters, setDraftFilters] = useState<PaymentFilterValues>(EMPTY_FILTERS);
  const [filters, setFilters] = useState<PaymentFilterValues>(EMPTY_FILTERS);

  useEffect(() => {
    let cancelled = false;
    paymentService
      .getAll()
      .then((res) => {
        if (!cancelled) setPayments(res.data);
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load payments."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const drafts = payments.filter(QUICK_FILTERS.DRAFT);
  const prepared = payments.filter(QUICK_FILTERS.PENDING);
  const paidThisMonth = payments.filter(QUICK_FILTERS.PAID_THIS_MONTH);
  const cards: StatCard<QuickFilter>[] = [
    { id: "ALL", title: "All Payments", value: payments.length, unit: "payments", subtitle: "Every supplier payment", tone: "brand", icon: StatIcons.all },
    { id: "DRAFT", title: "Drafts", value: sumByCurrency(drafts), subtitle: `${drafts.length} to prepare`, tone: "amber", icon: StatIcons.draft, badge: drafts.length },
    {
      id: "PENDING",
      title: "Prepared",
      value: sumByCurrency(prepared),
      subtitle: `${prepared.length} to execute`,
      tone: "blue",
      icon: StatIcons.pending,
      badge: prepared.length,
    },
    {
      id: "PAID_THIS_MONTH",
      title: "Paid This Month",
      value: sumByCurrency(paidThisMonth),
      subtitle: `${paidThisMonth.length} completed`,
      tone: "green",
      icon: StatIcons.done,
    },
  ];

  const visible = useMemo(() => {
    const term = keyword.trim().toLowerCase();
    return payments
      .filter(QUICK_FILTERS[quick])
      .filter((p) => !filters.status || p.status === filters.status)
      .filter((p) => !filters.method || p.paymentMethod === filters.method)
      .filter(
        (p) =>
          !term ||
          [p.paymentCode, p.supplierName, p.bankReference, ...p.lines.map((l) => l.invoiceCode)]
            .filter(Boolean)
            .some((v) => String(v).toLowerCase().includes(term))
      )
      .sort((a, b) => String(b.createdAt ?? "").localeCompare(String(a.createdAt ?? "")));
  }, [payments, quick, filters, keyword]);

  const paging = useClientPagination(visible);
  const pills: FilterPill[] = [
    ...(filters.status
      ? [{ label: `Status: ${PAYMENT_STATUS_LABELS[filters.status]}`, onRemove: () => setFilters({ ...filters, status: "" }) }]
      : []),
    ...(filters.method
      ? [{ label: `Method: ${PAYMENT_METHOD_LABELS[filters.method]}`, onRemove: () => setFilters({ ...filters, method: "" }) }]
      : []),
  ];

  return (
    <>
      <FloatingToast feedback={error ? { type: "error", text: error } : null} onClose={() => setError(null)} />

      <StatFilterCards cards={cards} active={quick} onSelect={setQuick} />

      <ListCard
        title="Payments List"
        search={{ value: keyword, onChange: setKeyword, placeholder: "Search payments..." }}
        filter={{
          activeCount: pills.length,
          isOpen: isFilterOpen,
          onToggle: () => {
            setDraftFilters(filters);
            setIsFilterOpen(!isFilterOpen);
          },
          panel: (
            <PaymentFilters
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
        action={permissions.canCreate ? { label: "New Payment", to: "/payments/create" } : undefined}
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
                <TableCell isHeader className={HEAD_CELL}>Payment</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Supplier</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Invoices</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Method / Reference</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Amount</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Status</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Action</TableCell>
              </TableRow>
            </TableHeader>
            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {loading ? (
                <TableStateRow colSpan={7} loading message="Fetching payments..." />
              ) : paging.pageItems.length === 0 ? (
                <TableStateRow colSpan={7} message="No payments found." />
              ) : (
                paging.pageItems.map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className={BODY_CELL}>
                      <StackedCell
                        main={
                          <Link to={`/payments/${p.id}`} className="font-mono hover:text-brand-500">
                            {p.paymentCode}
                          </Link>
                        }
                        sub={p.confirmedDate ? `Paid on ${p.confirmedDate.slice(0, 10)}` : "Not executed yet"}
                      />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <div className="flex items-center gap-3">
                        <InitialsAvatar name={p.supplierName} />
                        <StackedCell main={p.supplierName} sub={p.supplierCode} />
                      </div>
                    </TableCell>
                    <TableCell className={`${BODY_CELL} font-mono`}>{p.lines.map((l) => l.invoiceCode || l.invoiceId).join(", ")}</TableCell>
                    <TableCell className={BODY_CELL}>
                      <StackedCell
                        main={p.paymentMethod ? PAYMENT_METHOD_LABELS[p.paymentMethod] : "—"}
                        sub={p.bankReference ? <span className="font-mono">{p.bankReference}</span> : undefined}
                      />
                    </TableCell>
                    <TableCell className={`${BODY_CELL} whitespace-nowrap`}>{formatAmount(p.totalAmount, p.currencyCode)}</TableCell>
                    <TableCell className={BODY_CELL}>
                      <PaymentStatusBadge status={p.status} />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <ViewAction to={`/payments/${p.id}`} title="View Payment" />
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
