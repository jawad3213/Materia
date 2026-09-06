import React, { useState, useEffect } from "react";
import axios from "axios";
import { useParams, useNavigate } from "react-router-dom";
import { CategoryType } from "../enums/CategoryType";
import { CategoryStatus } from "../enums/CategoryStatus";
import CustomSelect from "../../materials/components/CustomSelect";
import CategoryTreeSelect from "./CategoryTreeSelect";
import { categoryApi } from "../services/categoryApi";
import type { UpdateCategoryRequest } from "../types/UpdateCategoryRequest";
import type { ErrorResponse } from "../../../shared/types/ErrorResponse";

// Shared Components
import Input from "../../../shared/components/form/input/InputField";
import TextArea from "../../../shared/components/form/input/TextArea";
import Label from "../../../shared/components/form/Label";
import Button from "../../../shared/components/ui/button/Button";
import Toast from "../../../shared/components/ui/notifications/Toast";

export default function EditCategoryForm() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitMessage, setSubmitMessage] = useState<{type: 'success' | 'error', text: string} | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const [formData, setFormData] = useState({
    name: "",
    description: "",
    shortDescription: "",
    parentId: "",
    categoryType: "",
    status: "" as any,
    updatedBy: "Admin", // Should be derived from logged in user ideally
  });

  // Fetch existing category data
  useEffect(() => {
    const fetchCategory = async () => {
      if (!id) return;
      try {
        setIsLoading(true);
        const res = await categoryApi.getById(id);
        const cat = res.data;
        setFormData({
          name: cat.name || "",
          description: cat.description || "",
          shortDescription: cat.shortDescription || "",
          parentId: cat.parentId || "",
          categoryType: cat.categoryType || "",
          status: cat.status || "",
          updatedBy: "Admin",
        });
      } catch (err: any) {
        console.error("Failed to load category details:", err);
        setSubmitMessage({ type: 'error', text: "Failed to load category data. It may have been deleted." });
      } finally {
        setIsLoading(false);
      }
    };
    fetchCategory();
  }, [id]);

  useEffect(() => {
    if (submitMessage) {
      const timer = setTimeout(() => {
        setSubmitMessage(null);
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [submitMessage]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id) return;
    
    setIsSubmitting(true);
    setSubmitMessage(null);
    
    const newErrors: Record<string, string> = {};
    if (!formData.name) newErrors.name = "Name is mandatory";
    if (!formData.categoryType) newErrors.categoryType = "Category Type is mandatory";
    if (!formData.status) newErrors.status = "Status is mandatory";
    
    if (Object.keys(newErrors).length > 0) {
      setFieldErrors(newErrors);
      setSubmitMessage({ type: 'error', text: 'Validation failed. Please check the highlighted fields below.' });
      setIsSubmitting(false);
      return;
    }

    try {
      const requestPayload: UpdateCategoryRequest = {
        name: formData.name,
        description: formData.description || undefined,
        shortDescription: formData.shortDescription || undefined,
        parentId: formData.parentId || undefined,
        categoryType: formData.categoryType as any,
        status: formData.status as any,
        updatedBy: formData.updatedBy,
      };
      
      await categoryApi.update(id, requestPayload);
      setSubmitMessage({ type: 'success', text: 'Category updated successfully!' });
      
      // Optionally navigate away after a short delay
      setTimeout(() => {
        navigate(`/categories/view/${id}`);
      }, 1000);

    } catch (error) {
      console.error("Error updating category:", error);
      if (axios.isAxiosError(error) && error.response?.data) {
        const errorData = error.response.data as ErrorResponse;
        if (errorData.validationErrors) {
          setFieldErrors(errorData.validationErrors);
          setSubmitMessage({ type: 'error', text: 'Validation failed. Please check the highlighted fields below.' });
        } else {
          setSubmitMessage({ type: 'error', text: errorData.message || 'An error occurred while updating the category.' });
        }
      } else {
        setSubmitMessage({ type: 'error', text: 'Failed to connect to the server. Please check your network and try again.' });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <div className="p-8 text-center text-gray-500">Loading category data...</div>;
  }

  return (
    <>
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
            Edit Category
          </h3>
        </div>
        
        <form onSubmit={handleSubmit} className="p-6">
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
                  placeholder="Enter category name"
                  error={!!fieldErrors.name}
                  hint={fieldErrors.name}
                />
              </div>
              
              <div className="sm:col-span-2">
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

              <div className="sm:col-span-2">
                <Label>Full Description</Label>
                <TextArea
                  value={formData.description}
                  onChange={(val) => handleStringChange('description', val)}
                  rows={3}
                  placeholder="Detailed description of the category..."
                  error={!!fieldErrors.description}
                  hint={fieldErrors.description}
                />
              </div>
            </div>
          </div>

          <div className="mb-8">
            <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
              2. Classification & Settings
            </h4>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div>
                <Label>Parent Category</Label>
                <CategoryTreeSelect
                  value={formData.parentId}
                  onChange={(val) => handleStringChange('parentId', val)}
                  placeholder="None (Root Category)"
                  maxHeightClass="max-h-[248px]"
                  error={!!fieldErrors.parentId}
                  excludeId={id} // We shouldn't be able to select ourselves as parent
                />
                {fieldErrors.parentId && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.parentId}</p>}
                <p className="mt-1 text-xs text-gray-500">Leave blank to keep as top-level</p>
              </div>
              
              <div>
                <Label>Category Type *</Label>
                <CustomSelect
                  value={formData.categoryType}
                  onChange={(val) => handleStringChange('categoryType', val)}
                  placeholder="Select Type"
                  showSearch
                  options={Object.entries(CategoryType).map(([key, val]) => ({
                    value: val,
                    label: key.replace(/_/g, ' ')
                  }))}
                  error={!!fieldErrors.categoryType}
                />
                {fieldErrors.categoryType && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.categoryType}</p>}
              </div>

              <div>
                <Label>Status *</Label>
                <CustomSelect
                  value={formData.status}
                  onChange={(val) => handleStringChange('status', val)}
                  placeholder="Select Status"
                  options={Object.entries(CategoryStatus).map(([key, val]) => ({
                    value: val,
                    label: key
                  }))}
                  error={!!fieldErrors.status}
                />
                {fieldErrors.status && <p className="mt-1.5 text-xs text-error-500">{fieldErrors.status}</p>}
              </div>
              
              <div>
                <Label>Updated By *</Label>
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

          <div className="mt-10 flex flex-wrap items-center justify-end gap-3 pt-6 border-t border-gray-200 dark:border-gray-800">
            <Button 
              variant="outline" 
              onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                e.preventDefault();
                navigate("/categories");
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
              {isSubmitting ? 'Saving...' : 'Update Category'}
            </Button>
          </div>
        </form>
      </div>
    </>
  );
}
