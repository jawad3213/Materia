import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { HelmetProvider } from 'react-helmet-async';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import * as useAuthModule from '../../auth/hooks/useAuth';
import ProfilePage from '../pages/ProfilePage';
import { profileService } from '../services/profileService';
import type { Profile } from '../types/profile.types';
import { passwordChecks, validatePasswordChange, validateProfile } from '../utils/profileValidation';

const PROFILE: Profile = {
  id: 'u-1',
  email: 'sara@materia.ma',
  firstName: 'Sara',
  lastName: 'Alami',
  fullName: 'Sara Alami',
  phone: null,
  department: 'Procurement',
  role: 'PURCHASER',
  roleLabel: 'Acheteur',
  status: 'ACTIVE',
  mustChangePassword: false,
  permissions: ['order:read', 'order:write', 'requisition:read'],
  createdAt: '2026-09-01T09:00:00',
  updatedAt: '2026-10-01T09:00:00',
};

const updateUser = vi.fn();
const changePassword = vi.fn();

function renderPage() {
  return render(
    <HelmetProvider>
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>
    </HelmetProvider>
  );
}

describe('profile page', () => {
  beforeEach(() => {
    vi.spyOn(useAuthModule, 'default').mockReturnValue({
      updateUser,
      changePassword,
    } as unknown as ReturnType<typeof useAuthModule.default>);
  });

  afterEach(() => {
    vi.restoreAllMocks();
    updateUser.mockReset();
    changePassword.mockReset();
  });

  it('rule: shows the signed-in user loaded from the profile API', async () => {
    vi.spyOn(profileService, 'getProfile').mockResolvedValue(PROFILE);

    renderPage();

    expect(await screen.findByRole('heading', { name: 'Sara Alami' })).toBeInTheDocument();
    expect(screen.getAllByText('sara@materia.ma').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Procurement').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Acheteur').length).toBeGreaterThan(0);
    expect(screen.getByText('Not set')).toBeInTheDocument(); // no phone yet
    expect(screen.getByText('1 September 2026')).toBeInTheDocument();
    expect(screen.getByText('order')).toBeInTheDocument();
  });

  it('rule: editing saves name and phone through the API and refreshes the signed-in user', async () => {
    vi.spyOn(profileService, 'getProfile').mockResolvedValue(PROFILE);
    const update = vi.spyOn(profileService, 'updateProfile').mockResolvedValue({
      ...PROFILE,
      firstName: 'Salma',
      fullName: 'Salma Alami',
      phone: '0522 11 22 33',
    });
    const user = userEvent.setup();

    renderPage();
    await user.click(await screen.findByRole('button', { name: /edit profile/i }));
    const firstName = screen.getByLabelText('First name');
    await user.clear(firstName);
    await user.type(firstName, 'Salma');
    await user.type(screen.getByLabelText('Phone'), '0522 11 22 33');
    await user.click(screen.getByRole('button', { name: /save changes/i }));

    await waitFor(() => expect(update).toHaveBeenCalledWith({ firstName: 'Salma', lastName: 'Alami', phone: '0522 11 22 33' }));
    expect(updateUser).toHaveBeenCalledWith({ name: 'Salma Alami' });
    expect(await screen.findByRole('heading', { name: 'Salma Alami' })).toBeInTheDocument();
    expect(screen.getByText('Your profile has been updated')).toBeInTheDocument();
  });

  it('rule: an empty name is refused before anything is sent', async () => {
    vi.spyOn(profileService, 'getProfile').mockResolvedValue(PROFILE);
    const update = vi.spyOn(profileService, 'updateProfile');
    const user = userEvent.setup();

    renderPage();
    await user.click(await screen.findByRole('button', { name: /edit profile/i }));
    await user.clear(screen.getByLabelText('Last name'));
    await user.click(screen.getByRole('button', { name: /save changes/i }));

    expect(screen.getByText('Last name is required')).toBeInTheDocument();
    expect(update).not.toHaveBeenCalled();
  });

  it('rule: a failed load offers to try again', async () => {
    const load = vi.spyOn(profileService, 'getProfile').mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce(PROFILE);
    const user = userEvent.setup();

    renderPage();
    await user.click(await screen.findByRole('button', { name: /try again/i }));

    expect(await screen.findByRole('heading', { name: 'Sara Alami' })).toBeInTheDocument();
    expect(load).toHaveBeenCalledTimes(2);
  });

  it('rule: changing the password needs the current one and a policy-compliant new one', async () => {
    vi.spyOn(profileService, 'getProfile').mockResolvedValue(PROFILE);
    changePassword.mockResolvedValue('ok');
    const user = userEvent.setup();

    renderPage();
    await screen.findByRole('heading', { name: 'Sara Alami' });
    const form = screen.getByRole('button', { name: /update password/i }).closest('form') as HTMLFormElement;
    await user.click(within(form).getByRole('button', { name: /update password/i }));
    expect(within(form).getByText('Enter your current password')).toBeInTheDocument();
    expect(changePassword).not.toHaveBeenCalled();

    await user.type(within(form).getByLabelText('Current password'), 'OldPass1!');
    await user.type(within(form).getByLabelText('New password'), 'NewPass2@');
    await user.type(within(form).getByLabelText('Confirm new password'), 'NewPass2@');
    await user.click(within(form).getByRole('button', { name: /update password/i }));

    await waitFor(() =>
      expect(changePassword).toHaveBeenCalledWith({ currentPassword: 'OldPass1!', newPassword: 'NewPass2@', confirmPassword: 'NewPass2@' })
    );
    expect(await screen.findByText('Your password has been changed')).toBeInTheDocument();
  });
});

describe('profile validation', () => {
  it('rule: matches the backend limits for names and phone', () => {
    expect(validateProfile({ firstName: ' ', lastName: 'A', phone: '' })).toEqual({ firstName: 'First name is required' });
    expect(validateProfile({ firstName: 'A', lastName: 'x'.repeat(101), phone: '' }).lastName).toMatch(/100/);
    expect(validateProfile({ firstName: 'A', lastName: 'B', phone: 'call me' }).phone).toMatch(/digits/i);
    expect(validateProfile({ firstName: 'A', lastName: 'B', phone: '+212 (0) 522-11.22/33' })).toEqual({});
  });

  it('rule: the password policy needs length, upper, lower, digit and special character', () => {
    expect(passwordChecks('Abcdef1!').every((c) => c.ok)).toBe(true);
    expect(passwordChecks('abcdefgh').filter((c) => !c.ok).map((c) => c.label)).toEqual([
      'An uppercase letter',
      'A digit',
      'A special character',
    ]);
    expect(validatePasswordChange({ currentPassword: 'Abcdef1!', newPassword: 'Abcdef1!', confirmPassword: 'Abcdef1!' }).newPassword).toMatch(/different/);
    expect(validatePasswordChange({ currentPassword: 'x', newPassword: 'Abcdef1!', confirmPassword: 'Abcdef1?' }).confirmPassword).toMatch(/match/);
  });
});
