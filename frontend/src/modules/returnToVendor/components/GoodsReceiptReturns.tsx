import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import returnToVendorService from "../services/returnToVendorService";
import type { ReturnToVendor } from "../types/returnToVendor.types";
import type { GoodsReceipt } from "../../goodsReceipts/types/goodsReceipt.types";
import ReturnStatusBadge, { ResolutionBadge } from "./ReturnStatusBadge";
import { isReturnableReceipt } from "../utils/returnLine";
import useAuth from "../../auth/hooks/useAuth";
import Button from "../../../shared/components/ui/button/Button";
import { DetailCard } from "../../../shared/components/page/DetailParts";
import { SectionIcons } from "../../../shared/components/page/pageIcons";

/** On a goods receipt with rejected goods: the returns sent back from it, and a shortcut to start one. */
export default function GoodsReceiptReturns({ receipt }: { receipt: GoodsReceipt }) {
  const { hasPermission } = useAuth();
  const canRead = hasPermission("return:read");
  const canWrite = hasPermission("return:write");
  const returnable = isReturnableReceipt(receipt);
  const [returns, setReturns] = useState<ReturnToVendor[] | null>(null);

  useEffect(() => {
    if (!canRead || !returnable) return;
    let cancelled = false;
    returnToVendorService
      .getByGoodsReceiptId(receipt.id)
      .then((res) => {
        if (!cancelled) setReturns(res.data);
      })
      .catch(() => {
        if (!cancelled) setReturns([]);
      });
    return () => {
      cancelled = true;
    };
  }, [canRead, returnable, receipt.id]);

  if (!canRead || !returnable || returns === null) return null;

  const returned = returns.filter((r) => r.status !== "CANCELLED").reduce((sum, r) => sum + (r.totalQuantity ?? 0), 0);
  const rejected = receipt.totalQuantityRejected ?? 0;

  return (
    <DetailCard
      title="Vendor Returns"
      icon={SectionIcons.box}
      tone={returned >= rejected ? "success" : "warning"}
      aside={
        canWrite && returned < rejected ? (
          <Link to={`/returns/create?goodsReceiptId=${receipt.id}`}>
            <Button size="sm">Return Rejected Goods</Button>
          </Link>
        ) : undefined
      }
    >
      <p className="mb-3 text-sm text-gray-600 dark:text-gray-400">
        {returned} of {rejected} rejected unit(s) are on a return.
      </p>
      {returns.length === 0 ? (
        <p className="text-sm text-gray-500 dark:text-gray-400">No return yet for this receipt.</p>
      ) : (
        <ul className="divide-y divide-gray-100 dark:divide-white/[0.05]">
          {returns.map((r) => (
            <li key={r.id} className="flex flex-wrap items-center justify-between gap-2 py-3 first:pt-0 last:pb-0">
              <span className="flex items-center gap-3">
                <Link to={`/returns/${r.id}`} className="font-mono text-sm font-medium text-brand-500 hover:underline">
                  {r.returnCode}
                </Link>
                <ReturnStatusBadge status={r.status} />
                {r.resolutionType && <ResolutionBadge type={r.resolutionType} />}
              </span>
              <span className="text-theme-sm text-gray-600 dark:text-gray-400">{r.totalQuantity ?? 0} unit(s)</span>
            </li>
          ))}
        </ul>
      )}
    </DetailCard>
  );
}
