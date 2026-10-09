import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import RoleGuard from '../RoleGuard';
import * as useAuthModule from '../../../modules/auth/hooks/useAuth';

describe('RoleGuard Component', () => {
  it('renders children when authenticated user has allowed role', () => {
    vi.spyOn(useAuthModule, 'default').mockReturnValue({
      user: {
        id: '1',
        name: 'Alice Admin',
        email: 'admin@materia.com',
        role: 'ADMIN',
      },
      isAuthenticated: true,
      isLoading: false,
      hasRole: vi.fn().mockReturnValue(true),
      hasPermission: vi.fn().mockReturnValue(true),
      error: null,
      login: vi.fn(),
      logout: vi.fn(),
      changePassword: vi.fn(),
      clearMustChangePassword: vi.fn(),
      updateUser: vi.fn(),
      resetPassword: vi.fn(),
      confirmPasswordReset: vi.fn(),
      clearError: vi.fn(),
    });

    render(
      <MemoryRouter initialEntries={['/admin-portal']}>
        <Routes>
          <Route
            path="/admin-portal"
            element={
              <RoleGuard allowedRoles={['ADMIN']}>
                <div>Protected Admin Content</div>
              </RoleGuard>
            }
          />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Protected Admin Content')).toBeInTheDocument();
  });

  it('redirects to fallback path when user does not have required role', () => {
    vi.spyOn(useAuthModule, 'default').mockReturnValue({
      user: {
        id: '2',
        name: 'Bob Receiver',
        email: 'receiver@materia.com',
        role: 'RECEIVER',
      },
      isAuthenticated: true,
      isLoading: false,
      hasRole: vi.fn().mockReturnValue(false),
      hasPermission: vi.fn().mockReturnValue(false),
      error: null,
      login: vi.fn(),
      logout: vi.fn(),
      changePassword: vi.fn(),
      clearMustChangePassword: vi.fn(),
      updateUser: vi.fn(),
      resetPassword: vi.fn(),
      confirmPasswordReset: vi.fn(),
      clearError: vi.fn(),
    });

    render(
      <MemoryRouter initialEntries={['/admin-portal']}>
        <Routes>
          <Route
            path="/admin-portal"
            element={
              <RoleGuard allowedRoles={['ADMIN']} fallbackPath="/forbidden">
                <div>Protected Admin Content</div>
              </RoleGuard>
            }
          />
          <Route path="/forbidden" element={<div>Access Denied Page</div>} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.queryByText('Protected Admin Content')).not.toBeInTheDocument();
    expect(screen.getByText('Access Denied Page')).toBeInTheDocument();
  });
});
