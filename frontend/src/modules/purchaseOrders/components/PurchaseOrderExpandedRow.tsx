import React from "react";
import { TableRow, TableCell } from "../../../shared/components/ui/table";
import type { PurchaseOrder } from "../types/PurchaseOrder";

export interface PurchaseOrderExpandedRowProps {
  order: PurchaseOrder;
  formatAmount: (amt: string | number, curr?: string) => string;
}

export default function PurchaseOrderExpandedRow({
  order,
  formatAmount,
}: PurchaseOrderExpandedRowProps) {
  const lines = order.lines || [];
  const linesCount = lines.length;

  return (
    <TableRow className="bg-gray-50/75 dark:bg-gray-800/30 border-b border-gray-100 dark:border-white/[0.05]">
      <TableCell colSpan={9} className="p-3 sm:px-6 sm:py-4">
        <div className="rounded-xl border border-gray-200/80 bg-white p-4 shadow-inner dark:border-white/[0.07] dark:bg-gray-900/90">
          <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
            <h5 className="text-xs font-bold text-gray-800 dark:text-white flex items-center gap-2">
              <svg
                className="size-4 text-brand-500"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 6h16M4 10h16M4 14h16M4 18h16"
                />
              </svg>
              Lignes de la commande ({linesCount})
            </h5>
            {order.requisitionCode && (
              <span className="text-[11px] text-gray-500 dark:text-gray-400">
                Liée à la Demande d'Achat :{" "}
                <strong className="text-brand-600 dark:text-brand-400">
                  {order.requisitionCode}
                </strong>
              </span>
            )}
          </div>

          {linesCount === 0 ? (
            <p className="text-xs text-gray-400 italic py-2">
              Aucun article enregistré pour cette commande.
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-100 dark:divide-white/[0.05] text-xs">
                <thead>
                  <tr className="text-left text-[11px] font-semibold text-gray-500 dark:text-gray-400">
                    <th className="py-2 pr-3">Ligne #</th>
                    <th className="py-2 px-3">Code Matériel</th>
                    <th className="py-2 px-3">Désignation</th>
                    <th className="py-2 px-3 text-right">Quantité</th>
                    <th className="py-2 px-3 text-right">Prix Unitaire</th>
                    <th className="py-2 px-3 text-right">Total Ligne</th>
                    <th className="py-2 pl-3">Date Prévue</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {lines.map((line, idx) => (
                    <tr
                      key={line.id || idx}
                      className="hover:bg-gray-50/50 dark:hover:bg-white/[0.02]"
                    >
                      <td className="py-2 pr-3 font-medium text-gray-500">
                        #{line.lineNumber ?? idx + 1}
                      </td>
                      <td className="py-2 px-3 font-semibold text-brand-600 dark:text-brand-400">
                        {line.materialCode}
                      </td>
                      <td className="py-2 px-3 text-gray-700 dark:text-gray-300">
                        {line.materialName || line.materialDescription || "-"}
                      </td>
                      <td className="py-2 px-3 text-right font-medium text-gray-800 dark:text-white">
                        {line.quantity} {line.unitOfMeasure || "U"}
                      </td>
                      <td className="py-2 px-3 text-right text-gray-600 dark:text-gray-300">
                        {formatAmount(line.unitPrice, line.currencyCode || order.currencyCode)}
                      </td>
                      <td className="py-2 px-3 text-right font-semibold text-gray-900 dark:text-white">
                        {formatAmount(line.lineTotal, line.currencyCode || order.currencyCode)}
                      </td>
                      <td className="py-2 pl-3 text-gray-500 dark:text-gray-400">
                        {line.expectedDeliveryDate || order.expectedDeliveryDate || "-"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Additional details footer */}
          {(order.notes || order.internalNotes || order.paymentTerms || order.deliveryTerms) && (
            <div className="mt-3 pt-3 border-t border-gray-100 dark:border-white/[0.05] grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 text-[11px]">
              {order.paymentTerms && (
                <div>
                  <span className="text-gray-400 block font-medium">Conditions de paiement</span>
                  <span className="text-gray-700 dark:text-gray-300">
                    {order.paymentTerms} {order.paymentDelayDays ? `(${order.paymentDelayDays}j)` : ""}
                  </span>
                </div>
              )}
              {order.deliveryTerms && (
                <div>
                  <span className="text-gray-400 block font-medium">Conditions de livraison</span>
                  <span className="text-gray-700 dark:text-gray-300">
                    {order.deliveryTerms} {order.incoterm ? `(${order.incoterm})` : ""}
                  </span>
                </div>
              )}
              {order.notes && (
                <div className="sm:col-span-2">
                  <span className="text-gray-400 block font-medium">Remarques</span>
                  <span className="text-gray-700 dark:text-gray-300 italic">"{order.notes}"</span>
                </div>
              )}
            </div>
          )}
        </div>
      </TableCell>
    </TableRow>
  );
}
