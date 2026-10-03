import QualityStatusBadge from "./QualityStatusBadge";
import { deriveQualityStatus, lineError, type ReceiptLineDraft } from "../utils/receiptLine";

interface GoodsReceiptLineProps {
  line: ReceiptLineDraft;
  onChange: (line: ReceiptLineDraft) => void;
}

const INPUT =
  "w-full rounded-lg border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-2 py-1.5 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none";

export default function GoodsReceiptLine({ line, onChange }: GoodsReceiptLineProps) {
  const error = lineError(line);
  const toQuantity = (value: string) => Math.max(0, Math.floor(Number(value) || 0));

  return (
    <div className="p-4 rounded-xl border border-gray-100 dark:border-white/[0.06] space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div>
          <p className="text-xs font-semibold text-gray-900 dark:text-white">
            <span className="font-mono text-brand-600 dark:text-brand-400">{line.materialCode}</span>
            {line.materialName && ` — ${line.materialName}`}
          </p>
          <p className="text-[11px] text-gray-500 dark:text-gray-400">
            Commandé : {line.ordered} {line.unitOfMeasure} • Restant : {line.remaining} {line.unitOfMeasure}
          </p>
        </div>
        {line.received > 0 && <QualityStatusBadge status={deriveQualityStatus(line)} />}
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <label className="text-[11px] font-semibold text-gray-600 dark:text-gray-300 space-y-1">
          <span>Quantité reçue</span>
          <input
            type="number"
            min={0}
            max={line.remaining}
            value={line.received}
            onChange={(e) => onChange({ ...line, received: toQuantity(e.target.value) })}
            className={INPUT}
          />
        </label>
        <label className="text-[11px] font-semibold text-gray-600 dark:text-gray-300 space-y-1">
          <span>Quantité rejetée</span>
          <input
            type="number"
            min={0}
            max={line.received}
            value={line.rejected}
            onChange={(e) => onChange({ ...line, rejected: toQuantity(e.target.value) })}
            className={INPUT}
          />
        </label>
        <label className="text-[11px] font-semibold text-gray-600 dark:text-gray-300 space-y-1">
          <span>N° de lot</span>
          <input
            type="text"
            maxLength={100}
            value={line.batchNumber}
            onChange={(e) => onChange({ ...line, batchNumber: e.target.value })}
            className={INPUT}
          />
        </label>
        <label className="text-[11px] font-semibold text-gray-600 dark:text-gray-300 space-y-1">
          <span>Emplacement</span>
          <input
            type="text"
            maxLength={100}
            value={line.storageLocation}
            onChange={(e) => onChange({ ...line, storageLocation: e.target.value })}
            className={INPUT}
          />
        </label>
      </div>

      {line.rejected > 0 && (
        <label className="block text-[11px] font-semibold text-gray-600 dark:text-gray-300 space-y-1">
          <span>
            Motif du rejet <span className="text-red-500">*</span>
          </span>
          <input
            type="text"
            maxLength={1000}
            value={line.rejectionReason}
            onChange={(e) => onChange({ ...line, rejectionReason: e.target.value })}
            className={INPUT}
          />
        </label>
      )}

      {error && <p className="text-[11px] text-red-600 dark:text-red-400">{error}</p>}
    </div>
  );
}
