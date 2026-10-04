import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import returnToVendorService from "../services/returnToVendorService";
import goodsReceiptService from "../../goodsReceipts/services/goodsReceiptService";
import type { GoodsReceipt } from "../../goodsReceipts/types/goodsReceipt.types";
import ReturnLine from "./ReturnLine";
import { isReturnableReceipt, lineError, toReturnDrafts, totalQuantity, type ReturnLineDraft } from "../utils/returnLine";
import Button from "../../../shared/components/ui/button/Button";
import Label from "../../../shared/components/form/Label";
import Input from "../../../shared/components/form/input/InputField";
import TextArea from "../../../shared/components/form/input/TextArea";
import { FloatingToast, FormCard, FormSection } from "../../../shared/components/page/DetailParts";
import { SELECT_CLASS } from "../../../shared/components/page/pageStyles";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

const today = () => new Date().toISOString().slice(0, 10);

/**
 * Prepares a return from a goods receipt: only its rejected lines can be sent back, for what other returns
 * do not already cover. Opened from a receipt (`?goodsReceiptId=`), that receipt is pre-selected.
 */
export default function ReturnForm() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialReceiptId = searchParams.get("goodsReceiptId") || "";

  const [receipts, setReceipts] = useState<GoodsReceipt[]>([]);
  const [loadingReceipts, setLoadingReceipts] = useState(true);
  const [receiptId, setReceiptId] = useState(initialReceiptId);
  const [receipt, setReceipt] = useState<GoodsReceipt | null>(null);
  const [lines, setLines] = useState<ReturnLineDraft[]>([]);
  const [loadingReceipt, setLoadingReceipt] = useState(!!initialReceiptId);
  const [returnReason, setReturnReason] = useState("");
  const [returnDate, setReturnDate] = useState(today());
  const [notes, setNotes] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    goodsReceiptService
      .getAll()
      .then((res) => {
        if (!cancelled) setReceipts(res.data.filter(isReturnableReceipt));
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load goods receipts."));
      })
      .finally(() => {
        if (!cancelled) setLoadingReceipts(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  // The receipt's other returns hold part of its rejected quantity.
  useEffect(() => {
    if (!receiptId) return;
    let cancelled = false;
    Promise.all([goodsReceiptService.getById(receiptId), returnToVendorService.getByGoodsReceiptId(receiptId)])
      .then(([receiptRes, returnsRes]) => {
        if (cancelled) return;
        if (!isReturnableReceipt(receiptRes.data)) {
          setError("This goods receipt has no rejected goods to return.");
          return;
        }
        setReceipt(receiptRes.data);
        setLines(toReturnDrafts(receiptRes.data, returnsRes.data));
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load the goods receipt."));
      })
      .finally(() => {
        if (!cancelled) setLoadingReceipt(false);
      });
    return () => {
      cancelled = true;
    };
  }, [receiptId]);

  const handleSelectReceipt = (id: string) => {
    setError(null);
    setReceipt(null);
    setLines([]);
    setLoadingReceipt(!!id);
    setReceiptId(id);
  };

  const updateLine = (index: number, line: ReturnLineDraft) => setLines((prev) => prev.map((l, i) => (i === index ? line : l)));

  const selected = useMemo(() => lines.filter((l) => l.selected), [lines]);
  const total = totalQuantity(lines);
  const noReceipts = !loadingReceipts && receipts.length === 0;

  const submit = async (ship: boolean) => {
    if (!receipt) return;
    setError(null);
    if (!returnReason.trim()) {
      setError("Enter the reason for the return.");
      return;
    }
    if (selected.length === 0) {
      setError("Select at least one rejected line to return.");
      return;
    }
    if (lines.some((l) => lineError(l))) {
      setError("Fix the highlighted lines before saving.");
      return;
    }
    try {
      setSubmitting(true);
      const created = await returnToVendorService.create({
        goodsReceiptId: receipt.id,
        returnDate,
        returnReason: returnReason.trim(),
        notes: notes.trim() || undefined,
        lines: selected.map((l) => ({
          goodsReceiptLineId: l.receiptLine.id,
          quantityToReturn: l.quantity,
          rejectionReason: l.reason.trim() || undefined,
        })),
      });
      if (ship) {
        try {
          await returnToVendorService.submit(created.data.id);
        } catch (err) {
          navigate(`/returns/${created.data.id}`, {
            state: { error: getApiErrorMessage(err, "The return was saved as a draft, but marking it as shipped failed.") },
          });
          return;
        }
      }
      navigate(`/returns/${created.data.id}`);
    } catch (err) {
      setError(getApiErrorMessage(err, "Saving the return failed."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <FloatingToast feedback={error ? { type: "error", text: error } : null} onClose={() => setError(null)} />

      <FormCard title="Return Goods to Supplier">
        <FormSection
          title="Goods Receipt"
          aside={
            <Link to="/returns">
              <Button variant="outline" size="sm">
                Cancel
              </Button>
            </Link>
          }
        >
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <Label>Receipt with Rejected Goods *</Label>
              <select
                value={receiptId}
                onChange={(e) => handleSelectReceipt(e.target.value)}
                className={SELECT_CLASS}
                disabled={loadingReceipts || noReceipts}
              >
                <option value="">
                  {loadingReceipts ? "Loading goods receipts..." : noReceipts ? "No receipt with rejected goods" : "Select a goods receipt..."}
                </option>
                {receipts.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.receiptCode} — {r.supplierName} ({r.totalQuantityRejected} rejected)
                  </option>
                ))}
              </select>
              <p className="mt-1.5 text-theme-xs text-gray-500 dark:text-gray-400">
                {noReceipts ? (
                  <>
                    Only completed receipts that rejected goods can be returned.{" "}
                    <Link to="/goods-receipts" className="text-brand-500 hover:underline">
                      View goods receipts
                    </Link>
                  </>
                ) : (
                  `${receipts.length} receipt(s) with rejected goods.`
                )}
              </p>
            </div>

            <div className="rounded-xl border border-gray-200 bg-gray-50/60 p-4 dark:border-gray-800 dark:bg-white/[0.02]">
              {loadingReceipt ? (
                <p className="text-sm text-gray-500">Loading goods receipt...</p>
              ) : receipt ? (
                <dl className="grid grid-cols-2 gap-x-4 gap-y-3 text-sm">
                  <div>
                    <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Supplier</dt>
                    <dd className="font-medium text-gray-800 dark:text-white/90">{receipt.supplierName || "—"}</dd>
                  </div>
                  <div>
                    <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Purchase Order</dt>
                    <dd className="font-mono font-medium text-gray-800 dark:text-white/90">{receipt.purchaseOrderCode || "—"}</dd>
                  </div>
                  <div>
                    <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Received On</dt>
                    <dd className="font-medium text-gray-800 dark:text-white/90">{receipt.receiptDate || "—"}</dd>
                  </div>
                  <div>
                    <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Rejected</dt>
                    <dd className="font-medium text-error-500">{receipt.totalQuantityRejected ?? 0} unit(s)</dd>
                  </div>
                </dl>
              ) : (
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  The supplier, the order and the prices come from the selected goods receipt.
                </p>
              )}
            </div>
          </div>
        </FormSection>

        <FormSection title="Return Details">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <Label>Return Reason *</Label>
              <Input value={returnReason} onChange={(e) => setReturnReason(e.target.value)} placeholder="e.g. Damaged during transport" />
            </div>
            <div>
              <Label>Return Date</Label>
              <Input type="date" value={returnDate} onChange={(e) => setReturnDate(e.target.value)} />
            </div>
            <div className="md:col-span-2">
              <Label>Notes</Label>
              <TextArea rows={2} value={notes} onChange={setNotes} placeholder="Pick-up arrangements, carrier, contact..." />
            </div>
          </div>
        </FormSection>

        <FormSection
          title="Rejected Lines"
          aside={receipt ? <span className="text-theme-xs text-gray-500 dark:text-gray-400">{selected.length} selected</span> : undefined}
        >
          {receipt && lines.length > 0 ? (
            <div className="space-y-4">
              {lines.map((line, index) => (
                <ReturnLine key={line.receiptLine.id} line={line} onChange={(l) => updateLine(index, l)} />
              ))}
            </div>
          ) : (
            <div className="rounded-xl border border-dashed border-gray-300 px-6 py-10 text-center text-sm text-gray-500 dark:border-gray-700 dark:text-gray-400">
              {receipt ? "Every rejected line of this receipt is already on a return." : "Select a goods receipt to load its rejected lines."}
            </div>
          )}
        </FormSection>

        <div className="flex flex-col gap-4 border-t border-gray-100 pt-6 dark:border-gray-800 md:flex-row md:items-center md:justify-between">
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Returning <span className="font-semibold text-gray-900 dark:text-white">{total}</span> unit(s). Rejected goods never entered
            stock, so stock does not change.
          </p>
          <div className="flex flex-wrap gap-3">
            <Button variant="outline" onClick={() => submit(false)} disabled={submitting || !receipt || total <= 0}>
              Save as Draft
            </Button>
            <Button onClick={() => submit(true)} disabled={submitting || !receipt || total <= 0}>
              {submitting ? "Saving..." : "Save and Mark Shipped"}
            </Button>
          </div>
        </div>
      </FormCard>
    </>
  );
}
