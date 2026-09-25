import React, { useState } from 'react';
import { Modal } from '../../../shared/components/ui/modal';
import Button from '../../../shared/components/ui/button/Button';
import InputField from '../../../shared/components/form/input/InputField';
import Label from '../../../shared/components/form/Label';
import TextArea from '../../../shared/components/form/input/TextArea';
import type { UserItem, OffboardUserRequest } from '../types';

interface OffboardUserModalProps {
  isOpen: boolean;
  user: UserItem | null;
  onClose: () => void;
  onSuccess: (data: OffboardUserRequest) => Promise<void>;
}

export const OffboardUserModal: React.FC<OffboardUserModalProps> = ({
  isOpen,
  user,
  onClose,
  onSuccess,
}) => {
  const [terminationDate, setTerminationDate] = useState(
    new Date().toISOString().split('T')[0]
  );
  const [terminationReason, setTerminationReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!user) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!terminationReason.trim()) {
      setError('Please provide a reason for offboarding');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await onSuccess({
        employeeId: user.id,
        terminationDate,
        reason: terminationReason.trim(),
        revokeUserAccess: true,
      });
      onClose();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Failed to offboard employee';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="max-w-md p-6 bg-white dark:bg-gray-900 rounded-2xl">
      <div className="mb-4">
        <div className="w-12 h-12 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center text-red-600 mb-3">
          <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h3 className="text-xl font-bold text-gray-800 dark:text-white">
          Offboard Employee
        </h3>
        <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
          Are you sure you want to offboard <strong className="text-gray-800 dark:text-white">{user.fullName}</strong> ({user.code})?
        </p>
      </div>

      <div className="p-3 mb-4 rounded-xl bg-amber-50 dark:bg-amber-900/20 text-amber-800 dark:text-amber-300 text-xs">
        ⚠️ This action marks the employee as <strong>TERMINATED</strong> and immediately revokes all system login credentials and active JWT sessions.
      </div>

      {error && (
        <div className="mb-4 p-3 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <Label htmlFor="terminationDate">Effective Termination Date</Label>
          <InputField
            id="terminationDate"
            type="date"
            value={terminationDate}
            onChange={(e) => setTerminationDate(e.target.value)}
            required
          />
        </div>

        <div>
          <Label htmlFor="terminationReason">Reason for Departure *</Label>
          <TextArea
            id="terminationReason"
            placeholder="e.g. Resignation, end of contract, internal transfer..."
            value={terminationReason}
            onChange={(val) => setTerminationReason(val)}
            rows={3}
          />
        </div>

        <div className="flex justify-end gap-3 pt-3 border-t border-gray-100 dark:border-gray-800">
          <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            type="submit"
            className="!bg-red-600 hover:!bg-red-700 text-white border-transparent"
            disabled={loading}
          >
            {loading ? 'Processing...' : 'Confirm Offboarding'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default OffboardUserModal;
