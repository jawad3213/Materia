import { useState, type FormEvent } from 'react';
import Button from '../../../shared/components/ui/button/Button';
import Input from '../../../shared/components/form/input/InputField';
import Label from '../../../shared/components/form/Label';
import useAuth from '../../auth/hooks/useAuth';
import { getApiErrorMessage } from '../../../shared/utils/apiError';
import type { ChangePasswordPayload } from '../types/profile.types';
import { hasErrors, passwordChecks, validatePasswordChange, type FieldErrors } from '../utils/profileValidation';

const EMPTY: ChangePasswordPayload = { currentPassword: '', newPassword: '', confirmPassword: '' };

/** Changes the signed-in user's password; the current password is always asked for. */
export default function ChangePasswordForm({ onChanged }: { onChanged: (message: string) => void }) {
  const { changePassword } = useAuth();
  const [values, setValues] = useState<ChangePasswordPayload>(EMPTY);
  const [errors, setErrors] = useState<FieldErrors<ChangePasswordPayload>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const set = (field: keyof ChangePasswordPayload) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setValues((current) => ({ ...current, [field]: e.target.value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const found = validatePasswordChange(values);
    setErrors(found);
    if (hasErrors(found)) return;
    setSaving(true);
    setSubmitError(null);
    try {
      await changePassword(values);
      setValues(EMPTY);
      onChanged('Your password has been changed');
    } catch (err) {
      setSubmitError(getApiErrorMessage(err, 'Your password could not be changed'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-5">
      {submitError && (
        <div role="alert" className="rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-400">
          {submitError}
        </div>
      )}
      <div className="grid grid-cols-1 gap-5 lg:grid-cols-3">
        <div>
          <Label htmlFor="current-password">Current password</Label>
          <Input id="current-password" type="password" value={values.currentPassword} onChange={set('currentPassword')} error={!!errors.currentPassword} hint={errors.currentPassword} autoComplete="current-password" />
        </div>
        <div>
          <Label htmlFor="new-password">New password</Label>
          <Input id="new-password" type="password" value={values.newPassword} onChange={set('newPassword')} error={!!errors.newPassword} hint={errors.newPassword} autoComplete="new-password" />
        </div>
        <div>
          <Label htmlFor="confirm-password">Confirm new password</Label>
          <Input id="confirm-password" type="password" value={values.confirmPassword} onChange={set('confirmPassword')} error={!!errors.confirmPassword} hint={errors.confirmPassword} autoComplete="new-password" />
        </div>
      </div>

      <ul className="grid grid-cols-1 gap-x-6 gap-y-1.5 text-xs sm:grid-cols-2 lg:grid-cols-5" aria-label="Password rules">
        {passwordChecks(values.newPassword).map((check) => (
          <li key={check.label} className={`flex items-center gap-1.5 ${check.ok ? 'text-success-600 dark:text-success-400' : 'text-gray-400'}`}>
            <span aria-hidden="true">{check.ok ? '✓' : '○'}</span>
            {check.label}
          </li>
        ))}
      </ul>

      <div className="flex justify-end">
        <Button size="sm" type="submit" disabled={saving}>
          {saving ? 'Updating…' : 'Update password'}
        </Button>
      </div>
    </form>
  );
}
