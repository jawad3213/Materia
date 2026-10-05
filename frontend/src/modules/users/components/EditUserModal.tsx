import React, { useState } from 'react';
import { Modal } from '../../../shared/components/ui/modal';
import Button from '../../../shared/components/ui/button/Button';
import InputField from '../../../shared/components/form/input/InputField';
import Label from '../../../shared/components/form/Label';
import Select from '../../../shared/components/form/Select';
import type { UserItem, UpdateUserRequest } from '../types';
import { EmploymentStatus } from '../enums/EmploymentStatus';

import { getApiErrorMessage } from '../../../shared/utils/apiError';
import type { EmploymentStatusValue } from '../enums/EmploymentStatus';
interface EditUserModalProps {
  isOpen: boolean;
  user: UserItem | null;
  onClose: () => void;
  onSuccess: (id: string, data: UpdateUserRequest) => Promise<void>;
}

export const EditUserModal: React.FC<EditUserModalProps> = ({
  isOpen,
  user,
  onClose,
  onSuccess,
}) => {
  const [formData, setFormData] = useState<UpdateUserRequest>({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Another employee opened in the modal: the form starts from their record (adjusting state during render).
  const [editedUser, setEditedUser] = useState<typeof user>(null);
  if (user && user !== editedUser) {
    setEditedUser(user);
    setFormData({
      firstName: user.firstName,
      lastName: user.lastName,
      phone: user.phone || '',
      status: user.status,
    });
    setError(null);
  }

  if (!user) return null;

  const handleChange = <K extends keyof UpdateUserRequest>(field: K, value: UpdateUserRequest[K]) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const payload: UpdateUserRequest = {
        firstName: formData.firstName?.trim() || undefined,
        lastName: formData.lastName?.trim() || undefined,
        phone: formData.phone?.trim() || undefined,
        status: formData.status,
      };
      await onSuccess(user.id, payload);
      onClose();
    } catch (err) {
      const msg = getApiErrorMessage(err, 'Failed to update employee');
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="max-w-xl p-6 bg-white dark:bg-gray-900 rounded-2xl">
      <div className="mb-5">
        <h3 className="text-xl font-bold text-gray-800 dark:text-white">
          Edit Employee: {user.fullName}
        </h3>
        <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
          Code: <span className="font-mono text-brand-600 dark:text-brand-400">{user.code}</span> • Email: {user.email}
        </p>
      </div>

      {error && (
        <div className="mb-4 p-3 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <Label htmlFor="editFirstName">First Name</Label>
            <InputField
              id="editFirstName"
              value={formData.firstName || ''}
              onChange={(e) => handleChange('firstName', e.target.value)}
              required
            />
          </div>
          <div>
            <Label htmlFor="editLastName">Last Name</Label>
            <InputField
              id="editLastName"
              value={formData.lastName || ''}
              onChange={(e) => handleChange('lastName', e.target.value)}
              required
            />
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <Label htmlFor="editPhone">Phone</Label>
            <InputField
              id="editPhone"
              value={formData.phone || ''}
              onChange={(e) => handleChange('phone', e.target.value)}
            />
          </div>
          <div>
            <Label htmlFor="editStatus">Status</Label>
            <Select
              id="editStatus"
              options={[
                { value: EmploymentStatus.ACTIVE, label: 'Active' },
                { value: EmploymentStatus.PROBATION, label: 'Probation' },
                { value: EmploymentStatus.ON_LEAVE, label: 'On Leave' },
                { value: EmploymentStatus.SUSPENDED, label: 'Suspended' },
                { value: EmploymentStatus.TERMINATED, label: 'Terminated' },
              ]}
              value={formData.status || EmploymentStatus.ACTIVE}
              onChange={(val) => handleChange('status', val as EmploymentStatusValue)}
            />
          </div>
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t border-gray-100 dark:border-gray-800">
          <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button type="submit" disabled={loading}>
            {loading ? 'Saving...' : 'Save Changes'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default EditUserModal;
