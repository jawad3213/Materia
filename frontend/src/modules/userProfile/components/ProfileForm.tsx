import { useState, type FormEvent } from 'react';
import Button from '../../../shared/components/ui/button/Button';
import Input from '../../../shared/components/form/input/InputField';
import Label from '../../../shared/components/form/Label';
import { errorMessage } from '../hooks/useProfile';
import type { UpdateProfilePayload, UserProfile } from '../types/profile.types';

interface ProfileFormProps {
  profile: UserProfile;
  onSave: (payload: UpdateProfilePayload) => Promise<unknown>;
  onCancel: () => void;
}

export default function ProfileForm({ profile, onSave, onCancel }: ProfileFormProps) {
  const [form, setForm] = useState<UpdateProfilePayload>({
    firstName: profile.firstName ?? '',
    lastName: profile.lastName ?? '',
    phone: profile.phone ?? '',
  });
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const set = (field: keyof UpdateProfilePayload) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((prev) => ({ ...prev, [field]: e.target.value }));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!form.firstName.trim() || !form.lastName.trim()) {
      setError('First name and last name are required.');
      return;
    }
    setIsSaving(true);
    setError(null);
    try {
      await onSave(form);
      onCancel();
    } catch (err) {
      setError(errorMessage(err, 'Could not save your profile'));
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col">
      {error && (
        <div className="mx-2 mb-5 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-800 dark:bg-error-500/10 dark:text-error-400">
          {error}
        </div>
      )}
      <div className="grid grid-cols-1 gap-x-6 gap-y-5 px-2 lg:grid-cols-2">
        <div>
          <Label htmlFor="profile-first-name">First Name</Label>
          <Input id="profile-first-name" value={form.firstName} onChange={set('firstName')} required />
        </div>
        <div>
          <Label htmlFor="profile-last-name">Last Name</Label>
          <Input id="profile-last-name" value={form.lastName} onChange={set('lastName')} required />
        </div>
        <div>
          <Label htmlFor="profile-email">Email Address</Label>
          <Input id="profile-email" type="email" value={profile.email} disabled hint="Your sign-in email can only be changed by an administrator." />
        </div>
        <div>
          <Label htmlFor="profile-phone">Phone</Label>
          <Input id="profile-phone" type="tel" value={form.phone} onChange={set('phone')} placeholder="+212 6 00 00 00 00" />
        </div>
      </div>
      <div className="mt-6 flex items-center gap-3 px-2 lg:justify-end">
        <Button size="sm" variant="outline" onClick={onCancel} disabled={isSaving}>
          Cancel
        </Button>
        <Button size="sm" type="submit" disabled={isSaving}>
          {isSaving ? 'Saving…' : 'Save Changes'}
        </Button>
      </div>
    </form>
  );
}
