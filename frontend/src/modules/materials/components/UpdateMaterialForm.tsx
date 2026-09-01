import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { MaterialStatus, MaterialType, UnitOfMeasure, CurrencyCode } from "../enums";
import CustomSelect from "./CustomSelect";
import CategoryTreeSelect from "../../categories/components/CategoryTreeSelect";
import { materialApi } from "../services/materialApi";
import { supplierApi } from "../../suppliers/services/supplierApi";
import type { UpdateMaterialRequest } from "../types/UpdateMaterialRequest";
import type { SupplierListItem } from "../../suppliers/types/SupplierListItem";
import type { ErrorResponse } from "../../../shared/types/ErrorResponse";

// Shared Components
import Input from "../../../shared/components/form/input/InputField";
import TextArea from "../../../shared/components/form/input/TextArea";
import Label from "../../../shared/components/form/Label";
import Button from "../../../shared/components/ui/button/Button";
import Toast from "../../../shared/components/ui/notifications/Toast";

interface Props {
  id: string;
}

export default function UpdateMaterialForm({ id }: Props) {
  const navigate = useNavigate();
  const [keywords, setKeywords] = useState<string[]>([]);
  const [keywordInput, setKeywordInput] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [submitMessage, setSubmitMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [suppliers, setSuppliers] = useState<SupplierListItem[]>([]);

  const [formData, setFormData] = useState({
    name: "",
    shortDescription: "",
    alternativeName: "",
    description: "",
    categoryId: "",
    supplierId: "",
    materialType: "",
    status: "ACTIVE",
    updatedBy: "System",
    unitOfMeasure: "",
    currentStock: 0,
    minimumStock: 0,
    maximumStock: 0,
    safetyStock: 0,
    reorderPoint: 0,
    economicOrderQuantity: 0,
    standardPrice: 0,
    standardPriceCurrency: "MAD",
    costPrice: 0,
    costPriceCurrency: "MAD",
  });

  // Load suppliers
  useEffect(() => {
    supplierApi
      .getAll()
      .then((res) => setSuppliers(res.data))
      .catch((err) => console.error("Failed to load suppliers", err));
  }, []);

  // Load material data by ID
  useEffect(() => {
    if (!id) return;
    setIsLoading(true);
    materialApi
      .getById(id)
      .then((res) => {
        const m = res.data;
        setFormData({
          name: m.name || "",
          shortDescription: m.shortDescription || "",
          alternativeName: m.alternativeName || "",
          description: m.description || "",
          categoryId: m.categoryId || "",
          supplierId: m.supplierId || "",
          materialType: m.materialType || "",
          status: m.status || "ACTIVE",
          updatedBy: "System",
          unitOfMeasure: m.unitOfMeasure || "",
          currentStock: m.currentStock ?? 0,
          minimumStock: m.minimumStock ?? 0,
          maximumStock: m.maximumStock ?? 0,
          safetyStock: m.safetyStock ?? 0,
          reorderPoint: m.reorderPoint ?? 0,
          economicOrderQuantity: m.economicOrderQuantity ?? 0,
          standardPrice: m.standardPrice ? Number(m.standardPrice) : 0,
          standardPriceCurrency: m.currencyCode || "MAD",
          costPrice: m.costPrice ? Number(m.costPrice) : 0,
          costPriceCurrency: m.currencyCode || "MAD",
        });
        // Parse comma-separated search keywords
        if (m.searchKeywords) {
          setKeywords(m.searchKeywords.split(",").map((k: string) => k.trim()).filter(Boolean));
        }
      })
      .catch((err) => {
        console.error("Failed to load material:", err);
        setSubmitMessage({ type: "error", text: "Failed to load material data." });
      })
      .finally(() => setIsLoading(false));
  }, [id]);

  // Auto-dismiss toast
  useEffect(() => {
    if (submitMessage) {
      const timer = setTimeout(() => setSubmitMessage(null), 5000);
      return () => clearTimeout(timer);
    }
  }, [submitMessage]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "number" ? (value === "" ? "" : Number(value)) : value,
    }));
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const handleStringChange = (name: string, value: string) => {
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const handleKeywordKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      e.preventDefault();
      const trimmed = keywordInput.trim();
      if (trimmed && !keywords.includes(trimmed)) {
        setKeywords([...keywords, trimmed]);
        setKeywordInput("");
      }
    }
  };

  const removeKeyword = (kw: string) => {
    setKeywords(keywords.filter((k) => k !== kw));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setSubmitMessage(null);
    setFieldErrors({});

    // Client-side mandatory validation
    const newErrors: Record<string, string> = {};
    if (!formData.name) newErrors.name = "Name is mandatory";
    if (!formData.categoryId) newErrors.categoryId = "Category is mandatory";
    if (!formData.supplierId) newErrors.supplierId = "Supplier is mandatory";
    if (!formData.materialType) newErrors.materialType = "Material Type is mandatory";
    if (!formData.unitOfMeasure) newErrors.unitOfMeasure = "Unit of Measure is mandatory";
    if (!formData.status) newErrors.status = "Status is mandatory";

    if (Object.keys(newErrors).length > 0) {
      setFieldErrors(newErrors);
      setSubmitMessage({ type: "error", text: "Validation failed. Please check the highlighted fields below." });
      setIsSubmitting(false);
      return;
    }

    try {
      const requestPayload: UpdateMaterialRequest = {
        ...formData,
        materialType: formData.materialType || undefined,
        unitOfMeasure: formData.unitOfMeasure || undefined,
        status: formData.status || undefined,
        standardPriceCurrency: formData.standardPriceCurrency || undefined,
        costPriceCurrency: formData.costPriceCurrency || undefined,
        categoryId: formData.categoryId || undefined,
        supplierId: formData.supplierId || undefined,
        searchKeywords: keywords.join(","),
        currentStock: Number(formData.currentStock) || 0,
        minimumStock: Number(formData.minimumStock) || 0,
        maximumStock: Number(formData.maximumStock) || 0,
        safetyStock: Number(formData.safetyStock) || 0,
        reorderPoint: Number(formData.reorderPoint) || 0,
        economicOrderQuantity: Number(formData.economicOrderQuantity) || 0,
        standardPrice: Number(formData.standardPrice) || 0,
        costPrice: Number(formData.costPrice) || 0,
      };

      await materialApi.update(id, requestPayload);
      setSubmitMessage({ type: "success", text: "Material updated successfully!" });

      // Navigate back to list after a short delay
      setTimeout(() => navigate("/materials"), 1500);
    } catch (error) {
      console.error("Error updating material:", error);

      if (axios.isAxiosError(error) && error.response?.data) {
        const errorData = error.response.data as ErrorResponse;
        if (errorData.validationErrors) {
          setFieldErrors(errorData.validationErrors);
          setSubmitMessage({ type: "error", text: "Validation failed. Please check the highlighted fields below." });
        } else {
          setSubmitMessage({ type: "error", text: errorData.message || "An error occurred while updating the material." });
        }
      } else {
        setSubmitMessage({ type: "error", text: "Failed to connect to the server. Please check your network and try again." });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03] p-10 flex items-center justify-center">
        <div className="flex items-center gap-3 text-gray-500">
          <svg className="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
          </svg>
          <span>Loading material data...</span>
        </div>
      </div>
    );
  }

  return (
    <>
      {/* Floating Toast */}
      {submitMessage && (
        <div className="fixed top-8 right-7 z-50 animate-in fade-in slide-in-from-top-5 duration-300">
          <Toast
            variant={submitMessage.type}
            message={submitMessage.text}
            onClose={() => setSubmitMessage(null)}
            duration={5000}
          />
        </div>
      )}

      <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
        <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Update Material
          </h3>
        </div>

        <form onSubmit={handleSubmit} className="p-6">

          {/* 1. Basic Information */}
          <div className="mb-8">
            <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
              1. Basic Information
            </h4>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div className="sm:col-span-2">
                <Label>Name *</Label>
                <Input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="Enter material name"
                  error={!!fieldErrors.name}
                  hint={fieldErrors.name}
                />
              </div>

              <div>
                <Label>Short Description</Label>
                <Input
                  type="text"
                  name="shortDescription"
                  value={formData.shortDescription}
                  onChange={handleChange}
                  placeholder="Brief summary"
                  error={!!fieldErrors.shortDescription}
                  hint={fieldErrors.shortDescription}
                />
              </div>
              <div>
                <Label>Alternative Name</Label>
                <Input
                  type="text"
                  name="alternativeName"
                  value={formData.alternativeName}
                  onChange={handleChange}
                  placeholder="Enter alternative name"
                  error={!!fieldErrors.alternativeName}
                  hint={fieldErrors.alternativeName}
                />
              </div>

              <div className="sm:col-span-2">
                <Label>Full Description</Label>
                <TextArea
                  value={formData.description}
                  onChange={(val) => handleStringChange("description", val)}
                  rows={3}
                  placeholder="Detailed description of the material..."
                  error={!!fieldErrors.description}
                  hint={fieldErrors.description}
                />
              </div>

              <div className="sm:col-span-2">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                  <div>
                    <Label>Search Keywords</Label>
                    <input
                      type="text"
                      value={keywordInput}
                      onChange={(e) => setKeywordInput(e.target.value)}
                      onKeyDown={handleKeywordKeyDown}
                      placeholder="Type and press Enter to add tags"
                      className="h-11 w-full rounded-lg border border-gray-300 appearance-none px-4 py-2.5 text-sm shadow-theme-xs placeholder:text-gray-400 focus:outline-hidden focus:ring-3 focus:border-brand-300 focus:ring-brand-500/20 bg-transparent text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                    />
                    {fieldErrors.searchKeywords && (
                      <p className="mt-1.5 text-xs text-error-500">{fieldErrors.searchKeywords}</p>
                    )}
                  </div>
                </div>
                {keywords.length > 0 && (
                  <div className="mt-3 flex flex-wrap gap-2">
                    {keywords.map((keyword, index) => (
                      <span
                        key={index}
                        className="inline-flex items-center gap-1.5 rounded-full bg-gray-100 px-3 py-1.5 text-sm font-medium text-gray-700 dark:bg-gray-800 dark:text-gray-300"
                      >
                        {keyword}
                        <button type="button" onClick={() => removeKeyword(keyword)} className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">
                          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                          </svg>
                        </button>
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* 2. Classification & Ownership */}
          <div className="mb-8">
            <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
              2. Classification &amp; Ownership
            </h4>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
              <div>
                <Label>Category *</Label>
                <CategoryTreeSelect
                  value={formData.categoryId}
                  onChange={(val) => handleStringChange("categoryId", val)}
                  placeholder="Select Category"
                  maxHeightClass="max-h-[248px]"
                  error={!!fieldErrors.categoryId}
                />
                {fieldErrors.categoryId && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.categoryId}</p>}
              </div>
              <div>
                <Label>Supplier *</Label>
                <CustomSelect
                  value={formData.supplierId}
                  onChange={(val) => handleStringChange("supplierId", val)}
                  placeholder="Select Supplier"
                  showSearch
                  options={suppliers.map((sup) => ({ value: sup.id, label: sup.name }))}
                  error={!!fieldErrors.supplierId}
                />
                {fieldErrors.supplierId && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.supplierId}</p>}
              </div>
              <div>
                <Label>Material Type *</Label>
                <CustomSelect
                  value={formData.materialType}
                  onChange={(val) => handleStringChange("materialType", val)}
                  placeholder="Select Type"
                  showSearch
                  options={Object.entries(MaterialType).map(([key, val]) => ({ value: val, label: key.replace(/_/g, " ") }))}
                  error={!!fieldErrors.materialType}
                />
                {fieldErrors.materialType && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.materialType}</p>}
              </div>
              <div>
                <Label>Status *</Label>
                <CustomSelect
                  value={formData.status}
                  onChange={(val) => handleStringChange("status", val)}
                  placeholder="Select Status"
                  options={Object.entries(MaterialStatus).map(([key, val]) => ({ value: val, label: key }))}
                  error={!!fieldErrors.status}
                />
                {fieldErrors.status && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.status}</p>}
              </div>
              <div>
                <Label>Updated By</Label>
                <Input
                  type="text"
                  name="updatedBy"
                  value={formData.updatedBy}
                  onChange={handleChange}
                  placeholder="User ID or Name"
                  error={!!fieldErrors.updatedBy}
                  hint={fieldErrors.updatedBy}
                />
              </div>
            </div>
          </div>

          {/* 3. Inventory & Stock Management */}
          <div className="mb-8">
            <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
              3. Inventory &amp; Stock Management
            </h4>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
              <div className="sm:col-span-2 lg:col-span-4 lg:w-1/4">
                <Label>Unit of Measure *</Label>
                <CustomSelect
                  value={formData.unitOfMeasure}
                  onChange={(val) => handleStringChange("unitOfMeasure", val)}
                  placeholder="Select UOM"
                  showSearch
                  options={Object.entries(UnitOfMeasure).map(([key, val]) => ({ value: val, label: key }))}
                  error={!!fieldErrors.unitOfMeasure}
                />
                {fieldErrors.unitOfMeasure && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.unitOfMeasure}</p>}
              </div>

              <div>
                <Label>Current Stock</Label>
                <Input type="number" name="currentStock" value={formData.currentStock} onChange={handleChange} placeholder="0" error={!!fieldErrors.currentStock} hint={fieldErrors.currentStock} />
              </div>
              <div>
                <Label>Minimum Stock</Label>
                <Input type="number" name="minimumStock" value={formData.minimumStock} onChange={handleChange} placeholder="0" error={!!fieldErrors.minimumStock} hint={fieldErrors.minimumStock} />
              </div>
              <div>
                <Label>Maximum Stock</Label>
                <Input type="number" name="maximumStock" value={formData.maximumStock} onChange={handleChange} placeholder="0" error={!!fieldErrors.maximumStock} hint={fieldErrors.maximumStock} />
              </div>
              <div>
                <Label>Safety Stock</Label>
                <Input type="number" name="safetyStock" value={formData.safetyStock} onChange={handleChange} placeholder="0" error={!!fieldErrors.safetyStock} hint={fieldErrors.safetyStock} />
              </div>
              <div>
                <Label>Reorder Point</Label>
                <Input type="number" name="reorderPoint" value={formData.reorderPoint} onChange={handleChange} placeholder="0" error={!!fieldErrors.reorderPoint} hint={fieldErrors.reorderPoint} />
              </div>
              <div>
                <Label>Economic Order Qty</Label>
                <Input type="number" name="economicOrderQuantity" value={formData.economicOrderQuantity} onChange={handleChange} placeholder="0" error={!!fieldErrors.economicOrderQuantity} hint={fieldErrors.economicOrderQuantity} />
              </div>
            </div>
          </div>

          {/* 4. Pricing & Cost */}
          <div className="mb-8">
            <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
              4. Pricing &amp; Cost
            </h4>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
              <div>
                <Label>Standard Price *</Label>
                <Input type="number" step={0.01} name="standardPrice" value={formData.standardPrice} onChange={handleChange} placeholder="0.00" error={!!fieldErrors.standardPrice} hint={fieldErrors.standardPrice} />
              </div>
              <div>
                <Label>Standard Price Currency *</Label>
                <CustomSelect
                  value={formData.standardPriceCurrency}
                  onChange={(val) => handleStringChange("standardPriceCurrency", val)}
                  options={Object.entries(CurrencyCode).map(([key, val]) => ({ value: val, label: key }))}
                  error={!!fieldErrors.standardPriceCurrency}
                />
                {fieldErrors.standardPriceCurrency && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.standardPriceCurrency}</p>}
              </div>
              <div>
                <Label>Cost Price</Label>
                <Input type="number" step={0.01} name="costPrice" value={formData.costPrice} onChange={handleChange} placeholder="0.00" error={!!fieldErrors.costPrice} hint={fieldErrors.costPrice} />
              </div>
              <div>
                <Label>Cost Price Currency</Label>
                <CustomSelect
                  value={formData.costPriceCurrency}
                  onChange={(val) => handleStringChange("costPriceCurrency", val)}
                  options={Object.entries(CurrencyCode).map(([key, val]) => ({ value: val, label: key }))}
                  error={!!fieldErrors.costPriceCurrency}
                />
                {fieldErrors.costPriceCurrency && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.costPriceCurrency}</p>}
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="mt-10 flex flex-wrap items-center justify-end gap-3 pt-6 border-t border-gray-200 dark:border-gray-800">
            <Button
              variant="outline"
              onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                e.preventDefault();
                navigate("/materials");
              }}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              disabled={isSubmitting}
              startIcon={
                isSubmitting ? (
                  <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                ) : (
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" />
                  </svg>
                )
              }
            >
              {isSubmitting ? "Saving..." : "Save Changes"}
            </Button>
          </div>
        </form>
      </div>
    </>
  );
}
