import { useEffect, useState } from "react";
import Button from "../../../shared/components/ui/button/Button";
import Badge from "../../../shared/components/ui/badge/Badge";
import { materialApi } from "../services/materialApi";
import type { ReorderRecommendation } from "../types/Material";

interface ReorderRecommendationModalProps {
  materialId: string | null;
  isOpen: boolean;
  onClose: () => void;
  onReorderSuccess: () => void;
}

export default function ReorderRecommendationModal({
  materialId,
  isOpen,
  onClose,
  onReorderSuccess,
}: ReorderRecommendationModalProps) {
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [recommendation, setRecommendation] = useState<ReorderRecommendation | null>(null);
  const [quantity, setQuantity] = useState<number>(0);
  const [reason, setReason] = useState<string>("");
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen && materialId) {
      setSuccessMessage(null);
      setErrorMessage(null);
      fetchRecommendation(materialId);
    } else {
      setRecommendation(null);
    }
  }, [isOpen, materialId]);

  const fetchRecommendation = async (id: string) => {
    try {
      setLoading(true);
      const res = await materialApi.getReorderRecommendation(id);
      setRecommendation(res.data);
      setQuantity(res.data.recommendedQuantity || 100);
      setReason(res.data.reason || "Automatic threshold replenishment");
    } catch (err: any) {
      console.error("Failed to load reorder recommendation:", err);
      setErrorMessage("Could not load reorder recommendation for this material.");
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmReorder = async () => {
    if (!materialId || quantity <= 0) return;

    try {
      setSubmitting(true);
      setErrorMessage(null);
      const res = await materialApi.triggerManualReorder(materialId, {
        quantity,
        reason: reason.trim() || undefined,
      });

      setSuccessMessage(res.data?.message || "Purchase requisition triggered successfully!");
      onReorderSuccess();

      // Close automatically after 2 seconds
      setTimeout(() => {
        onClose();
      }, 2000);
    } catch (err: any) {
      console.error("Failed to trigger reorder:", err);
      setErrorMessage(err.response?.data?.message || "Failed to trigger reorder.");
    } finally {
      setSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
      <div
        className="w-full max-w-lg rounded-2xl border border-gray-200 bg-white p-6 shadow-2xl dark:border-white/[0.08] dark:bg-gray-900 transition-all"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between border-b border-gray-100 pb-4 dark:border-white/[0.05]">
          <div className="flex items-center gap-2.5">
            <div className="flex size-9 items-center justify-center rounded-lg bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400">
              <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <div>
              <h3 className="text-base font-semibold text-gray-900 dark:text-white">
                1-Click Reorder Trigger
              </h3>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                Generate instant Purchase Requisition
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-700 dark:hover:bg-white/[0.05] dark:hover:text-white"
          >
            <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Content */}
        <div className="py-4">
          {loading ? (
            <div className="flex flex-col items-center justify-center py-10">
              <div className="animate-spin size-8 border-3 border-brand-500 border-t-transparent rounded-full" />
              <p className="mt-3 text-sm text-gray-500 dark:text-gray-400">
                Calculating recommended EOQ & estimated costs...
              </p>
            </div>
          ) : successMessage ? (
            <div className="rounded-xl bg-emerald-50 p-5 text-center dark:bg-emerald-500/10">
              <div className="mx-auto flex size-12 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 dark:bg-emerald-500/20 dark:text-emerald-400">
                <svg className="size-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h4 className="mt-3 font-semibold text-emerald-800 dark:text-emerald-300">
                Requisition Created
              </h4>
              <p className="mt-1 text-sm text-emerald-700 dark:text-emerald-400">
                {successMessage}
              </p>
            </div>
          ) : recommendation ? (
            <div className="space-y-4">
              {/* Material info card */}
              <div className="rounded-xl bg-gray-50 p-4 dark:bg-white/[0.02] border border-gray-100 dark:border-white/[0.05]">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="font-semibold text-gray-900 dark:text-white">
                      {recommendation.materialName}
                    </span>
                    <span className="ml-2 text-xs text-gray-500">
                      ({recommendation.materialCode})
                    </span>
                  </div>
                  {recommendation.isUrgent ? (
                    <Badge size="sm" variant="light" color="error">
                      🚨 URGENT
                    </Badge>
                  ) : (
                    <Badge size="sm" variant="light" color="warning">
                      ⚠️ Reorder Needed
                    </Badge>
                  )}
                </div>

                {/* Stock Metrics Grid */}
                <div className="mt-3 grid grid-cols-3 gap-2 border-t border-gray-200/60 pt-3 text-xs dark:border-white/[0.05]">
                  <div>
                    <span className="text-gray-400">Current Stock</span>
                    <p className="font-semibold text-gray-800 dark:text-white/90">
                      {recommendation.currentStock}
                    </p>
                  </div>
                  <div>
                    <span className="text-gray-400">Reorder Point</span>
                    <p className="font-semibold text-gray-800 dark:text-white/90">
                      {recommendation.reorderPoint}
                    </p>
                  </div>
                  <div>
                    <span className="text-gray-400">Safety Stock</span>
                    <p className="font-semibold text-gray-800 dark:text-white/90">
                      {recommendation.safetyStock}
                    </p>
                  </div>
                  <div>
                    <span className="text-gray-400">On Order</span>
                    <p className="font-semibold text-gray-800 dark:text-white/90">
                      {recommendation.stockOnOrder}
                    </p>
                  </div>
                  <div>
                    <span className="text-gray-400">Virtual Stock</span>
                    <p className="font-semibold text-gray-800 dark:text-white/90">
                      {recommendation.virtualStock}
                    </p>
                  </div>
                  <div>
                    <span className="text-gray-400">Est. Cost</span>
                    <p className="font-semibold text-brand-600 dark:text-brand-400">
                      {recommendation.estimatedCost ?? 0} {recommendation.currency}
                    </p>
                  </div>
                </div>
              </div>

              {/* Order Quantity Input */}
              <div>
                <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1">
                  Reorder Quantity (Units)
                </label>
                <div className="relative">
                  <input
                    type="number"
                    min="1"
                    value={quantity}
                    onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 0))}
                    className="w-full rounded-lg border border-gray-300 bg-white px-3.5 py-2 text-sm text-gray-900 shadow-xs focus:border-brand-500 focus:outline-hidden focus:ring-1 focus:ring-brand-500 dark:border-white/[0.1] dark:bg-gray-800 dark:text-white"
                  />
                  <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-gray-400">
                    Recommended EOQ: {recommendation.recommendedQuantity}
                  </span>
                </div>
              </div>

              {/* Notes / Reason */}
              <div>
                <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1">
                  Requisition Reason / Justification
                </label>
                <input
                  type="text"
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="Reason for purchase requisition..."
                  className="w-full rounded-lg border border-gray-300 bg-white px-3.5 py-2 text-sm text-gray-900 shadow-xs focus:border-brand-500 focus:outline-hidden focus:ring-1 focus:ring-brand-500 dark:border-white/[0.1] dark:bg-gray-800 dark:text-white"
                />
              </div>

              {errorMessage && (
                <div className="rounded-lg bg-rose-50 p-3 text-xs text-rose-600 dark:bg-rose-500/10 dark:text-rose-400">
                  {errorMessage}
                </div>
              )}
            </div>
          ) : (
            <p className="text-center text-sm text-gray-500">No recommendation available.</p>
          )}
        </div>

        {/* Footer actions */}
        {!successMessage && (
          <div className="flex items-center justify-end gap-3 border-t border-gray-100 pt-4 dark:border-white/[0.05]">
            <Button variant="outline" size="sm" onClick={onClose} disabled={submitting}>
              Cancel
            </Button>
            <Button
              variant="primary"
              size="sm"
              onClick={handleConfirmReorder}
              disabled={submitting || loading || quantity <= 0}
              className="flex items-center gap-1.5"
            >
              {submitting ? (
                <>
                  <div className="size-4 animate-spin border-2 border-white border-t-transparent rounded-full" />
                  <span>Submitting...</span>
                </>
              ) : (
                <>
                  <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                  <span>Create Requisition</span>
                </>
              )}
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
