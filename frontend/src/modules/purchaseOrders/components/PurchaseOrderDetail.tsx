import React, { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import purchaseOrderService from "../services/purchaseOrderService";
import type { PurchaseOrder, DeliveryStatus, AssignableReceiver } from "../types";
import PurchaseOrderStatusBadge, {
  PurchaseOrderDeliveryStatusBadge,
} from "./PurchaseOrderStatusBadge";
import PurchaseOrderCancelModal from "./PurchaseOrderCancelModal";
import Button from "../../../shared/components/ui/button/Button";
import { Modal } from "../../../shared/components/ui/modal";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../../shared/components/ui/table";
import useAuth from "../../auth/hooks/useAuth";
import usePurchaseOrderPermissions from "../hooks/usePurchaseOrder";

export default function PurchaseOrderDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const permissions = usePurchaseOrderPermissions();

  const [order, setOrder] = useState<PurchaseOrder | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Modals state
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [showDeliveryModal, setShowDeliveryModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);

  // Form states for modals
  const [assignedUserId, setAssignedUserId] = useState("");
  const [receivers, setReceivers] = useState<AssignableReceiver[]>([]);
  const [receiversLoading, setReceiversLoading] = useState(false);
  const [rejectReason, setRejectReason] = useState("");
  const [newDeliveryStatus, setNewDeliveryStatus] = useState<DeliveryStatus>("IN_TRANSIT");

  // Feedback toast
  const [feedback, setFeedback] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);

  useEffect(() => {
    if (id) {
      loadOrder(id);
    }
  }, [id]);

  const loadOrder = async (orderId: string) => {
    try {
      setLoading(true);
      setError(null);
      const res = await purchaseOrderService.getById(orderId);
      setOrder(res.data);
    } catch (err: any) {
      console.error("Failed to load purchase order details:", err);
      setError(
        err?.response?.data?.message ||
          "Impossible de récupérer les détails du bon de commande. Veuillez vérifier l'identifiant."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async (
    actionName: string,
    actionFn: () => Promise<any>,
    successMsg: string
  ) => {
    try {
      setActionLoading(true);
      const res = await actionFn();
      if (res?.data) {
        setOrder(res.data);
      } else {
        if (id) await loadOrder(id);
      }
      setFeedback({ type: "success", text: successMsg });
    } catch (err: any) {
      console.error(`Action ${actionName} failed:`, err);
      setFeedback({
        type: "error",
        text: err?.response?.data?.message || `L'action ${actionName} a échoué.`,
      });
    } finally {
      setActionLoading(false);
    }
  };

  const handleQuickSubmit = () => {
    if (!order) return;
    handleAction(
      "Soumettre",
      () => purchaseOrderService.submit(order.id, { userId: user?.id || "CURRENT_USER" }),
      `La commande ${order.orderCode} a été soumise au fournisseur.`
    );
  };

  const handleQuickConfirm = () => {
    if (!order) return;
    handleAction(
      "Confirmer",
      () => purchaseOrderService.confirm(order.id, { userId: user?.id || "CURRENT_USER" }),
      `La commande ${order.orderCode} a été confirmée par le fournisseur.`
    );
  };

  const openAssignModal = async () => {
    setShowAssignModal(true);
    setReceiversLoading(true);
    try {
      const res = await purchaseOrderService.getAssignableReceivers();
      setReceivers(res.data);
    } catch (err) {
      console.error("Failed to load receivers:", err);
      setReceivers([]);
    } finally {
      setReceiversLoading(false);
    }
  };

  const handleAssignReceiver = async (e: React.FormEvent) => {
    e.preventDefault();
    const receiver = receivers.find((r) => r.id === assignedUserId);
    if (!order || !receiver) return;

    await handleAction(
      "Assigner",
      () =>
        purchaseOrderService.assignReceiver(order.id, {
          userId: user?.id || "CURRENT_USER",
          userName: user?.name || user?.email || "Acheteur",
          assignedUserId: receiver.id,
          assignedUserName: receiver.name,
        }),
      `La commande ${order.orderCode} a été assignée à ${receiver.name}.`
    );
    setShowAssignModal(false);
    setAssignedUserId("");
  };

  const handleReject = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!order || !rejectReason.trim()) return;
    await handleAction(
      "Rejeter",
      () => purchaseOrderService.reject(order.id, { reason: rejectReason.trim() }),
      `Le rejet de la commande ${order.orderCode} par le fournisseur a été enregistré.`
    );
    setShowRejectModal(false);
    setRejectReason("");
  };

  const handleCloseShort = () => {
    if (!order) return;
    if (!window.confirm(`Clôturer la commande ${order.orderCode} ? Les quantités restantes ne seront plus attendues.`)) {
      return;
    }
    handleAction(
      "Clôturer",
      () => purchaseOrderService.complete(order.id, { userId: user?.id || "CURRENT_USER" }),
      `La commande ${order.orderCode} a été clôturée.`
    );
  };

  const handleConfirmReceipt = () => {
    if (!order) return;
    handleAction(
      "Confirmer Réception",
      () =>
        purchaseOrderService.confirmReceipt(order.id, {
          receiverId: user?.id || "CURRENT_USER",
          receiverName: user?.name || user?.email || "Magasinier",
        }),
      `La réception de la commande ${order.orderCode} a été confirmée.`
    );
  };

  const handleUpdateDeliveryStatus = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!order) return;

    await handleAction(
      "Statut Livraison",
      () =>
        purchaseOrderService.updateDeliveryStatus(order.id, {
          deliveryStatus: newDeliveryStatus,
          userId: user?.id || "CURRENT_USER",
        }),
      `Le statut de livraison a été mis à jour vers ${newDeliveryStatus}.`
    );
    setShowDeliveryModal(false);
  };

  const handleCancelOrder = async (reason: string) => {
    if (!order) return;
    await handleAction(
      "Annuler",
      () =>
        purchaseOrderService.cancel(order.id, {
          userId: user?.id || "CURRENT_USER",
          reason,
        }),
      `La commande ${order.orderCode} a été annulée avec succès.`
    );
  };

  const formatAmount = (amt: string | number, curr = "MAD") => {
    if (amt === undefined || amt === null) return `0.00 ${curr}`;
    const str = String(amt).trim();
    let detectedCurr = curr;
    if (!curr || curr === "MAD") {
      const upper = str.toUpperCase();
      if (upper.includes("EUR") || upper.includes("€") || upper.includes("â‚¬")) {
        detectedCurr = "EUR";
      } else if (upper.includes("USD") || upper.includes("$")) {
        detectedCurr = "USD";
      } else if (upper.includes("MAD") || upper.includes("DH")) {
        detectedCurr = "MAD";
      }
    }

    let cleaned = str.replace(/[^0-9.,-]+/g, "");
    if (cleaned.includes(",") && cleaned.includes(".")) {
      if (cleaned.indexOf(",") < cleaned.indexOf(".")) {
        cleaned = cleaned.replace(/,/g, "");
      } else {
        cleaned = cleaned.replace(/\./g, "").replace(/,/g, ".");
      }
    } else if (cleaned.includes(",")) {
      cleaned = cleaned.replace(/,/g, ".");
    }
    const num = parseFloat(cleaned);
    const validNum = isNaN(num) ? 0 : num;
    return `${new Intl.NumberFormat("fr-FR", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(validNum)} ${detectedCurr}`;
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-16 space-y-4">
        <div className="size-10 border-4 border-brand-500 border-t-transparent rounded-full animate-spin" />
        <p className="text-xs text-gray-500 dark:text-gray-400 font-medium">
          Chargement du bon de commande...
        </p>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="p-8 rounded-2xl border border-red-200 bg-red-50 dark:border-red-500/20 dark:bg-red-500/10 text-center max-w-lg mx-auto mt-6">
        <div className="size-12 rounded-full bg-red-100 text-red-600 dark:bg-red-500/20 dark:text-red-400 flex items-center justify-center mx-auto mb-3">
          <svg className="size-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <h3 className="text-base font-bold text-gray-900 dark:text-white mb-1">
          Bon de commande introuvable
        </h3>
        <p className="text-xs text-red-700 dark:text-red-300 mb-4">{error}</p>
        <Button variant="outline" size="sm" onClick={() => navigate("/purchase-orders")}>
          Retour à la liste des commandes
        </Button>
      </div>
    );
  }

  const lines = order.lines || [];

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Feedback Toast */}
      {feedback && (
        <div
          className={`flex items-center justify-between p-4 rounded-xl text-xs font-medium border transition-all ${
            feedback.type === "success"
              ? "bg-emerald-50 text-emerald-800 border-emerald-200 dark:bg-emerald-500/10 dark:text-emerald-300 dark:border-emerald-500/20"
              : "bg-red-50 text-red-800 border-red-200 dark:bg-red-500/10 dark:text-red-300 dark:border-red-500/20"
          }`}
        >
          <div className="flex items-center gap-2">
            <span>{feedback.text}</span>
          </div>
          <button type="button" onClick={() => setFeedback(null)} className="hover:opacity-75">
            <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      )}

      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm">
        <div className="space-y-1">
          <div className="flex flex-wrap items-center gap-2.5">
            <Link
              to="/purchase-orders"
              className="p-1 rounded-lg text-gray-400 hover:text-gray-700 dark:hover:text-white transition-colors"
              title="Retour aux commandes"
            >
              <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
            </Link>
            <h1 className="text-xl font-bold text-gray-900 dark:text-white">
              {order.orderCode}
            </h1>
            <PurchaseOrderStatusBadge status={order.status} size="md" />
            <PurchaseOrderDeliveryStatusBadge status={order.deliveryStatus} size="md" />
          </div>
          <p className="text-xs text-gray-500 dark:text-gray-400">
            Fournisseur :{" "}
            <strong className="text-gray-800 dark:text-white">
              {order.supplierName}
            </strong>{" "}
            • Émise le {order.orderDate}
            {order.requisitionCode && ` • Demande DA: ${order.requisitionCode}`}
          </p>
        </div>

        {/* Action Controls */}
        <div className="flex flex-wrap items-center gap-2">
          {permissions.canEdit(order) && (
            <Link to={`/purchase-orders/${order.id}/edit`}>
              <Button size="sm" variant="outline" disabled={actionLoading}>
                Modifier
              </Button>
            </Link>
          )}

          {permissions.canSubmit(order) && (
            <Button size="sm" onClick={handleQuickSubmit} disabled={actionLoading}>
              Soumettre au Fournisseur
            </Button>
          )}

          {permissions.canConfirm(order) && (
            <Button size="sm" onClick={handleQuickConfirm} disabled={actionLoading}>
              Confirmer par le Fournisseur
            </Button>
          )}

          {permissions.canReject(order) && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => setShowRejectModal(true)}
              disabled={actionLoading}
              className="text-rose-600 border-rose-200 hover:bg-rose-50 dark:border-rose-500/20"
            >
              Rejet Fournisseur
            </Button>
          )}

          {permissions.canAssignReceiver(order) && (
            <Button size="sm" variant="outline" onClick={openAssignModal} disabled={actionLoading}>
              Assigner Réceptionnaire
            </Button>
          )}

          {permissions.canReceive(order) && (
            <>
              <Link to={`/goods-receipts/create?purchaseOrderId=${order.id}`}>
                <Button size="sm" variant="outline" disabled={actionLoading}>
                  Saisir une Réception
                </Button>
              </Link>
              <Button
                size="sm"
                onClick={handleConfirmReceipt}
                disabled={actionLoading}
                className="bg-emerald-600 hover:bg-emerald-700 text-white"
              >
                Tout Réceptionner
              </Button>
            </>
          )}

          {permissions.canTrackDelivery(order) && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => setShowDeliveryModal(true)}
              disabled={actionLoading}
            >
              Statut Livraison
            </Button>
          )}

          {permissions.canCloseShort(order) && (
            <Button size="sm" variant="outline" onClick={handleCloseShort} disabled={actionLoading}>
              Clôturer (reliquat abandonné)
            </Button>
          )}

          {permissions.canCancel(order) && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => setShowCancelModal(true)}
              disabled={actionLoading}
              className="text-rose-600 border-rose-200 hover:bg-rose-50 dark:border-rose-500/20"
            >
              Annuler Commande
            </Button>
          )}
        </div>
      </div>

      {/* Main Grid Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Line items and financials */}
        <div className="lg:col-span-2 space-y-6">
          {/* Line items table */}
          <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-white/[0.07] overflow-hidden shadow-sm">
            <div className="p-5 border-b border-gray-100 dark:border-white/[0.05] flex items-center justify-between">
              <h3 className="text-sm font-bold text-gray-800 dark:text-white flex items-center gap-2">
                <svg className="size-4 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 10h16M4 14h16M4 18h16" />
                </svg>
                Articles Commandés ({lines.length})
              </h3>
              <span className="text-xs text-gray-500 dark:text-gray-400">
                Devise : <strong className="text-gray-800 dark:text-white">{order.currencyCode}</strong>
              </span>
            </div>

            <div className="overflow-x-auto">
              <Table>
                <TableHeader className="bg-gray-50/50 dark:bg-gray-800/20 border-b border-gray-100 dark:border-white/[0.05]">
                  <TableRow>
                    <TableCell isHeader className="py-2.5 px-4 text-xs font-semibold text-gray-500">
                      Ligne
                    </TableCell>
                    <TableCell isHeader className="py-2.5 px-4 text-xs font-semibold text-gray-500">
                      Code Matériel
                    </TableCell>
                    <TableCell isHeader className="py-2.5 px-4 text-xs font-semibold text-gray-500">
                      Désignation
                    </TableCell>
                    <TableCell isHeader className="py-2.5 px-4 text-xs font-semibold text-gray-500 text-right">
                      Quantité
                    </TableCell>
                    <TableCell isHeader className="py-2.5 px-4 text-xs font-semibold text-gray-500 text-right">
                      Prix Unitaire
                    </TableCell>
                    <TableCell isHeader className="py-2.5 px-4 text-xs font-semibold text-gray-500 text-right">
                      Total Ligne
                    </TableCell>
                  </TableRow>
                </TableHeader>
                <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {lines.map((l, i) => (
                    <TableRow key={l.id || i}>
                      <TableCell className="py-3 px-4 text-xs text-gray-500">
                        #{l.lineNumber ?? i + 1}
                      </TableCell>
                      <TableCell className="py-3 px-4 text-xs font-semibold text-brand-600 dark:text-brand-400">
                        {l.materialCode}
                      </TableCell>
                      <TableCell className="py-3 px-4 text-xs text-gray-700 dark:text-gray-300">
                        {l.materialName || l.materialDescription || "-"}
                      </TableCell>
                      <TableCell className="py-3 px-4 text-xs text-right font-medium text-gray-800 dark:text-white">
                        {l.quantity} {l.unitOfMeasure || "U"}
                      </TableCell>
                      <TableCell className="py-3 px-4 text-xs text-right text-gray-600 dark:text-gray-300">
                        {formatAmount(l.unitPrice, l.currencyCode || order.currencyCode)}
                      </TableCell>
                      <TableCell className="py-3 px-4 text-xs text-right font-bold text-gray-900 dark:text-white">
                        {formatAmount(l.lineTotal, l.currencyCode || order.currencyCode)}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>

            {/* Financial Summary Card */}
            <div className="p-5 bg-gray-50/60 dark:bg-gray-800/20 border-t border-gray-100 dark:border-white/[0.05]">
              <div className="flex flex-col items-end space-y-1.5 text-xs">
                <div className="flex justify-between w-64 text-gray-600 dark:text-gray-400">
                  <span>Sous-total HT :</span>
                  <span className="font-semibold text-gray-800 dark:text-gray-200">
                    {formatAmount(order.totalAmount, order.currencyCode)}
                  </span>
                </div>
                {order.taxAmount != null && (
                  <div className="flex justify-between w-64 text-gray-600 dark:text-gray-400">
                    <span>TVA / Taxes :</span>
                    <span className="font-semibold text-gray-800 dark:text-gray-200">
                      {formatAmount(order.taxAmount, order.currencyCode)}
                    </span>
                  </div>
                )}
                {order.shippingCost != null && (
                  <div className="flex justify-between w-64 text-gray-600 dark:text-gray-400">
                    <span>Frais de livraison :</span>
                    <span className="font-semibold text-gray-800 dark:text-gray-200">
                      {formatAmount(order.shippingCost, order.currencyCode)}
                    </span>
                  </div>
                )}
                <div className="flex justify-between w-64 pt-2 border-t border-gray-200 dark:border-white/[0.1] text-sm font-bold text-gray-900 dark:text-white">
                  <span>Total TTC :</span>
                  <span className="text-brand-600 dark:text-brand-400">
                    {formatAmount(order.grandTotal, order.currencyCode)}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Notes Card */}
          {(order.notes || order.internalNotes) && (
            <div className="bg-white dark:bg-gray-900 p-5 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-3">
              <h4 className="text-xs font-bold text-gray-800 dark:text-white uppercase tracking-wider">
                Notes & Remarques
              </h4>
              {order.notes && (
                <div>
                  <span className="text-[11px] text-gray-400 font-medium block">
                    Remarques destinées au fournisseur :
                  </span>
                  <p className="text-xs text-gray-700 dark:text-gray-300 italic bg-gray-50 dark:bg-gray-800/40 p-3 rounded-xl mt-1">
                    "{order.notes}"
                  </p>
                </div>
              )}
              {order.internalNotes && (
                <div>
                  <span className="text-[11px] text-gray-400 font-medium block">
                    Remarques internes :
                  </span>
                  <p className="text-xs text-gray-700 dark:text-gray-300 italic bg-gray-50 dark:bg-gray-800/40 p-3 rounded-xl mt-1">
                    "{order.internalNotes}"
                  </p>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Right Column: Metadata & stakeholders */}
        <div className="space-y-6">
          {/* Supplier details card */}
          <div className="bg-white dark:bg-gray-900 p-5 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-3">
            <h4 className="text-xs font-bold text-gray-800 dark:text-white uppercase tracking-wider flex items-center gap-2">
              <svg className="size-4 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
              Fournisseur
            </h4>
            <div className="space-y-1 text-xs">
              <div className="font-semibold text-gray-900 dark:text-white text-sm">
                {order.supplierName}
              </div>
              {order.supplierCode && (
                <div className="text-gray-500 dark:text-gray-400">
                  Code Fournisseur : <strong className="text-gray-700 dark:text-gray-300">{order.supplierCode}</strong>
                </div>
              )}
              <Link
                to={`/suppliers/${order.supplierId}`}
                className="inline-block pt-1 text-xs text-brand-600 dark:text-brand-400 hover:underline font-medium"
              >
                Voir fiche fournisseur →
              </Link>
            </div>
          </div>

          {/* Delivery & Terms Card */}
          <div className="bg-white dark:bg-gray-900 p-5 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-3">
            <h4 className="text-xs font-bold text-gray-800 dark:text-white uppercase tracking-wider flex items-center gap-2">
              <svg className="size-4 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              Livraison & Conditions
            </h4>
            <div className="space-y-2 text-xs divide-y divide-gray-100 dark:divide-white/[0.05]">
              <div className="pt-1.5 flex justify-between">
                <span className="text-gray-500">Date Prévue :</span>
                <span className="font-semibold text-gray-800 dark:text-white">
                  {order.expectedDeliveryDate || "-"}
                </span>
              </div>
              <div className="pt-1.5 flex justify-between">
                <span className="text-gray-500">Date Confirmée :</span>
                <span className="font-semibold text-gray-800 dark:text-white">
                  {order.confirmedDeliveryDate || "-"}
                </span>
              </div>
              <div className="pt-1.5 flex justify-between">
                <span className="text-gray-500">Date Réception :</span>
                <span className="font-semibold text-gray-800 dark:text-white">
                  {order.receivedDate || "-"}
                </span>
              </div>
              <div className="pt-1.5 flex justify-between">
                <span className="text-gray-500">Paiement :</span>
                <span className="font-semibold text-gray-800 dark:text-white">
                  {order.paymentTerms || "-"}
                  {order.paymentDelayDays ? ` (${order.paymentDelayDays}j)` : ""}
                </span>
              </div>
              <div className="pt-1.5 flex justify-between">
                <span className="text-gray-500">Incoterm :</span>
                <span className="font-semibold text-gray-800 dark:text-white">
                  {order.incoterm || order.deliveryTerms || "-"}
                </span>
              </div>
            </div>
          </div>

          {/* Stakeholders & Assignment Card */}
          <div className="bg-white dark:bg-gray-900 p-5 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-3">
            <h4 className="text-xs font-bold text-gray-800 dark:text-white uppercase tracking-wider flex items-center gap-2">
              <svg className="size-4 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              Responsables & Réception
            </h4>
            <div className="space-y-2 text-xs divide-y divide-gray-100 dark:divide-white/[0.05]">
              <div className="pt-1.5 flex justify-between">
                <span className="text-gray-500">Commandé par :</span>
                <span className="font-semibold text-gray-800 dark:text-white">
                  {order.orderedByName || order.orderedBy || "-"}
                </span>
              </div>
              <div className="pt-1.5 flex justify-between">
                <span className="text-gray-500">Approuvé par :</span>
                <span className="font-semibold text-gray-800 dark:text-white">
                  {order.approvedByName || order.approvedBy || "-"}
                </span>
              </div>
              <div className="pt-1.5 flex justify-between">
                <span className="text-gray-500">Assigné à (réception) :</span>
                <span className="font-semibold text-brand-600 dark:text-brand-400">
                  {order.assignedToName || order.assignedTo || "Non assigné"}
                </span>
              </div>
              {order.assignedAt && (
                <div className="pt-1.5 flex justify-between">
                  <span className="text-gray-500">Assigné le :</span>
                  <span className="text-gray-700 dark:text-gray-300">
                    {order.assignedAt}
                  </span>
                </div>
              )}
            </div>
          </div>

          {/* Audit trail */}
          <div className="bg-gray-50/75 dark:bg-gray-800/40 p-4 rounded-2xl border border-gray-100 dark:border-white/[0.05] text-[11px] text-gray-400 space-y-1">
            <div>Créé par : {order.createdBy} le {order.createdAt}</div>
            {order.updatedAt && (
              <div>Dernière modification : {order.updatedBy || "-"} le {order.updatedAt}</div>
            )}
          </div>
        </div>
      </div>

      {/* Cancel Order Modal */}
      {showCancelModal && (
        <PurchaseOrderCancelModal
          isOpen={showCancelModal}
          onClose={() => setShowCancelModal(false)}
          order={order}
          onConfirm={handleCancelOrder}
          isLoading={actionLoading}
        />
      )}

      {/* Assign Receiver Modal */}
      {showAssignModal && (
        <Modal
          isOpen={showAssignModal}
          onClose={() => setShowAssignModal(false)}
          className="max-w-md p-6"
        >
          <div className="space-y-4">
            <h3 className="text-base font-bold text-gray-900 dark:text-white">
              Assigner un Réceptionnaire
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Désignez l'agent d'entrepôt responsable de la réception physique de la commande {order.orderCode}.
            </p>

            <form onSubmit={handleAssignReceiver} className="space-y-3">
              <div>
                <label className="block mb-1 text-xs font-semibold text-gray-700 dark:text-gray-300">
                  Réceptionnaire <span className="text-red-500">*</span>
                </label>
                <select
                  required
                  value={assignedUserId}
                  onChange={(e) => setAssignedUserId(e.target.value)}
                  disabled={receiversLoading}
                  className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
                >
                  <option value="">
                    {receiversLoading ? "Chargement..." : "Sélectionnez un réceptionnaire"}
                  </option>
                  {receivers.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.name} ({r.email})
                    </option>
                  ))}
                </select>
                {!receiversLoading && receivers.length === 0 && (
                  <p className="mt-1 text-[11px] text-amber-600">
                    Aucun réceptionnaire actif n'est disponible.
                  </p>
                )}
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t border-gray-100 dark:border-white/[0.05]">
                <Button variant="outline" size="sm" onClick={() => setShowAssignModal(false)}>
                  Annuler
                </Button>
                <Button size="sm" type="submit" disabled={actionLoading || !assignedUserId}>
                  {actionLoading ? "Assignation..." : "Confirmer l'assignation"}
                </Button>
              </div>
            </form>
          </div>
        </Modal>
      )}

      {/* Supplier Rejection Modal */}
      {showRejectModal && (
        <Modal
          isOpen={showRejectModal}
          onClose={() => setShowRejectModal(false)}
          className="max-w-md p-6"
        >
          <div className="space-y-4">
            <h3 className="text-base font-bold text-gray-900 dark:text-white">
              Rejet par le Fournisseur
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Enregistrez le refus de la commande {order.orderCode} par le fournisseur. La demande
              d'achat d'origine redeviendra disponible pour une nouvelle commande.
            </p>
            <form onSubmit={handleReject} className="space-y-3">
              <div>
                <label className="block mb-1 text-xs font-semibold text-gray-700 dark:text-gray-300">
                  Motif du rejet <span className="text-red-500">*</span>
                </label>
                <textarea
                  required
                  maxLength={500}
                  rows={3}
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
                />
              </div>
              <div className="flex justify-end gap-2 pt-3 border-t border-gray-100 dark:border-white/[0.05]">
                <Button variant="outline" size="sm" onClick={() => setShowRejectModal(false)}>
                  Annuler
                </Button>
                <Button size="sm" type="submit" disabled={actionLoading || !rejectReason.trim()}>
                  {actionLoading ? "Enregistrement..." : "Enregistrer le rejet"}
                </Button>
              </div>
            </form>
          </div>
        </Modal>
      )}

      {/* Update Delivery Status Modal */}
      {showDeliveryModal && (
        <Modal
          isOpen={showDeliveryModal}
          onClose={() => setShowDeliveryModal(false)}
          className="max-w-md p-6"
        >
          <div className="space-y-4">
            <h3 className="text-base font-bold text-gray-900 dark:text-white">
              Mettre à jour le Statut de Livraison
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Modifiez l'état d'expédition de la commande auprès du transporteur.
            </p>

            <form onSubmit={handleUpdateDeliveryStatus} className="space-y-3">
              <div>
                <label className="block mb-1 text-xs font-semibold text-gray-700 dark:text-gray-300">
                  Statut Transport / Livraison
                </label>
                <select
                  value={newDeliveryStatus}
                  onChange={(e) => setNewDeliveryStatus(e.target.value as DeliveryStatus)}
                  className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
                >
                  <option value="NOT_SHIPPED">Non expédié (NOT_SHIPPED)</option>
                  <option value="SHIPPED">Expédié (SHIPPED)</option>
                  <option value="IN_TRANSIT">En transit (IN_TRANSIT)</option>
                  <option value="DELAYED">Retardée (DELAYED)</option>
                </select>
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t border-gray-100 dark:border-white/[0.05]">
                <Button variant="outline" size="sm" onClick={() => setShowDeliveryModal(false)}>
                  Annuler
                </Button>
                <Button size="sm" type="submit" disabled={actionLoading}>
                  {actionLoading ? "Mise à jour..." : "Enregistrer"}
                </Button>
              </div>
            </form>
          </div>
        </Modal>
      )}
    </div>
  );
}
