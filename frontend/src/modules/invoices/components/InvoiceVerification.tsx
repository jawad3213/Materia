import type { Invoice } from "../types/invoice.types";
import { formatAmount } from "../utils/invoiceLine";
import Badge from "../../../shared/components/ui/badge/Badge";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";
import { DetailCard } from "../../../shared/components/page/DetailParts";
import { StackedCell } from "../../../shared/components/page/ListParts";
import { SectionIcons } from "../../../shared/components/page/pageIcons";
import { BODY_CELL, HEAD_CELL } from "../../../shared/components/page/pageStyles";

/**
 * Three-way match: for each line, what was ordered, what was received and what is billed.
 * Lines whose billed quantity exceeds what was received (or whose price differs) are highlighted.
 */
export default function InvoiceVerification({ invoice }: { invoice: Invoice }) {
  return (
    <DetailCard
      title="Order / Receipt / Invoice Match"
      icon={SectionIcons.check}
      tone={invoice.hasDiscrepancy ? "warning" : "success"}
      padded={false}
      aside={
        <Badge size="sm" color={invoice.hasDiscrepancy ? "warning" : "success"}>
          {invoice.hasDiscrepancy ? "Discrepancies" : "Matching"}
        </Badge>
      }
    >
      {invoice.discrepancySummary && (
        <p className="border-b border-gray-100 px-6 py-3 text-theme-sm text-warning-600 dark:border-gray-800 dark:text-orange-400">
          {invoice.discrepancySummary}
        </p>
      )}
      <div className="max-w-full overflow-x-auto">
        <Table>
          <TableHeader className="border-b border-gray-100 bg-gray-50/50 dark:border-white/[0.05] dark:bg-gray-900/50">
            <TableRow>
              <TableCell isHeader className={HEAD_CELL}>Material</TableCell>
              <TableCell isHeader className={HEAD_CELL}>Ordered</TableCell>
              <TableCell isHeader className={HEAD_CELL}>Received</TableCell>
              <TableCell isHeader className={HEAD_CELL}>Invoiced</TableCell>
              <TableCell isHeader className={HEAD_CELL}>Unit Price</TableCell>
              <TableCell isHeader className={HEAD_CELL}>Tax</TableCell>
              <TableCell isHeader className={HEAD_CELL}>Total incl. Tax</TableCell>
            </TableRow>
          </TableHeader>
          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {invoice.lines.map((l) => (
              <TableRow key={l.id} className={l.hasQuantityDiscrepancy || l.discrepancyNotes ? "bg-warning-50/50 dark:bg-warning-500/[0.06]" : ""}>
                <TableCell className={BODY_CELL}>
                  <StackedCell main={<span className="font-mono">{l.materialCode || "—"}</span>} sub={l.discrepancyNotes || l.materialName} />
                </TableCell>
                <TableCell className={BODY_CELL}>{l.quantityOrdered ?? "—"}</TableCell>
                <TableCell className={BODY_CELL}>{l.quantityReceived ?? "—"}</TableCell>
                <TableCell className={BODY_CELL}>
                  <StackedCell
                    main={`${l.quantityInvoiced} ${l.unitOfMeasure ?? ""}`.trim()}
                    sub={l.hasQuantityDiscrepancy && l.quantityDiscrepancy ? `+${l.quantityDiscrepancy} over` : undefined}
                  />
                </TableCell>
                <TableCell className={BODY_CELL}>{formatAmount(l.unitPrice, invoice.currencyCode)}</TableCell>
                <TableCell className={BODY_CELL}>{formatAmount(l.taxAmount, invoice.currencyCode)}</TableCell>
                <TableCell className={`${BODY_CELL} whitespace-nowrap`}>
                  {formatAmount(l.lineTotalWithTax ?? l.lineTotal, invoice.currencyCode)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </DetailCard>
  );
}
