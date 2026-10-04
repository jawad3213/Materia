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

const FIELD =
  "w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none";
const LABEL = "block text-xs font-semibold text-gray-700 dark:text-gray-300 space-y-1";

const today = () => new Date().toISOString().slice(0, 10);

export default function InvoiceForm() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const initialOrderId = searchParams.get("purchaseOrderId") || "";

  const [orders, setOrders] = useState<PurchaseOrder[]>([]);
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
      .catch((err) => console.error("Failed to load purchase orders:", err));
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
          setError("Cette commande n'a encore rien reçu : elle ne peut pas être facturée.");
          return;
        }
        setOrder(loaded);
        setLines(toInvoiceDrafts(loaded, receiptsRes.data, invoicesRes.data));
        setDueDate(defaultDueDate(invoiceDate, loaded.paymentDelayDays));
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Impossible de charger la commande."));
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
      setError("Saisissez le numéro de facture du fournisseur.");
      return;
    }
    if (billed.length === 0) {
      setError("Saisissez au moins une quantité facturée.");
      return;
    }
    if (lines.some((l) => lineError(l))) {
      setError("Corrigez les lignes signalées avant d'enregistrer.");
      return;
    }
    if (dueDate && dueDate < invoiceDate) {
      setError("L'échéance ne peut pas précéder la date de facture.");
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
            state: { error: getApiErrorMessage(err, "Facture enregistrée en brouillon, mais la soumission a échoué.") },
          });
          return;
        }
      }
      navigate(`/invoices/${created.data.id}`);
    } catch (err) {
      setError(getApiErrorMessage(err, "L'enregistrement de la facture a échoué."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {error && (
        <div className="p-4 rounded-xl bg-red-50 text-red-700 dark:bg-red-500/10 dark:text-red-400 text-xs border border-red-200 dark:border-red-500/20">
          {error}
        </div>
      )}

      <div className="bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-4">
        <h2 className="text-base font-bold text-gray-900 dark:text-white">Commande facturée</h2>
        <label className={LABEL}>
          <span>
            Commande <span className="text-red-500">*</span>
          </span>
          <select value={selectedOrderId} onChange={(e) => handleSelectOrder(e.target.value)} className={FIELD}>
            <option value="">Choisir une commande réceptionnée...</option>
            {orders.map((o) => (
              <option key={o.id} value={o.id}>
                {o.orderCode} — {o.supplierName}
              </option>
            ))}
          </select>
        </label>
        {loadingOrder && <p className="text-xs text-gray-500">Chargement de la commande...</p>}
        {order && (
          <p className="text-xs text-gray-500 dark:text-gray-400">
            Fournisseur : <span className="font-semibold">{order.supplierName}</span> • Devise : {order.currencyCode}
            {order.paymentTerms && ` • Conditions : ${order.paymentTerms}`}
          </p>
        )}
      </div>

      {order && (
        <>
          <div className="bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm grid grid-cols-1 md:grid-cols-2 gap-4">
            <label className={LABEL}>
              <span>Type</span>
              <select value={invoiceType} onChange={(e) => setInvoiceType(e.target.value as InvoiceType)} className={FIELD}>
                {(Object.keys(INVOICE_TYPE_LABELS) as InvoiceType[]).map((type) => (
                  <option key={type} value={type}>
                    {INVOICE_TYPE_LABELS[type]}
                  </option>
                ))}
              </select>
            </label>
            <label className={LABEL}>
              <span>
                N° facture fournisseur <span className="text-red-500">*</span>
              </span>
              <input
                value={externalReference}
                maxLength={100}
                onChange={(e) => setExternalReference(e.target.value)}
                className={FIELD}
                placeholder="ex. FA-2026-00123"
              />
            </label>
            <label className={LABEL}>
              <span>
                Date de facture <span className="text-red-500">*</span>
              </span>
              <input type="date" value={invoiceDate} onChange={(e) => setInvoiceDate(e.target.value)} className={FIELD} />
            </label>
            <label className={LABEL}>
              <span>Échéance</span>
              <input type="date" value={dueDate} min={invoiceDate} onChange={(e) => setDueDate(e.target.value)} className={FIELD} />
            </label>
            <label className={`${LABEL} md:col-span-2`}>
              <span>Notes</span>
              <textarea rows={2} maxLength={1000} value={notes} onChange={(e) => setNotes(e.target.value)} className={FIELD} />
            </label>
          </div>

          <div className="bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-base font-bold text-gray-900 dark:text-white">Lignes</h2>
              {warningCount > 0 && (
                <span className="text-[11px] text-amber-700 dark:text-amber-400">
                  {warningCount} écart(s) avec la commande ou les réceptions
                </span>
              )}
            </div>
            {lines.map((line, index) => (
              <InvoiceLine
                key={line.purchaseOrderLineId}
                line={line}
                currency={order.currencyCode}
                onChange={(l) => updateLine(index, l)}
              />
            ))}
          </div>

          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm">
            <div className="text-xs text-gray-700 dark:text-gray-300 space-y-0.5">
              <p>Total HT : <span className="font-semibold">{formatAmount(totals.totalAmount, order.currencyCode)}</span></p>
              <p>Taxes : <span className="font-semibold">{formatAmount(totals.totalTaxAmount, order.currencyCode)}</span></p>
              <p className="text-sm">
                Total TTC :{" "}
                <span className="font-bold text-gray-900 dark:text-white">
                  {formatAmount(totals.totalAmountWithTax, order.currencyCode)}
                </span>
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Link to="/invoices">
                <Button size="sm" variant="outline">Annuler</Button>
              </Link>
              <Button size="sm" variant="outline" onClick={() => submit(false)} disabled={submitting}>
                Enregistrer en brouillon
              </Button>
              <Button size="sm" onClick={() => submit(true)} disabled={submitting}>
                Enregistrer et soumettre
              </Button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
