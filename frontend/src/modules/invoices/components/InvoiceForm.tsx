import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import invoiceService from "../services/invoiceService";
import purchaseOrderService from "../../purchaseOrders/services/purchaseOrderService";
import goodsReceiptService from "../../goodsReceipts/services/goodsReceiptService";
import type { PurchaseOrder } from "../../purchaseOrders/types";
import { INVOICE_TYPE_LABELS, type InvoiceType } from "../types/invoice.types";
import InvoiceLine from "./InvoiceLine";
import {
  INVOICEABLE_ORDER_STATUSES,
  defaultDueDate,
  formatAmount,
  invoiceTotals,
  lineError,
  lineWarnings,
  toInvoiceDrafts,
  type InvoiceLineDraft,
} from "../utils/invoiceLine";
import Button from "../../../shared/components/ui/button/Button";
import useAuth from "../../auth/hooks/useAuth";
import { getApiErrorMessage } from "../../../shared/utils/apiError";
import Label from "../../../shared/components/form/Label";
import Input from "../../../shared/components/form/input/InputField";
import TextArea from "../../../shared/components/form/input/TextArea";
import { FloatingToast, FormCard, FormSection } from "../../../shared/components/page/DetailParts";
import { SELECT_CLASS } from "../../../shared/components/page/pageStyles";

const today = () => new Date().toISOString().slice(0, 10);

