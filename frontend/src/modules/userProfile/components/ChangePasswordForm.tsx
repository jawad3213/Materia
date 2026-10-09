import { useState, type FormEvent } from 'react';
import Button from '../../../shared/components/ui/button/Button';
import Input from '../../../shared/components/form/input/InputField';
import Label from '../../../shared/components/form/Label';
import authService from '../../auth/services/authService';
import { errorMessage } from '../hooks/useProfile';

const EMPTY = { currentPassword: '', newPassword: '', confirmPassword: '' };

export default function ChangePasswordForm() {
  const [form, setForm] = useState(EMPTY);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const set = (field: keyof typeof EMPTY) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((prev) => ({ ...prev, [field]: e.target.value }));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    if (!form.currentPassword) {
      setError('Enter your current password.');
      return;
    }
    if (form.newPassword.length < 8) {
      setError('The new password must be at least 8 characters.');
      return;
    }
    if (form.newPassword !== form.confirmPassword) {
      setError('The new passwords do not match.');
      return;
    }
    setIsSaving(true);
    try {
      // Called on the service directly: the context's changePassword toggles the global
      // loading flag, which makes PrivateRoute unmount this page mid-request.
      const res = await authService.changePassword(form);
      setSuccess(res.message || 'Password updated.');
      setForm(EMPTY);
    } catch (err) {
      setError(errorMessage(err, 'Could not change your password'));
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-5">
      {error && (
        <div className="rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-800 dark:bg-error-500/10 dark:text-error-400">
          {error}
        </div>
      )}
      {success && (
        <div className="rounded-lg border border-success-200 bg-success-50 px-4 py-3 text-sm text-success-700 dark:border-success-800 dark:bg-success-500/10 dark:text-success-400">
          {success}
        </div>
      )}
      <div className="grid grid-cols-1 gap-x-6 gap-y-5 lg:grid-cols-3">
        <div>
          <Label htmlFor="pw-current">Current Password</Label>
          <Input id="pw-current" type="password" autoComplete="current-password" value={form.currentPassword} onChange={set('currentPassword')} />
        </div>
        <div>
          <Label htmlFor="pw-new">New Password</Label>
          <Input id="pw-new" type="password" autoComplete="new-password" value={form.newPassword} onChange={set('newPassword')} hint="At least 8 characters, with upper and lower case and a number or symbol." />
        </div>
        <div>
          <Label htmlFor="pw-confirm">Confirm New Password</Label>
          <Input id="pw-confirm" type="password" autoComplete="new-password" value={form.confirmPassword} onChange={set('confirmPassword')} />
        </div>
      </div>
      <div className="flex lg:justify-end">
        <Button size="sm" type="submit" disabled={isSaving}>
          {isSaving ? 'Updating…' : 'Update Password'}
        </Button>
      </div>
    </form>
  );
}
