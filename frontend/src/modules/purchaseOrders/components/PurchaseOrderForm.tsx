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
import Label from "../../../shared/components/form/Label";
import Input from "../../../shared/components/form/input/InputField";
import TextArea from "../../../shared/components/form/input/TextArea";
import { FloatingToast, FormCard, FormSection } from "../../../shared/components/page/DetailParts";
import { SELECT_CLASS } from "../../../shared/components/page/pageStyles";
import { formatAmount } from "../../invoices/utils/invoiceLine";
import useAuth from "../../auth/hooks/useAuth";

/** The APIs answer with a plain array, a page (`content`) or a wrapper (`data`); this returns the items. */
function asList<T>(raw: unknown): T[] {
  if (Array.isArray(raw)) return raw as T[];
  const wrapped = raw as { content?: unknown; data?: unknown } | null | undefined;
  if (Array.isArray(wrapped?.content)) return wrapped.content as T[];
  if (Array.isArray(wrapped?.data)) return wrapped.data as T[];
  return [];
}

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
  const [paymentTerms, setPaymentTerms] = useState("Bank transfer, 30 days");
  const [paymentDelayDays, setPaymentDelayDays] = useState<number>(30);
  const [deliveryTerms, setDeliveryTerms] = useState("Delivered on site");
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
      setError("This order can no longer be edited.");
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
    Promise.allSettled([supplierApi.getAllUnpaginated(), materialApi.getAllForSelection()]).then(
      ([supRes, matRes]) => {
        if (cancelled) return;
        if (supRes.status === "fulfilled") {
          setSuppliers(asList<SupplierListItem>(supRes.value));
        } else {
          setSuppliers([]);
        }

        if (matRes.status === "fulfilled") {
          // Only materials that can still be ordered are offered on a new line.
          setMaterials(asList<MaterialListItem>(matRes.value).filter((m) => !m?.status || m.status === "ACTIVE"));
        } else {
          console.error("Failed to load materials:", matRes.reason);
          setMaterials([]);
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
        if (!cancelled) setError("Could not load the requisition to convert.");
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
          setError(getApiErrorMessage(err, "Could not load the purchase order to edit."));
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

  /** The quantity requested on the requisition line, the most this order may order for it. */
  const requestedQuantity = (requisitionLineId?: string) => {
    if (!originRequisition || !requisitionLineId) return undefined;
    const requested = originRequisition.lines?.find((rl) => rl.id === requisitionLineId);
    return requested ? Number(requested.quantity) : undefined;
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
          const safeMats = Array.isArray(materials) ? materials : [];
          const mat = safeMats.find((m) => m.id === val);
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
      setError("Select a supplier.");
      return;
    }

    if (lines.length === 0) {
      setError("Order at least one line.");
      return;
    }

    for (let i = 0; i < lines.length; i++) {
      const l = lines[i];
      if (!l.materialCode.trim()) {
        setError(`Enter the material code on line ${i + 1}.`);
        return;
      }
      if (!l.quantity || l.quantity <= 0) {
        setError(`The quantity on line ${i + 1} must be greater than 0.`);
        return;
      }
      if (l.unitPrice === undefined || Number(l.unitPrice) < 0) {
        setError(`The unit price on line ${i + 1} is invalid.`);
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
        orderedByName: user?.name || user?.email || "Buyer",
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
          "Saving the purchase order failed. Check the details entered."
        )
      );
    } finally {
      setSubmitting(false);
    }
  };

  const safeSuppliers = Array.isArray(suppliers) ? suppliers : [];
  const safeMaterials = Array.isArray(materials) ? materials : [];
  const cancelTo = isEdit ? `/purchase-orders/${purchaseOrderId}` : "/purchase-orders";

  return (
    <>
      <FloatingToast feedback={error ? { type: "error", text: error } : null} onClose={() => setError(null)} />

      <form onSubmit={handleSubmit}>
        <FormCard title={isEdit ? `Edit Purchase Order ${editedOrderCode ?? ""}` : "New Purchase Order"}>
          {(loadingOrder || loadingRequisition) && (
            <p className="text-sm text-gray-500">{loadingOrder ? "Loading purchase order..." : "Loading the requisition to convert..."}</p>
          )}

          {originRequisition && !loadingRequisition && (
            <div className="flex flex-col gap-2 rounded-xl border border-brand-200 bg-brand-25 p-4 text-sm sm:flex-row sm:items-center sm:justify-between dark:border-brand-500/30 dark:bg-brand-500/[0.06]">
              <div>
                <p className="font-medium text-gray-800 dark:text-white/90">
                  Converting requisition <span className="font-mono text-brand-500">{originRequisition.requisitionCode}</span>
                </p>
                <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                  {originRequisition.title} — {originRequisition.lines?.length || 0} line(s) prefilled. Materials come from the requisition and
                  quantities cannot exceed what was requested.
                </p>
              </div>
              <Link to={`/requisitions/${originRequisition.id}`} className="text-theme-sm font-medium text-brand-500 hover:underline">
                View requisition
              </Link>
            </div>
          )}

          {isEdit && editedRequisitionCode && (
            <p className="text-theme-sm text-gray-500 dark:text-gray-400">
              From requisition <span className="font-mono font-medium text-brand-500">{editedRequisitionCode}</span>.
            </p>
          )}

          <FormSection
            title="Supplier & Dates"
            aside={
              <Link to={cancelTo}>
                <Button variant="outline" size="sm">
                  Cancel
                </Button>
              </Link>
            }
          >
            <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
              <div className="md:col-span-3">
                <Label>Supplier *</Label>
                <select value={supplierId} onChange={(e) => handleSupplierChange(e.target.value)} className={SELECT_CLASS}>
                  <option value="">Select a supplier...</option>
                  {safeSuppliers.map((sup) => (
                    <option key={sup.id} value={sup.id}>
                      {sup.name} {sup.code ? `(${sup.code})` : ""}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <Label>Order Date *</Label>
                <Input type="date" value={orderDate} onChange={(e) => setOrderDate(e.target.value)} />
              </div>
              <div>
                <Label>Expected Delivery</Label>
                <Input type="date" value={expectedDeliveryDate} min={orderDate} onChange={(e) => setExpectedDeliveryDate(e.target.value)} />
              </div>
              <div>
                <Label>Currency</Label>
                <select value={currencyCode} onChange={(e) => setCurrencyCode(e.target.value)} className={SELECT_CLASS}>
                  <option value="MAD">MAD (Moroccan Dirham)</option>
                  <option value="EUR">EUR (Euro)</option>
                  <option value="USD">USD (US Dollar)</option>
                </select>
              </div>
            </div>
          </FormSection>

          <FormSection title="Terms">
            <div className="grid grid-cols-1 gap-6 md:grid-cols-4">
              <div className="md:col-span-2">
                <Label>Payment Terms</Label>
                <Input value={paymentTerms} onChange={(e) => setPaymentTerms(e.target.value)} placeholder="e.g. Bank transfer, 30 days" />
              </div>
              <div>
                <Label>Payment Delay (days)</Label>
                <Input type="number" min="0" value={paymentDelayDays} onChange={(e) => setPaymentDelayDays(parseInt(e.target.value) || 0)} />
              </div>
              <div>
                <Label>Incoterm</Label>
                <Input value={incoterm} onChange={(e) => setIncoterm(e.target.value)} placeholder="e.g. DAP, FOB, EXW" />
              </div>
              <div className="md:col-span-4">
                <Label>Delivery Terms</Label>
                <Input value={deliveryTerms} onChange={(e) => setDeliveryTerms(e.target.value)} placeholder="e.g. Delivered on site" />
              </div>
            </div>
          </FormSection>

          <FormSection
            title={`Order Lines (${lines.length})`}
            aside={
              !originRequisition ? (
                <Button type="button" size="sm" variant="outline" onClick={handleAddLine}>
                  Add Line
                </Button>
              ) : undefined
            }
          >
            <div className="space-y-4">
              {lines.map((l, index) => {
                const lineTotal = (Number(l.quantity) || 0) * (Number(l.unitPrice) || 0);
                const maxQuantity = requestedQuantity(l.requisitionLineId);
                return (
                  <div key={l.tempId} className="space-y-4 rounded-xl border border-gray-200 p-4 dark:border-white/[0.05]">
                    <div className="flex items-center justify-between gap-3">
                      <span className="text-sm font-medium text-gray-800 dark:text-white/90">Line {index + 1}</span>
                      {lines.length > 1 && !originRequisition && (
                        <button
                          type="button"
                          onClick={() => handleRemoveLine(l.tempId)}
                          className="text-theme-xs font-medium text-error-500 hover:underline"
                        >
                          Remove
                        </button>
                      )}
                    </div>
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-6">
                      <div className="lg:col-span-2">
                        <Label>Material</Label>
                        <select
                          value={l.materialId || ""}
                          disabled={!!originRequisition}
                          onChange={(e) => handleLineChange(l.tempId, "materialId", e.target.value)}
                          className={SELECT_CLASS}
                        >
                          <option value="">Select from the catalogue...</option>
                          {safeMaterials.map((m) => (
                            <option key={m.id} value={m.id}>
                              {m.name} ({m.code})
                            </option>
                          ))}
                        </select>
                      </div>
                      <div>
                        <Label>Code *</Label>
                        <Input value={l.materialCode} placeholder="e.g. MAT-001" onChange={(e) => handleLineChange(l.tempId, "materialCode", e.target.value)} />
                      </div>
                      <div>
                        <Label>Quantity *</Label>
                        <Input
                          type="number"
                          min="1"
                          max={maxQuantity !== undefined ? String(maxQuantity) : undefined}
                          value={l.quantity}
                          onChange={(e) => handleLineChange(l.tempId, "quantity", parseInt(e.target.value) || 0)}
                          hint={maxQuantity !== undefined ? `Requested: ${maxQuantity}` : undefined}
                        />
                      </div>
                      <div>
                        <Label>Unit Price *</Label>
                        <Input
                          type="number"
                          min="0"
                          step={0.01}
                          value={l.unitPrice}
                          onChange={(e) => handleLineChange(l.tempId, "unitPrice", parseFloat(e.target.value) || 0)}
                        />
                      </div>
                      <div>
                        <Label>Line Total</Label>
                        <p className="flex h-11 items-center text-sm font-semibold text-gray-800 dark:text-white/90">
                          {formatAmount(lineTotal, currencyCode)}
                        </p>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </FormSection>

          <FormSection title="Totals">
            <div className="grid grid-cols-1 gap-6 md:grid-cols-4">
              <div>
                <Label>Subtotal excl. Tax</Label>
                <p className="flex h-11 items-center text-sm font-medium text-gray-800 dark:text-white/90">{formatAmount(subtotal, currencyCode)}</p>
              </div>
              <div>
                <Label>Tax</Label>
                <Input type="number" min="0" step={0.01} value={taxAmount} onChange={(e) => setTaxAmount(parseFloat(e.target.value) || 0)} />
              </div>
              <div>
                <Label>Shipping</Label>
                <Input type="number" min="0" step={0.01} value={shippingCost} onChange={(e) => setShippingCost(parseFloat(e.target.value) || 0)} />
              </div>
              <div>
                <Label>Grand Total</Label>
                <p className="flex h-11 items-center text-base font-semibold text-brand-500">{formatAmount(grandTotal, currencyCode)}</p>
              </div>
            </div>
          </FormSection>

          <FormSection title="Notes">
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
              <div>
                <Label>For the Supplier</Label>
                <TextArea rows={3} value={notes} onChange={setNotes} placeholder="e.g. Please quote the order number on the delivery note" />
              </div>
              <div>
                <Label>Internal</Label>
                <TextArea rows={3} value={internalNotes} onChange={setInternalNotes} placeholder="Only visible to your team" />
              </div>
            </div>
          </FormSection>

          <div className="flex justify-end gap-3 border-t border-gray-100 pt-6 dark:border-gray-800">
            <Link to={cancelTo}>
              <Button variant="outline">Cancel</Button>
            </Link>
            <Button type="submit" disabled={submitting || loadingOrder || loadingRequisition}>
              {submitting ? "Saving..." : isEdit ? "Save Changes" : "Create Order"}
            </Button>
          </div>
        </FormCard>
      </form>
    </>
  );
}
