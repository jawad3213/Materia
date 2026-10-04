import type { Invoice } from "../types/invoice.types";
import { formatAmount } from "../utils/invoiceLine";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";

const HEAD = "px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400";
const CELL = "px-4 py-3 text-xs text-gray-700 dark:text-gray-300";

/**
 * Three-way match: for each line, what was ordered, what was received and what is billed.
 * Lines whose billed quantity differs from the received quantity are highlighted.
 */
export default function InvoiceVerification({ invoice }: { invoice: Invoice }) {
  return (
    <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-2 px-5 pt-5">
        <h3 className="text-sm font-bold text-gray-900 dark:text-white">Rapprochement commande / réception / facture</h3>
        {invoice.hasDiscrepancy ? (
          <span className="text-[11px] font-semibold text-amber-700 dark:text-amber-400">Écarts détectés</span>
        ) : (
          <span className="text-[11px] font-semibold text-emerald-700 dark:text-emerald-400">Concordant</span>
        )}
      </div>
      {invoice.discrepancySummary && (
        <p className="px-5 pt-2 text-xs text-amber-700 dark:text-amber-400">{invoice.discrepancySummary}</p>
      )}
      <div className="overflow-x-auto mt-3">
        <Table>
          <TableHeader className="border-y border-gray-100 dark:border-white/[0.05]">
            <TableRow>
              <TableCell isHeader className={HEAD}>#</TableCell>
              <TableCell isHeader className={HEAD}>Article</TableCell>
              <TableCell isHeader className={HEAD}>Commandé</TableCell>
              <TableCell isHeader className={HEAD}>Reçu</TableCell>
              <TableCell isHeader className={HEAD}>Facturé</TableCell>
              <TableCell isHeader className={HEAD}>Prix unitaire</TableCell>
              <TableCell isHeader className={HEAD}>Taxe</TableCell>
              <TableCell isHeader className={HEAD}>Total TTC</TableCell>
            </TableRow>
          </TableHeader>
          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {invoice.lines.map((l) => (
              <TableRow key={l.id} className={l.hasQuantityDiscrepancy ? "bg-amber-50/60 dark:bg-amber-500/[0.06]" : ""}>
                <TableCell className={CELL}>{l.lineNumber}</TableCell>
                <TableCell className={CELL}>
                  <span className="font-mono font-semibold">{l.materialCode || "-"}</span>
                  <span className="block text-gray-500">{l.materialName}</span>
                  {l.discrepancyNotes && (
                    <span className="block text-amber-700 dark:text-amber-400">{l.discrepancyNotes}</span>
                  )}
                </TableCell>
                <TableCell className={CELL}>{l.quantityOrdered ?? "-"}</TableCell>
                <TableCell className={CELL}>{l.quantityReceived ?? "-"}</TableCell>
                <TableCell className={`${CELL} font-semibold`}>
                  {l.quantityInvoiced} {l.unitOfMeasure}
                  {l.hasQuantityDiscrepancy && l.quantityDiscrepancy != null && (
                    <span className="block text-[11px] font-normal text-amber-700 dark:text-amber-400">
                      Écart : {l.quantityDiscrepancy > 0 ? "+" : ""}
                      {l.quantityDiscrepancy}
                    </span>
                  )}
                </TableCell>
                <TableCell className={CELL}>{formatAmount(l.unitPrice, invoice.currencyCode)}</TableCell>
                <TableCell className={CELL}>{formatAmount(l.taxAmount, invoice.currencyCode)}</TableCell>
                <TableCell className={`${CELL} whitespace-nowrap`}>
                  {formatAmount(l.lineTotalWithTax ?? l.lineTotal, invoice.currencyCode)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
