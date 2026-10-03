import React, { useState, useEffect } from "react";
import { useNavigate, Link, useSearchParams } from "react-router-dom";
import purchaseOrderService from "../services/purchaseOrderService";
import { supplierApi } from "../../suppliers/services/supplierApi";
import { materialApi } from "../../materials/services/materialApi";
import { requisitionApi } from "../../requisitions/services/requisitionApi";
import type { Requisition } from "../../requisitions/types";
import type { CreatePurchaseOrderRequest, PurchaseOrder, PurchaseOrderLineRequest } from "../types";
import { EDITABLE_STATUSES } from "../hooks/usePurchaseOrder";
import { getApiErrorMessage } from "../../../shared/utils/apiError";
import { parseAmount } from "../../../shared/utils/moneyUtils";
import type { SupplierListItem } from "../../suppliers/types/SupplierListItem";
import type { MaterialListItem } from "../../materials/types/MaterialListItem";
import Button from "../../../shared/components/ui/button/Button";
import useAuth from "../../auth/hooks/useAuth";

interface LocalLineItem extends PurchaseOrderLineRequest {
  tempId: string;
}

interface PurchaseOrderFormProps {
  /** When set, the form edits this draft or submitted order instead of creating a new one. */
  purchaseOrderId?: string;
}

export default function PurchaseOrderForm({ purchaseOrderId }: PurchaseOrderFormProps) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const isEdit = !!purchaseOrderId;
  const fromRequisitionId = isEdit
    ? null
    : searchParams.get("fromRequisition") || searchParams.get("requisitionId");
  const { user } = useAuth();

  // Suppliers & Materials data
  const [suppliers, setSuppliers] = useState<SupplierListItem[]>([]);
  const [materials, setMaterials] = useState<MaterialListItem[]>([]);

  // Order being edited (edit mode only)
  const [editedOrderCode, setEditedOrderCode] = useState<string | null>(null);
  const [editedRequisitionCode, setEditedRequisitionCode] = useState<string | null>(null);
  const [loadingOrder, setLoadingOrder] = useState(isEdit);

  // Originating requisition data (if converting)
  const [originRequisition, setOriginRequisition] = useState<Requisition | null>(null);
  const [loadingRequisition, setLoadingRequisition] = useState(!!fromRequisitionId);

  // Form Fields
  const [supplierId, setSupplierId] = useState("");
  const [supplierName, setSupplierName] = useState("");
  const [supplierCode, setSupplierCode] = useState("");
  const [orderDate, setOrderDate] = useState(
    new Date().toISOString().split("T")[0]
  );
  const [expectedDeliveryDate, setExpectedDeliveryDate] = useState("");
  const [paymentTerms, setPaymentTerms] = useState("Virement 30 jours");
  const [paymentDelayDays, setPaymentDelayDays] = useState<number>(30);
  const [deliveryTerms, setDeliveryTerms] = useState("Livraison sur site DAP");
  const [incoterm, setIncoterm] = useState("DAP");
  const [currencyCode, setCurrencyCode] = useState("MAD");
  const [taxAmount, setTaxAmount] = useState<number>(0);
  const [shippingCost, setShippingCost] = useState<number>(0);
  const [notes, setNotes] = useState("");
  const [internalNotes, setInternalNotes] = useState("");

  // Line items
  const [lines, setLines] = useState<LocalLineItem[]>([
    {
      tempId: "1",
      lineNumber: 1,
      materialCode: "",
      materialName: "",
      quantity: 1,
      unitPrice: 0,
      currencyCode: "MAD",
    },
  ]);

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const applyRequisition = (req: Requisition) => {
    setOriginRequisition(req);

    if (req.currencyCode) {
      setCurrencyCode(req.currencyCode);
    }
    if (req.requiredDate) {
      setExpectedDeliveryDate(req.requiredDate);
    }
    if (req.title || req.description) {
      setNotes(req.title + (req.description ? ` - ${req.description}` : ""));
    }

    if (req.lines && req.lines.length > 0) {
      const mappedLines: LocalLineItem[] = req.lines.map((l, idx) => ({
        tempId: l.id || `req-line-${idx + 1}`,
        lineNumber: idx + 1,
        requisitionLineId: l.id,
        materialCode: l.materialCode,
        materialId: l.materialId || "",
        materialName: l.materialName || "",
        materialDescription: l.materialDescription || "",
        unitOfMeasure: l.unitOfMeasure || "U",
        quantity: Number(l.quantity) || 1,
        unitPrice: Number(l.unitPrice) || 0,
        currencyCode: l.currencyCode || req.currencyCode || "MAD",
        expectedDeliveryDate: l.requiredDate || req.requiredDate || "",
        notes: l.notes || "",
      }));
      setLines(mappedLines);

      // Check if any line has supplier assigned
      const lineWithSup = req.lines.find((l) => l.supplierId && l.supplierName);
      if (lineWithSup && lineWithSup.supplierId) {
        setSupplierId(lineWithSup.supplierId);
        setSupplierName(lineWithSup.supplierName || "");
        setSupplierCode(lineWithSup.supplierCode || "");
      }
    }
  };

  const applyOrder = (order: PurchaseOrder) => {
    if (!EDITABLE_STATUSES.includes(order.status)) {
      setError("Cette commande ne peut plus être modifiée.");
      return;
    }
    setEditedOrderCode(order.orderCode);
    setEditedRequisitionCode(order.requisitionCode || null);
    setSupplierId(order.supplierId);
    setSupplierName(order.supplierName);
    setSupplierCode(order.supplierCode || "");
    setOrderDate(order.orderDate);
    setExpectedDeliveryDate(order.expectedDeliveryDate || "");
    setPaymentTerms(order.paymentTerms || "");
    setPaymentDelayDays(order.paymentDelayDays ?? 0);
    setDeliveryTerms(order.deliveryTerms || "");
    setIncoterm(order.incoterm || "");
    setCurrencyCode(order.currencyCode);
    setTaxAmount(parseAmount(order.taxAmount));
    setShippingCost(parseAmount(order.shippingCost));
    setNotes(order.notes || "");
    setInternalNotes(order.internalNotes || "");
    setLines(
      order.lines.map((l, idx) => ({
        tempId: l.id || `line-${idx + 1}`,
        id: l.id,
        lineNumber: idx + 1,
        requisitionLineId: l.requisitionLineId || undefined,
        materialCode: l.materialCode,
        materialId: l.materialId || "",
        materialName: l.materialName || "",
        materialDescription: l.materialDescription || "",
        unitOfMeasure: l.unitOfMeasure || "U",
        quantity: Number(l.quantity) || 1,
        unitPrice: parseAmount(l.unitPrice),
        currencyCode: l.currencyCode || order.currencyCode,
        expectedDeliveryDate: l.expectedDeliveryDate || "",
        notes: l.notes || "",
      }))
    );
  };

  useEffect(() => {
    let cancelled = false;
    Promise.allSettled([supplierApi.getAllUnpaginated(), materialApi.getAll()]).then(
      ([supRes, matRes]) => {
        if (cancelled) return;
        if (supRes.status === "fulfilled" && supRes.value?.data) {
          setSuppliers(supRes.value.data);
        }
        if (matRes.status === "fulfilled" && matRes.value?.data) {
          setMaterials(matRes.value.data);
        }
      }
    );
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!fromRequisitionId) return;
    let cancelled = false;
    requisitionApi
      .getById(fromRequisitionId)
      .then((res) => {
        if (!cancelled) applyRequisition(res.data);
      })
      .catch((err) => {
        console.error("Failed to load requisition data for conversion:", err);
        if (!cancelled) setError("Impossible de charger les données de la demande d'achat à convertir.");
      })
      .finally(() => {
        if (!cancelled) setLoadingRequisition(false);
      });
    return () => {
      cancelled = true;
    };
  }, [fromRequisitionId]);

  useEffect(() => {
    if (!purchaseOrderId) return;
    let cancelled = false;
    purchaseOrderService
      .getById(purchaseOrderId)
      .then((res) => {
        if (!cancelled) applyOrder(res.data);
      })
      .catch((err) => {
        console.error("Failed to load purchase order for editing:", err);
        if (!cancelled) {
          setError(getApiErrorMessage(err, "Impossible de charger le bon de commande à modifier."));
        }
      })
      .finally(() => {
        if (!cancelled) setLoadingOrder(false);
      });
    return () => {
      cancelled = true;
    };
  }, [purchaseOrderId]);

  const handleSupplierChange = (supId: string) => {
    setSupplierId(supId);
    const found = suppliers.find((s) => s.id === supId);
    if (found) {
      setSupplierName(found.name);
      setSupplierCode(found.code || "");
      if (found.currencyCode) {
        setCurrencyCode(found.currencyCode);
      }
    }
  };

  const handleAddLine = () => {
    const nextNum = lines.length + 1;
    setLines((prev) => [
      ...prev,
      {
        tempId: Date.now().toString(),
        lineNumber: nextNum,
        materialCode: "",
        materialName: "",
        quantity: 1,
        unitPrice: 0,
        currencyCode,
      },
    ]);
  };

  const handleRemoveLine = (tempId: string) => {
    if (lines.length <= 1) return;
    setLines((prev) =>
      prev
        .filter((l) => l.tempId !== tempId)
        .map((l, idx) => ({ ...l, lineNumber: idx + 1 }))
    );
  };

  const handleLineChange = (
    tempId: string,
    field: keyof LocalLineItem,
    val: string | number
  ) => {
    setLines((prev) =>
      prev.map((l) => {
        if (l.tempId !== tempId) return l;

        const updated = { ...l, [field]: val };

        // If material changed, auto-populate code & name
        if (field === "materialId") {
          const mat = materials.find((m) => m.id === val);
          if (mat) {
            updated.materialCode = mat.code;
            updated.materialName = mat.name;
            updated.unitOfMeasure = mat.unitOfMeasure || "U";
            if (mat.standardPrice) {
              const numPrice = parseFloat(String(mat.standardPrice).replace(/[^0-9.]/g, ""));
              if (!isNaN(numPrice)) {
                updated.unitPrice = numPrice;
              }
            }
          }
        }

        return updated;
      })
    );
  };

  // Calculations
  const subtotal = lines.reduce((acc, l) => {
    const q = Number(l.quantity) || 0;
    const p = Number(l.unitPrice) || 0;
    return acc + q * p;
  }, 0);

  const grandTotal = subtotal + Number(taxAmount || 0) + Number(shippingCost || 0);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!supplierId || !supplierName) {
      setError("Veuillez sélectionner un fournisseur.");
      return;
    }

    if (lines.length === 0) {
      setError("Au moins un article doit être commandé.");
      return;
    }

    for (let i = 0; i < lines.length; i++) {
      const l = lines[i];
      if (!l.materialCode.trim()) {
        setError(`Veuillez renseigner le code matériel pour la ligne #${i + 1}.`);
        return;
      }
      if (!l.quantity || l.quantity <= 0) {
        setError(`La quantité de la ligne #${i + 1} doit être supérieure à 0.`);
        return;
      }
      if (l.unitPrice === undefined || Number(l.unitPrice) < 0) {
        setError(`Le prix unitaire de la ligne #${i + 1} est invalide.`);
        return;
      }
    }

    try {
      setSubmitting(true);
      const payload: CreatePurchaseOrderRequest = {
        requisitionId: originRequisition ? originRequisition.id : undefined,
        requisitionCode: originRequisition ? originRequisition.requisitionCode : undefined,
        supplierId,
        supplierName,
        supplierCode: supplierCode || undefined,
        orderDate,
        expectedDeliveryDate: expectedDeliveryDate || undefined,
        paymentTerms: paymentTerms || undefined,
        paymentDelayDays: Number(paymentDelayDays) || 0,
        deliveryTerms: deliveryTerms || undefined,
        incoterm: incoterm || undefined,
        currencyCode,
        taxAmount: Number(taxAmount) || 0,
        shippingCost: Number(shippingCost) || 0,
        orderedBy: user?.id || "CURRENT_USER",
        orderedByName: user?.name || user?.email || "Acheteur",
        notes: notes || undefined,
        internalNotes: internalNotes || undefined,
        lines: lines.map((l, idx) => ({
          lineNumber: idx + 1,
          requisitionLineId: l.requisitionLineId || undefined,
          materialCode: l.materialCode,
          materialId: l.materialId || undefined,
          materialName: l.materialName || undefined,
          unitOfMeasure: l.unitOfMeasure || undefined,
          quantity: Number(l.quantity),
          unitPrice: Number(l.unitPrice),
          currencyCode,
          expectedDeliveryDate: l.expectedDeliveryDate || expectedDeliveryDate || undefined,
          notes: l.notes || undefined,
        })),
        createdBy: user?.email || "system",
      };

      if (purchaseOrderId) {
        // The originating requisition cannot change once the order exists.
        const res = await purchaseOrderService.update(purchaseOrderId, {
          ...payload,
          requisitionId: undefined,
          requisitionCode: undefined,
          lines: payload.lines.map((l, idx) => ({ ...l, id: lines[idx].id || undefined })),
          updatedBy: user?.id || "CURRENT_USER",
        });
        navigate(`/purchase-orders/${res.data.id}`);
        return;
      }

      // The backend converts the originating requisition in the same transaction.
      const res = await purchaseOrderService.create(payload);

      navigate(`/purchase-orders/${res.data.id}`);
    } catch (err) {
      console.error("Failed to save purchase order:", err);
      setError(
        getApiErrorMessage(
          err,
          "Échec de l'enregistrement du bon de commande. Veuillez vérifier les informations saisies."
        )
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm">
        <div>
          <h2 className="text-xl font-bold text-gray-900 dark:text-white">
            {isEdit
              ? `Modifier le Bon de Commande ${editedOrderCode ?? ""}`
              : "Nouveau Bon de Commande Fournisseur"}
          </h2>
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
            {isEdit
              ? editedRequisitionCode
                ? `Issu de la demande d'achat ${editedRequisitionCode}.`
                : "Modifiez la commande avant sa confirmation par le fournisseur."
              : "Émettez un bon de commande officiel pour vos approvisionnements et livraisons."}
          </p>
        </div>
        <Link to={isEdit ? `/purchase-orders/${purchaseOrderId}` : "/purchase-orders"}>
          <Button variant="outline" size="sm">
            Annuler
          </Button>
        </Link>
      </div>

      {/* Originating Requisition Alert Banner */}
      {loadingRequisition && (
        <div className="p-4 rounded-2xl bg-purple-50 text-purple-700 dark:bg-purple-500/10 dark:text-purple-300 text-xs flex items-center gap-2 border border-purple-200 dark:border-purple-500/20">
          <div className="size-4 animate-spin rounded-full border-2 border-purple-600 border-t-transparent" />
          Chargement des données de la demande d'achat à convertir...
        </div>
      )}

      {originRequisition && !loadingRequisition && (
        <div className="p-4 rounded-2xl bg-gradient-to-r from-purple-50 to-indigo-50/60 dark:from-purple-950/20 dark:to-indigo-950/20 border border-purple-200 dark:border-purple-500/30 text-xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="size-9 rounded-xl bg-purple-600 text-white flex items-center justify-center font-bold shadow-sm">
              <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
              </svg>
            </div>
            <div>
              <p className="font-bold text-gray-900 dark:text-white">
                Conversion de la Demande d'Achat: <span className="text-purple-600 dark:text-purple-400 font-mono">{originRequisition.requisitionCode}</span>
              </p>
              <p className="text-gray-600 dark:text-gray-300 mt-0.5">
                {originRequisition.title} — {originRequisition.lines?.length || 0} ligne(s) préremplie(s)
              </p>
            </div>
          </div>
          <Link
            to={`/requisitions/${originRequisition.id}`}
            className="text-purple-700 dark:text-purple-300 hover:text-purple-900 dark:hover:text-white font-semibold underline text-xs"
          >
            Consulter la demande d'origine →
          </Link>
        </div>
      )}

      {loadingOrder && (
        <div className="p-4 rounded-2xl bg-gray-50 text-gray-600 dark:bg-white/[0.03] dark:text-gray-300 text-xs flex items-center gap-2 border border-gray-200 dark:border-white/[0.07]">
          <div className="size-4 animate-spin rounded-full border-2 border-brand-600 border-t-transparent" />
          Chargement du bon de commande...
        </div>
      )}

      {error && (
        <div className="p-4 rounded-xl bg-red-50 text-red-700 dark:bg-red-500/10 dark:text-red-400 text-xs border border-red-200 dark:border-red-500/20">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Section 1: Informations Générales & Fournisseur */}
        <div className="bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-4">
          <h3 className="text-sm font-bold text-gray-800 dark:text-white flex items-center gap-2 border-b border-gray-100 dark:border-white/[0.05] pb-3">
            <span className="flex h-6 w-6 items-center justify-center rounded-full bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400 text-xs font-bold">
              1
            </span>
            Fournisseur & Dates de Livraison
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block mb-1.5 text-xs font-semibold text-gray-700 dark:text-gray-300">
                Fournisseur <span className="text-red-500">*</span>
              </label>
              <select
                required
                value={supplierId}
                onChange={(e) => handleSupplierChange(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
              >
                <option value="">Sélectionnez un fournisseur...</option>
                {suppliers.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name} ({s.code || "Sans code"})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block mb-1.5 text-xs font-semibold text-gray-700 dark:text-gray-300">
                Date de la Commande <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                required
                value={orderDate}
                onChange={(e) => setOrderDate(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
              />
            </div>

            <div>
              <label className="block mb-1.5 text-xs font-semibold text-gray-700 dark:text-gray-300">
                Date de Livraison Prévue
              </label>
              <input
                type="date"
                value={expectedDeliveryDate}
                onChange={(e) => setExpectedDeliveryDate(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 pt-2">
            <div>
              <label className="block mb-1 text-xs font-medium text-gray-600 dark:text-gray-300">
                Devise
              </label>
              <select
                value={currencyCode}
                onChange={(e) => setCurrencyCode(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
              >
                <option value="MAD">MAD (Dirham Marocain)</option>
                <option value="EUR">EUR (€ Euro)</option>
                <option value="USD">USD ($ Dollar)</option>
              </select>
            </div>

            <div>
              <label className="block mb-1 text-xs font-medium text-gray-600 dark:text-gray-300">
                Conditions de Paiement
              </label>
              <input
                type="text"
                value={paymentTerms}
                onChange={(e) => setPaymentTerms(e.target.value)}
                placeholder="Ex: Virement 30j"
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
              />
            </div>

            <div>
              <label className="block mb-1 text-xs font-medium text-gray-600 dark:text-gray-300">
                Délai de Paiement (jours)
              </label>
              <input
                type="number"
                min="0"
                value={paymentDelayDays}
                onChange={(e) => setPaymentDelayDays(parseInt(e.target.value) || 0)}
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
              />
            </div>

            <div>
              <label className="block mb-1 text-xs font-medium text-gray-600 dark:text-gray-300">
                Incoterm
              </label>
              <input
                type="text"
                value={incoterm}
                onChange={(e) => setIncoterm(e.target.value)}
                placeholder="Ex: DAP, FOB, EXW"
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
              />
            </div>
          </div>
        </div>

        {/* Section 2: Lignes d'articles commandés */}
        <div className="bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-4">
          <div className="flex items-center justify-between border-b border-gray-100 dark:border-white/[0.05] pb-3">
            <h3 className="text-sm font-bold text-gray-800 dark:text-white flex items-center gap-2">
              <span className="flex h-6 w-6 items-center justify-center rounded-full bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400 text-xs font-bold">
                2
              </span>
              Articles & Lignes de Commande
            </h3>
            <Button type="button" variant="outline" size="sm" onClick={handleAddLine}>
              + Ajouter une Ligne
            </Button>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-100 dark:divide-white/[0.05] text-xs">
              <thead>
                <tr className="text-left font-semibold text-gray-500 dark:text-gray-400">
                  <th className="py-2 pr-2">#</th>
                  <th className="py-2 px-2 w-64">Article / Matériel</th>
                  <th className="py-2 px-2 w-48">Code</th>
                  <th className="py-2 px-2 w-28 text-right">Quantité</th>
                  <th className="py-2 px-2 w-32 text-right">Prix Unitaire</th>
                  <th className="py-2 px-2 text-right">Total Ligne</th>
                  <th className="py-2 pl-2 w-10"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                {lines.map((l, index) => {
                  const lineTotal = (Number(l.quantity) || 0) * (Number(l.unitPrice) || 0);

                  return (
                    <tr key={l.tempId}>
                      <td className="py-2 pr-2 text-gray-400 font-medium">
                        {index + 1}
                      </td>

                      {/* Select Material */}
                      <td className="py-2 px-2">
                        <select
                          value={l.materialId || ""}
                          onChange={(e) =>
                            handleLineChange(l.tempId, "materialId", e.target.value)
                          }
                          className="w-full rounded-lg border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 p-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
                        >
                          <option value="">Sélectionner un catalogue...</option>
                          {materials.map((m) => (
                            <option key={m.id} value={m.id}>
                              {m.name} ({m.code})
                            </option>
                          ))}
                        </select>
                      </td>

                      {/* Material Code */}
                      <td className="py-2 px-2">
                        <input
                          type="text"
                          required
                          placeholder="Code (ex: MAT-001)"
                          value={l.materialCode}
                          onChange={(e) =>
                            handleLineChange(l.tempId, "materialCode", e.target.value)
                          }
                          className="w-full rounded-lg border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 p-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none font-semibold text-brand-600 dark:text-brand-400"
                        />
                      </td>

                      {/* Quantity */}
                      <td className="py-2 px-2">
                        <input
                          type="number"
                          required
                          min="1"
                          value={l.quantity}
                          onChange={(e) =>
                            handleLineChange(l.tempId, "quantity", parseInt(e.target.value) || 0)
                          }
                          className="w-full rounded-lg border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 p-2 text-xs text-right text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
                        />
                      </td>

                      {/* Unit Price */}
                      <td className="py-2 px-2">
                        <input
                          type="number"
                          required
                          min="0"
                          step="0.01"
                          value={l.unitPrice}
                          onChange={(e) =>
                            handleLineChange(l.tempId, "unitPrice", parseFloat(e.target.value) || 0)
                          }
                          className="w-full rounded-lg border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 p-2 text-xs text-right text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
                        />
                      </td>

                      {/* Line Total */}
                      <td className="py-2 px-2 text-right font-bold text-gray-900 dark:text-white">
                        {lineTotal.toFixed(2)} {currencyCode}
                      </td>

                      {/* Remove Button */}
                      <td className="py-2 pl-2 text-center">
                        {lines.length > 1 && (
                          <button
                            type="button"
                            onClick={() => handleRemoveLine(l.tempId)}
                            className="p-1 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors"
                          >
                            <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Financial Totals */}
          <div className="flex flex-col items-end pt-4 border-t border-gray-100 dark:border-white/[0.05] space-y-2 text-xs">
            <div className="flex justify-between w-64 text-gray-600 dark:text-gray-400">
              <span>Sous-total HT :</span>
              <span className="font-semibold text-gray-800 dark:text-gray-200">
                {subtotal.toFixed(2)} {currencyCode}
              </span>
            </div>

            <div className="flex items-center justify-between w-64 text-gray-600 dark:text-gray-400">
              <span>TVA / Taxes :</span>
              <input
                type="number"
                min="0"
                step="0.01"
                value={taxAmount}
                onChange={(e) => setTaxAmount(parseFloat(e.target.value) || 0)}
                className="w-24 rounded-lg border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 p-1 text-xs text-right text-gray-800 dark:text-white"
              />
            </div>

            <div className="flex items-center justify-between w-64 text-gray-600 dark:text-gray-400">
              <span>Frais de port :</span>
              <input
                type="number"
                min="0"
                step="0.01"
                value={shippingCost}
                onChange={(e) => setShippingCost(parseFloat(e.target.value) || 0)}
                className="w-24 rounded-lg border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 p-1 text-xs text-right text-gray-800 dark:text-white"
              />
            </div>

            <div className="flex justify-between w-64 pt-2 border-t border-gray-200 dark:border-white/[0.1] text-sm font-bold text-gray-900 dark:text-white">
              <span>Grand Total TTC :</span>
              <span className="text-brand-600 dark:text-brand-400">
                {grandTotal.toFixed(2)} {currencyCode}
              </span>
            </div>
          </div>
        </div>

        {/* Section 3: Remarques & Notes */}
        <div className="bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-4">
          <h3 className="text-sm font-bold text-gray-800 dark:text-white flex items-center gap-2 border-b border-gray-100 dark:border-white/[0.05] pb-3">
            <span className="flex h-6 w-6 items-center justify-center rounded-full bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400 text-xs font-bold">
              3
            </span>
            Notes & Instructions
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-xs font-medium text-gray-700 dark:text-gray-300">
                Instructions pour le Fournisseur (figurera sur le bon imprimé)
              </label>
              <textarea
                rows={3}
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Ex: Merci de mentionner le numéro de commande sur le bon de livraison..."
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 p-3 text-xs text-gray-800 dark:text-white placeholder:text-gray-400 focus:border-brand-500 focus:outline-none"
              />
            </div>

            <div>
              <label className="block mb-1 text-xs font-medium text-gray-700 dark:text-gray-300">
                Remarques Internes (visibles uniquement par l'équipe)
              </label>
              <textarea
                rows={3}
                value={internalNotes}
                onChange={(e) => setInternalNotes(e.target.value)}
                placeholder="Ex: Validation budgétaire accordée par la direction..."
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 p-3 text-xs text-gray-800 dark:text-white placeholder:text-gray-400 focus:border-brand-500 focus:outline-none"
              />
            </div>
          </div>
        </div>

        {/* Submit Actions */}
        <div className="flex items-center justify-end gap-3 pt-2">
          <Link to="/purchase-orders">
            <Button variant="outline" size="md">
              Annuler
            </Button>
          </Link>
          <Button size="md" type="submit" disabled={submitting}>
            {isEdit
              ? submitting
                ? "Enregistrement..."
                : "Enregistrer les modifications"
              : submitting
                ? "Création en cours..."
                : "Créer le Bon de Commande"}
          </Button>
        </div>
      </form>
    </div>
  );
}
