import { TableRow, TableCell } from "../../../shared/components/ui/table";
import type { Requisition } from "../types";
import { formatAmount } from "../../invoices/utils/invoiceLine";

/** The requested lines, shown under the row when it is expanded in the list. */
export default function RequisitionExpandedRow({ requisition, colSpan }: { requisition: Requisition; colSpan: number }) {
  const lines = requisition.lines || [];

  return (
    <TableRow className="bg-gray-50/60 dark:bg-white/[0.02]">
      <TableCell colSpan={colSpan} className="px-5 py-4">
        <div className="rounded-xl border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-gray-900">
          <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
            <h5 className="text-sm font-semibold text-gray-800 dark:text-white/90">Requested Lines ({lines.length})</h5>
            {requisition.justification && (
              <span className="text-theme-xs text-gray-500 dark:text-gray-400">Justification: {requisition.justification}</span>
            )}
          </div>
          {lines.length === 0 ? (
            <p className="py-2 text-sm text-gray-400">No line on this requisition.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-100 text-theme-sm dark:divide-white/[0.05]">
                <thead>
                  <tr className="text-left text-theme-xs font-medium text-gray-500 dark:text-gray-400">
                    <th className="py-2 pr-3">#</th>
                    <th className="px-3 py-2">Material</th>
                    <th className="px-3 py-2 text-right">Quantity</th>
                    <th className="px-3 py-2 text-right">Unit Price</th>
                    <th className="px-3 py-2 text-right">Line Total</th>
                    <th className="py-2 pl-3">Supplier</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {lines.map((line, idx) => (
                    <tr key={line.id || idx}>
                      <td className="py-2 pr-3 text-gray-500">{line.lineNumber ?? idx + 1}</td>
                      <td className="px-3 py-2">
                        <span className="font-mono font-medium text-brand-500">{line.materialCode}</span>
                        <span className="ml-2 text-gray-600 dark:text-gray-300">{line.materialName}</span>
                      </td>
                      <td className="px-3 py-2 text-right text-gray-800 dark:text-white/90">
                        {line.quantity} {line.unitOfMeasure ?? ""}
                      </td>
                      <td className="px-3 py-2 text-right text-gray-600 dark:text-gray-300">
                        {formatAmount(line.unitPrice, line.currencyCode || requisition.currencyCode)}
                      </td>
                      <td className="px-3 py-2 text-right font-medium text-gray-800 dark:text-white/90">
                        {formatAmount(line.lineTotal, line.currencyCode || requisition.currencyCode)}
                      </td>
                      <td className="py-2 pl-3 text-gray-500 dark:text-gray-400">{line.supplierName || "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </TableCell>
    </TableRow>
  );
}
