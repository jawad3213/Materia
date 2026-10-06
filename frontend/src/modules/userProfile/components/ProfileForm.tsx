import { useState, type FormEvent } from 'react';
import Button from '../../../shared/components/ui/button/Button';
import Input from '../../../shared/components/form/input/InputField';
import Label from '../../../shared/components/form/Label';
import { getApiErrorMessage } from '../../../shared/utils/apiError';
import type { Profile, UpdateProfilePayload } from '../types/profile.types';
import { hasErrors, validateProfile, type FieldErrors } from '../utils/profileValidation';

interface ProfileFormProps {
  profile: Profile;
  onSave: (payload: UpdateProfilePayload) => Promise<unknown>;
  onCancel: () => void;
}

/** Edits the user's own name and phone. E-mail, role and department are shown read-only: an administrator manages them. */
export default function ProfileForm({ profile, onSave, onCancel }: ProfileFormProps) {
  const [values, setValues] = useState<UpdateProfilePayload>({
    firstName: profile.firstName ?? '',
    lastName: profile.lastName ?? '',
    phone: profile.phone ?? '',
  });
  const [errors, setErrors] = useState<FieldErrors<UpdateProfilePayload>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const set = (field: keyof UpdateProfilePayload) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setValues((current) => ({ ...current, [field]: e.target.value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
  };

  const unchanged =
    values.firstName.trim() === (profile.firstName ?? '') &&
    values.lastName.trim() === (profile.lastName ?? '') &&
    values.phone.trim() === (profile.phone ?? '');

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const found = validateProfile(values);
    setErrors(found);
    if (hasErrors(found)) return;
    setSaving(true);
    setSubmitError(null);
    try {
      await onSave(values);
    } catch (err) {
      setSubmitError(getApiErrorMessage(err, 'Your profile could not be saved'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col">
      <div className="px-2">
        <h4 className="mb-2 text-2xl font-semibold text-gray-800 dark:text-white/90">Edit personal information</h4>
        <p className="mb-6 text-sm text-gray-500 dark:text-gray-400">Your name and phone number appear on the documents you create and approve.</p>
      </div>

      {submitError && (
        <div role="alert" className="mx-2 mb-4 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-400">
          {submitError}
        </div>
      )}

      <div className="grid grid-cols-1 gap-x-6 gap-y-5 px-2 lg:grid-cols-2">
        <div>
          <Label htmlFor="profile-first-name">First name</Label>
          <Input id="profile-first-name" value={values.firstName} onChange={set('firstName')} error={!!errors.firstName} hint={errors.firstName} autoComplete="given-name" />
        </div>
        <div>
          <Label htmlFor="profile-last-name">Last name</Label>
          <Input id="profile-last-name" value={values.lastName} onChange={set('lastName')} error={!!errors.lastName} hint={errors.lastName} autoComplete="family-name" />
        </div>
        <div>
          <Label htmlFor="profile-phone">Phone</Label>
          <Input id="profile-phone" type="tel" value={values.phone} onChange={set('phone')} error={!!errors.phone} hint={errors.phone} placeholder="+212 6 00 00 00 00" autoComplete="tel" />
        </div>
        <div>
          <Label htmlFor="profile-email">E-mail</Label>
          <Input id="profile-email" type="email" value={profile.email} disabled readOnly hint="Managed by an administrator" />
        </div>
      </div>

      <div className="mt-8 flex items-center justify-end gap-3 px-2">
        <Button size="sm" variant="outline" type="button" onClick={onCancel} disabled={saving}>
          Cancel
        </Button>
        <Button size="sm" type="submit" disabled={saving || unchanged}>
          {saving ? 'Saving…' : 'Save changes'}
        </Button>
      </div>
    </form>
  );
}
