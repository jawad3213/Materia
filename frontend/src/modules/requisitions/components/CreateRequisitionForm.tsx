import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { requisitionApi } from "../services/requisitionApi";
import { materialApi } from "../../materials/services/materialApi";
import { supplierApi } from "../../suppliers/services/supplierApi";
import type { CreateRequisitionRequest } from "../types/CreateRequisitionRequest";
import type { RequisitionLineRequest } from "../types/RequisitionLine";
import type { MaterialListItem } from "../../materials/types/MaterialListItem";
import type { SupplierListItem } from "../../suppliers/types/SupplierListItem";

// Shared UI components
import Input from "../../../shared/components/form/input/InputField";
import TextArea from "../../../shared/components/form/input/TextArea";
import Label from "../../../shared/components/form/Label";
import Button from "../../../shared/components/ui/button/Button";
import Toast from "../../../shared/components/ui/notifications/Toast";
import Badge from "../../../shared/components/ui/badge/Badge";
import { useCurrencyConverter } from "../../../shared/hooks";

interface LocalLineItem extends RequisitionLineRequest {
  tempId: string;
  materialName?: string;
  unitOfMeasure?: string;
  estimatedUnitPrice?: number;
  lineTotal?: number;
  originalPrice?: number;
  originalCurrency?: string;
  exchangeRateUsed?: number;
}

import type { UpdateRequisitionRequest } from "../types/UpdateRequisitionRequest";

const parseNumericAmount = (val: unknown): number => {
  if (val === undefined || val === null) return 0;
  if (typeof val === "number") return isNaN(val) ? 0 : val;
  const str = String(val).trim();
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
  return isNaN(num) ? 0 : num;
};

const extractCurrency = (val: unknown, fallback: string = "MAD"): string => {
  if (!val) return fallback;
  const s = String(val).toUpperCase();
  if (s.includes("EUR") || s.includes("€") || s.includes("â‚¬") || s.includes("\u20AC")) return "EUR";
  if (s.includes("USD") || s.includes("$")) return "USD";
  if (s.includes("MAD") || s.includes("DH") || s.includes("DIRHAM")) return "MAD";
  return fallback;
};

interface CreateRequisitionFormProps {
  requisitionId?: string;
}

