import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import paymentService from "../services/paymentService";
import invoiceService from "../../invoices/services/invoiceService";
import type { Invoice } from "../../invoices/types/invoice.types";
import PaymentLine from "./PaymentLine";
import PaymentSimulation from "./PaymentSimulation";
import {
  lineError,
  payableInvoices,
  paymentTotal,
  suppliersWithVerifiedInvoices,
  toPaymentDrafts,
  type PaymentLineDraft,
} from "../utils/paymentLine";
import Button from "../../../shared/components/ui/button/Button";
import Label from "../../../shared/components/form/Label";
import TextArea from "../../../shared/components/form/input/TextArea";
import { FloatingToast, FormCard, FormSection } from "../../../shared/components/page/DetailParts";
import { SELECT_CLASS } from "../../../shared/components/page/pageStyles";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

/**
 * Creates a payment for one supplier: pick among their verified invoices and the amount for each.
 * Opened from an invoice (`?invoiceId=`), the supplier and that invoice are pre-selected.
 */
export default function PaymentForm() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const fromInvoiceId = searchParams.get("invoiceId");

  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [supplierId, setSupplierId] = useState("");
  const [lines, setLines] = useState<PaymentLineDraft[]>([]);
  const [notes, setNotes] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingSupplier, setLoadingSupplier] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    invoiceService
      .getAll()
      .then((res) => {
        if (cancelled) return;
        setInvoices(res.data);
        const origin = fromInvoiceId ? res.data.find((i) => i.id === fromInvoiceId) : undefined;
        if (origin) {
          setLoadingSupplier(true);
          setSupplierId(origin.supplierId);
        }
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load invoices."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [fromInvoiceId]);

  // The supplier's open payments set money aside on their invoices.
  useEffect(() => {
    if (!supplierId) return;
    let cancelled = false;
    paymentService
      .getBySupplierId(supplierId)
      .then((res) => {
        if (!cancelled) setLines(toPaymentDrafts(payableInvoices(invoices, res.data, supplierId), fromInvoiceId));
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load the supplier's payments."));
      })
      .finally(() => {
        if (!cancelled) setLoadingSupplier(false);
      });
    return () => {
      cancelled = true;
    };
  }, [supplierId, invoices, fromInvoiceId]);

  const suppliers = useMemo(() => suppliersWithVerifiedInvoices(invoices), [invoices]);
  const currency = lines[0]?.invoice.currencyCode ?? "MAD";
  const selected = lines.filter((l) => l.selected);

  const handleSupplier = (id: string) => {
    setError(null);
    setLines([]);
    setLoadingSupplier(!!id);
    setSupplierId(id);
  };

  const updateLine = (index: number, line: PaymentLineDraft) =>
    setLines((prev) => prev.map((l, i) => (i === index ? line : l)));

  const submit = async (prepare: boolean) => {
    setError(null);
    if (selected.length === 0) {
      setError("Select at least one invoice.");
      return;
    }
    if (lines.some((l) => lineError(l))) {
      setError("Fix the highlighted amounts before saving.");
      return;
    }
    if (new Set(selected.map((l) => l.invoice.currencyCode)).size > 1) {
      setError("A payment can only cover invoices in a single currency.");
      return;
    }
    try {
      setSubmitting(true);
      const created = await paymentService.create({
        supplierId,
        currencyCode: currency,
        notes: notes.trim() || undefined,
        lines: selected.map((l) => ({ invoiceId: l.invoice.id, amount: l.amount })),
      });
      if (prepare) {
        try {
          await paymentService.prepare(created.data.id);
        } catch (err) {
          navigate(`/payments/${created.data.id}`, {
            state: { error: getApiErrorMessage(err, "The payment was saved as a draft, but preparing it failed.") },
          });
          return;
        }
      }
      navigate(`/payments/${created.data.id}`);
    } catch (err) {
      setError(getApiErrorMessage(err, "Saving the payment failed."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <FloatingToast feedback={error ? { type: "error", text: error } : null} onClose={() => setError(null)} />

      <FormCard title="Create Payment">
        <FormSection
          title="Supplier"
          aside={
            <Link to="/payments">
              <Button variant="outline" size="sm">
                Cancel
              </Button>
            </Link>
          }
        >
          <Label>Supplier paid *</Label>
          <select value={supplierId} onChange={(e) => handleSupplier(e.target.value)} className={SELECT_CLASS} disabled={loading}>
            <option value="">{loading ? "Loading..." : "Select a supplier with invoices to pay..."}</option>
            {suppliers.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
          {!loading && suppliers.length === 0 && (
            <p className="mt-1.5 text-theme-xs text-gray-500 dark:text-gray-400">No verified invoice is waiting for payment.</p>
          )}
        </FormSection>

        {supplierId && (
          <>
            <FormSection title="Invoices to Pay" aside={<span className="text-theme-xs text-gray-500 dark:text-gray-400">{selected.length} selected</span>}>
              <div className="space-y-4">
                {loadingSupplier && <p className="text-sm text-gray-500">Loading invoices...</p>}
                {!loadingSupplier && lines.length === 0 && (
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    Nothing to pay: this supplier&apos;s invoices are settled or already set aside by other payments.
                  </p>
                )}
                {lines.map((line, index) => (
                  <PaymentLine key={line.invoice.id} line={line} onChange={(l) => updateLine(index, l)} />
                ))}
              </div>
            </FormSection>

            <FormSection title="Notes">
              <TextArea rows={2} value={notes} onChange={setNotes} placeholder="Optional remarks" />
            </FormSection>

            <div className="flex flex-col gap-6 border-t border-gray-100 pt-6 dark:border-gray-800 md:flex-row md:items-start md:justify-between">
              <div className="md:w-1/2">
                <PaymentSimulation lines={lines} currency={currency} />
              </div>
              <div className="flex flex-wrap gap-3">
                <Button variant="outline" onClick={() => submit(false)} disabled={submitting || paymentTotal(lines) <= 0}>
                  Save as Draft
                </Button>
                <Button onClick={() => submit(true)} disabled={submitting || paymentTotal(lines) <= 0}>
                  {submitting ? "Saving..." : "Save and Prepare"}
                </Button>
              </div>
            </div>
          </>
        )}
      </FormCard>
    </>
  );
}
