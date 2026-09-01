import React, { useState, useEffect } from "react";
import axios from "axios";
import { supplierApi } from "../services/supplierApi";
import type { CreateSupplierRequest } from "../types/CreateSupplierRequest";
import type { ErrorResponse } from "../../../shared/types/ErrorResponse";

import Input from "../../../shared/components/form/input/InputField";
import TextArea from "../../../shared/components/form/input/TextArea";
import Label from "../../../shared/components/form/Label";
import Button from "../../../shared/components/ui/button/Button";
import Toast from "../../../shared/components/ui/notifications/Toast";
import PhoneInput from "../../../shared/components/form/group-input/PhoneInput";
import { EnvelopeIcon } from "../../../shared/icons";

export default function CreateSupplierForm() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitMessage, setSubmitMessage] = useState<{type: 'success' | 'error', text: string} | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  
  const [paymentTermsTags, setPaymentTermsTags] = useState<string[]>(["Net 30"]);
  const [paymentTermInput, setPaymentTermInput] = useState("");

  // Auto-dismiss the toast notification after 5 seconds
  useEffect(() => {
    if (submitMessage) {
      const timer = setTimeout(() => {
        setSubmitMessage(null);
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [submitMessage]);

  const [formData, setFormData] = useState({
    name: "",
    description: "",
    contactPerson: "",
    contactEmail: "",
    contactPhone: "",
    address: "",
    city: "",
    country: "",
    postalCode: "",
    currencyCode: "MAD",
    createdBy: "System",
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'number' ? (value === '' ? '' : Number(value)) : value
    }));
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

  const handlePaymentTermKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" || e.key === "Tab") {
      e.preventDefault();
      const trimmed = paymentTermInput.trim();
      if (trimmed && !paymentTermsTags.includes(trimmed)) {
        setPaymentTermsTags([...paymentTermsTags, trimmed]);
        setPaymentTermInput("");
      }
      if (fieldErrors.paymentTerms) {
        setFieldErrors(prev => ({ ...prev, paymentTerms: "" }));
      }
    }
  };

  const removePaymentTerm = (termToRemove: string) => {
    setPaymentTermsTags(paymentTermsTags.filter(t => t !== termToRemove));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setSubmitMessage(null);
    const newErrors: Record<string, string> = {};
    if (!formData.name) newErrors.name = "Company Name is mandatory";
    if (!formData.address) newErrors.address = "Street Address is mandatory";
    if (!formData.city) newErrors.city = "City is mandatory";
    if (!formData.country) newErrors.country = "Country is mandatory";
    if (!formData.contactPerson) newErrors.contactPerson = "Contact Person is mandatory";
    if (!formData.contactEmail) newErrors.contactEmail = "Email Address is mandatory";
    if (!formData.contactPhone) newErrors.contactPhone = "Phone Number is mandatory";
    if (paymentTermsTags.length === 0) newErrors.paymentTerms = "Payment Terms are mandatory";
    if (!formData.currencyCode) newErrors.currencyCode = "Default Currency is mandatory";
    if (!formData.createdBy) newErrors.createdBy = "Created By is mandatory";

    if (Object.keys(newErrors).length > 0) {
      setFieldErrors(newErrors);
      setSubmitMessage({ type: 'error', text: 'Validation failed. Please check the highlighted fields below.' });
      setIsSubmitting(false);
      return;
    }
    
    try {
      const requestPayload: CreateSupplierRequest = {
        ...formData,
        paymentTerms: paymentTermsTags
      };
      
      await supplierApi.create(requestPayload);
      setSubmitMessage({ type: 'success', text: 'Supplier created successfully!' });
      
    } catch (error) {
      console.error("Error creating supplier:", error);
      if (axios.isAxiosError(error) && error.response?.data) {
        const errorData = error.response.data as ErrorResponse;
        if (errorData.validationErrors) {
          setFieldErrors(errorData.validationErrors);
          setSubmitMessage({ type: 'error', text: 'Validation failed. Please check the highlighted fields below.' });
        } else {
          setSubmitMessage({ type: 'error', text: errorData.message || 'An error occurred while creating the supplier.' });
        }
      } else {
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
            Create Supplier
          </h3>
        </div>
        
        <form onSubmit={handleSubmit} className="p-6">
          
          {/* 1. Basic Info */}
          <div className="mb-8">
            <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
              1. Basic Information
            </h4>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div className="sm:col-span-2">
                <Label>Company Name *</Label>
                <Input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="Enter supplier company name"
                  error={!!fieldErrors.name}
                  hint={fieldErrors.name}
                />
              </div>
              <div className="sm:col-span-2">
                <Label>Description</Label>
                <TextArea
                  value={formData.description}
                  onChange={(val) => handleStringChange('description', val)}
                  rows={3}
                  placeholder="Brief description of the supplier..."
                  error={!!fieldErrors.description}
                  hint={fieldErrors.description}
                />
              </div>
            </div>
          </div>

          {/* 2. Address */}
          <div className="mb-8">
            <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
              2. Address
            </h4>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-3">
              <div className="sm:col-span-3">
                <Label>Street Address *</Label>
                <Input
                  type="text"
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  placeholder="123 Supplier Street"
                  error={!!fieldErrors.address}
                  hint={fieldErrors.address}
                />
              </div>
              <div>
                <Label>City *</Label>
                <Input
                  type="text"
                  name="city"
                  value={formData.city}
                  onChange={handleChange}
                  placeholder="City"
                  error={!!fieldErrors.city}
                  hint={fieldErrors.city}
                />
              </div>
              <div>
                <Label>Postal Code</Label>
                <Input
                  type="text"
                  name="postalCode"
                  value={formData.postalCode}
                  onChange={handleChange}
                  placeholder="Postal Code"
                  error={!!fieldErrors.postalCode}
                  hint={fieldErrors.postalCode}
                />
              </div>
              <div>
                <Label>Country *</Label>
                <Input
                  type="text"
                  name="country"
                  value={formData.country}
                  onChange={handleChange}
                  placeholder="Country"
                  error={!!fieldErrors.country}
                  hint={fieldErrors.country}
                />
              </div>
            </div>
          </div>

          {/* 3. Contact Details */}
          <div className="mb-8">
            <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
              3. Contact Details
            </h4>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
              <div>
                <Label>Contact Person *</Label>
                <Input
                  type="text"
                  name="contactPerson"
                  value={formData.contactPerson}
                  onChange={handleChange}
                  placeholder="Full Name"
                  error={!!fieldErrors.contactPerson}
                  hint={fieldErrors.contactPerson}
                />
              </div>
              <div>
                <Label>Email Address *</Label>
                <div className="relative">
                  <Input
                    type="email"
                    name="contactEmail"
                    value={formData.contactEmail}
                    onChange={handleChange}
                    placeholder="info@gmail.com"
                    error={!!fieldErrors.contactEmail}
                    hint={fieldErrors.contactEmail}
                    className="pl-[62px]"
                  />
                  <span className="absolute left-0 top-0 flex h-11 items-center border-r border-gray-200 px-3.5 text-gray-500 dark:border-gray-800 dark:text-gray-400">
                    <EnvelopeIcon className="size-6" />
                  </span>
                </div>
              </div>
              <div>
                <Label>Phone Number *</Label>
                <PhoneInput
                  selectPosition="start"
                  countries={[
                    { code: "US", label: "+1" },
                    { code: "GB", label: "+44" },
                    { code: "MA", label: "+212" },
                    { code: "FR", label: "+33" },
                    { code: "DE", label: "+49" },
                    { code: "IT", label: "+39" },
                    { code: "ES", label: "+34" }
                  ]}
                  placeholder="+1 (555) 000-0000"
                  onChange={(val: string) => handleStringChange("contactPhone", val)}
                  error={!!fieldErrors.contactPhone}
                />
                {fieldErrors.contactPhone && (
                  <p className="mt-1.5 text-xs text-error-500">{fieldErrors.contactPhone}</p>
                )}
              </div>
            </div>
          </div>

          {/* 4. Commercial Terms */}
          <div className="mb-8">
            <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800">
              4. Commercial Terms
            </h4>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
              <div className="sm:col-span-2 lg:col-span-3">
                <Label>Payment Terms *</Label>
                <div className="relative">
                  <input
                    type="text"
                    value={paymentTermInput}
                    onChange={(e) => setPaymentTermInput(e.target.value)}
                    onKeyDown={handlePaymentTermKeyDown}
                    placeholder="Type and press Enter or Tab to add payment term"
                    className="h-11 w-full rounded-lg border border-gray-300 appearance-none px-4 py-2.5 text-sm shadow-theme-xs placeholder:text-gray-400 focus:outline-hidden focus:ring-3 focus:border-brand-300 focus:ring-brand-500/20 bg-transparent text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                  />
                </div>
                {fieldErrors.paymentTerms && (
                  <p className="mt-1.5 text-xs text-error-500">{fieldErrors.paymentTerms}</p>
                )}
                {paymentTermsTags.length > 0 && (
                  <div className="mt-3 flex flex-wrap gap-2">
                    {paymentTermsTags.map((term, index) => (
                      <span key={index} className="inline-flex items-center gap-1.5 rounded-full bg-gray-100 px-3 py-1.5 text-sm font-medium text-gray-700 dark:bg-gray-800 dark:text-gray-300">
                        {term}
                        <button type="button" onClick={() => removePaymentTerm(term)} className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">
                          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                          </svg>
                        </button>
                      </span>
                    ))}
                  </div>
                )}
              </div>
              <div>
                <Label>Default Currency *</Label>
                <Input
                  type="text"
                  name="currencyCode"
                  value={formData.currencyCode}
                  onChange={handleChange}
                  placeholder="MAD, USD, EUR"
                  error={!!fieldErrors.currencyCode}
                  hint={fieldErrors.currencyCode}
                />
              </div>
              <div>
                <Label>Created By *</Label>
                <Input
                  type="text"
                  name="createdBy"
                  value={formData.createdBy}
                  onChange={handleChange}
                  placeholder="System / User ID"
                  error={!!fieldErrors.createdBy}
                  hint={fieldErrors.createdBy}
                />
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="mt-10 flex flex-wrap items-center justify-end gap-3 pt-6 border-t border-gray-200 dark:border-gray-800">
            <Button 
              variant="outline" 
              onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                e.preventDefault();
                // Add cancel logic here if needed
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
              {isSubmitting ? 'Saving...' : 'Save Supplier'}
            </Button>
          </div>
        </form>
      </div>
    </>
  );
}
