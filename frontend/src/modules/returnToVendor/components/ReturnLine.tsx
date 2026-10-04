import { lineError, type ReturnLineDraft } from "../utils/returnLine";
import Label from "../../../shared/components/form/Label";
import Input from "../../../shared/components/form/input/InputField";
import Checkbox from "../../../shared/components/form/input/Checkbox";

/** One rejected receipt line: tick it, then choose how much to send back and why. */
export default function ReturnLine({ line, onChange }: { line: ReturnLineDraft; onChange: (line: ReturnLineDraft) => void }) {
  const { receiptLine } = line;
  const error = lineError(line);
  const exhausted = line.available === 0;
  const unit = receiptLine.unitOfMeasure ?? "";

  return (
    <div
      className={`space-y-4 rounded-xl border p-4 ${
        line.selected ? "border-brand-300 bg-brand-25 dark:border-brand-500/40 dark:bg-brand-500/[0.04]" : "border-gray-200 dark:border-white/[0.05]"
      } ${exhausted ? "opacity-60" : ""}`}
    >
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="flex items-start gap-3">
          <Checkbox checked={line.selected} disabled={exhausted} onChange={(checked) => onChange({ ...line, selected: checked })} />
          <div>
            <p className="text-sm font-medium text-gray-800 dark:text-white/90">
              <span className="font-mono text-brand-500">{receiptLine.materialCode}</span>
              {receiptLine.materialName && <span className="ml-2 text-gray-500 dark:text-gray-400">{receiptLine.materialName}</span>}
            </p>
            <p className="text-theme-xs text-gray-500 dark:text-gray-400">
              Rejected at receipt: {line.rejected} {unit}
              {receiptLine.rejectionReason && ` — ${receiptLine.rejectionReason}`}
            </p>
          </div>
        </div>
        <div className="text-right text-theme-xs text-gray-500 dark:text-gray-400">
          <p>
            Still returnable: <span className="font-medium text-gray-800 dark:text-white/90">{line.available}</span> {unit}
          </p>
          {line.held > 0 && <p className="text-warning-600 dark:text-orange-400">Already on other returns: {line.held}</p>}
        </div>
      </div>

      {!exhausted && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div>
            <Label>Quantity to Return</Label>
            <Input
              type="number"
              min="1"
              max={String(line.available)}
              step={1}
              disabled={!line.selected}
              value={line.quantity}
              onChange={(e) => onChange({ ...line, quantity: e.target.value === "" ? 0 : Number(e.target.value) })}
            />
          </div>
          <div className="sm:col-span-2">
            <Label>Defect / Reason</Label>
            <Input
              value={line.reason}
              disabled={!line.selected}
              placeholder="Defaults to the reason recorded at receipt"
              onChange={(e) => onChange({ ...line, reason: e.target.value })}
            />
          </div>
        </div>
      )}
      {error && <p className="text-theme-xs text-error-500">{error}</p>}
    </div>
  );
}
