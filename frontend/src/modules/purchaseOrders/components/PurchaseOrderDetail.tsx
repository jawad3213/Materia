import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import purchaseOrderService from "../services/purchaseOrderService";
import { DELIVERY_STATUS_INFO, type AssignableReceiver, type DeliveryStatus, type PurchaseOrder } from "../types";
import PurchaseOrderStatusBadge, { PurchaseOrderDeliveryStatusBadge } from "./PurchaseOrderStatusBadge";
import PurchaseOrderCancelModal from "./PurchaseOrderCancelModal";
import useAuth from "../../auth/hooks/useAuth";
import usePurchaseOrderPermissions from "../hooks/usePurchaseOrder";
import useInvoicePermissions from "../../invoices/hooks/useInvoice";
import { INVOICEABLE_ORDER_STATUSES, formatAmount } from "../../invoices/utils/invoiceLine";
import Button from "../../../shared/components/ui/button/Button";
import { Modal } from "../../../shared/components/ui/modal";
import Label from "../../../shared/components/form/Label";
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
import { InitialsAvatar, StackedCell } from "../../../shared/components/page/ListParts";
import { SectionIcons } from "../../../shared/components/page/pageIcons";
import { BODY_CELL, HEAD_CELL, SELECT_CLASS } from "../../../shared/components/page/pageStyles";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

type Feedback = { type: "success" | "error"; text: string };

/** Delivery statuses a buyer sets by hand; PARTIAL and DELIVERED follow from goods receipts. */
const TRACKABLE_DELIVERY: DeliveryStatus[] = ["NOT_SHIPPED", "SHIPPED", "IN_TRANSIT", "DELAYED"];

const formatDateTime = (value?: string | null) => (value ? new Date(value).toLocaleString() : "—");

