import React from "react";
import { TableRow, TableCell } from "../../../shared/components/ui/table";
import type { Requisition } from "../types";

export interface RequisitionExpandedRowProps {
  requisition: Requisition;
  formatAmount: (amt: string | number, curr?: string) => string;
}

export default function RequisitionExpandedRow({
  requisition,
  formatAmount,
}: RequisitionExpandedRowProps) {
  const lines = requisition.lines || [];
  const linesCount = lines.length;

  return (
    <TableRow className="bg-gray-50/75 dark:bg-gray-800/30 border-b border-gray-100 dark:border-white/[0.05]">
      <TableCell colSpan={9} className="p-3 sm:px-6 sm:py-4">
        <div className="rounded-xl border border-gray-200/80 bg-white p-4 shadow-inner dark:border-white/[0.07] dark:bg-gray-900/90">
          <div className="flex items-center justify-between mb-3">
            <h5 className="text-xs font-bold text-gray-800 dark:text-white flex items-center gap-2">
              <svg className="size-4 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 10h16M4 14h16M4 18h16" />
              </svg>
              Requisition Line Items ({linesCount})
            </h5>
            {requisition.justification && (
              <span className="text-[11px] text-gray-500 dark:text-gray-400 italic">
                Justification: "{requisition.justification}"
              </span>
            )}
          </div>

          {linesCount === 0 ? (
            <p className="text-xs text-gray-400 italic py-2">
              No line items recorded for this requisition.
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-100 dark:divide-white/[0.05] text-xs">
                <thead>
                  <tr className="text-left text-[11px] font-semibold text-gray-500 dark:text-gray-400">
                    <th className="py-2 pr-3">Line #</th>
                    <th className="py-2 px-3">Material Code</th>
                    <th className="py-2 px-3">Description</th>
                    <th className="py-2 px-3 text-right">Qty</th>
                    <th className="py-2 px-3 text-right">Unit Price</th>
                    <th className="py-2 px-3 text-right">Line Total</th>
                    <th className="py-2 pl-3">Supplier</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {lines.map((line, idx) => (
                    <tr key={line.id || idx} className="hover:bg-gray-50/50 dark:hover:bg-white/[0.02]">
                      <td className="py-2 pr-3 font-medium text-gray-500">
                        #{line.lineNumber ?? idx + 1}
                      </td>
                      <td className="py-2 px-3 font-semibold text-brand-600 dark:text-brand-400">
                        {line.materialCode}
                      </td>
                      <td className="py-2 px-3 text-gray-800 dark:text-gray-200">
                        {line.materialName}
                      </td>
                      <td className="py-2 px-3 text-right font-medium text-gray-900 dark:text-white">
                        {line.quantity} {line.unitOfMeasure}
                      </td>
                      <td className="py-2 px-3 text-right text-gray-600 dark:text-gray-400">
                        {formatAmount(line.unitPrice, line.currencyCode || requisition.currencyCode)}
                      </td>
                      <td className="py-2 px-3 text-right font-bold text-gray-900 dark:text-white">
                        {formatAmount(line.lineTotal, line.currencyCode || requisition.currencyCode)}
                      </td>
                      <td className="py-2 pl-3 text-gray-500 dark:text-gray-400">
                        {line.supplierName || "—"}
                      </td>
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
