import QualityStatusBadge from "./QualityStatusBadge";
import { deriveQualityStatus, lineError, type ReceiptLineDraft } from "../utils/receiptLine";
import Label from "../../../shared/components/form/Label";
import Input from "../../../shared/components/form/input/InputField";

interface GoodsReceiptLineProps {
  line: ReceiptLineDraft;
  onChange: (line: ReceiptLineDraft) => void;
}

/** One order line being received: quantities, batch, location and, when goods are rejected, the reason. */
export default function GoodsReceiptLine({ line, onChange }: GoodsReceiptLineProps) {
  const error = lineError(line);
  const toQuantity = (value: string) => Math.max(0, Math.floor(Number(value) || 0));

  return (
    <div className="space-y-4 rounded-xl border border-gray-200 p-4 dark:border-white/[0.05]">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div>
          <p className="text-sm font-medium text-gray-800 dark:text-white/90">
            <span className="font-mono text-brand-500">{line.materialCode}</span>
            {line.materialName && ` — ${line.materialName}`}
          </p>
          <p className="text-theme-xs text-gray-500 dark:text-gray-400">
            Ordered: {line.ordered} {line.unitOfMeasure} • Remaining: {line.remaining} {line.unitOfMeasure}
          </p>
        </div>
        {line.received > 0 && <QualityStatusBadge status={deriveQualityStatus(line)} />}
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div>
          <Label>Quantity Received</Label>
          <Input
            type="number"
            min="0"
            max={String(line.remaining)}
            value={line.received}
            onChange={(e) => onChange({ ...line, received: toQuantity(e.target.value) })}
          />
        </div>
        <div>
          <Label>Quantity Rejected</Label>
          <Input
            type="number"
            min="0"
            max={String(line.received)}
            value={line.rejected}
            onChange={(e) => onChange({ ...line, rejected: toQuantity(e.target.value) })}
          />
        </div>
        <div>
          <Label>Batch Number</Label>
          <Input value={line.batchNumber} onChange={(e) => onChange({ ...line, batchNumber: e.target.value })} />
        </div>
        <div>
          <Label>Storage Location</Label>
          <Input value={line.storageLocation} onChange={(e) => onChange({ ...line, storageLocation: e.target.value })} />
        </div>
      </div>

      {line.rejected > 0 && (
        <div>
          <Label>Rejection Reason *</Label>
          <Input
            value={line.rejectionReason}
            onChange={(e) => onChange({ ...line, rejectionReason: e.target.value })}
            placeholder="e.g. Damaged packaging"
          />
        </div>
      )}

      {error && <p className="text-theme-xs text-error-500">{error}</p>}
    </div>
  );
}
