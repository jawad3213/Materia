import React, { useState } from 'react';
import { Modal } from '../../../shared/components/ui/modal';
import Button from '../../../shared/components/ui/button/Button';
import InputField from '../../../shared/components/form/input/InputField';
import Label from '../../../shared/components/form/Label';
import Select from '../../../shared/components/form/Select';
import Checkbox from '../../../shared/components/form/input/Checkbox';
import type { OnboardUserRequest } from '../types';
import { EmploymentStatus } from '../enums/EmploymentStatus';
import { UserRole } from '../enums/UserRole';

interface OnboardUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (data: OnboardUserRequest) => Promise<void>;
}

export const OnboardUserModal: React.FC<OnboardUserModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
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

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange = (field: keyof OnboardUserRequest, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.firstName.trim() || !formData.lastName.trim() || !formData.email.trim()) {
      setError('First name, last name, and email are required');
      return;
    }

    if (formData.provisionCredentials && formData.initialPassword && formData.initialPassword.length < 8) {
      setError('Initial password must be at least 8 characters long');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const payload: OnboardUserRequest = {
        ...formData,
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email.trim(),
        phone: formData.phone?.trim() || undefined,
        initialPassword: formData.initialPassword?.trim() ? formData.initialPassword.trim() : undefined,
      };
      await onSuccess(payload);
      onClose();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Failed to onboard employee';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="max-w-2xl p-6 bg-white dark:bg-gray-900 rounded-2xl">
      <div className="mb-5">
        <h3 className="text-xl font-bold text-gray-800 dark:text-white">
          Onboard New Employee / User
        </h3>
        <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
          Create employee profile and optionally provision their system login credentials.
        </p>
      </div>

      {error && (
        <div className="mb-4 p-3 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Name Fields */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <Label htmlFor="firstName">First Name *</Label>
            <InputField
              id="firstName"
              placeholder="e.g. John"
              value={formData.firstName}
              onChange={(e) => handleChange('firstName', e.target.value)}
              required
            />
          </div>
          <div>
            <Label htmlFor="lastName">Last Name *</Label>
            <InputField
              id="lastName"
              placeholder="e.g. Doe"
              value={formData.lastName}
              onChange={(e) => handleChange('lastName', e.target.value)}
              required
            />
          </div>
        </div>

        {/* Email & Phone */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <Label htmlFor="email">Business Email *</Label>
            <InputField
              id="email"
              type="email"
              placeholder="john.doe@materia.com"
              value={formData.email}
              onChange={(e) => handleChange('email', e.target.value)}
              required
            />
          </div>
          <div>
            <Label htmlFor="phone">Phone Number</Label>
            <InputField
              id="phone"
              placeholder="+212 600-000000"
              value={formData.phone}
              onChange={(e) => handleChange('phone', e.target.value)}
            />
          </div>
        </div>

        {/* Hire Date & Status */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <Label htmlFor="hireDate">Hire Date</Label>
            <InputField
              id="hireDate"
              type="date"
              value={formData.hireDate}
              onChange={(e) => handleChange('hireDate', e.target.value)}
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

        {/* Provision Credentials Section */}
        <div className="p-4 rounded-xl bg-gray-50 dark:bg-gray-800/50 border border-gray-200 dark:border-gray-700/60 mt-4">
          <div className="flex items-center gap-3">
            <Checkbox
              id="provisionCredentials"
              checked={formData.provisionCredentials}
              onChange={(checked) => handleChange('provisionCredentials', checked)}
              label="Provision System Login Credentials"
            />
          </div>

          {formData.provisionCredentials && (
            <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-4 pt-3 border-t border-gray-200 dark:border-gray-700">
              <div>
                <Label htmlFor="roleCode">System Role *</Label>
                <Select
                  id="roleCode"
                  options={[
                    { value: UserRole.PURCHASER, label: 'Purchaser (Acheteur)' },
                    { value: UserRole.RECEIVER, label: 'Receiver (Réceptionnaire)' },
                    { value: UserRole.ADMIN, label: 'Admin (Administrateur)' },
                  ]}
                  value={formData.roleCode || UserRole.PURCHASER}
                  onChange={(val) => handleChange('roleCode', val)}
                />
              </div>
              <div>
                <Label htmlFor="initialPassword">Initial Password (Optional)</Label>
                <InputField
                  id="initialPassword"
                  type="password"
                  placeholder="Leave empty for auto-generated"
                  value={formData.initialPassword || ''}
                  onChange={(e) => handleChange('initialPassword', e.target.value)}
                />
              </div>
            </div>
          )}
        </div>

        {/* Actions */}
        <div className="flex justify-end gap-3 pt-4 border-t border-gray-100 dark:border-gray-800">
          <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button type="submit" disabled={loading}>
            {loading ? 'Onboarding...' : 'Complete Onboarding'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default OnboardUserModal;