export default function InvoiceForm() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const initialOrderId = searchParams.get("purchaseOrderId") || "";

  const [orders, setOrders] = useState<PurchaseOrder[]>([]);
  const [loadingOrders, setLoadingOrders] = useState(true);
  const [selectedOrderId, setSelectedOrderId] = useState(initialOrderId);
  const [order, setOrder] = useState<PurchaseOrder | null>(null);
  const [lines, setLines] = useState<InvoiceLineDraft[]>([]);
  const [invoiceType, setInvoiceType] = useState<InvoiceType>("STANDARD");
  const [externalReference, setExternalReference] = useState("");
  const [invoiceDate, setInvoiceDate] = useState(today());
  const [dueDate, setDueDate] = useState("");
  const [notes, setNotes] = useState("");
  const [loadingOrder, setLoadingOrder] = useState(!!initialOrderId);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    purchaseOrderService
      .getAll()
      .then((res) => {
        if (!cancelled) setOrders(res.data.filter((o) => INVOICEABLE_ORDER_STATUSES.includes(o.status)));
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load purchase orders."));
      })
      .finally(() => {
        if (!cancelled) setLoadingOrders(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!selectedOrderId) return;
    let cancelled = false;
    Promise.all([
      purchaseOrderService.getById(selectedOrderId),
      goodsReceiptService.getByPurchaseOrderId(selectedOrderId),
      invoiceService.getByPurchaseOrderId(selectedOrderId),
    ])
      .then(([orderRes, receiptsRes, invoicesRes]) => {
        if (cancelled) return;
        const loaded = orderRes.data;
        if (!INVOICEABLE_ORDER_STATUSES.includes(loaded.status)) {
          setError("This order has not received anything yet, so it cannot be invoiced.");
          return;
        }
        setOrder(loaded);
        setLines(toInvoiceDrafts(loaded, receiptsRes.data, invoicesRes.data));
        setDueDate(defaultDueDate(invoiceDate, loaded.paymentDelayDays));
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load the purchase order."));
      })
      .finally(() => {
        if (!cancelled) setLoadingOrder(false);
      });
    return () => {
      cancelled = true;
    };
    // The due date is only defaulted when an order is chosen; later date edits keep the user's due date.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedOrderId]);

  const handleSelectOrder = (orderId: string) => {
    setError(null);
    setOrder(null);
    setLines([]);
    setLoadingOrder(!!orderId);
    setSelectedOrderId(orderId);
  };

  const updateLine = (index: number, line: InvoiceLineDraft) =>
    setLines((prev) => prev.map((l, i) => (i === index ? line : l)));

  const billed = useMemo(() => lines.filter((l) => l.invoiced > 0), [lines]);
  const totals = useMemo(() => invoiceTotals(billed), [billed]);
  const warningCount = useMemo(() => billed.reduce((n, l) => n + lineWarnings(l).length, 0), [billed]);

  const submit = async (submitForVerification: boolean) => {
    if (!order || !user) return;
    setError(null);

    if (!externalReference.trim()) {
      setError("Enter the supplier's invoice number.");
      return;
    }
    if (billed.length === 0) {
      setError("Enter at least one invoiced quantity.");
      return;
    }
    if (lines.some((l) => lineError(l))) {
      setError("Fix the highlighted lines before saving.");
      return;
    }
    if (dueDate && dueDate < invoiceDate) {
      setError("The due date cannot be before the invoice date.");
      return;
    }

    try {
      setSubmitting(true);
      const created = await invoiceService.create({
        purchaseOrderId: order.id,
        purchaseOrderCode: order.orderCode,
        supplierId: order.supplierId,
        supplierName: order.supplierName,
        supplierCode: order.supplierCode || undefined,
        invoiceType,
        externalReference: externalReference.trim(),
        invoiceDate,
        dueDate: dueDate || undefined,
        currencyCode: order.currencyCode,
        notes: notes.trim() || undefined,
        lines: billed.map((l, index) => ({
          lineNumber: index + 1,
          purchaseOrderLineId: l.purchaseOrderLineId,
          materialCode: l.materialCode,
          materialName: l.materialName,
          unitOfMeasure: l.unitOfMeasure || undefined,
          quantityInvoiced: l.invoiced,
          unitPrice: l.unitPrice,
          taxAmount: l.taxAmount,
        })),
      });
      if (submitForVerification) {
        try {
          await invoiceService.submit(created.data.id);
        } catch (err) {
          navigate(`/invoices/${created.data.id}`, {
            state: { error: getApiErrorMessage(err, "The invoice was saved as a draft, but submitting it failed.") },
          });
          return;
        }
      }
      navigate(`/invoices/${created.data.id}`);
    } catch (err) {
      setError(getApiErrorMessage(err, "Saving the invoice failed."));
    } finally {
      setSubmitting(false);
    }
  };

  const currency = order?.currencyCode ?? "MAD";
  const noOrders = !loadingOrders && orders.length === 0;

  return (
    <>
      <FloatingToast feedback={error ? { type: "error", text: error } : null} onClose={() => setError(null)} />

      <FormCard title="Record Supplier Invoice">
        <FormSection
          title="Purchase Order"
          aside={
            <Link to="/invoices">
              <Button variant="outline" size="sm">
                Cancel
              </Button>
            </Link>
          }
        >
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <Label>Invoiced Order *</Label>
              <select
                value={selectedOrderId}
                onChange={(e) => handleSelectOrder(e.target.value)}
                className={SELECT_CLASS}
                disabled={loadingOrders || noOrders}
              >
                <option value="">
                  {loadingOrders ? "Loading purchase orders..." : noOrders ? "No order to invoice" : "Select a purchase order with received goods..."}
                </option>
                {orders.map((o) => (
                  <option key={o.id} value={o.id}>
                    {o.orderCode} — {o.supplierName}
                  </option>
                ))}
              </select>
              <p className="mt-1.5 text-theme-xs text-gray-500 dark:text-gray-400">
                {noOrders ? (
                  <>
                    Only orders with received goods can be invoiced.{" "}
                    <Link to="/purchase-orders" className="text-brand-500 hover:underline">
                      View purchase orders
                    </Link>
                  </>
                ) : (
                  `${orders.length} order(s) partly or fully received.`
                )}
              </p>
            </div>

            <div className="rounded-xl border border-gray-200 bg-gray-50/60 p-4 dark:border-gray-800 dark:bg-white/[0.02]">
              {loadingOrder ? (
                <p className="text-sm text-gray-500">Loading purchase order...</p>
              ) : order ? (
                <dl className="grid grid-cols-2 gap-x-4 gap-y-3 text-sm">
                  <div>
                    <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Supplier</dt>
                    <dd className="font-medium text-gray-800 dark:text-white/90">{order.supplierName}</dd>
                  </div>
                  <div>
                    <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Order Date</dt>
                    <dd className="font-medium text-gray-800 dark:text-white/90">{order.orderDate}</dd>
                  </div>
                  <div>
                    <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Payment Terms</dt>
                    <dd className="font-medium text-gray-800 dark:text-white/90">{order.paymentTerms || "—"}</dd>
                  </div>
                  <div>
                    <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Order Total</dt>
                    <dd className="font-medium text-gray-800 dark:text-white/90">{formatAmount(order.grandTotal, order.currencyCode)}</dd>
                  </div>
                </dl>
              ) : (
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  The supplier, currency and payment terms come from the selected order.
                </p>
              )}
            </div>
          </div>
        </FormSection>

        <FormSection title="Invoice Details">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <Label>Type</Label>
              <select value={invoiceType} onChange={(e) => setInvoiceType(e.target.value as InvoiceType)} className={SELECT_CLASS}>
                {(Object.keys(INVOICE_TYPE_LABELS) as InvoiceType[]).map((type) => (
                  <option key={type} value={type}>
                    {INVOICE_TYPE_LABELS[type]}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <Label>Supplier Invoice Number *</Label>
              <Input value={externalReference} onChange={(e) => setExternalReference(e.target.value)} placeholder="e.g. FA-2026-00123" />
            </div>
            <div>
              <Label>Invoice Date *</Label>
              <Input type="date" value={invoiceDate} onChange={(e) => setInvoiceDate(e.target.value)} />
            </div>
            <div>
              <Label>Due Date</Label>
              <Input type="date" value={dueDate} min={invoiceDate} onChange={(e) => setDueDate(e.target.value)} hint="Defaults from the order's payment delay." />
            </div>
            <div className="md:col-span-2">
              <Label>Notes</Label>
              <TextArea rows={2} value={notes} onChange={setNotes} placeholder="Optional remarks" />
            </div>
          </div>
        </FormSection>

        <FormSection
          title="Invoice Lines"
          aside={
            warningCount > 0 ? (
              <span className="text-theme-xs font-medium text-warning-600 dark:text-orange-400">
                {warningCount} difference(s) with the order or receipts
              </span>
            ) : order ? (
              <span className="text-theme-xs text-gray-500 dark:text-gray-400">
                {billed.length} of {lines.length} line(s) billed
              </span>
            ) : undefined
          }
        >
          {order && lines.length > 0 ? (
            <div className="space-y-4">
              {lines.map((line, index) => (
                <InvoiceLine key={line.purchaseOrderLineId} line={line} currency={currency} onChange={(l) => updateLine(index, l)} />
              ))}
            </div>
          ) : (
            <div className="rounded-xl border border-dashed border-gray-300 px-6 py-10 text-center text-sm text-gray-500 dark:border-gray-700 dark:text-gray-400">
              {order ? "This order has no line left to invoice." : "Select a purchase order to load its received lines."}
            </div>
          )}
        </FormSection>

        <div className="flex flex-col gap-4 border-t border-gray-100 pt-6 dark:border-gray-800 md:flex-row md:items-center md:justify-between">
          <div className="space-y-0.5 text-sm text-gray-600 dark:text-gray-400">
            <p>
              Total excl. tax: <span className="font-medium text-gray-800 dark:text-white/90">{formatAmount(totals.totalAmount, currency)}</span>
            </p>
            <p>
              Tax: <span className="font-medium text-gray-800 dark:text-white/90">{formatAmount(totals.totalTaxAmount, currency)}</span>
            </p>
            <p className="text-base">
              Total incl. tax: <span className="font-semibold text-gray-900 dark:text-white">{formatAmount(totals.totalAmountWithTax, currency)}</span>
            </p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Button variant="outline" onClick={() => submit(false)} disabled={submitting || !order}>
              Save as Draft
            </Button>
            <Button onClick={() => submit(true)} disabled={submitting || !order}>
              {submitting ? "Saving..." : "Save and Submit"}
            </Button>
          </div>
        </div>
      </FormCard>
    </>
  );
}
