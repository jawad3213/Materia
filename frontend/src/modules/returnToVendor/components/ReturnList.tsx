import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import returnToVendorService from "../services/returnToVendorService";
import { RESOLUTION_TYPE_LABELS, RETURN_STATUS_LABELS, type ReturnToVendor } from "../types/returnToVendor.types";
import ReturnFilters, { type ReturnFilterValues } from "./ReturnFilters";
import ReturnStatusBadge, { ResolutionBadge } from "./ReturnStatusBadge";
import useReturnToVendorPermissions from "../hooks/useReturnToVendor";
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

type QuickFilter = "ALL" | "DRAFT" | "PENDING" | "RESOLVED";

const QUICK_FILTERS: Record<QuickFilter, (r: ReturnToVendor) => boolean> = {
  ALL: () => true,
  DRAFT: (r) => r.status === "DRAFT",
  PENDING: (r) => r.status === "PENDING",
  RESOLVED: (r) => r.status === "RESOLVED",
};

const EMPTY_FILTERS: ReturnFilterValues = { status: "", resolution: "" };

const units = (returns: ReturnToVendor[]) => returns.reduce((sum, r) => sum + (r.totalQuantity ?? 0), 0);

export default function ReturnList() {
  const permissions = useReturnToVendorPermissions();
  const [returns, setReturns] = useState<ReturnToVendor[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [keyword, setKeyword] = useState("");
  const [quick, setQuick] = useState<QuickFilter>("ALL");
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [draftFilters, setDraftFilters] = useState<ReturnFilterValues>(EMPTY_FILTERS);
  const [filters, setFilters] = useState<ReturnFilterValues>(EMPTY_FILTERS);

  useEffect(() => {
    let cancelled = false;
    returnToVendorService
      .getAll()
      .then((res) => {
        if (!cancelled) setReturns(res.data);
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load returns."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const drafts = returns.filter(QUICK_FILTERS.DRAFT);
  const shipped = returns.filter(QUICK_FILTERS.PENDING);
  const resolved = returns.filter(QUICK_FILTERS.RESOLVED);
  const cards: StatCard<QuickFilter>[] = [
    { id: "ALL", title: "All Returns", value: returns.length, unit: "returns", subtitle: "Every return to a supplier", tone: "brand", icon: StatIcons.all },
    { id: "DRAFT", title: "Drafts", value: drafts.length, unit: "returns", subtitle: `${units(drafts)} unit(s) to ship`, tone: "amber", icon: StatIcons.draft, badge: drafts.length },
    {
      id: "PENDING",
      title: "With Supplier",
      value: shipped.length,
      unit: "returns",
      subtitle: `${units(shipped)} unit(s) awaiting resolution`,
      tone: "orange",
      icon: StatIcons.pending,
      badge: shipped.length,
    },
    { id: "RESOLVED", title: "Resolved", value: resolved.length, unit: "returns", subtitle: "Replaced or credited", tone: "green", icon: StatIcons.done },
  ];

  const visible = useMemo(() => {
    const term = keyword.trim().toLowerCase();
    return returns
      .filter(QUICK_FILTERS[quick])
      .filter((r) => !filters.status || r.status === filters.status)
      .filter((r) => !filters.resolution || r.resolutionType === filters.resolution)
      .filter(
        (r) =>
          !term ||
          [r.returnCode, r.supplierName, r.goodsReceiptCode, r.purchaseOrderCode, ...r.lines.map((l) => l.materialCode)]
            .filter(Boolean)
            .some((v) => String(v).toLowerCase().includes(term))
      )
      .sort((a, b) => String(b.createdAt ?? "").localeCompare(String(a.createdAt ?? "")));
  }, [returns, quick, filters, keyword]);

  const paging = useClientPagination(visible);
  const pills: FilterPill[] = [
    ...(filters.status
      ? [{ label: `Status: ${RETURN_STATUS_LABELS[filters.status]}`, onRemove: () => setFilters({ ...filters, status: "" }) }]
      : []),
    ...(filters.resolution
      ? [{ label: `Resolution: ${RESOLUTION_TYPE_LABELS[filters.resolution]}`, onRemove: () => setFilters({ ...filters, resolution: "" }) }]
      : []),
  ];

  return (
    <>
      <FloatingToast feedback={error ? { type: "error", text: error } : null} onClose={() => setError(null)} />

      <StatFilterCards cards={cards} active={quick} onSelect={setQuick} />

      <ListCard
        title="Returns List"
        search={{ value: keyword, onChange: setKeyword, placeholder: "Search returns..." }}
        filter={{
          activeCount: pills.length,
          isOpen: isFilterOpen,
          onToggle: () => {
            setDraftFilters(filters);
            setIsFilterOpen(!isFilterOpen);
          },
          panel: (
            <ReturnFilters
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
        action={permissions.canCreate ? { label: "New Return", to: "/returns/create" } : undefined}
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
                <TableCell isHeader className={HEAD_CELL}>Return</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Supplier</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Receipt / Order</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Quantity / Value</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Resolution</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Status</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Action</TableCell>
              </TableRow>
            </TableHeader>
            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {loading ? (
                <TableStateRow colSpan={7} loading message="Fetching returns..." />
              ) : paging.pageItems.length === 0 ? (
                <TableStateRow colSpan={7} message="No returns found." />
              ) : (
                paging.pageItems.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell className={BODY_CELL}>
                      <StackedCell
                        main={
                          <Link to={`/returns/${r.id}`} className="font-mono hover:text-brand-500">
                            {r.returnCode}
                          </Link>
                        }
                        sub={r.returnDate ?? undefined}
                      />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <div className="flex items-center gap-3">
                        <InitialsAvatar name={r.supplierName} />
                        <StackedCell main={r.supplierName} sub={r.supplierCode} />
                      </div>
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <StackedCell
                        main={
                          <Link to={`/goods-receipts/${r.goodsReceiptId}`} className="font-mono hover:text-brand-500">
                            {r.goodsReceiptCode || "—"}
                          </Link>
                        }
                        sub={r.purchaseOrderCode ?? undefined}
                      />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <StackedCell
                        main={`${r.totalQuantity ?? 0} unit(s)`}
                        sub={<span className="whitespace-nowrap">{formatAmount(r.totalValue ?? 0, r.currencyCode)}</span>}
                      />
                    </TableCell>
                    <TableCell className={BODY_CELL}>{r.resolutionType ? <ResolutionBadge type={r.resolutionType} /> : "—"}</TableCell>
                    <TableCell className={BODY_CELL}>
                      <ReturnStatusBadge status={r.status} />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <ViewAction to={`/returns/${r.id}`} title="View Return" />
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
