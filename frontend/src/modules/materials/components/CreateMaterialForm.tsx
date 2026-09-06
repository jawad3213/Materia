import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { MaterialStatus, MaterialType, UnitOfMeasure, CurrencyCode } from "../enums";
import CustomSelect from "./CustomSelect";
import CategoryTreeSelect from "../../categories/components/CategoryTreeSelect";
import { materialApi } from "../services/materialApi";
import { supplierApi } from "../../suppliers/services/supplierApi";
import type { CreateMaterialRequest } from "../types/CreateMaterialRequest";
import type { CategoryListItem } from "../../categories/types/CategoryListItem";
import type { SupplierListItem } from "../../suppliers/types/SupplierListItem";
import type { ErrorResponse } from "../../../shared/types/ErrorResponse";

// Shared Components
import Input from "../../../shared/components/form/input/InputField";
import TextArea from "../../../shared/components/form/input/TextArea";
import Label from "../../../shared/components/form/Label";
import Button from "../../../shared/components/ui/button/Button";
import Toast from "../../../shared/components/ui/notifications/Toast";

export default function CreateMaterialForm() {
  const navigate = useNavigate();
  const [keywords, setKeywords] = useState<string[]>([]);
  const [keywordInput, setKeywordInput] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitMessage, setSubmitMessage] = useState<{type: 'success' | 'error', text: string} | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const [suppliers, setSuppliers] = useState<SupplierListItem[]>([]);

  React.useEffect(() => {
    supplierApi.getAllUnpaginated()
      .then((res) => {
        const data = Array.isArray(res.data)
          ? res.data
          : (res.data as any)?.content || [];
        setSuppliers(data);
      })
      .catch((err) => console.error("Failed to load suppliers", err));
  }, []);

  // Auto-dismiss the toast notification after 5 seconds
  React.useEffect(() => {
    if (submitMessage) {
      const timer = setTimeout(() => {
        setSubmitMessage(null);
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [submitMessage]);

  // Group all state into a single efficient form object
  const [formData, setFormData] = useState({
    name: "",
    shortDescription: "",
    alternativeName: "",
    description: "",
    categoryId: "",
    supplierId: "",
    materialType: "",
    status: "ACTIVE",
    createdBy: "",
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

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'number' ? (value === '' ? '' : Number(value)) : value
    }));
    // Clear error for this field as the user types
    if (fieldErrors[name]) {
      setFieldErrors(prev => ({ ...prev, [name]: "" }));
    }
  };

  const handleStringChange = (name: string, value: string) => {
    setFormData(prev => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) {
      setFieldErrors(prev => ({ ...prev, [name]: "" }));
    }
  };

  const handleKeywordKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      const trimmed = keywordInput.trim();
      if (trimmed && !keywords.includes(trimmed)) {
        setKeywords([...keywords, trimmed]);
        setKeywordInput("");
      }
    }
  };

  const removeKeyword = (keywordToRemove: string) => {
    setKeywords(keywords.filter(k => k !== keywordToRemove));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setSubmitMessage(null);
    setFieldErrors({}); // Reset errors on new submission
    
    try {
      const requestPayload: CreateMaterialRequest = {
        ...formData,
        materialType: formData.materialType || undefined as any,
        unitOfMeasure: formData.unitOfMeasure || undefined as any,
        status: formData.status || undefined as any,
        standardPriceCurrency: formData.standardPriceCurrency || undefined as any,
        costPriceCurrency: formData.costPriceCurrency || undefined as any,
        categoryId: formData.categoryId || undefined as any,
        supplierId: formData.supplierId || undefined as any,
        searchKeywords: keywords.join(","),
        // Ensure numbers are properly cast for the backend
        currentStock: Number(formData.currentStock) || 0,
        minimumStock: Number(formData.minimumStock) || 0,
        maximumStock: Number(formData.maximumStock) || 0,
        safetyStock: Number(formData.safetyStock) || 0,
        reorderPoint: Number(formData.reorderPoint) || 0,
        economicOrderQuantity: Number(formData.economicOrderQuantity) || 0,
        standardPrice: Number(formData.standardPrice) || 0,
        costPrice: Number(formData.costPrice) || 0,
      };
      
      await materialApi.create(requestPayload);
      setSubmitMessage({ type: 'success', text: 'Material created successfully!' });
      
    } catch (error) {
      console.error("Error creating material:", error);
      
      // Handle Axios Errors that return our GlobalExceptionHandler's ErrorResponse
      if (axios.isAxiosError(error) && error.response?.data) {
        const errorData = error.response.data as ErrorResponse;
        
        if (errorData.validationErrors) {
          // It's a validation error, mark the specific fields
          setFieldErrors(errorData.validationErrors);
          setSubmitMessage({ type: 'error', text: 'Validation failed. Please check the highlighted fields below.' });
        } else {
          // It's a business error or other backend exception
          setSubmitMessage({ type: 'error', text: errorData.message || 'An error occurred while creating the material.' });
        }
      } else {
        // Fallback for network issues or unexpected errors
        setSubmitMessage({ type: 'error', text: 'Failed to connect to the server. Please check your network and try again.' });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      {/* Floating Toast Notification */}
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
          Create Material
        </h3>
      </div>
      
      <form onSubmit={handleSubmit} className="p-6">
        
        {/* (Banner moved to floating Toast outside the form) */}
        
        {/* Basic Information */}
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
                onChange={(val) => handleStringChange('description', val)}
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
                  <div className="relative">
                    <input
                      type="text"
                      value={keywordInput}
                      onChange={(e) => setKeywordInput(e.target.value)}
                      onKeyDown={handleKeywordKeyDown}
                      placeholder="Type and press Enter to add tags"
                      className="h-11 w-full rounded-lg border border-gray-300 appearance-none px-4 py-2.5 text-sm shadow-theme-xs placeholder:text-gray-400 focus:outline-hidden focus:ring-3 focus:border-brand-300 focus:ring-brand-500/20 bg-transparent text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                    />
                  </div>
                  {fieldErrors.searchKeywords && (
                    <p className="mt-1.5 text-xs text-error-500">{fieldErrors.searchKeywords}</p>
                  )}
                </div>
              </div>
              {keywords.length > 0 && (
                <div className="mt-3 flex flex-wrap gap-2">
                  {keywords.map((keyword, index) => (
                    <span key={index} className="inline-flex items-center gap-1.5 rounded-full bg-gray-100 px-3 py-1.5 text-sm font-medium text-gray-700 dark:bg-gray-800 dark:text-gray-300">
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

        {/* Classification & Ownership */}
        <div className="mb-8">
          <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
            2. Classification & Ownership
          </h4>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            <div>
              <Label>Category ID *</Label>
              <CategoryTreeSelect
                value={formData.categoryId}
                onChange={(val) => handleStringChange('categoryId', val)}
                placeholder="Select Category"
                maxHeightClass="max-h-[248px]"
                error={!!fieldErrors.categoryId}
              />
              {fieldErrors.categoryId && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.categoryId}</p>}
            </div>
            <div>
              <Label>Supplier ID *</Label>
              <CustomSelect
                value={formData.supplierId}
                onChange={(val) => handleStringChange('supplierId', val)}
                placeholder="Select Supplier"
                showSearch
                options={(Array.isArray(suppliers) ? suppliers : []).map((sup) => ({
                  value: sup.id,
                  label: sup.name
                }))}
                error={!!fieldErrors.supplierId}
              />
              {fieldErrors.supplierId && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.supplierId}</p>}
            </div>
            <div>
              <Label>Material Type *</Label>
              <CustomSelect
                value={formData.materialType}
                onChange={(val) => handleStringChange('materialType', val)}
                placeholder="Select Type"
                showSearch
                options={Object.entries(MaterialType).map(([key, val]) => ({
                  value: val,
                  label: key.replace(/_/g, ' ')
                }))}
                error={!!fieldErrors.materialType}
              />
              {fieldErrors.materialType && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.materialType}</p>}
            </div>
            <div>
              <Label>Status</Label>
              <CustomSelect
                value={formData.status}
                onChange={(val) => handleStringChange('status', val)}
                placeholder="Select Status"
                options={Object.entries(MaterialStatus).map(([key, val]) => ({
                  value: val,
                  label: key
                }))}
                error={!!fieldErrors.status}
              />
              {fieldErrors.status && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.status}</p>}
            </div>
            <div>
              <Label>Created By *</Label>
              <Input
                type="text"
                name="createdBy"
                value={formData.createdBy}
                onChange={handleChange}
                placeholder="User ID or Name"
                error={!!fieldErrors.createdBy}
                hint={fieldErrors.createdBy}
              />
            </div>
          </div>
        </div>

        {/* Inventory & Stock Management */}
        <div className="mb-8">
          <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
            3. Inventory & Stock Management
          </h4>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            <div className="sm:col-span-2 lg:col-span-4 lg:w-1/4">
              <Label>Unit of Measure *</Label>
              <CustomSelect
                value={formData.unitOfMeasure}
                onChange={(val) => handleStringChange('unitOfMeasure', val)}
                placeholder="Select UOM"
                showSearch
                options={Object.entries(UnitOfMeasure).map(([key, val]) => ({
                  value: val,
                  label: key
                }))}
                error={!!fieldErrors.unitOfMeasure}
              />
              {fieldErrors.unitOfMeasure && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.unitOfMeasure}</p>}
            </div>
            
            <div>
              <Label>Current Stock *</Label>
              <Input
                type="number"
                name="currentStock"
                value={formData.currentStock}
                onChange={handleChange}
                placeholder="0"
                error={!!fieldErrors.currentStock}
                hint={fieldErrors.currentStock}
              />
            </div>
            <div>
              <Label>Minimum Stock *</Label>
              <Input
                type="number"
                name="minimumStock"
                value={formData.minimumStock}
                onChange={handleChange}
                placeholder="0"
                error={!!fieldErrors.minimumStock}
                hint={fieldErrors.minimumStock}
              />
            </div>
            <div>
              <Label>Maximum Stock *</Label>
              <Input
                type="number"
                name="maximumStock"
                value={formData.maximumStock}
                onChange={handleChange}
                placeholder="0"
                error={!!fieldErrors.maximumStock}
                hint={fieldErrors.maximumStock}
              />
            </div>
            <div>
              <Label>Safety Stock *</Label>
              <Input
                type="number"
                name="safetyStock"
                value={formData.safetyStock}
                onChange={handleChange}
                placeholder="0"
                error={!!fieldErrors.safetyStock}
                hint={fieldErrors.safetyStock}
              />
            </div>
            <div>
              <Label>Reorder Point</Label>
              <Input
                type="number"
                name="reorderPoint"
                value={formData.reorderPoint}
                onChange={handleChange}
                placeholder="0"
                error={!!fieldErrors.reorderPoint}
                hint={fieldErrors.reorderPoint}
              />
            </div>
            <div>
              <Label>Economic Order Qty</Label>
              <Input
                type="number"
                name="economicOrderQuantity"
                value={formData.economicOrderQuantity}
                onChange={handleChange}
                placeholder="0"
                error={!!fieldErrors.economicOrderQuantity}
                hint={fieldErrors.economicOrderQuantity}
              />
            </div>
          </div>
        </div>

        {/* Pricing & Cost */}
        <div className="mb-8">
          <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
            4. Pricing & Cost
          </h4>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            <div>
              <Label>Standard Price *</Label>
              <Input
                type="number"
                step={0.01}
                name="standardPrice"
                value={formData.standardPrice}
                onChange={handleChange}
                placeholder="0.00"
                error={!!fieldErrors.standardPrice}
                hint={fieldErrors.standardPrice}
              />
            </div>
            <div>
              <Label>Standard Price Currency *</Label>
              <CustomSelect
                value={formData.standardPriceCurrency}
                onChange={(val) => handleStringChange('standardPriceCurrency', val)}
                options={Object.entries(CurrencyCode).map(([key, val]) => ({
                  value: val,
                  label: key
                }))}
                error={!!fieldErrors.standardPriceCurrency}
              />
              {fieldErrors.standardPriceCurrency && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.standardPriceCurrency}</p>}
            </div>
            <div>
              <Label>Cost Price</Label>
              <Input
                type="number"
                step={0.01}
                name="costPrice"
                value={formData.costPrice}
                onChange={handleChange}
                placeholder="0.00"
                error={!!fieldErrors.costPrice}
                hint={fieldErrors.costPrice}
              />
            </div>
            <div>
              <Label>Cost Price Currency</Label>
              <CustomSelect
                value={formData.costPriceCurrency}
                onChange={(val) => handleStringChange('costPriceCurrency', val)}
                options={Object.entries(CurrencyCode).map(([key, val]) => ({
                  value: val,
                  label: key
                }))}
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
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              ) : (
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" />
                </svg>
              )
            }
          >
            {isSubmitting ? 'Saving...' : 'Save Material'}
          </Button>
        </div>
      </form>
    </div>
    </>
  );
}
