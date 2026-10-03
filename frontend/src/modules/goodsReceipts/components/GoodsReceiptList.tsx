import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import goodsReceiptService from "../services/goodsReceiptService";
import type { GoodsReceipt } from "../types/goodsReceipt.types";
import GoodsReceiptFilters, { type GoodsReceiptFilterValues } from "./GoodsReceiptFilters";
import { ReceiptStatusBadge } from "./QualityStatusBadge";
import useGoodsReceiptPermissions from "../hooks/useGoodsReceipt";
import Button from "../../../shared/components/ui/button/Button";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

const HEAD = "px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400";
const CELL = "px-4 py-3 text-xs text-gray-700 dark:text-gray-300";

export default function GoodsReceiptList() {
  const permissions = useGoodsReceiptPermissions();
  const [receipts, setReceipts] = useState<GoodsReceipt[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<GoodsReceiptFilterValues>({ keyword: "", status: "" });

  useEffect(() => {
    let cancelled = false;
    goodsReceiptService
      .getAll()
      .then((res) => {
        if (!cancelled) setReceipts(res.data);
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Impossible de charger les réceptions."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const visible = useMemo(() => {
    const keyword = filters.keyword.trim().toLowerCase();
    return receipts
      .filter((r) => !filters.status || r.status === filters.status)
      .filter(
        (r) =>
          !keyword ||
          [r.receiptCode, r.purchaseOrderCode, r.supplierName, r.receivedByName]
            .filter(Boolean)
            .some((v) => String(v).toLowerCase().includes(keyword))
      )
      .sort((a, b) => String(b.createdAt ?? "").localeCompare(String(a.createdAt ?? "")));
  }, [receipts, filters]);

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-white dark:bg-gray-900 p-5 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm">
        <div>
          <h2 className="text-base font-bold text-gray-900 dark:text-white">Réceptions de marchandises</h2>
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
            Contrôle des quantités et de la qualité à l'arrivée en entrepôt.
          </p>
        </div>
        {permissions.canRecord && (
          <Link to="/goods-receipts/create">
            <Button size="sm">Nouvelle réception</Button>
          </Link>
        )}
      </div>

      <GoodsReceiptFilters value={filters} onChange={setFilters} />

      {error && (
        <div className="p-4 rounded-xl bg-red-50 text-red-700 dark:bg-red-500/10 dark:text-red-400 text-xs border border-red-200 dark:border-red-500/20">
          {error}
        </div>
      )}

      <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-white/[0.07] overflow-x-auto shadow-sm">
        <Table>
          <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
            <TableRow>
              <TableCell isHeader className={HEAD}>Réception</TableCell>
              <TableCell isHeader className={HEAD}>Commande</TableCell>
              <TableCell isHeader className={HEAD}>Fournisseur</TableCell>
              <TableCell isHeader className={HEAD}>Réceptionnaire</TableCell>
              <TableCell isHeader className={HEAD}>Date</TableCell>
              <TableCell isHeader className={HEAD}>Reçu / Rejeté</TableCell>
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
                  Aucune réception trouvée.
                </TableCell>
              </TableRow>
            )}
            {visible.map((r) => (
              <TableRow key={r.id} className="hover:bg-gray-50/60 dark:hover:bg-white/[0.02]">
                <TableCell className={CELL}>
                  <Link
                    to={`/goods-receipts/${r.id}`}
                    className="font-mono font-semibold text-brand-600 dark:text-brand-400 hover:underline"
                  >
                    {r.receiptCode}
                  </Link>
                </TableCell>
                <TableCell className={`${CELL} font-mono`}>{r.purchaseOrderCode || "-"}</TableCell>
                <TableCell className={CELL}>{r.supplierName || "-"}</TableCell>
                <TableCell className={CELL}>{r.receivedByName || "-"}</TableCell>
                <TableCell className={CELL}>{r.receiptDate || "-"}</TableCell>
                <TableCell className={CELL}>
                  {r.totalQuantityReceived ?? 0} / {r.totalQuantityRejected ?? 0}
                </TableCell>
                <TableCell className={CELL}>
                  <ReceiptStatusBadge status={r.status} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
