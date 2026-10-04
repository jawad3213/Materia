import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import invoiceService from "../services/invoiceService";
import type { Invoice } from "../types/invoice.types";
import InvoiceFilters, { type InvoiceFilterValues } from "./InvoiceFilters";
import InvoiceStatusBadge, { InvoiceTypeBadge } from "./InvoiceStatusBadge";
import useInvoicePermissions from "../hooks/useInvoice";
import { formatAmount, isOverdue, isPartiallyPaid, outstandingAmount } from "../utils/invoiceLine";
import Button from "../../../shared/components/ui/button/Button";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";
import { getApiErrorMessage } from "../../../shared/utils/apiError";
import { parseAmount } from "../../../shared/utils/moneyUtils";

const HEAD = "px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400";
const CELL = "px-4 py-3 text-xs text-gray-700 dark:text-gray-300";

/**
 * Sums amounts per currency, so totals in different currencies are never added together. By default
 * it sums what is still owed, so partly paid invoices count only for their remaining balance.
 */
function sumByCurrency(invoices: Invoice[], amount: (inv: Invoice) => number = outstandingAmount): string {
  const totals = new Map<string, number>();
  for (const inv of invoices) {
    const sign = inv.invoiceType === "CREDIT_NOTE" ? -1 : 1;
    totals.set(inv.currencyCode, (totals.get(inv.currencyCode) ?? 0) + sign * amount(inv));
  }
  if (totals.size === 0) return formatAmount(0);
  return [...totals.entries()].map(([currency, amount]) => formatAmount(amount, currency)).join(" + ");
}

export default function InvoiceList() {
  const permissions = useInvoicePermissions();
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<InvoiceFilterValues>({ keyword: "", status: "", overdueOnly: false });

  useEffect(() => {
    let cancelled = false;
    invoiceService
      .getAll()
      .then((res) => {
        if (!cancelled) setInvoices(res.data);
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Impossible de charger les factures."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const summary = useMemo(
    () => [
      {
        label: "À vérifier",
        value: sumByCurrency(invoices.filter((i) => i.status === "SUBMITTED"), (i) => parseAmount(i.totalAmountWithTax ?? 0)),
      },
      { label: "À payer", value: sumByCurrency(invoices.filter((i) => i.status === "VERIFIED")) },
      { label: "En retard", value: sumByCurrency(invoices.filter((i) => isOverdue(i))), alert: true },
      { label: "Avec écarts", value: String(invoices.filter((i) => i.hasDiscrepancy && i.status !== "CANCELLED").length) },
    ],
    [invoices]
  );

  const visible = useMemo(() => {
    const keyword = filters.keyword.trim().toLowerCase();
    return invoices
      .filter((i) => !filters.status || i.status === filters.status)
      .filter((i) => !filters.overdueOnly || isOverdue(i))
      .filter(
        (i) =>
          !keyword ||
          [i.invoiceCode, i.externalReference, i.purchaseOrderCode, i.supplierName]
            .filter(Boolean)
            .some((v) => String(v).toLowerCase().includes(keyword))
      )
      .sort((a, b) => String(b.invoiceDate ?? "").localeCompare(String(a.invoiceDate ?? "")));
  }, [invoices, filters]);

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-white dark:bg-gray-900 p-5 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm">
        <div>
          <h2 className="text-base font-bold text-gray-900 dark:text-white">Factures fournisseurs</h2>
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
            Rapprochement commande, réception et facture avant paiement.
          </p>
        </div>
        {permissions.canRecord && (
          <Link to="/invoices/create">
            <Button size="sm">Nouvelle facture</Button>
          </Link>
        )}
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {summary.map((card) => (
          <div
            key={card.label}
            className="bg-white dark:bg-gray-900 p-4 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm"
          >
            <p className="text-[11px] uppercase tracking-wider text-gray-500">{card.label}</p>
            <p className={`text-sm font-bold ${card.alert ? "text-rose-600 dark:text-rose-400" : "text-gray-900 dark:text-white"}`}>
              {loading ? "…" : card.value}
            </p>
          </div>
        ))}
      </div>

      <InvoiceFilters value={filters} onChange={setFilters} />

      {error && (
        <div className="p-4 rounded-xl bg-red-50 text-red-700 dark:bg-red-500/10 dark:text-red-400 text-xs border border-red-200 dark:border-red-500/20">
          {error}
        </div>
      )}

      <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-white/[0.07] overflow-x-auto shadow-sm">
        <Table>
          <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
            <TableRow>
              <TableCell isHeader className={HEAD}>Facture</TableCell>
              <TableCell isHeader className={HEAD}>Réf. fournisseur</TableCell>
              <TableCell isHeader className={HEAD}>Commande</TableCell>
              <TableCell isHeader className={HEAD}>Fournisseur</TableCell>
              <TableCell isHeader className={HEAD}>Date / Échéance</TableCell>
              <TableCell isHeader className={HEAD}>Montant TTC</TableCell>
              <TableCell isHeader className={HEAD}>Statut</TableCell>
            </TableRow>
          </TableHeader>
          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {loading && (
              <TableRow>
                <TableCell className={CELL} colSpan={7}>Chargement...</TableCell>
              </TableRow>
            )}
            {!loading && visible.length === 0 && (
              <TableRow>
                <TableCell className={`${CELL} text-center text-gray-400`} colSpan={7}>
                  Aucune facture trouvée.
                </TableCell>
              </TableRow>
            )}
            {visible.map((inv) => (
              <TableRow key={inv.id} className="hover:bg-gray-50/60 dark:hover:bg-white/[0.02]">
                <TableCell className={CELL}>
                  <Link
                    to={`/invoices/${inv.id}`}
                    className="font-mono font-semibold text-brand-600 dark:text-brand-400 hover:underline"
                  >
                    {inv.invoiceCode}
                  </Link>
                  {inv.invoiceType === "CREDIT_NOTE" && (
                    <span className="ml-2">
                      <InvoiceTypeBadge type={inv.invoiceType} />
                    </span>
                  )}
                </TableCell>
                <TableCell className={`${CELL} font-mono`}>{inv.externalReference || "-"}</TableCell>
                <TableCell className={`${CELL} font-mono`}>{inv.purchaseOrderCode || "-"}</TableCell>
                <TableCell className={CELL}>{inv.supplierName}</TableCell>
                <TableCell className={CELL}>
                  {inv.invoiceDate}
                  <span className={`block ${isOverdue(inv) ? "text-rose-600 dark:text-rose-400 font-semibold" : "text-gray-500"}`}>
                    {inv.dueDate ? `Échéance ${inv.dueDate}` : "Sans échéance"}
                  </span>
                </TableCell>
                <TableCell className={`${CELL} font-semibold whitespace-nowrap`}>
                  {formatAmount(inv.totalAmountWithTax, inv.currencyCode)}
                  {inv.hasDiscrepancy && (
                    <span className="block text-[11px] font-normal text-amber-600 dark:text-amber-400">Écarts détectés</span>
                  )}
                </TableCell>
                <TableCell className={CELL}>
                  <InvoiceStatusBadge status={inv.status} />
                  {isPartiallyPaid(inv) && (
                    <span className="block mt-1 text-[11px] text-indigo-600 dark:text-indigo-400">
                      Reste {formatAmount(outstandingAmount(inv), inv.currencyCode)}
                    </span>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
