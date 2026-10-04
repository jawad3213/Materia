import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import goodsReceiptService from "../services/goodsReceiptService";
import { RECEIPT_STATUS_LABELS, type GoodsReceipt } from "../types/goodsReceipt.types";
import GoodsReceiptFilters, { type GoodsReceiptFilterValues } from "./GoodsReceiptFilters";
import { ReceiptStatusBadge } from "./QualityStatusBadge";
import useGoodsReceiptPermissions from "../hooks/useGoodsReceipt";
import ListCard, { type FilterPill } from "../../../shared/components/page/ListCard";
import StatFilterCards, { type StatCard } from "../../../shared/components/page/StatFilterCards";
import { StatIcons } from "../../../shared/components/page/pageIcons";
import { InitialsAvatar, ListFooter, StackedCell, TableStateRow, ViewAction } from "../../../shared/components/page/ListParts";
import { BODY_CELL, HEAD_CELL } from "../../../shared/components/page/pageStyles";
import { useClientPagination } from "../../../shared/components/page/useClientPagination";
import { FloatingToast } from "../../../shared/components/page/DetailParts";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

type QuickFilter = "ALL" | "DRAFT" | "COMPLETED" | "DISCREPANCY";

const QUICK_FILTERS: Record<QuickFilter, (r: GoodsReceipt) => boolean> = {
  ALL: () => true,
  DRAFT: (r) => r.status === "DRAFT" || r.status === "IN_PROGRESS",
  COMPLETED: (r) => r.status === "COMPLETED",
  DISCREPANCY: (r) => r.status === "PARTIAL" || (r.hasDiscrepancy && r.status !== "CANCELLED"),
};

export default function GoodsReceiptList() {
  const permissions = useGoodsReceiptPermissions();
  const [receipts, setReceipts] = useState<GoodsReceipt[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [keyword, setKeyword] = useState("");
  const [quick, setQuick] = useState<QuickFilter>("ALL");
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [draftFilters, setDraftFilters] = useState<GoodsReceiptFilterValues>({ status: "" });
  const [filters, setFilters] = useState<GoodsReceiptFilterValues>({ status: "" });

  useEffect(() => {
    let cancelled = false;
    goodsReceiptService
      .getAll()
      .then((res) => {
        if (!cancelled) setReceipts(res.data);
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load goods receipts."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const cards: StatCard<QuickFilter>[] = [
    { id: "ALL", title: "All Receipts", value: receipts.length, unit: "receipts", subtitle: "Every delivery", tone: "brand", icon: StatIcons.all },
    { id: "DRAFT", title: "Drafts", value: receipts.filter(QUICK_FILTERS.DRAFT).length, unit: "receipts", subtitle: "Not validated yet", tone: "blue", icon: StatIcons.draft },
    { id: "COMPLETED", title: "Completed", value: receipts.filter(QUICK_FILTERS.COMPLETED).length, unit: "receipts", subtitle: "Matched the order", tone: "green", icon: StatIcons.done },
    {
      id: "DISCREPANCY",
      title: "With Discrepancies",
      value: receipts.filter(QUICK_FILTERS.DISCREPANCY).length,
      unit: "receipts",
      subtitle: "Short or rejected",
      tone: "amber",
      icon: StatIcons.warning,
      badge: receipts.filter(QUICK_FILTERS.DISCREPANCY).length,
    },
  ];

  const visible = useMemo(() => {
    const term = keyword.trim().toLowerCase();
    return receipts
      .filter(QUICK_FILTERS[quick])
      .filter((r) => !filters.status || r.status === filters.status)
      .filter(
        (r) =>
          !term ||
          [r.receiptCode, r.purchaseOrderCode, r.supplierName, r.receivedByName]
            .filter(Boolean)
            .some((v) => String(v).toLowerCase().includes(term))
      )
      .sort((a, b) => String(b.createdAt ?? "").localeCompare(String(a.createdAt ?? "")));
  }, [receipts, quick, filters, keyword]);

  const paging = useClientPagination(visible);
  const pills: FilterPill[] = filters.status
    ? [{ label: `Status: ${RECEIPT_STATUS_LABELS[filters.status]}`, onRemove: () => setFilters({ status: "" }) }]
    : [];

  return (
    <>
      <FloatingToast feedback={error ? { type: "error", text: error } : null} onClose={() => setError(null)} />

      <StatFilterCards cards={cards} active={quick} onSelect={setQuick} />

      <ListCard
        title="Goods Receipts List"
        search={{ value: keyword, onChange: setKeyword, placeholder: "Search receipts..." }}
        filter={{
          activeCount: pills.length,
          isOpen: isFilterOpen,
          onToggle: () => {
            setDraftFilters(filters);
            setIsFilterOpen(!isFilterOpen);
          },
          panel: (
            <GoodsReceiptFilters
              isOpen={isFilterOpen}
              onClose={() => setIsFilterOpen(false)}
              value={draftFilters}
              onChange={setDraftFilters}
              onApply={() => {
                setFilters(draftFilters);
                setIsFilterOpen(false);
              }}
              onClear={() => {
                setDraftFilters({ status: "" });
                setFilters({ status: "" });
                setIsFilterOpen(false);
              }}
            />
          ),
        }}
        action={permissions.canRecord ? { label: "New Receipt", to: "/goods-receipts/create" } : undefined}
        pills={pills}
        onClearPills={() => setFilters({ status: "" })}
        footer={
          <ListFooter
            page={paging.page}
            size={paging.size}
            total={paging.total}
            totalPages={paging.totalPages}
            onPageChange={paging.setPage}
          />
        }
      >
        <div className="max-w-full overflow-x-auto">
          <Table>
            <TableHeader className="border-b border-gray-100 bg-gray-50/50 dark:border-white/[0.05] dark:bg-gray-900/50">
              <TableRow>
                <TableCell isHeader className={HEAD_CELL}>Receipt</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Supplier</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Purchase Order</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Received By</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Received / Rejected</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Status</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Action</TableCell>
              </TableRow>
            </TableHeader>
            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {loading ? (
                <TableStateRow colSpan={7} loading message="Fetching goods receipts..." />
              ) : paging.pageItems.length === 0 ? (
                <TableStateRow colSpan={7} message="No goods receipts found." />
              ) : (
                paging.pageItems.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell className={BODY_CELL}>
                      <StackedCell
                        main={
                          <Link to={`/goods-receipts/${r.id}`} className="font-mono hover:text-brand-500">
                            {r.receiptCode}
                          </Link>
                        }
                        sub={r.receiptDate || "Not validated"}
                      />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <div className="flex items-center gap-3">
                        <InitialsAvatar name={r.supplierName} />
                        <StackedCell main={r.supplierName || "—"} />
                      </div>
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <Link to={`/purchase-orders/${r.purchaseOrderId}`} className="font-mono hover:text-brand-500">
                        {r.purchaseOrderCode || "—"}
                      </Link>
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <StackedCell main={r.receivedByName || r.receivedBy} />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <StackedCell
                        main={`${r.totalQuantityReceived ?? 0} received`}
                        sub={(r.totalQuantityRejected ?? 0) > 0 ? `${r.totalQuantityRejected} rejected` : "None rejected"}
                      />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <ReceiptStatusBadge status={r.status} />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <ViewAction to={`/goods-receipts/${r.id}`} title="View Receipt" />
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
