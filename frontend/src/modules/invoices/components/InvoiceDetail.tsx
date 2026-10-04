import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import invoiceService from "../services/invoiceService";
import type { Invoice } from "../types/invoice.types";
import InvoiceStatusBadge, { InvoiceTypeBadge } from "./InvoiceStatusBadge";
import InvoiceVerification from "./InvoiceVerification";
import InvoicePaymentHistory from "./InvoicePaymentHistory";
import useInvoicePermissions from "../hooks/useInvoice";
import { formatAmount, isOverdue, isPartiallyPaid, outstandingAmount } from "../utils/invoiceLine";
import Button from "../../../shared/components/ui/button/Button";
import Badge from "../../../shared/components/ui/badge/Badge";
import { Modal } from "../../../shared/components/ui/modal";
import Label from "../../../shared/components/form/Label";
import TextArea from "../../../shared/components/form/input/TextArea";
import {
  BackToListButton,
  DetailCard,
  DetailField,
  DetailGrid,
  DetailHeader,
  FloatingToast,
  PageLoader,
  PageNotFound,
} from "../../../shared/components/page/DetailParts";
import { InitialsAvatar } from "../../../shared/components/page/ListParts";
import { SectionIcons } from "../../../shared/components/page/pageIcons";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

type Feedback = { type: "success" | "error"; text: string };

