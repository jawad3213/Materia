import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import paymentService from "../services/paymentService";
import { METHODS_NEEDING_REFERENCE, PAYMENT_METHOD_LABELS, type Payment, type PaymentMethod } from "../types/payment.types";
import PaymentStatusBadge from "./PaymentStatusBadge";
import usePaymentPermissions from "../hooks/usePayment";
import { completionError } from "../utils/paymentLine";
import { formatAmount } from "../../invoices/utils/invoiceLine";
import Button from "../../../shared/components/ui/button/Button";
import Badge from "../../../shared/components/ui/badge/Badge";
import { Modal } from "../../../shared/components/ui/modal";
import Label from "../../../shared/components/form/Label";
import Input from "../../../shared/components/form/input/InputField";
import TextArea from "../../../shared/components/form/input/TextArea";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";
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
import { BODY_CELL, HEAD_CELL, SELECT_CLASS } from "../../../shared/components/page/pageStyles";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

type Feedback = { type: "success" | "error"; text: string };

const formatDateTime = (value?: string | null) => (value ? new Date(value).toLocaleString() : "—");

export default function PaymentDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const permissions = usePaymentPermissions();

  const [payment, setPayment] = useState<Payment | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(() => {
    const state = location.state as { error?: string } | null;
    return state?.error ? { type: "error", text: state.error } : null;
  });
  const [showComplete, setShowComplete] = useState(false);
  const [method, setMethod] = useState<PaymentMethod | "">("BANK_TRANSFER");
  const [bankReference, setBankReference] = useState("");
  const [transactionId, setTransactionId] = useState("");
  const [showCancel, setShowCancel] = useState(false);
  const [cancelReason, setCancelReason] = useState("");

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    paymentService
      .getById(id)
      .then((res) => {
        if (!cancelled) setPayment(res.data);
      })
      .catch((err) => {
        if (!cancelled) setLoadError(getApiErrorMessage(err, "The payment you requested does not exist."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  const runAction = async (action: () => Promise<{ data: Payment }>, success: string) => {
    try {
      setActionLoading(true);
      const res = await action();
      setPayment(res.data);
      setFeedback({ type: "success", text: success });
      return true;
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "The action failed.") });
      return false;
    } finally {
      setActionLoading(false);
    }
  };

  const handlePrepare = () => payment && runAction(() => paymentService.prepare(payment.id), "Payment prepared: it can now be executed.");

  const completeError = completionError(method, bankReference);

  const handleComplete = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!payment || completeError || !method) return;
    const done = await runAction(
      () =>
        paymentService.complete(payment.id, {
          paymentMethod: method,
          bankReference: bankReference.trim() || undefined,
          transactionId: transactionId.trim() || undefined,
        }),
      "Payment executed: the invoices have been updated."
    );
    if (done) setShowComplete(false);
  };

  const handleCancel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!payment || !cancelReason.trim()) return;
    if (await runAction(() => paymentService.cancel(payment.id, cancelReason.trim()), "Payment cancelled.")) {
      setShowCancel(false);
      setCancelReason("");
    }
  };

  const handleDelete = async () => {
    if (!payment || !window.confirm(`Delete payment ${payment.paymentCode}?`)) return;
    try {
      setActionLoading(true);
      await paymentService.delete(payment.id);
      navigate("/payments");
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "The deletion failed.") });
      setActionLoading(false);
    }
  };

  if (loading) return <PageLoader message="Loading payment..." />;
  if (!payment) {
    return (
      <PageNotFound
        title="Payment Not Found"
        message={loadError || "The payment you requested does not exist."}
        backTo="/payments"
        backLabel="Back to Payments"
      />
    );
  }

  const settledLines = payment.lines.filter((l) => l.isPaid).length;

  return (
    <>
      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />

      <DetailHeader title={payment.paymentCode} parentName="Payments" parentUrl="/payments">
        <BackToListButton to="/payments" />
        {permissions.canDelete(payment) && (
          <Button size="sm" variant="outline" onClick={handleDelete} disabled={actionLoading}>
            Delete
          </Button>
        )}
        {permissions.canCancel(payment) && (
          <Button size="sm" variant="outline" onClick={() => setShowCancel(true)} disabled={actionLoading}>
            Cancel Payment
          </Button>
        )}
        {permissions.canPrepare(payment) && (
          <Button size="sm" onClick={handlePrepare} disabled={actionLoading}>
            Prepare
          </Button>
        )}
        {permissions.canComplete(payment) && (
          <Button size="sm" onClick={() => setShowComplete(true)} disabled={actionLoading}>
            Execute Payment
          </Button>
        )}
      </DetailHeader>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="flex flex-col gap-6 lg:col-span-1">
          <DetailCard>
            <div className="mb-6 flex items-center justify-between">
              <InitialsAvatar name={payment.supplierName} size="lg" />
              <PaymentStatusBadge status={payment.status} size="md" />
            </div>
            <h3 className="mb-1 font-mono text-xl font-bold text-gray-900 dark:text-white">{payment.paymentCode}</h3>
            <p className="mb-6 text-sm font-medium text-gray-500 dark:text-gray-400">{payment.supplierName}</p>
            <div className="space-y-4">
              <DetailField label="Supplier Code">{payment.supplierCode || "—"}</DetailField>
              <DetailField label="Currency">{payment.currencyCode}</DetailField>
              <DetailField label="Invoices Covered">{payment.lines.length}</DetailField>
            </div>
          </DetailCard>

          <DetailCard title="Execution" icon={SectionIcons.check} tone={payment.status === "COMPLETED" ? "success" : "gray"}>
            <div className="space-y-4">
              <DetailField label="Method">{payment.paymentMethod ? PAYMENT_METHOD_LABELS[payment.paymentMethod] : "Not executed yet"}</DetailField>
              <DetailField label="Bank Reference">
                <span className="font-mono">{payment.bankReference || "—"}</span>
              </DetailField>
              <DetailField label="Transaction ID">
                <span className="font-mono">{payment.transactionId || "—"}</span>
              </DetailField>
              <DetailField label="Paid On">{formatDateTime(payment.confirmedDate)}</DetailField>
            </div>
          </DetailCard>
        </div>

        <div className="flex flex-col gap-6 lg:col-span-2">
          <DetailCard title="Amounts" icon={SectionIcons.money} tone="success">
            <DetailGrid>
              <DetailField label="Total">{formatAmount(payment.totalAmount, payment.currencyCode)}</DetailField>
              <DetailField label="Paid">{formatAmount(payment.paidAmount ?? 0, payment.currencyCode)}</DetailField>
            </DetailGrid>
          </DetailCard>

          <DetailCard
            title="Invoices Paid"
            icon={SectionIcons.list}
            tone="brand"
            padded={false}
            aside={
              <Badge size="sm" color={settledLines === payment.lines.length ? "success" : "light"}>
                {settledLines} / {payment.lines.length} settled
              </Badge>
            }
          >
            <div className="max-w-full overflow-x-auto">
              <Table>
                <TableHeader className="border-b border-gray-100 bg-gray-50/50 dark:border-white/[0.05] dark:bg-gray-900/50">
                  <TableRow>
                    <TableCell isHeader className={HEAD_CELL}>#</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Invoice</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Amount</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>State</TableCell>
                  </TableRow>
                </TableHeader>
                <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {payment.lines.map((l) => (
                    <TableRow key={l.id}>
                      <TableCell className={BODY_CELL}>{l.lineNumber}</TableCell>
                      <TableCell className={BODY_CELL}>
                        <Link to={`/invoices/${l.invoiceId}`} className="font-mono text-brand-500 hover:underline">
                          {l.invoiceCode || l.invoiceId}
                        </Link>
                      </TableCell>
                      <TableCell className={`${BODY_CELL} whitespace-nowrap`}>{formatAmount(l.amount, payment.currencyCode)}</TableCell>
                      <TableCell className={BODY_CELL}>
                        <Badge size="sm" color={l.isPaid ? "success" : "light"}>
                          {l.isPaid ? "Settled" : "Pending"}
                        </Badge>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </DetailCard>

          <DetailCard title="System Information" icon={SectionIcons.info}>
            <DetailGrid>
              <DetailField label="Created">{formatDateTime(payment.createdAt)}</DetailField>
              <DetailField label="Created By">{payment.createdBy || "—"}</DetailField>
              <DetailField label="Last Updated">{formatDateTime(payment.updatedAt)}</DetailField>
              <DetailField label="Updated By">{payment.updatedBy || "—"}</DetailField>
            </DetailGrid>
          </DetailCard>

          {(payment.notes || payment.internalNotes) && (
            <DetailCard title="Notes" icon={SectionIcons.note}>
              <div className="space-y-3 text-sm">
                {payment.notes && <p className="text-gray-700 dark:text-gray-300">{payment.notes}</p>}
                {payment.internalNotes && <p className="text-gray-500">{payment.internalNotes}</p>}
              </div>
            </DetailCard>
          )}
        </div>
      </div>

      {showComplete && (
        <Modal isOpen={showComplete} onClose={() => setShowComplete(false)} className="max-w-md p-6">
          <form onSubmit={handleComplete} className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Execute Payment</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              {formatAmount(payment.totalAmount, payment.currencyCode)} will be recorded on {payment.lines.length} invoice(s).
            </p>
            <div>
              <Label>Method *</Label>
              <select value={method} onChange={(e) => setMethod(e.target.value as PaymentMethod)} className={SELECT_CLASS}>
                {(Object.keys(PAYMENT_METHOD_LABELS) as PaymentMethod[]).map((m) => (
                  <option key={m} value={m}>
                    {PAYMENT_METHOD_LABELS[m]}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <Label>Bank Reference{method && METHODS_NEEDING_REFERENCE.includes(method) ? " *" : ""}</Label>
              <Input value={bankReference} onChange={(e) => setBankReference(e.target.value)} />
            </div>
            <div>
              <Label>Transaction ID</Label>
              <Input value={transactionId} onChange={(e) => setTransactionId(e.target.value)} />
            </div>
            {completeError && <p className="text-theme-xs text-warning-600 dark:text-orange-400">{completeError}</p>}
            <div className="flex justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowComplete(false)}>
                Back
              </Button>
              <Button size="sm" type="submit" disabled={actionLoading || !!completeError}>
                Confirm Payment
              </Button>
            </div>
          </form>
        </Modal>
      )}

      {showCancel && (
        <Modal isOpen={showCancel} onClose={() => setShowCancel(false)} className="max-w-md p-6">
          <form onSubmit={handleCancel} className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Cancel Payment</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">The amounts set aside on the invoices will be released.</p>
            <div>
              <Label>Reason *</Label>
              <TextArea rows={3} value={cancelReason} onChange={setCancelReason} placeholder="Why is this payment cancelled?" />
            </div>
            <div className="flex justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowCancel(false)}>
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
