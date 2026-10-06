import type { ChangePasswordPayload, UpdateProfilePayload } from '../types/profile.types';

export type FieldErrors<T> = Partial<Record<keyof T, string>>;

const NAME_MAX = 100;
const PHONE_MAX = 50;
const PHONE_PATTERN = /^[+0-9 ()./-]+$/;
const SPECIAL_PATTERN = /[!@#$%^&*(),.?":{}|<>]/;

/** Same rules as the backend (UpdateProfileWebRequest). */
export function validateProfile(values: UpdateProfilePayload): FieldErrors<UpdateProfilePayload> {
  const errors: FieldErrors<UpdateProfilePayload> = {};
  const firstName = values.firstName.trim();
  const lastName = values.lastName.trim();
  const phone = values.phone.trim();

  if (!firstName) errors.firstName = 'First name is required';
  else if (firstName.length > NAME_MAX) errors.firstName = `At most ${NAME_MAX} characters`;

  if (!lastName) errors.lastName = 'Last name is required';
  else if (lastName.length > NAME_MAX) errors.lastName = `At most ${NAME_MAX} characters`;

  if (phone && phone.length > PHONE_MAX) errors.phone = `At most ${PHONE_MAX} characters`;
  else if (phone && !PHONE_PATTERN.test(phone)) errors.phone = 'Digits, spaces and + ( ) . / - only';

  return errors;
}

/** The password policy enforced by the backend (Password value object). */
export function passwordChecks(password: string) {
  return [
    { label: 'At least 8 characters', ok: password.length >= 8 },
    { label: 'An uppercase letter', ok: /[A-Z]/.test(password) },
    { label: 'A lowercase letter', ok: /[a-z]/.test(password) },
    { label: 'A digit', ok: /[0-9]/.test(password) },
    { label: 'A special character', ok: SPECIAL_PATTERN.test(password) },
  ];
}

export function validatePasswordChange(values: ChangePasswordPayload): FieldErrors<ChangePasswordPayload> {
  const errors: FieldErrors<ChangePasswordPayload> = {};
  if (!values.currentPassword) errors.currentPassword = 'Enter your current password';
  if (!passwordChecks(values.newPassword).every((check) => check.ok)) {
    errors.newPassword = 'The new password does not meet every rule';
  } else if (values.newPassword === values.currentPassword) {
    errors.newPassword = 'Choose a password different from the current one';
  }
  if (values.confirmPassword !== values.newPassword) errors.confirmPassword = 'Passwords do not match';
  return errors;
}

export function hasErrors<T>(errors: FieldErrors<T>): boolean {
  return Object.values(errors).some(Boolean);
}