export default function CreateRequisitionForm({ requisitionId }: CreateRequisitionFormProps = {}) {
  const navigate = useNavigate();
  const isEditMode = Boolean(requisitionId);
  const [isLoadingExisting, setIsLoadingExisting] = useState(false);
  const [existingStatus, setExistingStatus] = useState<string | null>(null);
  const isReadOnly = Boolean(
    isEditMode &&
    existingStatus &&
    existingStatus !== "DRAFT" &&
    existingStatus !== "SUBMITTED"
  );

  const [materials, setMaterials] = useState<MaterialListItem[]>([]);
  const [suppliers, setSuppliers] = useState<SupplierListItem[]>([]);
  const [isLoadingCatalogs, setIsLoadingCatalogs] = useState(true);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSavingDraft, setIsSavingDraft] = useState(false);
  const [showExitModal, setShowExitModal] = useState(false);
  const [submitMessage, setSubmitMessage] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [openMaterialDropdownId, setOpenMaterialDropdownId] = useState<string | null>(null);
  const [materialSearchQuery, setMaterialSearchQuery] = useState("");

  // Header form state
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    justification: "",
    requesterName: "Ahmed Bennani",
    requiredDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
      .toISOString()
      .split("T")[0],
    currencyCode: "MAD",
  });

  // Live currency converter hook
  const { convertToTarget, isLoadingRates, rates } = useCurrencyConverter(formData.currencyCode);

  // Dynamic lines state
  const [lines, setLines] = useState<LocalLineItem[]>([
    {
      tempId: crypto.randomUUID(),
      materialId: "",
      materialCode: "",
      materialName: "",
      quantity: 1,
      unitOfMeasure: "PCS",
      estimatedUnitPrice: 0,
      lineTotal: 0,
      requiredDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0],
      supplierId: "",
      supplierCode: "",
      notes: "",
      deliveryTerms: "",
      storageLocation: "",
    },
  ]);

  // Auto-dismiss toast notification
  useEffect(() => {
    if (submitMessage) {
      const timer = setTimeout(() => {
        setSubmitMessage(null);
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [submitMessage]);

  // Load materials & suppliers catalogs for selection
  useEffect(() => {
    const loadCatalogs = async () => {
      try {
        setIsLoadingCatalogs(true);
        const [materialsRes, suppliersRes] = await Promise.all([
          materialApi.getAll(0, 100).catch(() => ({ data: { content: [] } })),
          supplierApi.getAllUnpaginated().catch(() => ({ data: [] })),
        ]);

        const matList: MaterialListItem[] = Array.isArray(materialsRes.data)
          ? materialsRes.data
          : (materialsRes.data as any)?.content || [];
        setMaterials(matList);

        const supList: SupplierListItem[] = Array.isArray(suppliersRes.data)
          ? suppliersRes.data
          : (suppliersRes.data as any)?.content || [];
        setSuppliers(supList);
      } catch (err) {
        console.error("Failed to load materials or suppliers:", err);
      } finally {
        setIsLoadingCatalogs(false);
      }
    };

    loadCatalogs();
  }, []);

  // Load existing requisition if in edit mode
  useEffect(() => {
    if (!requisitionId) return;

    const loadExistingRequisition = async () => {
      try {
        setIsLoadingExisting(true);
        const res = await requisitionApi.getById(requisitionId);
        const req = res.data;
        if (req) {
          setExistingStatus(req.status || null);
          const reqCurr = extractCurrency(req.currencyCode, "MAD");
          setFormData({
            title: req.title || "",
            description: req.description || "",
            justification: req.justification || "",
            requesterName: req.requesterName || "Ahmed Bennani",
            requiredDate: req.requiredDate || "",
            currencyCode: reqCurr,
          });

          if (req.lines && req.lines.length > 0) {
            setLines(
              req.lines.map((l) => {
                const unitPriceNum = parseNumericAmount(l.unitPrice || l.standardPrice);
                const qtyNum = Math.max(1, Number(l.quantity) || 1);
                const lineTotalNum =
                  parseNumericAmount(l.lineTotal) ||
                  Math.round(qtyNum * unitPriceNum * 100) / 100;
                const lineCurr = extractCurrency(
                  l.currencyCodeLine || l.currencyCode,
                  reqCurr
                );

                return {
                  id: l.id,
                  tempId: crypto.randomUUID(),
                  materialId: l.materialId || "",
                  materialCode: l.materialCode || "",
                  materialName: l.materialName || "",
                  quantity: qtyNum,
                  unitOfMeasure: l.unitOfMeasure || "PCS",
                  estimatedUnitPrice: unitPriceNum,
                  lineTotal: lineTotalNum,
                  originalPrice: unitPriceNum,
                  originalCurrency: lineCurr,
                  exchangeRateUsed: 1,
                  requiredDate: l.requiredDate || req.requiredDate || "",
                  supplierId: l.supplierId || "",
                  supplierCode: l.supplierCode || "",
                  notes: l.notes || "",
                  deliveryTerms: l.deliveryTerms || "",
                  storageLocation: l.storageLocation || "",
                };
              })
            );
          }
        }
      } catch (err: any) {
        console.error("Failed to load requisition for editing:", err);
        setSubmitMessage({
          type: "error",
          text: err?.response?.data?.message || "Failed to load requisition for editing.",
        });
      } finally {
        setIsLoadingExisting(false);
      }
    };

    loadExistingRequisition();
  }, [requisitionId]);

  // Close material searchable dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest(".material-dropdown-container")) {
        setOpenMaterialDropdownId(null);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // Automatically re-convert line items whenever live exchange rates or header currency updates
  useEffect(() => {
    setLines((prevLines) =>
      prevLines.map((line) => {
        const rawOrigPrice =
          line.originalPrice !== undefined && line.originalPrice !== null
            ? parseNumericAmount(line.originalPrice)
            : parseNumericAmount(line.estimatedUnitPrice);
        const origCurr = line.originalCurrency || formData.currencyCode;

        if (rawOrigPrice > 0) {
          const { converted, rate } = convertToTarget(
            rawOrigPrice,
            origCurr,
            formData.currencyCode
          );
          const safeConverted = isNaN(converted) ? rawOrigPrice : converted;
          const qty = line.quantity || 1;
          return {
            ...line,
            estimatedUnitPrice: safeConverted,
            lineTotal: Math.round(qty * safeConverted * 100) / 100,
            exchangeRateUsed: rate,
          };
        }
        return line;
      })
    );
  }, [rates, formData.currencyCode, convertToTarget]);

  const handleHeaderChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const handleLineChange = (
    tempId: string,
    field: keyof LocalLineItem,
    value: any
  ) => {
    setLines((prevLines) =>
      prevLines.map((line) => {
        if (line.tempId !== tempId) return line;

        const updated = { ...line, [field]: value };

        // Auto-populate material details when selecting material
        if (field === "materialId") {
          const selectedMat = materials.find((m) => m.id === value || m.code === value);
          if (selectedMat) {
            updated.materialId = selectedMat.id;
            updated.materialCode = selectedMat.code;
            updated.materialName = selectedMat.name;
            updated.unitOfMeasure = selectedMat.unitOfMeasure || "PCS";

            const rawPrice = parseNumericAmount(selectedMat.standardPrice);
            const matCurrency = extractCurrency(
              selectedMat.standardPriceCurrency || selectedMat.currencyCode,
              formData.currencyCode || "MAD"
            );
            const { converted, rate } = convertToTarget(
              rawPrice,
              matCurrency,
              formData.currencyCode
            );
            const safeConverted = isNaN(converted) ? rawPrice : converted;
            const lineQty = Math.max(1, Number(updated.quantity) || 1);

            updated.estimatedUnitPrice = safeConverted;
            updated.lineTotal = Math.round(lineQty * safeConverted * 100) / 100;
            updated.originalPrice = rawPrice;
            updated.originalCurrency = matCurrency;
            updated.exchangeRateUsed = rate;

            if (selectedMat.supplierId) {
              updated.supplierId = selectedMat.supplierId;
              const sup = suppliers.find((s) => s.id === selectedMat.supplierId);
              if (sup) {
                updated.supplierCode = (sup as any).code || "";
              }
            } else {
              updated.supplierId = "";
              updated.supplierCode = "";
            }
          } else {
            updated.materialId = value;
            updated.materialCode = "";
            updated.materialName = "";
            updated.supplierId = "";
            updated.supplierCode = "";
          }
        }

        // Recalculate line total if quantity or price changes
        if (field === "quantity" || field === "estimatedUnitPrice") {
          const qty =
            field === "quantity"
              ? Math.max(0, parseInt(String(value), 10) || 0)
              : Math.max(0, Number(line.quantity) || 0);
          const price = parseNumericAmount(
            field === "estimatedUnitPrice" ? value : line.estimatedUnitPrice
          );
          updated.quantity = qty;
          updated.estimatedUnitPrice = price;
          updated.lineTotal = Math.round(qty * price * 100) / 100;

          if (field === "estimatedUnitPrice") {
            updated.originalPrice = price;
            updated.originalCurrency = formData.currencyCode;
            updated.exchangeRateUsed = 1;
          }
        }

        return updated;
      })
    );
  };

  const handleAddLine = () => {
    setLines((prev) => [
      ...prev,
      {
        tempId: crypto.randomUUID(),
        materialId: "",
        materialCode: "",
        materialName: "",
        quantity: 1,
        unitOfMeasure: "PCS",
        estimatedUnitPrice: 0,
        lineTotal: 0,
        requiredDate: formData.requiredDate,
        supplierId: "",
        supplierCode: "",
        notes: "",
        deliveryTerms: "",
        storageLocation: "",
      },
    ]);
  };

  const handleRemoveLine = (tempId: string) => {
    if (lines.length <= 1) {
      setSubmitMessage({
        type: "error",
        text: "A purchase requisition must contain at least one line item.",
      });
      return;
    }
    setLines((prev) => prev.filter((l) => l.tempId !== tempId));
  };

  const totalRequisitionAmount = lines.reduce(
    (sum, l) => sum + parseNumericAmount(l.lineTotal),
    0
  );

  // Check whether user has entered any requisition data
  const hasStartedForm = () => {
    return (
      formData.title.trim() !== "" ||
      formData.description.trim() !== "" ||
      formData.justification.trim() !== "" ||
      lines.some(
        (l) =>
          Boolean(l.materialId || l.materialCode) ||
          (l.notes && l.notes.trim() !== "")
      )
    );
  };

  const handleCancelClick = () => {
    if (hasStartedForm()) {
      setShowExitModal(true);
    } else {
      navigate("/requisitions");
    }
  };

  const handleSave = async (isDraft: boolean = false): Promise<boolean> => {
    // If full submission ("Create Requisition"):
    if (!isDraft) {
      const errors: Record<string, string> = {};
      if (!formData.title.trim()) errors.title = "Requisition title is mandatory";
      if (!formData.requesterName.trim())
        errors.requesterName = "Requester name is mandatory";
      if (!formData.currencyCode.trim())
        errors.currencyCode = "Currency is mandatory";

      // Validate lines
      if (lines.length === 0) {
        errors.lines = "At least one line item is required";
      } else {
        lines.forEach((l, idx) => {
          if (!l.materialCode && !l.materialId) {
            errors[`line_${idx}_material`] = `Line #${idx + 1}: Select a material or provide a code`;
          }
          if (!l.quantity || l.quantity <= 0) {
            errors[`line_${idx}_quantity`] = `Line #${idx + 1}: Quantity must be greater than 0`;
          }
        });
      }

      if (Object.keys(errors).length > 0) {
        setFieldErrors(errors);
        setSubmitMessage({
          type: "error",
          text: "Please correct the highlighted form errors before submitting.",
        });
        return false;
      }
    } else {
      // For Draft: require at least one line with material selected
      const validLines = lines.filter((l) => Boolean(l.materialId || l.materialCode));
      if (validLines.length === 0) {
        setSubmitMessage({
          type: "error",
          text: "To save as a draft, please select at least one material in the line items.",
        });
        return false;
      }
    }

    try {
      if (isDraft) {
        setIsSavingDraft(true);
      } else {
        setIsSubmitting(true);
      }
      setFieldErrors({});

      const effectiveTitle =
        formData.title.trim() ||
        `Draft Requisition - ${new Date().toLocaleDateString()}`;
      const effectiveRequester = formData.requesterName.trim() || "Ahmed Bennani";

      const sourceLines = isDraft
        ? lines.filter((l) => Boolean(l.materialId || l.materialCode))
        : lines;

      const payload: CreateRequisitionRequest = {
        title: effectiveTitle,
        description: formData.description.trim() || undefined,
        justification: formData.justification.trim() || undefined,
        requesterName: effectiveRequester,
        requiredDate: formData.requiredDate ? formData.requiredDate.trim() : undefined,
        currencyCode: formData.currencyCode.trim() || "MAD",
        createdBy: effectiveRequester,
        status: isDraft ? "DRAFT" : "SUBMITTED",
        lines: sourceLines.map((l) => {
          const matId =
            l.materialId && l.materialId.trim() !== "" ? l.materialId.trim() : undefined;
          const matCode =
            l.materialCode && l.materialCode.trim() !== "" ? l.materialCode.trim() : undefined;
          const supId =
            l.supplierId && l.supplierId.trim() !== "" ? l.supplierId.trim() : undefined;
          const supCode =
            l.supplierCode && l.supplierCode.trim() !== "" ? l.supplierCode.trim() : undefined;
          const reqDate =
            l.requiredDate && l.requiredDate.trim() !== ""
              ? l.requiredDate.trim()
              : formData.requiredDate
              ? formData.requiredDate.trim()
              : undefined;

          return {
            id: l.id || undefined,
            materialId: matId,
            materialCode: matCode,
            quantity: Math.max(1, Number(l.quantity) || 1),
            requiredDate: reqDate,
            supplierId: supId,
            supplierCode: supCode,
            notes: l.notes && l.notes.trim() !== "" ? l.notes.trim() : undefined,
            deliveryTerms:
              l.deliveryTerms && l.deliveryTerms.trim() !== "" ? l.deliveryTerms.trim() : undefined,
            storageLocation:
              l.storageLocation && l.storageLocation.trim() !== ""
                ? l.storageLocation.trim()
                : undefined,
          };
        }),
      };

      if (isEditMode && requisitionId) {
        const updatePayload: UpdateRequisitionRequest = {
          title: effectiveTitle,
          description: formData.description.trim() || undefined,
          justification: formData.justification.trim() || undefined,
          requiredDate: formData.requiredDate ? formData.requiredDate.trim() : undefined,
          currencyCode: formData.currencyCode.trim() || "MAD",
          updatedBy: effectiveRequester,
          lines: payload.lines,
        };
        const res = await requisitionApi.update(requisitionId, updatePayload);
        const code = res.data?.requisitionCode || "";
        setSubmitMessage({
          type: "success",
          text: `Purchase Requisition ${code} updated successfully!`,
        });
        setTimeout(() => {
          navigate(`/requisitions/view/${requisitionId}`);
        }, 1200);
        return true;
      }

      const res = await requisitionApi.create(payload);

      if (!isDraft) {
        setSubmitMessage({
          type: "success",
          text: `Purchase Requisition ${res.data?.requisitionCode || ""} created and submitted successfully!`,
        });
      } else {
        // Stored as DRAFT
        setSubmitMessage({
          type: "success",
          text: `Purchase Requisition ${res.data?.requisitionCode || ""} saved as draft.`,
        });
      }

      setTimeout(() => {
        navigate("/requisitions");
      }, 1500);
      return true;
    } catch (err: any) {
      console.error("Failed to process requisition:", err);
      setSubmitMessage({
        type: "error",
        text:
          err?.response?.data?.message ||
          err?.message ||
          "Failed to process purchase requisition. Please verify backend status.",
      });
      return false;
    } finally {
      setIsSubmitting(false);
      setIsSavingDraft(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSave(false);
  };

  if (isLoadingExisting) {
    return (
      <div className="flex flex-col items-center justify-center py-20 gap-3">
        <div className="size-8 animate-spin rounded-full border-2 border-brand-500 border-t-transparent" />
        <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
          Loading requisition for editing...
        </span>
      </div>
    );
  }

  return (
    <div className="relative">
      {/* Toast Alert Notification */}
      {submitMessage && (
        <div className="fixed top-6 right-6 z-99999 animate-fade-in">
          <Toast
            variant={submitMessage.type}
            message={submitMessage.text}
            onClose={() => setSubmitMessage(null)}
          />
        </div>
      )}

      <form onSubmit={handleSubmit}>
        {/* Guard for non-modifiable requisitions (Only DRAFT and SUBMITTED can be edited) */}
        {isEditMode && existingStatus && existingStatus !== "DRAFT" && existingStatus !== "SUBMITTED" && (
          <div className="mb-6 p-4 rounded-2xl bg-amber-50 border border-amber-200 text-amber-900 dark:bg-amber-950/30 dark:border-amber-500/30 dark:text-amber-200 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="size-9 rounded-xl bg-amber-100 dark:bg-amber-500/20 text-amber-600 dark:text-amber-400 flex items-center justify-center flex-shrink-0">
                <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <div>
                <p className="text-xs font-bold">This requisition is in {existingStatus} status and cannot be modified.</p>
                <p className="text-[11px] text-amber-700 dark:text-amber-300 mt-0.5">Per procurement lifecycle policy, only DRAFT and SUBMITTED requisitions can be edited.</p>
              </div>
            </div>
            <Button size="sm" variant="outline" type="button" onClick={() => navigate(`/requisitions/view/${requisitionId}`)}>
              View Details
            </Button>
          </div>
        )}

        <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm dark:border-white/[0.07] dark:bg-gray-900">
          {/* Section 1: General Header Details */}
          <div className="mb-8">
            <div className="flex items-center gap-3 pb-3 mb-6 border-b border-gray-100 dark:border-white/[0.07]">
              <div className="flex size-9 items-center justify-center rounded-lg bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400">
                <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <div>
                <h4 className="text-base font-semibold text-gray-800 dark:text-white/90">
                  1. Requisition Header & Logistics
                </h4>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                  General purpose, justification, and delivery timeline for this procurement batch.
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {/* Title - Full Width */}
              <div className="col-span-full">
                <Label>Requisition Title *</Label>
                <Input
                  type="text"
                  name="title"
                  value={formData.title}
                  onChange={handleHeaderChange}
                  disabled={isReadOnly}
                  placeholder="e.g. Q4 Raw Material Replenishment - Steel Sheets"
                  error={!!fieldErrors.title}
                />
                {fieldErrors.title && (
                  <p className="mt-1.5 text-xs text-error-500">{fieldErrors.title}</p>
                )}
              </div>

              {/* Justification - Full Width */}
              <div className="col-span-full">
                <Label>Business Justification</Label>
                <TextArea
                  value={formData.justification}
                  onChange={(val: string) =>
                    setFormData((prev) => ({ ...prev, justification: val }))
                  }
                  rows={2}
                  disabled={isReadOnly}
                  placeholder="e.g. Current safety stock below critical threshold. Needed for ongoing production line."
                />
              </div>

              {/* Row with the 3 inputs: Requester Name, Required Need-By Date, Currency */}
              <div>
                <Label>Requester Name *</Label>
                <Input
                  type="text"
                  name="requesterName"
                  value={formData.requesterName}
                  onChange={handleHeaderChange}
                  disabled={isReadOnly}
                  placeholder="e.g. Ahmed Bennani"
                  error={!!fieldErrors.requesterName}
                />
                {fieldErrors.requesterName && (
                  <p className="mt-1.5 text-xs text-error-500">{fieldErrors.requesterName}</p>
                )}
              </div>

              <div>
                <Label>Required Need-By Date *</Label>
                <Input
                  type="date"
                  name="requiredDate"
                  value={formData.requiredDate}
                  onChange={handleHeaderChange}
                  disabled={isReadOnly}
                />
              </div>

              <div>
                <Label>Currency *</Label>
                <select
                  name="currencyCode"
                  value={formData.currencyCode}
                  onChange={handleHeaderChange}
                  disabled={isReadOnly}
                  className={`h-11 w-full rounded-lg border px-4 py-2.5 text-sm transition-colors ${
                    isReadOnly
                      ? "border-gray-300 bg-gray-100 text-gray-500 cursor-not-allowed dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400"
                      : "border-gray-300 bg-transparent text-gray-800 focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  }`}
                >
                  <option value="MAD">MAD - Moroccan Dirham</option>
                  <option value="USD">USD - US Dollar</option>
                  <option value="EUR">EUR - Euro</option>
                </select>
              </div>
            </div>
          </div>

          {/* Section 2: Requisition Lines Item Builder */}
          <div className="mb-8">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 mb-6 border-b border-gray-100 dark:border-white/[0.07]">
              <div className="flex items-center gap-3">
                <div className="flex size-9 items-center justify-center rounded-lg bg-emerald-50 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-400">
                  <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                  </svg>
                </div>
                <div>
                  <h4 className="text-base font-semibold text-gray-800 dark:text-white/90">
                    2. Requisition Line Items ({lines.length})
                  </h4>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                    Select materials, specify required quantities, and assign target suppliers.
                  </p>
                </div>
              </div>

              {!isReadOnly && (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={handleAddLine}
                >
                  <span className="flex items-center gap-1.5 text-xs font-semibold">
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                    </svg>
                    Add Material Line
                  </span>
                </Button>
              )}
            </div>

            {/* Line Items Container */}
            <div className="space-y-4">
              {lines.map((line, index) => (
                <div
                  key={line.tempId}
                  className="p-5 rounded-2xl border border-gray-200/90 bg-gray-50/40 dark:border-white/[0.07] dark:bg-white/[0.02] relative transition-all"
                >
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-2">
                      <span className="flex size-6 items-center justify-center rounded-full bg-brand-500 text-white font-bold text-xs">
                        {index + 1}
                      </span>
                      <span className="text-xs font-bold text-gray-800 dark:text-white">
                        Line #{index + 1}
                      </span>
                    </div>

                    {!isReadOnly && lines.length > 1 && (
                      <button
                        type="button"
                        onClick={() => handleRemoveLine(line.tempId)}
                        className="text-xs text-red-500 hover:text-red-700 dark:hover:text-red-400 flex items-center gap-1 transition-colors"
                      >
                        <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                        Remove Line
                      </button>
                    )}
                  </div>

                  <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-12">
                    {/* Material Select Searchable Dropdown */}
                    <div className="lg:col-span-5 material-dropdown-container relative">
                      <Label>Material *</Label>
                      {(() => {
                        const isDropdownOpen = openMaterialDropdownId === line.tempId;
                        const selectedMat = materials.find(
                          (m) => m.id === line.materialId || m.code === line.materialCode
                        );

                        const filteredMaterials = materials.filter((mat) => {
                          if (!materialSearchQuery.trim()) return true;
                          const q = materialSearchQuery.toLowerCase();
                          return (
                            mat.name?.toLowerCase().includes(q) ||
                            mat.code?.toLowerCase().includes(q) ||
                            mat.categoryName?.toLowerCase().includes(q)
                          );
                        });

                        const getStockBadgeColor = (stock: number) => {
                          if (stock <= 0) return "error";
                          if (stock < 50) return "warning";
                          return "success";
                        };

                        return (
                          <div className="relative">
                            {/* Trigger Button / Display */}
                            <button
                              type="button"
                              disabled={isReadOnly}
                              onClick={() => {
                                if (isReadOnly) return;
                                setOpenMaterialDropdownId(
                                  isDropdownOpen ? null : line.tempId
                                );
                                setMaterialSearchQuery("");
                              }}
                              className={`h-11 w-full flex items-center justify-between rounded-lg border px-3.5 py-2 text-left text-sm shadow-theme-xs transition-all focus:outline-hidden focus:ring-3 ${
                                isReadOnly
                                  ? "border-gray-300 bg-gray-100/80 cursor-not-allowed text-gray-500 dark:border-gray-700 dark:bg-gray-800/80 dark:text-gray-400"
                                  : fieldErrors[`line_${index}_material`]
                                  ? "border-error-500 bg-transparent text-gray-800 focus:border-error-300 focus:ring-error-500/20 dark:border-error-500 dark:text-error-400"
                                  : isDropdownOpen
                                  ? "border-brand-500 ring-3 ring-brand-500/20 bg-white dark:bg-gray-900 dark:border-brand-400"
                                  : "border-gray-300 bg-white text-gray-800 hover:border-gray-400 focus:border-brand-300 focus:ring-brand-500/20 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                              }`}
                            >
                              <div className="flex items-center gap-2 truncate pr-2">
                                {selectedMat ? (
                                  <>
                                    <span className="font-mono text-xs font-semibold text-brand-600 dark:text-brand-400">
                                      [{selectedMat.code}]
                                    </span>
                                    <span className="truncate text-gray-800 dark:text-white font-medium text-xs">
                                      {selectedMat.name}
                                    </span>
                                    <Badge
                                      size="sm"
                                      color={getStockBadgeColor(selectedMat.currentStock)}
                                      variant="light"
                                    >
                                      {selectedMat.currentStock} {selectedMat.unitOfMeasure || "in stock"}
                                    </Badge>
                                  </>
                                ) : (
                                  <span className="text-gray-400 dark:text-gray-500 text-xs">
                                    {isLoadingCatalogs ? "Loading materials..." : "-- Select or Search Material --"}
                                  </span>
                                )}
                              </div>
                              {!isReadOnly && (
                                <svg
                                  className={`size-4 text-gray-400 transition-transform duration-200 ${
                                    isDropdownOpen ? "rotate-180 text-brand-500" : ""
                                  }`}
                                  fill="none"
                                  viewBox="0 0 24 24"
                                  stroke="currentColor"
                                >
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                                </svg>
                              )}
                            </button>

                            {/* Dropdown Menu with Search Input */}
                            {!isReadOnly && isDropdownOpen && (
                              <div className="absolute z-50 mt-1.5 w-full rounded-xl border border-gray-200 bg-white shadow-xl dark:border-gray-700 dark:bg-gray-900 overflow-hidden animate-in fade-in zoom-in-95 duration-100">
                                {/* Search Box */}
                                <div className="p-2.5 border-b border-gray-100 dark:border-gray-800 bg-gray-50/70 dark:bg-gray-800/40">
                                  <div className="relative">
                                    <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
                                      <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                      </svg>
                                    </div>
                                    <input
                                      type="text"
                                      autoFocus
                                      value={materialSearchQuery}
                                      onChange={(e) => setMaterialSearchQuery(e.target.value)}
                                      placeholder="Search by code, name, or category..."
                                      className="h-8.5 w-full rounded-lg border border-gray-300 bg-white pl-8 pr-3 text-xs text-gray-800 placeholder:text-gray-400 focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white"
                                    />
                                  </div>
                                </div>

                                {/* Materials Items List */}
                                <div className="max-h-60 overflow-y-auto divide-y divide-gray-50 dark:divide-gray-800/50 p-1">
                                  {filteredMaterials.length === 0 ? (
                                    <div className="py-6 text-center text-xs text-gray-400">
                                      No materials match "{materialSearchQuery}"
                                    </div>
                                  ) : (
                                    filteredMaterials.map((mat) => {
                                      const isSelected = line.materialId === mat.id;
                                      return (
                                        <button
                                          key={mat.id}
                                          type="button"
                                          onClick={() => {
                                            handleLineChange(line.tempId, "materialId", mat.id);
                                            setOpenMaterialDropdownId(null);
                                            setMaterialSearchQuery("");
                                          }}
                                          className={`w-full flex items-center justify-between p-2.5 rounded-lg text-left transition-colors ${
                                            isSelected
                                              ? "bg-brand-50/80 dark:bg-brand-500/15 text-brand-900 dark:text-brand-300"
                                              : "hover:bg-gray-50 dark:hover:bg-gray-800/60 text-gray-700 dark:text-gray-200"
                                          }`}
                                        >
                                          <div className="flex flex-col pr-2">
                                            <div className="flex items-center gap-1.5">
                                              <span className="font-mono text-xs font-bold text-brand-600 dark:text-brand-400">
                                                [{mat.code}]
                                              </span>
                                              <span className="font-medium text-xs text-gray-900 dark:text-white">
                                                {mat.name}
                                              </span>
                                            </div>
                                            {mat.categoryName && (
                                              <span className="text-[10px] text-gray-400 mt-0.5">
                                                {mat.categoryName} • Unit: {mat.unitOfMeasure || "PCS"}
                                              </span>
                                            )}
                                          </div>

                                          {/* Stock Level Chip */}
                                          <div className="flex items-center shrink-0">
                                            <Badge
                                              size="sm"
                                              color={getStockBadgeColor(mat.currentStock)}
                                              variant="light"
                                            >
                                              {mat.currentStock} {mat.unitOfMeasure || "units"}
                                            </Badge>
                                          </div>
                                        </button>
                                      );
                                    })
                                  )}
                                </div>
                              </div>
                            )}
                          </div>
                        );
                      })()}
                      {fieldErrors[`line_${index}_material`] && (
                        <p className="mt-1.5 text-xs text-error-500">
                          {fieldErrors[`line_${index}_material`]}
                        </p>
                      )}
                    </div>

                    {/* Quantity */}
                    <div className="lg:col-span-2">
                      <Label>Quantity ({line.unitOfMeasure || "Units"}) *</Label>
                      <Input
                        type="number"
                        min="1"
                        disabled={isReadOnly}
                        value={line.quantity}
                        onChange={(e) =>
                          handleLineChange(
                            line.tempId,
                            "quantity",
                            Math.max(1, parseInt(e.target.value) || 1)
                          )
                        }
                        placeholder="Qty"
                      />
                      {fieldErrors[`line_${index}_quantity`] && (
                        <p className="mt-1 text-[11px] text-error-500">
                          {fieldErrors[`line_${index}_quantity`]}
                        </p>
                      )}
                    </div>

                    {/* Unit Price */}
                    <div className="lg:col-span-2">
                      <Label>Est. Price ({formData.currencyCode})</Label>
                      <Input
                        type="number"
                        step={0.01}
                        disabled={isReadOnly}
                        value={
                          line.estimatedUnitPrice !== undefined && line.estimatedUnitPrice !== null
                            ? line.estimatedUnitPrice
                            : ""
                        }
                        onChange={(e) =>
                          handleLineChange(
                            line.tempId,
                            "estimatedUnitPrice",
                            e.target.value === "" ? 0 : parseFloat(e.target.value) || 0
                          )
                        }
                        placeholder="0.00"
                      />
                      {line.originalCurrency &&
                        line.originalCurrency !== formData.currencyCode &&
                        line.originalPrice !== undefined && line.originalPrice > 0 && (
                          <div className="mt-1 flex flex-col gap-0.5">
                            <span className="inline-flex items-center gap-1 text-[11px] font-medium text-emerald-700 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-500/10 px-1.5 py-0.5 rounded border border-emerald-200 dark:border-emerald-500/20">
                              <span>Orig: {parseNumericAmount(line.originalPrice).toFixed(2)} {line.originalCurrency}</span>
                              {line.exchangeRateUsed && (
                                <span className="text-gray-500 dark:text-gray-400 font-normal">
                                  (@ {Number(line.exchangeRateUsed).toFixed(4)})
                                </span>
                              )}
                            </span>
                          </div>
                        )}
                    </div>

                    {/* Line Total */}
                    <div className="lg:col-span-3">
                      <Label>Estimated Total</Label>
                      <div className="h-11 flex items-center px-3.5 rounded-lg bg-gray-100 dark:bg-gray-800 text-xs font-bold text-gray-900 dark:text-white border border-gray-200 dark:border-gray-700">
                        {new Intl.NumberFormat("en-US", {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2,
                        }).format(parseNumericAmount(line.lineTotal))}{" "}
                        {formData.currencyCode}
                      </div>
                    </div>

                    {/* Target Supplier */}
                    <div className="lg:col-span-6">
                      <Label>Target Supplier</Label>
                      {(() => {
                        const selectedMat = materials.find(
                          (m) => m.id === line.materialId || m.code === line.materialCode
                        );
                        const assignedSupplierId = selectedMat?.supplierId || line.supplierId;
                        const visibleSuppliers = assignedSupplierId
                          ? suppliers.filter((s) => s.id === assignedSupplierId)
                          : suppliers;

                        return (
                          <div className="relative">
                            <select
                              value={line.supplierId}
                              disabled={isReadOnly || !!selectedMat?.supplierId}
                              onChange={(e) => {
                                const supId = e.target.value;
                                const sup = suppliers.find((s) => s.id === supId);
                                handleLineChange(line.tempId, "supplierId", supId);
                                handleLineChange(
                                  line.tempId,
                                  "supplierCode",
                                  (sup as any)?.code || ""
                                );
                              }}
                              className={`h-11 w-full appearance-none rounded-lg border px-4 py-2.5 pr-10 text-sm shadow-theme-xs transition-all focus:outline-hidden focus:ring-3 ${
                                isReadOnly || selectedMat?.supplierId
                                  ? "border-gray-300 bg-gray-100/80 cursor-not-allowed text-gray-500 font-medium dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400"
                                  : "border-gray-300 bg-white text-gray-800 focus:border-brand-300 focus:ring-brand-500/20 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                              }`}
                            >
                              {!selectedMat?.supplierId && (
                                <option value="">-- Select Material First / Any Supplier --</option>
                              )}
                              {visibleSuppliers.map((sup) => (
                                <option key={sup.id} value={sup.id}>
                                  {sup.name} {selectedMat?.supplierId ? "(Dedicated Supplier)" : ""}
                                </option>
                              ))}
                            </select>
                            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3.5 text-gray-400 dark:text-gray-500">
                              <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                              </svg>
                            </div>
                          </div>
                        );
                      })()}
                    </div>

                    {/* Notes / Special Terms */}
                    <div className="lg:col-span-6">
                      <Label>Line Notes / Delivery Terms</Label>
                      <Input
                        type="text"
                        disabled={isReadOnly}
                        value={line.notes || ""}
                        onChange={(e) =>
                          handleLineChange(line.tempId, "notes", e.target.value)
                        }
                        placeholder="e.g. Deliver to Warehouse Bay 3"
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Total Aggregate Box */}
            <div className="mt-6 flex flex-col sm:flex-row items-center justify-between p-4 rounded-xl bg-brand-50/50 dark:bg-brand-500/10 border border-brand-200/60 dark:border-brand-500/20">
              <div className="flex items-center gap-2 flex-wrap">
                <span className="text-xs text-gray-600 dark:text-gray-300">
                  Total Lines: <strong className="text-gray-900 dark:text-white">{lines.length}</strong>
                </span>
                <span className="text-gray-300 dark:text-gray-600">|</span>
                <span className="text-xs text-gray-600 dark:text-gray-300">
                  Target Currency: <strong className="text-gray-900 dark:text-white">{formData.currencyCode}</strong>
                </span>
                <span className="text-gray-300 dark:text-gray-600">|</span>
                <span className="inline-flex items-center gap-1.5 text-[11px] text-emerald-600 dark:text-emerald-400 font-medium">
                  <span className="size-1.5 rounded-full bg-emerald-500 animate-pulse" />
                  Live FX Active
                </span>
              </div>
              <div className="flex items-center gap-2 mt-2 sm:mt-0">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400">
                  Total Estimated Commitment:
                </span>
                <span className="text-lg font-bold text-brand-600 dark:text-brand-400">
                  {new Intl.NumberFormat("en-US", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  }).format(parseNumericAmount(totalRequisitionAmount))}{" "}
                  {formData.currencyCode}
                </span>
              </div>
            </div>
          </div>

          {/* Form Actions Footer */}
          <div className="flex flex-wrap items-center justify-between gap-3 pt-6 border-t border-gray-100 dark:border-white/[0.07]">
            <Button
              type="button"
              variant="outline"
              onClick={handleCancelClick}
              disabled={isSubmitting || isSavingDraft}
            >
              Cancel
            </Button>

            <div className="flex items-center gap-3">
              {/* Save as Draft Button (Only when not read-only) */}
              {!isReadOnly && (
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => handleSave(true)}
                  disabled={isSubmitting || isSavingDraft}
                  className="border-gray-300 text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-gray-800"
                >
                  {isSavingDraft ? (
                    <span className="flex items-center gap-2">
                      <svg className="size-4 animate-spin text-gray-500" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                      </svg>
                      Saving Draft...
                    </span>
                  ) : (
                    <span className="flex items-center gap-1.5">
                      <svg className="size-4 text-gray-500 dark:text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" />
                      </svg>
                      Save as Draft
                    </span>
                  )}
                </Button>
              )}

              {/* Primary Create / Update Requisition (Submits) */}
              <Button
                type="submit"
                disabled={isSubmitting || isSavingDraft || isReadOnly}
                className={isReadOnly ? "bg-gray-400 hover:bg-gray-400 cursor-not-allowed text-white dark:bg-gray-700 dark:text-gray-400" : ""}
              >
                {isSubmitting ? (
                  <span className="flex items-center gap-2">
                    <svg className="size-4 animate-spin text-white" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                    </svg>
                    {isEditMode ? "Updating Requisition..." : "Submitting Requisition..."}
                  </span>
                ) : isReadOnly ? (
                  <span className="flex items-center gap-1.5">
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                    Modification Locked ({existingStatus})
                  </span>
                ) : (
                  <span className="flex items-center gap-1.5">
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    {isEditMode ? "Update Requisition" : "Create Requisition"}
                  </span>
                )}
              </Button>
            </div>
          </div>
        </div>
      </form>

      {/* Exit Confirmation Modal */}
      {showExitModal && (
        <div className="fixed inset-0 z-99999 flex items-center justify-center bg-gray-900/50 backdrop-blur-xs p-4 animate-fade-in">
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl dark:bg-gray-900 border border-gray-100 dark:border-white/[0.08]">
            <div className="flex items-center gap-3 mb-3">
              <div className="flex size-10 items-center justify-center rounded-full bg-amber-100 text-amber-600 dark:bg-amber-500/20 dark:text-amber-400">
                <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <div>
                <h3 className="text-base font-semibold text-gray-900 dark:text-white">
                  Unsaved Requisition in Progress
                </h3>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  You have entered requisition information.
                </p>
              </div>
            </div>
            <p className="text-sm text-gray-600 dark:text-gray-300 mb-6">
              Would you like to store your progress as a <strong>Draft</strong> so you can finish it later, or discard your changes?
            </p>
            <div className="flex flex-col-reverse sm:flex-row items-center justify-end gap-2.5">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setShowExitModal(false)}
              >
                Keep Editing
              </Button>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => {
                  setShowExitModal(false);
                  navigate("/requisitions");
                }}
                className="text-red-600 hover:bg-red-50 hover:border-red-200 dark:text-red-400 dark:hover:bg-red-500/10"
              >
                Discard & Exit
              </Button>
              <Button
                type="button"
                size="sm"
                disabled={isSavingDraft}
                onClick={async () => {
                  const saved = await handleSave(true);
                  if (saved) {
                    setShowExitModal(false);
                  }
                }}
                className="bg-brand-600 hover:bg-brand-700 text-white"
              >
                {isSavingDraft ? "Saving Draft..." : "Save as Draft & Exit"}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
