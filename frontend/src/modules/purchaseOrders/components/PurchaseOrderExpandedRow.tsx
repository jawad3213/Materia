import { TableRow, TableCell } from "../../../shared/components/ui/table";
import type { PurchaseOrder } from "../types/PurchaseOrder";
import { formatAmount } from "../../invoices/utils/invoiceLine";

/** The order lines, shown under the row when it is expanded in the list. */
export default function PurchaseOrderExpandedRow({ order, colSpan }: { order: PurchaseOrder; colSpan: number }) {
  const lines = order.lines || [];

  return (
    <TableRow className="bg-gray-50/60 dark:bg-white/[0.02]">
      <TableCell colSpan={colSpan} className="px-5 py-4">
        <div className="rounded-xl border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-gray-900">
          <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
            <h5 className="text-sm font-semibold text-gray-800 dark:text-white/90">Order Lines ({lines.length})</h5>
            {order.requisitionCode && (
              <span className="text-theme-xs text-gray-500 dark:text-gray-400">
                From requisition <span className="font-mono font-medium text-brand-500">{order.requisitionCode}</span>
              </span>
            )}
          </div>

          {lines.length === 0 ? (
            <p className="py-2 text-sm text-gray-400">No line on this order.</p>
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
                    <th className="py-2 pl-3">Expected</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {lines.map((line, idx) => (
                    <tr key={line.id || idx}>
                      <td className="py-2 pr-3 text-gray-500">{line.lineNumber ?? idx + 1}</td>
                      <td className="px-3 py-2">
                        <span className="font-mono font-medium text-brand-500">{line.materialCode}</span>
                        <span className="ml-2 text-gray-600 dark:text-gray-300">{line.materialName || line.materialDescription || ""}</span>
                      </td>
                      <td className="px-3 py-2 text-right text-gray-800 dark:text-white/90">
                        {line.quantity} {line.unitOfMeasure || ""}
                      </td>
                      <td className="px-3 py-2 text-right text-gray-600 dark:text-gray-300">
                        {formatAmount(line.unitPrice, line.currencyCode || order.currencyCode)}
                      </td>
                      <td className="px-3 py-2 text-right font-medium text-gray-800 dark:text-white/90">
                        {formatAmount(line.lineTotal, line.currencyCode || order.currencyCode)}
                      </td>
                      <td className="py-2 pl-3 text-gray-500 dark:text-gray-400">
                        {line.expectedDeliveryDate || order.expectedDeliveryDate || "—"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {(order.paymentTerms || order.deliveryTerms || order.notes) && (
            <div className="mt-3 grid grid-cols-1 gap-3 border-t border-gray-100 pt-3 text-theme-xs sm:grid-cols-2 lg:grid-cols-4 dark:border-white/[0.05]">
              {order.paymentTerms && (
                <div>
                  <span className="block font-medium text-gray-400">Payment Terms</span>
                  <span className="text-gray-700 dark:text-gray-300">
                    {order.paymentTerms} {order.paymentDelayDays ? `(${order.paymentDelayDays} days)` : ""}
                  </span>
                </div>
              )}
              {order.deliveryTerms && (
                <div>
                  <span className="block font-medium text-gray-400">Delivery Terms</span>
                  <span className="text-gray-700 dark:text-gray-300">
                    {order.deliveryTerms} {order.incoterm ? `(${order.incoterm})` : ""}
                  </span>
                </div>
              )}
              {order.notes && (
                <div className="sm:col-span-2">
                  <span className="block font-medium text-gray-400">Notes</span>
                  <span className="text-gray-700 dark:text-gray-300">{order.notes}</span>
                </div>
              )}
            </div>
          )}
        </div>
      </TableCell>
    </TableRow>
  );
}