export default function InvoiceDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const permissions = useInvoicePermissions();

  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(() => {
    const state = location.state as { error?: string } | null;
    return state?.error ? { type: "error", text: state.error } : null;
  });
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelReason, setCancelReason] = useState("");

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    invoiceService
      .getById(id)
      .then((res) => {
        if (!cancelled) setInvoice(res.data);
      })
      .catch((err) => {
        if (!cancelled) setLoadError(getApiErrorMessage(err, "The invoice you requested does not exist."));
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
      setFeedback({ type: "error", text: getApiErrorMessage(err, "The action failed.") });
      return false;
    } finally {
      setActionLoading(false);
    }
  };

  const handleSubmit = () => invoice && runAction(() => invoiceService.submit(invoice.id), "Invoice submitted for verification.");

  const handleVerify = () => {
    if (!invoice) return;
    if (invoice.hasDiscrepancy && !window.confirm("This invoice has discrepancies. Verify it anyway?")) return;
    runAction(() => invoiceService.verify(invoice.id), "Invoice verified: it can now be paid.");
  };

  const handleCancel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!invoice || !cancelReason.trim()) return;
    if (await runAction(() => invoiceService.cancel(invoice.id, cancelReason.trim()), "Invoice cancelled.")) {
      setShowCancelModal(false);
      setCancelReason("");
    }
  };

  const handleDelete = async () => {
    if (!invoice || !window.confirm(`Delete invoice ${invoice.invoiceCode}?`)) return;
    try {
      setActionLoading(true);
      await invoiceService.delete(invoice.id);
      navigate("/invoices");
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "The deletion failed.") });
      setActionLoading(false);
    }
  };

  if (loading) return <PageLoader message="Loading invoice..." />;
  if (!invoice) {
    return (
      <PageNotFound
        title="Invoice Not Found"
        message={loadError || "The invoice you requested does not exist."}
        backTo="/invoices"
        backLabel="Back to Invoices"
      />
    );
  }

  const outstanding = outstandingAmount(invoice);

  return (
    <>
      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />

      <DetailHeader title={invoice.invoiceCode} parentName="Invoices" parentUrl="/invoices">
        <BackToListButton to="/invoices" />
        {permissions.canDelete(invoice) && (
          <Button size="sm" variant="outline" onClick={handleDelete} disabled={actionLoading}>
            Delete
          </Button>
        )}
        {permissions.canCancel(invoice) && (
          <Button size="sm" variant="outline" onClick={() => setShowCancelModal(true)} disabled={actionLoading}>
            Cancel Invoice
          </Button>
        )}
        {permissions.canSubmit(invoice) && (
          <Button size="sm" onClick={handleSubmit} disabled={actionLoading}>
            Submit
          </Button>
        )}
        {permissions.canVerify(invoice) && (
          <Button size="sm" onClick={handleVerify} disabled={actionLoading}>
            Verify
          </Button>
        )}
        {/* Invoices are paid through the payments module: this opens a payment pre-filled for this invoice. */}
        {permissions.canPay(invoice) && (
          <Link to={`/payments/create?invoiceId=${invoice.id}`}>
            <Button size="sm" disabled={actionLoading}>
              Pay
            </Button>
          </Link>
        )}
      </DetailHeader>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="flex flex-col gap-6 lg:col-span-1">
          <DetailCard>
            <div className="mb-6 flex items-center justify-between">
              <InitialsAvatar name={invoice.supplierName} size="lg" />
              <div className="flex flex-col items-end gap-2">
                <InvoiceStatusBadge status={invoice.status} size="md" />
                {isPartiallyPaid(invoice) && <Badge size="sm" color="info">Partly paid</Badge>}
                {isOverdue(invoice) && <Badge size="sm" color="error">Overdue</Badge>}
              </div>
            </div>
            <h3 className="mb-1 font-mono text-xl font-bold text-gray-900 dark:text-white">{invoice.invoiceCode}</h3>
            <p className="mb-6 text-sm font-medium text-gray-500 dark:text-gray-400">{invoice.supplierName}</p>
            <div className="space-y-4">
              <DetailField label="Type">
                <InvoiceTypeBadge type={invoice.invoiceType} />
              </DetailField>
              <DetailField label="Supplier Reference">{invoice.externalReference || "—"}</DetailField>
              <DetailField label="Purchase Order">
                {invoice.purchaseOrderId ? (
                  <Link to={`/purchase-orders/${invoice.purchaseOrderId}`} className="font-mono text-brand-500 hover:underline">
                    {invoice.purchaseOrderCode || invoice.purchaseOrderId}
                  </Link>
                ) : (
                  "—"
                )}
              </DetailField>
            </div>
          </DetailCard>

          <DetailCard title="Dates" icon={SectionIcons.calendar}>
            <div className="space-y-4">
              <DetailField label="Invoice Date">{invoice.invoiceDate}</DetailField>
              <DetailField label="Due Date">
                <span className={isOverdue(invoice) ? "text-error-500" : ""}>{invoice.dueDate || "—"}</span>
              </DetailField>
              <DetailField label="Received">{invoice.receivedDate || "—"}</DetailField>
              <DetailField label="Paid On">{invoice.paymentDate || "—"}</DetailField>
            </div>
          </DetailCard>
        </div>

        <div className="flex flex-col gap-6 lg:col-span-2">
          <DetailCard title="Amounts" icon={SectionIcons.money} tone="success">
            <DetailGrid>
              <DetailField label="Total excl. Tax">{formatAmount(invoice.totalAmount, invoice.currencyCode)}</DetailField>
              <DetailField label="Tax">{formatAmount(invoice.totalTaxAmount, invoice.currencyCode)}</DetailField>
              <DetailField label="Total incl. Tax">{formatAmount(invoice.totalAmountWithTax, invoice.currencyCode)}</DetailField>
              <DetailField label="Paid / Left to Pay">
                {formatAmount(invoice.paidAmount ?? 0, invoice.currencyCode)} / {formatAmount(outstanding, invoice.currencyCode)}
              </DetailField>
            </DetailGrid>
          </DetailCard>

          <InvoiceVerification invoice={invoice} />

          <InvoicePaymentHistory invoice={invoice} refreshKey={invoice.status} />

          <DetailCard title="Verification & Payment" icon={SectionIcons.info}>
            <DetailGrid>
              <DetailField label="Verified By">{invoice.verifiedByName || invoice.verifiedBy || "Not verified yet"}</DetailField>
              <DetailField label="Verified On">
                {invoice.verificationDate ? new Date(invoice.verificationDate).toLocaleString() : "—"}
              </DetailField>
              <DetailField label="Last Paid By">{invoice.paidByName || invoice.paidBy || "Not paid yet"}</DetailField>
              <DetailField label="Last Paid On">{invoice.paidAt ? new Date(invoice.paidAt).toLocaleString() : "—"}</DetailField>
            </DetailGrid>
          </DetailCard>

          {(invoice.notes || invoice.internalNotes) && (
            <DetailCard title="Notes" icon={SectionIcons.note}>
              <div className="space-y-3 text-sm">
                {invoice.notes && <p className="text-gray-700 dark:text-gray-300">{invoice.notes}</p>}
                {invoice.internalNotes && <p className="text-gray-500">{invoice.internalNotes}</p>}
              </div>
            </DetailCard>
          )}
        </div>
      </div>

      {showCancelModal && (
        <Modal isOpen={showCancelModal} onClose={() => setShowCancelModal(false)} className="max-w-md p-6">
          <form onSubmit={handleCancel} className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Cancel Invoice</h3>
            <div>
              <Label>Reason *</Label>
              <TextArea rows={3} value={cancelReason} onChange={setCancelReason} placeholder="Why is this invoice cancelled?" />
            </div>
            <div className="flex justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowCancelModal(false)}>
                Back
              </Button>
              <Button size="sm" type="submit" disabled={actionLoading || !cancelReason.trim()}>
                Confirm Cancellation
              </Button>
            </div>
          </form>
        </Modal>
      )}
    </>
  );
}