export default function PurchaseOrderDetail() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const permissions = usePurchaseOrderPermissions();
  const invoicePermissions = useInvoicePermissions();

  const [order, setOrder] = useState<PurchaseOrder | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(null);

  const [showCancel, setShowCancel] = useState(false);
  const [showAssign, setShowAssign] = useState(false);
  const [showDelivery, setShowDelivery] = useState(false);
  const [showReject, setShowReject] = useState(false);
  const [receivers, setReceivers] = useState<AssignableReceiver[]>([]);
  const [receiversLoading, setReceiversLoading] = useState(false);
  const [assignedUserId, setAssignedUserId] = useState("");
  const [rejectReason, setRejectReason] = useState("");
  const [deliveryStatus, setDeliveryStatus] = useState<DeliveryStatus>("IN_TRANSIT");

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    purchaseOrderService
      .getById(id)
      .then((res) => {
        if (!cancelled) setOrder(res.data);
      })
      .catch((err) => {
        if (!cancelled) setLoadError(getApiErrorMessage(err, "The purchase order you requested does not exist."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  const userId = user?.id || "";
  const userName = user?.name || user?.email || "";

  /** Runs a workflow action and refreshes the order; returns whether it succeeded. */
  const runAction = async (action: () => Promise<{ data?: PurchaseOrder } | unknown>, success: string, failure: string) => {
    if (!order) return false;
    try {
      setActionLoading(true);
      const res = (await action()) as { data?: PurchaseOrder } | undefined;
      setOrder(res?.data ?? (await purchaseOrderService.getById(order.id)).data);
      setFeedback({ type: "success", text: success });
      return true;
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, failure) });
      return false;
    } finally {
      setActionLoading(false);
    }
  };

  const handleSubmit = () =>
    order && runAction(() => purchaseOrderService.submit(order.id, { userId }), `Order ${order.orderCode} was sent to the supplier.`, "Submitting the order failed.");

  const handleConfirm = () =>
    order &&
    runAction(() => purchaseOrderService.confirm(order.id, { userId }), `Order ${order.orderCode} is confirmed by the supplier.`, "Confirming the order failed.");

  const handleReceiveAll = () =>
    order &&
    runAction(
      () => purchaseOrderService.confirmReceipt(order.id, { receiverId: userId, receiverName: userName }),
      `Everything outstanding on ${order.orderCode} was received.`,
      "Receiving the order failed."
    );

  const handleCloseShort = () => {
    if (!order || !window.confirm(`Close ${order.orderCode}? The remaining quantities will no longer be expected.`)) return;
    runAction(() => purchaseOrderService.complete(order.id, { userId }), `Order ${order.orderCode} was closed.`, "Closing the order failed.");
  };

  const openAssign = async () => {
    setShowAssign(true);
    setReceiversLoading(true);
    try {
      setReceivers((await purchaseOrderService.getAssignableReceivers()).data);
    } catch {
      setReceivers([]);
    } finally {
      setReceiversLoading(false);
    }
  };

  const handleAssign = async (e: React.FormEvent) => {
    e.preventDefault();
    const receiver = receivers.find((r) => r.id === assignedUserId);
    if (!order || !receiver) return;
    const done = await runAction(
      () =>
        purchaseOrderService.assignReceiver(order.id, {
          userId,
          userName,
          assignedUserId: receiver.id,
          assignedUserName: receiver.name,
        }),
      `${receiver.name} will receive order ${order.orderCode}.`,
      "Assigning the receiver failed."
    );
    if (done) {
      setShowAssign(false);
      setAssignedUserId("");
    }
  };

  const handleReject = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!order || !rejectReason.trim()) return;
    const done = await runAction(
      () => purchaseOrderService.reject(order.id, { reason: rejectReason.trim() }),
      `The supplier rejection of ${order.orderCode} was recorded.`,
      "Recording the rejection failed."
    );
    if (done) {
      setShowReject(false);
      setRejectReason("");
    }
  };

  const handleDelivery = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!order) return;
    const done = await runAction(
      () => purchaseOrderService.updateDeliveryStatus(order.id, { deliveryStatus, userId }),
      `Delivery status set to ${DELIVERY_STATUS_INFO[deliveryStatus].label}.`,
      "Updating the delivery status failed."
    );
    if (done) setShowDelivery(false);
  };

  const handleCancel = async (reason: string) => {
    if (!order) return;
    await purchaseOrderService.cancel(order.id, { userId, reason });
    setOrder((await purchaseOrderService.getById(order.id)).data);
    setFeedback({ type: "success", text: `Order ${order.orderCode} was cancelled.` });
  };

  if (loading) return <PageLoader message="Loading purchase order..." />;
  if (!order) {
    return (
      <PageNotFound
        title="Purchase Order Not Found"
        message={loadError || "The purchase order you requested does not exist."}
        backTo="/purchase-orders"
        backLabel="Back to Purchase Orders"
      />
    );
  }

  const lines = order.lines || [];
  const currency = order.currencyCode;

  return (
    <>
      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />

      <DetailHeader title={order.orderCode} parentName="Purchase Orders" parentUrl="/purchase-orders">
        <BackToListButton to="/purchase-orders" />
        {permissions.canCancel(order) && (
          <Button size="sm" variant="outline" onClick={() => setShowCancel(true)} disabled={actionLoading}>
            Cancel Order
          </Button>
        )}
        {permissions.canCloseShort(order) && (
          <Button size="sm" variant="outline" onClick={handleCloseShort} disabled={actionLoading}>
            Close Short
          </Button>
        )}
        {permissions.canTrackDelivery(order) && (
          <Button size="sm" variant="outline" onClick={() => setShowDelivery(true)} disabled={actionLoading}>
            Delivery Status
          </Button>
        )}
        {permissions.canReject(order) && (
          <Button size="sm" variant="outline" onClick={() => setShowReject(true)} disabled={actionLoading}>
            Supplier Rejected
          </Button>
        )}
        {permissions.canEdit(order) && (
          <Link to={`/purchase-orders/${order.id}/edit`}>
            <Button size="sm" variant="outline" disabled={actionLoading}>
              Edit
            </Button>
          </Link>
        )}
        {invoicePermissions.canRecord && INVOICEABLE_ORDER_STATUSES.includes(order.status) && (
          <Link to={`/invoices/create?purchaseOrderId=${order.id}`}>
            <Button size="sm" variant="outline" disabled={actionLoading}>
              Record Invoice
            </Button>
          </Link>
        )}
        {permissions.canAssignReceiver(order) && (
          <Button size="sm" onClick={openAssign} disabled={actionLoading}>
            Assign Receiver
          </Button>
        )}
        {permissions.canSubmit(order) && (
          <Button size="sm" onClick={handleSubmit} disabled={actionLoading}>
            Send to Supplier
          </Button>
        )}
        {permissions.canConfirm(order) && (
          <Button size="sm" onClick={handleConfirm} disabled={actionLoading}>
            Record Confirmation
          </Button>
        )}
        {permissions.canReceive(order) && (
          <>
            <Link to={`/goods-receipts/create?purchaseOrderId=${order.id}`}>
              <Button size="sm" variant="outline" disabled={actionLoading}>
                Record Receipt
              </Button>
            </Link>
            <Button size="sm" onClick={handleReceiveAll} disabled={actionLoading}>
              Receive All
            </Button>
          </>
        )}
      </DetailHeader>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="flex flex-col gap-6 lg:col-span-1">
          <DetailCard>
            <div className="mb-6 flex items-center justify-between">
              <InitialsAvatar name={order.supplierName} size="lg" />
              <div className="flex flex-col items-end gap-2">
                <PurchaseOrderStatusBadge status={order.status} size="md" />
                <PurchaseOrderDeliveryStatusBadge status={order.deliveryStatus} />
              </div>
            </div>
            <h3 className="mb-1 font-mono text-xl font-bold text-gray-900 dark:text-white">{order.orderCode}</h3>
            <p className="mb-6 text-sm font-medium text-gray-500 dark:text-gray-400">{order.supplierName}</p>
            <div className="space-y-4">
              <DetailField label="Supplier">
                <Link to={`/suppliers/${order.supplierId}`} className="text-brand-500 hover:underline">
                  {order.supplierCode || "View supplier"}
                </Link>
              </DetailField>
              <DetailField label="Requisition">
                {order.requisitionId ? (
                  <Link to={`/requisitions/${order.requisitionId}`} className="font-mono text-brand-500 hover:underline">
                    {order.requisitionCode || order.requisitionId}
                  </Link>
                ) : (
                  "—"
                )}
              </DetailField>
              <DetailField label="Ordered On">{order.orderDate}</DetailField>
            </div>
          </DetailCard>

          <DetailCard title="Delivery & Terms" icon={SectionIcons.calendar}>
            <div className="space-y-4">
              <DetailField label="Expected Delivery">{order.expectedDeliveryDate || "—"}</DetailField>
              <DetailField label="Confirmed Delivery">{order.confirmedDeliveryDate || "—"}</DetailField>
              <DetailField label="Received On">{order.receivedDate || "—"}</DetailField>
              <DetailField label="Payment Terms">
                {order.paymentTerms || "—"}
                {order.paymentDelayDays ? ` (${order.paymentDelayDays} days)` : ""}
              </DetailField>
              <DetailField label="Incoterm">{order.incoterm || order.deliveryTerms || "—"}</DetailField>
            </div>
          </DetailCard>
        </div>

        <div className="flex flex-col gap-6 lg:col-span-2">
          <DetailCard title="Amounts" icon={SectionIcons.money} tone="success">
            <DetailGrid>
              <DetailField label="Subtotal excl. Tax">{formatAmount(order.totalAmount, currency)}</DetailField>
              <DetailField label="Tax">{order.taxAmount != null ? formatAmount(order.taxAmount, currency) : "—"}</DetailField>
              <DetailField label="Shipping">{order.shippingCost != null ? formatAmount(order.shippingCost, currency) : "—"}</DetailField>
              <DetailField label="Grand Total">
                <span className="font-semibold text-brand-500">{formatAmount(order.grandTotal, currency)}</span>
              </DetailField>
            </DetailGrid>
          </DetailCard>

          <DetailCard title={`Order Lines (${lines.length})`} icon={SectionIcons.list} tone="brand" padded={false}>
            <div className="max-w-full overflow-x-auto">
              <Table>
                <TableHeader className="border-b border-gray-100 bg-gray-50/50 dark:border-white/[0.05] dark:bg-gray-900/50">
                  <TableRow>
                    <TableCell isHeader className={HEAD_CELL}>#</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Material</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Quantity</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Unit Price</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Line Total</TableCell>
                  </TableRow>
                </TableHeader>
                <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {lines.map((l, i) => (
                    <TableRow key={l.id || i}>
                      <TableCell className={BODY_CELL}>{l.lineNumber ?? i + 1}</TableCell>
                      <TableCell className={BODY_CELL}>
                        <StackedCell main={<span className="font-mono">{l.materialCode}</span>} sub={l.materialName || l.materialDescription} />
                      </TableCell>
                      <TableCell className={BODY_CELL}>{`${l.quantity} ${l.unitOfMeasure ?? ""}`.trim()}</TableCell>
                      <TableCell className={`${BODY_CELL} whitespace-nowrap`}>{formatAmount(l.unitPrice, l.currencyCode || currency)}</TableCell>
                      <TableCell className={`${BODY_CELL} whitespace-nowrap`}>{formatAmount(l.lineTotal, l.currencyCode || currency)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </DetailCard>

          <DetailCard title="People" icon={SectionIcons.info}>
            <DetailGrid>
              <DetailField label="Ordered By">{order.orderedByName || order.orderedBy || "—"}</DetailField>
              <DetailField label="Approved By">{order.approvedByName || order.approvedBy || "—"}</DetailField>
              <DetailField label="Receiver">{order.assignedToName || order.assignedTo || "Not assigned yet"}</DetailField>
              <DetailField label="Assigned On">{formatDateTime(order.assignedAt)}</DetailField>
            </DetailGrid>
          </DetailCard>

          <DetailCard title="System Information" icon={SectionIcons.info}>
            <DetailGrid>
              <DetailField label="Created">{formatDateTime(order.createdAt)}</DetailField>
              <DetailField label="Created By">{order.createdBy || "—"}</DetailField>
              <DetailField label="Last Updated">{formatDateTime(order.updatedAt)}</DetailField>
              <DetailField label="Updated By">{order.updatedBy || "—"}</DetailField>
            </DetailGrid>
          </DetailCard>

          {(order.notes || order.internalNotes) && (
            <DetailCard title="Notes" icon={SectionIcons.note}>
              <div className="space-y-4">
                {order.notes && <DetailField label="For the Supplier">{order.notes}</DetailField>}
                {order.internalNotes && <DetailField label="Internal">{order.internalNotes}</DetailField>}
              </div>
            </DetailCard>
          )}
        </div>
      </div>

      {showCancel && (
        <PurchaseOrderCancelModal isOpen onClose={() => setShowCancel(false)} order={order} onConfirm={handleCancel} isLoading={actionLoading} />
      )}

      {showAssign && (
        <Modal isOpen onClose={() => setShowAssign(false)} className="max-w-md p-6">
          <form onSubmit={handleAssign} className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Assign Receiver</h3>
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">Who will receive {order.orderCode} at the warehouse?</p>
            </div>
            <div>
              <Label>Receiver *</Label>
              <select value={assignedUserId} onChange={(e) => setAssignedUserId(e.target.value)} disabled={receiversLoading} className={SELECT_CLASS}>
                <option value="">{receiversLoading ? "Loading..." : "Select a receiver"}</option>
                {receivers.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.name} ({r.email})
                  </option>
                ))}
              </select>
              {!receiversLoading && receivers.length === 0 && (
                <p className="mt-1.5 text-theme-xs text-warning-600 dark:text-orange-400">No active receiver is available.</p>
              )}
            </div>
            <div className="flex justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowAssign(false)}>
                Back
              </Button>
              <Button size="sm" type="submit" disabled={actionLoading || !assignedUserId}>
                Confirm Assignment
              </Button>
            </div>
          </form>
        </Modal>
      )}

      {showReject && (
        <Modal isOpen onClose={() => setShowReject(false)} className="max-w-md p-6">
          <form onSubmit={handleReject} className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Supplier Rejected the Order</h3>
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                The requisition behind {order.orderCode} becomes available again for a new order.
              </p>
            </div>
            <div>
              <Label>Reason *</Label>
              <TextArea rows={3} value={rejectReason} onChange={setRejectReason} placeholder="What did the supplier say?" />
            </div>
            <div className="flex justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowReject(false)}>
                Back
              </Button>
              <Button size="sm" type="submit" disabled={actionLoading || !rejectReason.trim()}>
                Record Rejection
              </Button>
            </div>
          </form>
        </Modal>
      )}

      {showDelivery && (
        <Modal isOpen onClose={() => setShowDelivery(false)} className="max-w-md p-6">
          <form onSubmit={handleDelivery} className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Delivery Status</h3>
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Track the shipment. Partial and delivered statuses follow from goods receipts.
              </p>
            </div>
            <div>
              <Label>Status</Label>
              <select value={deliveryStatus} onChange={(e) => setDeliveryStatus(e.target.value as DeliveryStatus)} className={SELECT_CLASS}>
                {TRACKABLE_DELIVERY.map((s) => (
                  <option key={s} value={s}>
                    {DELIVERY_STATUS_INFO[s].label}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowDelivery(false)}>
                Back
              </Button>
              <Button size="sm" type="submit" disabled={actionLoading}>
                Save
              </Button>
            </div>
          </form>
        </Modal>
      )}
    </>
  );
}
