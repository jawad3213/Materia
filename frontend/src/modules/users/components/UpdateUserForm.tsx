import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Input from '../../../shared/components/form/input/InputField';
import Label from '../../../shared/components/form/Label';
import Select from '../../../shared/components/form/Select';
import Button from '../../../shared/components/ui/button/Button';
import UserStatusBadge from './UserStatusBadge';
import UserRoleBadge from './UserRoleBadge';
import { EmploymentStatus, type EmploymentStatusValue } from '../enums/EmploymentStatus';
import { userApi } from '../services/userApi';
import type { UserItem, UpdateUserRequest } from '../types';

interface UpdateUserFormProps {
  id: string;
}

export default function UpdateUserForm({ id }: UpdateUserFormProps) {
  const navigate = useNavigate();

  const [user, setUser] = useState<UserItem | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  // Form State
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [status, setStatus] = useState<EmploymentStatusValue>(EmploymentStatus.ACTIVE);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    let isMounted = true;
    const fetchUserData = async () => {
      setIsLoading(true);
      setLoadError(null);
      try {
        const response = await userApi.getById(id);
        if (isMounted) {
          const u = response.data;
          setUser(u);
          setFirstName(u.firstName || '');
          setLastName(u.lastName || '');
          setPhone(u.phone || '');
          setStatus(u.status || EmploymentStatus.ACTIVE);
        }
      } catch (err: any) {
        if (isMounted) {
          const msg =
            err.response?.data?.message ||
            err.message ||
            'Failed to load employee details';
          setLoadError(msg);
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    fetchUserData();
    return () => {
      isMounted = false;
    };
  }, [id]);

  const validate = (): boolean => {
    const errors: Record<string, string> = {};
    if (!firstName.trim()) {
      errors.firstName = 'First name is required';
    }
    if (!lastName.trim()) {
      errors.lastName = 'Last name is required';
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
      const payload: UpdateUserRequest = {
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        phone: phone.trim() || undefined,
        status,
      };

      await userApi.update(id, payload);
      setSuccessMessage('Employee updated successfully! Redirecting...');
      setTimeout(() => {
        navigate('/users');
      }, 1000);
    } catch (err: any) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.detail ||
        err.response?.data?.error ||
        err.message ||
        'Failed to update employee';
      setErrorMessage(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="rounded-2xl border border-gray-200 bg-white p-12 text-center dark:border-gray-800 dark:bg-white/[0.03]">
        <div className="flex flex-col items-center justify-center gap-3">
          <div className="w-10 h-10 border-3 border-brand-500 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 dark:text-gray-400 font-medium">Loading employee profile...</p>
        </div>
      </div>
    );
  }

  if (loadError || !user) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50/50 p-8 text-center dark:border-red-900/50 dark:bg-red-950/20">
        <div className="max-w-md mx-auto space-y-4">
          <div className="w-12 h-12 rounded-full bg-red-100 text-red-600 mx-auto flex items-center justify-center dark:bg-red-900/40">
            <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h3 className="text-lg font-bold text-gray-800 dark:text-white">Unable to Load Employee</h3>
          <p className="text-sm text-gray-600 dark:text-gray-400">{loadError || 'Employee not found.'}</p>
          <Button type="button" variant="outline" onClick={() => navigate('/users')}>
            ← Back to Staff List
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] lg:p-8">
      {/* Header bar with Employee Code & Live Status */}
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4 pb-5 border-b border-gray-100 dark:border-gray-800">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-2xl bg-brand-50 dark:bg-brand-500/15 text-brand-600 dark:text-brand-400 font-bold flex items-center justify-center text-base shrink-0">
            {user.firstName[0]}
            {user.lastName[0]}
          </div>
          <div>
            <h3 className="text-lg font-bold text-gray-800 dark:text-white flex items-center gap-2.5">
              <span>{user.fullName}</span>
              <span className="font-mono text-xs font-semibold px-2 py-0.5 rounded-md bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300">
                {user.code}
              </span>
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Registered on {new Date(user.createdAt).toLocaleDateString()}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs text-gray-400">Current Status:</span>
          <UserStatusBadge status={user.status} />
        </div>
      </div>

      {/* Notifications */}
      {errorMessage && (
        <div className="mb-6 p-4 rounded-xl bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 text-sm flex items-start gap-3">
          <svg className="w-5 h-5 shrink-0 text-red-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <div className="font-semibold">Update Error</div>
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
        {/* Section 1: Personal & Contact Information */}
        <div className="mb-8">
          <h4 className="mb-4 text-base font-semibold text-gray-800 dark:text-white/90 border-b border-gray-100 pb-2 dark:border-gray-800 flex items-center gap-2">
            <span className="flex items-center justify-center w-6 h-6 rounded-full bg-brand-50 text-brand-600 dark:bg-brand-500/15 text-xs font-bold">1</span>
            Personal & Contact Information
          </h4>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {/* First Name - Editable */}
            <div>
              <Label htmlFor="firstName">First Name <span className="text-error-500">*</span></Label>
              <Input
                id="firstName"
                placeholder="e.g. John"
                value={firstName}
                onChange={(e) => {
                  setFirstName(e.target.value);
                  if (fieldErrors.firstName) setFieldErrors((prev) => ({ ...prev, firstName: '' }));
                }}
                error={!!fieldErrors.firstName}
                hint={fieldErrors.firstName}
                disabled={isSubmitting}
              />
            </div>

            {/* Last Name - Editable */}
            <div>
              <Label htmlFor="lastName">Last Name <span className="text-error-500">*</span></Label>
              <Input
                id="lastName"
                placeholder="e.g. Doe"
                value={lastName}
                onChange={(e) => {
                  setLastName(e.target.value);
                  if (fieldErrors.lastName) setFieldErrors((prev) => ({ ...prev, lastName: '' }));
                }}
                error={!!fieldErrors.lastName}
                hint={fieldErrors.lastName}
                disabled={isSubmitting}
              />
            </div>

            {/* Business Email - DISABLED */}
            <div>
              <div className="flex items-center justify-between">
                <Label htmlFor="email">Business Email</Label>
                <span className="text-[11px] font-medium text-gray-400 flex items-center gap-1">
                  🔒 Immutable
                </span>
              </div>
              <Input
                id="email"
                type="email"
                value={user.email}
                disabled={true}
                hint="Security login anchor. Cannot be changed directly."
              />
            </div>

            {/* Phone Number - Editable */}
            <div>
              <Label htmlFor="phone">Phone Number</Label>
              <Input
                id="phone"
                placeholder="+212 600-000000"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
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
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {/* Employee Code - DISABLED */}
            <div>
              <div className="flex items-center justify-between">
                <Label htmlFor="code">Employee Code</Label>
                <span className="text-[11px] font-medium text-gray-400 flex items-center gap-1">
                  🔒 System Assigned
                </span>
              </div>
              <Input
                id="code"
                value={user.code}
                disabled={true}
                hint="Unique ledger key used across requisitions & audit records."
              />
            </div>

            {/* Hire Date - DISABLED */}
            <div>
              <div className="flex items-center justify-between">
                <Label htmlFor="hireDate">Hire Date</Label>
                <span className="text-[11px] font-medium text-gray-400 flex items-center gap-1">
                  🔒 Contract Baseline
                </span>
              </div>
              <Input
                id="hireDate"
                type="date"
                value={user.hireDate || ''}
                disabled={true}
                hint="Contractual start date. Preserves seniority records."
              />
            </div>

            {/* Employment Status - Editable */}
            <div>
              <Label htmlFor="status">Employment Status <span className="text-error-500">*</span></Label>
              <Select
                id="status"
                options={[
                  { value: EmploymentStatus.ACTIVE, label: 'Active — Operational' },
                  { value: EmploymentStatus.PROBATION, label: 'Probation — Under Evaluation' },
                  { value: EmploymentStatus.ON_LEAVE, label: 'On Leave — Temporarily Away' },
                  { value: EmploymentStatus.SUSPENDED, label: 'Suspended — Access On Hold' },
                  { value: EmploymentStatus.TERMINATED, label: 'Terminated — Offboarded' },
                ]}
                value={status}
                onChange={(val) => setStatus(val as EmploymentStatusValue)}
                disabled={isSubmitting}
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
                <span className={`inline-flex items-center justify-center w-8 h-8 rounded-full ${user.userId ? 'bg-green-100 text-green-600 dark:bg-green-950/60 dark:text-green-400' : 'bg-gray-200 text-gray-600 dark:bg-gray-800 dark:text-gray-400'}`}>
                  {user.userId ? '✓' : '—'}
                </span>
                <div>
                  <div className="font-semibold text-sm text-gray-800 dark:text-white">
                    {user.userId ? 'Materia ERP Login Account Active' : 'HR Profile Only (No ERP Login Provisioned)'}
                  </div>
                  <div className="text-xs text-gray-500 dark:text-gray-400">
                    {user.userId
                      ? `Linked Auth User ID: ${user.userId}`
                      : 'This employee exists in organizational records without direct system login privileges.'}
                  </div>
                </div>
              </div>

              {user.role && (
                <div className="flex items-center gap-2">
                  <span className="text-xs text-gray-400 font-medium">Assigned Role:</span>
                  <UserRoleBadge role={user.role} hasAccount={!!user.userId} />
                </div>
              )}
            </div>

            <div className="mt-4 text-xs text-gray-500 dark:text-gray-400 space-y-1">
              <p>
                ℹ️ <strong>Security Notice:</strong> Credentials (passwords, JWT authority scopes, and role permissions) are isolated in the Auth module for security compliance.
              </p>
              <p>
                To revoke user access or record contract termination, use the dedicated <strong>Offboard</strong> action from the staff directory.
              </p>
            </div>
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
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              )
            }
          >
            {isSubmitting ? 'Saving Changes...' : 'Save Changes'}
          </Button>
        </div>
      </form>
    </div>
  );
}
