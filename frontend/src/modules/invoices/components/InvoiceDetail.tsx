import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import invoiceService from "../services/invoiceService";
import type { Invoice } from "../types/invoice.types";
import InvoiceStatusBadge, { InvoiceTypeBadge } from "./InvoiceStatusBadge";
import InvoiceVerification from "./InvoiceVerification";
import useInvoicePermissions from "../hooks/useInvoice";
import { formatAmount, isOverdue, isPartiallyPaid, outstandingAmount } from "../utils/invoiceLine";
import Button from "../../../shared/components/ui/button/Button";
import { Modal } from "../../../shared/components/ui/modal";
import { getApiErrorMessage } from "../../../shared/utils/apiError";
import { parseAmount } from "../../../shared/utils/moneyUtils";

type Feedback = { type: "success" | "error"; text: string };

const FIELD =
  "w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none";

export default function InvoiceDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const permissions = useInvoicePermissions();

  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(() => {
    const state = location.state as { error?: string } | null;
    return state?.error ? { type: "error", text: state.error } : null;
  });
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelReason, setCancelReason] = useState("");
  const [showPayModal, setShowPayModal] = useState(false);
  const [payAmount, setPayAmount] = useState("");

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    invoiceService
      .getById(id)
      .then((res) => {
        if (!cancelled) setInvoice(res.data);
      })
      .catch((err) => {
        if (!cancelled) setFeedback({ type: "error", text: getApiErrorMessage(err, "Facture introuvable.") });
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  const runAction = async (action: () => Promise<{ data: Invoice }>, success: string) => {
    try {
      setActionLoading(true);
      const res = await action();
      setInvoice(res.data);
      setFeedback({ type: "success", text: success });
      return true;
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "L'action a échoué.") });
      return false;
    } finally {
      setActionLoading(false);
    }
  };

  const handleSubmit = () =>
    invoice && runAction(() => invoiceService.submit(invoice.id), "Facture soumise pour vérification.");

  const handleVerify = () => {
    if (!invoice) return;
    if (invoice.hasDiscrepancy && !window.confirm("Cette facture présente des écarts. La vérifier quand même ?")) return;
    runAction(() => invoiceService.verify(invoice.id), "Facture vérifiée : elle peut être payée.");
  };

  const openPay = () => {
    if (!invoice) return;
    setPayAmount(String(outstandingAmount(invoice)));
    setShowPayModal(true);
  };

  const handlePay = async (e: React.FormEvent) => {
    e.preventDefault();
    const amount = Number(payAmount);
    if (!invoice || !(amount > 0) || amount > outstandingAmount(invoice) + 0.001) return;
    const settles = Math.abs(amount - outstandingAmount(invoice)) < 0.005;
    if (await runAction(() => invoiceService.pay(invoice.id, amount),
        settles ? "Paiement enregistré : la facture est soldée." : "Paiement partiel enregistré.")) {
      setShowPayModal(false);
    }
  };

  const handleCancel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!invoice || !cancelReason.trim()) return;
    if (await runAction(() => invoiceService.cancel(invoice.id, cancelReason.trim()), "Facture annulée.")) {
      setShowCancelModal(false);
      setCancelReason("");
    }
  };

  const handleDelete = async () => {
    if (!invoice || !window.confirm(`Supprimer la facture ${invoice.invoiceCode} ?`)) return;
    try {
      setActionLoading(true);
      await invoiceService.delete(invoice.id);
      navigate("/invoices");
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "La suppression a échoué.") });
      setActionLoading(false);
    }
  };

  if (loading) {
    return <div className="p-6 text-xs text-gray-500">Chargement de la facture...</div>;
  }

  if (!invoice) {
    return (
      <div className="p-6 space-y-3">
        <p className="text-xs text-red-600">{feedback?.text || "Facture introuvable."}</p>
        <Link to="/invoices" className="text-xs text-brand-600 underline">
          Retour aux factures
        </Link>
      </div>
    );
  }

  const payAmountValue = Number(payAmount);
  const outstanding = outstandingAmount(invoice);

  return (
    <div className="space-y-6">
      {feedback && (
        <div
          className={`p-4 rounded-xl text-xs border flex justify-between gap-3 ${
            feedback.type === "success"
              ? "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-500/10 dark:text-emerald-400 dark:border-emerald-500/20"
              : "bg-red-50 text-red-700 border-red-200 dark:bg-red-500/10 dark:text-red-400 dark:border-red-500/20"
          }`}
        >
          <span>{feedback.text}</span>
          <button type="button" onClick={() => setFeedback(null)} className="hover:opacity-75">
            ✕
          </button>
        </div>
      )}

      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm">
        <div className="space-y-1">
          <div className="flex flex-wrap items-center gap-3">
            <h2 className="text-xl font-bold font-mono text-gray-900 dark:text-white">{invoice.invoiceCode}</h2>
            <InvoiceStatusBadge status={invoice.status} />
            {isPartiallyPaid(invoice) && (
              <span className="text-[11px] font-semibold text-indigo-600 dark:text-indigo-400">Partiellement payée</span>
            )}
            <InvoiceTypeBadge type={invoice.invoiceType} />
            {isOverdue(invoice) && (
              <span className="text-[11px] font-semibold text-rose-600 dark:text-rose-400">En retard</span>
            )}
          </div>
          <p className="text-xs text-gray-500 dark:text-gray-400">
            {invoice.supplierName}
            {invoice.externalReference && ` • Réf. fournisseur ${invoice.externalReference}`}
            {invoice.purchaseOrderId && (
              <>
                {" • Commande "}
                <Link
                  to={`/purchase-orders/${invoice.purchaseOrderId}`}
                  className="font-mono font-semibold text-brand-600 dark:text-brand-400 hover:underline"
                >
                  {invoice.purchaseOrderCode || invoice.purchaseOrderId}
                </Link>
              </>
            )}
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {permissions.canSubmit(invoice) && (
            <Button size="sm" onClick={handleSubmit} disabled={actionLoading}>
              Soumettre
            </Button>
          )}
          {permissions.canVerify(invoice) && (
            <Button size="sm" onClick={handleVerify} disabled={actionLoading}>
              Vérifier
            </Button>
          )}
          {permissions.canPay(invoice) && (
            <Button size="sm" onClick={openPay} disabled={actionLoading}>
              Enregistrer le paiement
            </Button>
          )}
          {permissions.canCancel(invoice) && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => setShowCancelModal(true)}
              disabled={actionLoading}
              className="text-rose-600 border-rose-200 hover:bg-rose-50 dark:border-rose-500/20"
            >
              Annuler
            </Button>
          )}
          {permissions.canDelete(invoice) && (
            <Button size="sm" variant="outline" onClick={handleDelete} disabled={actionLoading}>
              Supprimer
            </Button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          ["Total HT", formatAmount(invoice.totalAmount, invoice.currencyCode)],
          ["Taxes", formatAmount(invoice.totalTaxAmount, invoice.currencyCode)],
          ["Total TTC", formatAmount(invoice.totalAmountWithTax, invoice.currencyCode)],
          ["Payé / Reste à payer",
            `${formatAmount(invoice.paidAmount ?? 0, invoice.currencyCode)} / ${formatAmount(outstanding, invoice.currencyCode)}`],
        ].map(([label, value]) => (
          <div
            key={label}
            className="bg-white dark:bg-gray-900 p-4 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm"
          >
            <p className="text-[11px] uppercase tracking-wider text-gray-500">{label}</p>
            <p className="text-sm font-bold text-gray-900 dark:text-white">{value}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
        <div className="bg-white dark:bg-gray-900 p-5 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-1">
          <p className="font-semibold text-gray-900 dark:text-white">Dates</p>
          <p className="text-gray-600 dark:text-gray-400">Facture : {invoice.invoiceDate}</p>
          <p className="text-gray-600 dark:text-gray-400">Échéance : {invoice.dueDate || "-"}</p>
          {invoice.receivedDate && <p className="text-gray-600 dark:text-gray-400">Reçue : {invoice.receivedDate}</p>}
        </div>
        <div className="bg-white dark:bg-gray-900 p-5 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-1">
          <p className="font-semibold text-gray-900 dark:text-white">Vérification</p>
          {invoice.isVerified || invoice.verifiedBy ? (
            <p className="text-gray-600 dark:text-gray-400">
              Par {invoice.verifiedByName || invoice.verifiedBy}
              {invoice.verificationDate && ` le ${invoice.verificationDate.slice(0, 10)}`}
            </p>
          ) : (
            <p className="text-gray-400">Pas encore vérifiée</p>
          )}
        </div>
        <div className="bg-white dark:bg-gray-900 p-5 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-1">
          <p className="font-semibold text-gray-900 dark:text-white">Paiement</p>
          {invoice.paidAt || invoice.paidBy ? (
            <p className="text-gray-600 dark:text-gray-400">
              Par {invoice.paidByName || invoice.paidBy}
              {invoice.paidAt && ` le ${invoice.paidAt.slice(0, 10)}`}
            </p>
          ) : (
            <p className="text-gray-400">Non payée</p>
          )}
        </div>
      </div>

      <InvoiceVerification invoice={invoice} />

      {(invoice.notes || invoice.internalNotes) && (
        <div className="bg-white dark:bg-gray-900 p-5 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm text-xs space-y-2">
          {invoice.notes && <p className="text-gray-700 dark:text-gray-300">{invoice.notes}</p>}
          {invoice.internalNotes && <p className="text-gray-500">{invoice.internalNotes}</p>}
        </div>
      )}

      {showPayModal && (
        <Modal isOpen={showPayModal} onClose={() => setShowPayModal(false)} className="max-w-md p-6">
          <form onSubmit={handlePay} className="space-y-4">
            <h3 className="text-base font-bold text-gray-900 dark:text-white">Enregistrer le paiement</h3>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 space-y-1">
              <span>
                Montant payé ({invoice.currencyCode}) <span className="text-red-500">*</span>
              </span>
              <input
                type="number"
                min={0.01}
                max={outstanding}
                step="0.01"
                required
                value={payAmount}
                onChange={(e) => setPayAmount(e.target.value)}
                className={FIELD}
              />
            </label>
            <p className="text-[11px] text-gray-500">
              Reste à payer : {formatAmount(outstanding, invoice.currencyCode)}
              {parseAmount(invoice.paidAmount ?? 0) > 0 &&
                ` (déjà payé : ${formatAmount(invoice.paidAmount, invoice.currencyCode)})`}
            </p>
            {payAmountValue > outstanding + 0.001 && (
              <p className="text-[11px] text-red-600 dark:text-red-400">Le montant dépasse le reste à payer.</p>
            )}
            {payAmountValue > 0 && payAmountValue < outstanding - 0.001 && (
              <p className="text-[11px] text-amber-700 dark:text-amber-400">
                Paiement partiel : la facture restera à payer pour{" "}
                {formatAmount(outstanding - payAmountValue, invoice.currencyCode)}.
              </p>
            )}
            <div className="flex justify-end gap-2 pt-3 border-t border-gray-100 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowPayModal(false)}>
                Retour
              </Button>
              <Button
                size="sm"
                type="submit"
                disabled={actionLoading || !(payAmountValue > 0) || payAmountValue > outstanding + 0.001}
              >
                Confirmer le paiement
              </Button>
            </div>
          </form>
        </Modal>
      )}

      {showCancelModal && (
        <Modal isOpen={showCancelModal} onClose={() => setShowCancelModal(false)} className="max-w-md p-6">
          <form onSubmit={handleCancel} className="space-y-4">
            <h3 className="text-base font-bold text-gray-900 dark:text-white">Annuler la facture</h3>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 space-y-1">
              <span>
                Motif <span className="text-red-500">*</span>
              </span>
              <textarea
                required
                rows={3}
                maxLength={1000}
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                className={FIELD}
              />
            </label>
            <div className="flex justify-end gap-2 pt-3 border-t border-gray-100 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowCancelModal(false)}>
                Retour
              </Button>
              <Button size="sm" type="submit" disabled={actionLoading || !cancelReason.trim()}>
                Confirmer l'annulation
              </Button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
