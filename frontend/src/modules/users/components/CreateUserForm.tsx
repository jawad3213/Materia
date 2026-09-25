import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Input from '../../../shared/components/form/input/InputField';
import Label from '../../../shared/components/form/Label';
import Select from '../../../shared/components/form/Select';
import Checkbox from '../../../shared/components/form/input/Checkbox';
import Button from '../../../shared/components/ui/button/Button';
import { EmploymentStatus } from '../enums/EmploymentStatus';
import { UserRole } from '../enums/UserRole';
import { userApi } from '../services/userApi';
import type { OnboardUserRequest } from '../types';

export default function CreateUserForm() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState<OnboardUserRequest>({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    hireDate: new Date().toISOString().split('T')[0],
    status: EmploymentStatus.ACTIVE,
    provisionCredentials: true,
    roleCode: UserRole.PURCHASER,
    initialPassword: '',
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const handleChange = (field: keyof OnboardUserRequest, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (fieldErrors[field]) {
      setFieldErrors((prev) => ({ ...prev, [field]: '' }));
    }
  };

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.firstName?.trim()) {
      errors.firstName = 'First name is required';
    }
    if (!formData.lastName?.trim()) {
      errors.lastName = 'Last name is required';
    }
    if (!formData.email?.trim()) {
      errors.email = 'Business email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
      errors.email = 'Please enter a valid email address';
    }

    if (
      formData.provisionCredentials &&
      formData.initialPassword &&
      formData.initialPassword.trim().length > 0 &&
      formData.initialPassword.trim().length < 8
    ) {
      errors.initialPassword = 'Initial password must be at least 8 characters long';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    if (!validate()) {
      return;
    }

    setIsSubmitting(true);
    try {
      const payload: OnboardUserRequest = {
        ...formData,
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email.trim().toLowerCase(),
        phone: formData.phone?.trim() || undefined,
        hireDate: formData.hireDate || undefined,
        initialPassword: formData.initialPassword?.trim() ? formData.initialPassword.trim() : undefined,
      };

      await userApi.onboard(payload);
      setSuccessMessage('Employee onboarded successfully! Redirecting...');
      setTimeout(() => {
        navigate('/users');
      }, 1200);
    } catch (err: any) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.detail ||
        err.response?.data?.error ||
        err.message ||
        'Failed to onboard employee';
      setErrorMessage(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] lg:p-8">
      {/* Notifications */}
      {errorMessage && (
        <div className="mb-6 p-4 rounded-xl bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 text-sm flex items-start gap-3">
          <svg className="w-5 h-5 shrink-0 text-red-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <div className="font-semibold">Onboarding Error</div>
            <div>{errorMessage}</div>
          </div>
        </div>
      )}

      {successMessage && (
        <div className="mb-6 p-4 rounded-xl bg-green-50 dark:bg-green-950/40 border border-green-200 dark:border-green-800 text-green-700 dark:text-green-400 text-sm flex items-center gap-3">
          <svg className="w-5 h-5 shrink-0 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
          <span className="font-medium">{successMessage}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        {/* Section 1: Personal Details */}
        <div className="mb-8">
          <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800 flex items-center gap-2">
            <span className="flex items-center justify-center w-6 h-6 rounded-full bg-brand-50 text-brand-600 dark:bg-brand-500/15 text-xs font-bold">1</span>
            Personal & Contact Information
          </h4>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            <div>
              <Label htmlFor="firstName">First Name <span className="text-error-500">*</span></Label>
              <Input
                id="firstName"
                placeholder="e.g. John"
                value={formData.firstName}
                onChange={(e) => handleChange('firstName', e.target.value)}
                error={!!fieldErrors.firstName}
                hint={fieldErrors.firstName}
                disabled={isSubmitting}
              />
            </div>

            <div>
              <Label htmlFor="lastName">Last Name <span className="text-error-500">*</span></Label>
              <Input
                id="lastName"
                placeholder="e.g. Doe"
                value={formData.lastName}
                onChange={(e) => handleChange('lastName', e.target.value)}
                error={!!fieldErrors.lastName}
                hint={fieldErrors.lastName}
                disabled={isSubmitting}
              />
            </div>

            <div>
              <Label htmlFor="email">Business Email <span className="text-error-500">*</span></Label>
              <Input
                id="email"
                type="email"
                placeholder="john.doe@materia.com"
                value={formData.email}
                onChange={(e) => handleChange('email', e.target.value)}
                error={!!fieldErrors.email}
                hint={fieldErrors.email}
                disabled={isSubmitting}
              />
            </div>

            <div>
              <Label htmlFor="phone">Phone Number</Label>
              <Input
                id="phone"
                placeholder="+212 600-000000"
                value={formData.phone}
                onChange={(e) => handleChange('phone', e.target.value)}
                disabled={isSubmitting}
              />
            </div>
          </div>
        </div>

        {/* Section 2: Employment Details */}
        <div className="mb-8">
          <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800 flex items-center gap-2">
            <span className="flex items-center justify-center w-6 h-6 rounded-full bg-brand-50 text-brand-600 dark:bg-brand-500/15 text-xs font-bold">2</span>
            Employment Details
          </h4>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <div>
              <Label htmlFor="hireDate">Hire Date</Label>
              <Input
                id="hireDate"
                type="date"
                value={formData.hireDate}
                onChange={(e) => handleChange('hireDate', e.target.value)}
                disabled={isSubmitting}
              />
            </div>

            <div>
              <Label htmlFor="status">Initial Status</Label>
              <Select
                id="status"
                options={[
                  { value: EmploymentStatus.ACTIVE, label: 'Active' },
                  { value: EmploymentStatus.PROBATION, label: 'Probation' },
                  { value: EmploymentStatus.ON_LEAVE, label: 'On Leave' },
                ]}
                value={formData.status || EmploymentStatus.ACTIVE}
                onChange={(val) => handleChange('status', val)}
              />
            </div>
          </div>
        </div>

        {/* Section 3: System Access & Security Credentials */}
        <div className="mb-8">
          <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800 flex items-center gap-2">
            <span className="flex items-center justify-center w-6 h-6 rounded-full bg-brand-50 text-brand-600 dark:bg-brand-500/15 text-xs font-bold">3</span>
            System Access & Security Credentials
          </h4>

          <div className="p-5 rounded-2xl border border-gray-200 dark:border-gray-700/60 bg-gray-50/60 dark:bg-white/[0.02]">
            <div className="flex items-center justify-between flex-wrap gap-4 pb-4 border-b border-gray-200 dark:border-gray-700/60">
              <div className="flex items-center gap-3">
                <Checkbox
                  id="provisionCredentials"
                  checked={formData.provisionCredentials}
                  onChange={(checked) => handleChange('provisionCredentials', checked)}
                  label="Provision Materia ERP Login Account"
                />
              </div>
              <span className="text-xs text-gray-500 dark:text-gray-400">
                {formData.provisionCredentials
                  ? 'A system user will be provisioned in the authentication module.'
                  : 'Employee will exist in HR records only without ERP login privileges.'}
              </span>
            </div>

            {formData.provisionCredentials && (
              <div className="mt-5 grid grid-cols-1 gap-6 sm:grid-cols-2">
                <div>
                  <Label htmlFor="roleCode">Assigned System Role <span className="text-error-500">*</span></Label>
                  <Select
                    id="roleCode"
                    options={[
                      { value: UserRole.PURCHASER, label: 'PURCHASER (Acheteur) — 19 permissions' },
                      { value: UserRole.RECEIVER, label: 'RECEIVER (Réceptionnaire) — 14 permissions' },
                      { value: UserRole.ADMIN, label: 'ADMIN (Administrateur) — Full System Access' },
                    ]}
                    value={formData.roleCode || UserRole.PURCHASER}
                    onChange={(val) => handleChange('roleCode', val)}
                  />
                  <p className="mt-1.5 text-xs text-gray-400">
                    Determines route access, approval rights, and JWT authority scopes.
                  </p>
                </div>

                <div>
                  <Label htmlFor="initialPassword">Initial Password (Optional)</Label>
                  <Input
                    id="initialPassword"
                    type="password"
                    placeholder="Leave empty for auto-generated temporary password"
                    value={formData.initialPassword || ''}
                    onChange={(e) => handleChange('initialPassword', e.target.value)}
                    error={!!fieldErrors.initialPassword}
                    hint={fieldErrors.initialPassword}
                    disabled={isSubmitting}
                  />
                  <p className="mt-1.5 text-xs text-gray-400">
                    If left empty, a secure temporary password will be generated and emailed automatically.
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Action Buttons */}
        <div className="mt-10 flex flex-wrap items-center justify-end gap-3 pt-6 border-t border-gray-200 dark:border-gray-800">
          <Button
            type="button"
            variant="outline"
            onClick={() => navigate('/users')}
            disabled={isSubmitting}
          >
            Cancel
          </Button>

          <Button
            type="submit"
            disabled={isSubmitting}
            startIcon={
              isSubmitting ? (
                <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              ) : (
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
                </svg>
              )
            }
          >
            {isSubmitting ? 'Onboarding Employee...' : 'Complete Onboarding'}
          </Button>
        </div>
      </form>
    </div>
  );
}
